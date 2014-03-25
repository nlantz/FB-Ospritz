/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package net.clintarmstrong.fbreader.plugin.ospritz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.clintarmstrong.textspritzer.lib.Spritzer;
import net.clintarmstrong.textspritzer.lib.SpritzerTextView;

import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;
import org.geometerplus.android.fbreader.api.TextPosition;

public class SpritzActivity extends Activity implements ApiClientImplementation.ConnectionListener {

    public static final String TAG = SpritzActivity.class.getName();
    private SpritzerTextView mSpritzerTextView;
    private Spritzer mSpritzer;
    private SeekBar mSeekBarTextSize;
    private SeekBar mSeekBarWpm;
    private TextView wpmTV;
    private TextView TextSizeTV;

    private SharedPreferences myPreferences;
    private SharedPreferences.Editor myEditor;
    private int myParagraphIndex = -1;
    private int myParagraphsNumber;
    private boolean myIsActive = false;
    private ApiClientImplementation myApi;
    private volatile PowerManager.WakeLock myWakeLock;
    private int initialTheme;
    private boolean initialTextColorEnabled = false;
    private boolean initialBackgroundColorEnabled = false;


    private void setListener(int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // store initial theme, so we can tell if it has changed and if we need to reload it later.
        initialTheme = getResources().getIdentifier(myPreferences.getString("theme_style", getString(R.string.prev_list_theme_defaultValue)), "style", getPackageName());
        this.setTheme(initialTheme);

        setContentView(R.layout.control_panel);

        // put window in initializing status
        setTitle(R.string.initializing);
        setActive(false);
        setActionsEnabled(false);

        // get all of the views we'll need later
        wpmTV = (TextView) findViewById(R.id.tv_wpm);
        TextSizeTV = (TextView) findViewById(R.id.tv_textSize);
        mSpritzerTextView = (SpritzerTextView) findViewById(R.id.spritzTV);
        mSpritzer = mSpritzerTextView.getSpritzer();
        mSeekBarTextSize = (SeekBar) findViewById(R.id.seekbar_textSize);
        mSeekBarWpm = (SeekBar) findViewById(R.id.seekbar_wpm);

        setupButtons();
        setSpritzerListener();

        myApi = new ApiClientImplementation(this, this);
    }

    // onresume is created after oncreate, so no need to run these in oncreate as well.
    @Override
    protected void onResume(){
        super.onResume();
        setThemeFromPref();
        setupSeekBars();
    }

    private void setSpritzerListener(){
        mSpritzerTextView.setRemainingWordsListener( new Spritzer.RemainingWordsListener() {
            @Override
            public void onWordsRemaining() {
                if(myIsActive){
                    if (myParagraphIndex < myParagraphsNumber) {
                        ++myParagraphIndex;
                        mSpritzerTextView.setSpritzText(gotoNextParagraph());
                        mSpritzerTextView.play();
                    }
                }
            }
        });
        mSpritzerTextView.setWordsRemainingToTriggerListener(0);
    }

    private void setThemeFromPref(){
        int theme_style = getResources().getIdentifier(myPreferences.getString("theme_style", getString(R.string.prev_list_theme_defaultValue)), "style", getPackageName());
        boolean custom_text_color_enabled = myPreferences.getBoolean("custom_text_color_enabled", false);
        boolean custom_background_color_enabled = myPreferences.getBoolean("custom_background_color_enabled", false);
    //    this.setTheme(theme_style);

        if ((initialTheme != theme_style) || (initialTextColorEnabled && !custom_text_color_enabled) || (initialBackgroundColorEnabled && !custom_background_color_enabled))
            {reloadview();}
        if (custom_text_color_enabled){
            mSpritzerTextView.setTextColor(myPreferences.getInt("custom_text_color", -1));
            initialTextColorEnabled = true;
        }
        if (custom_background_color_enabled){
            mSpritzerTextView.setBackgroundColor(myPreferences.getInt("custom_background_color", -16777216));
            initialBackgroundColorEnabled = true;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopSpritzing();
    }

    private void setupSeekBars() {

        // Setup listener for changes.
        mSeekBarWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int minWPM = Integer.parseInt(myPreferences.getString("pref_text_minWPM", "100"));
            @Override
            public void onProgressChanged(SeekBar seekBar, int wpmSeekBarProgress, boolean fromUser) {
                // math needed to turn progress from 1 to 100 into a value between min and max
                int wpm = wpmSeekBarProgress + minWPM;
                mSpritzerTextView.setWpm(wpm);
                wpmTV.setText(getText(R.string.tv_wpm_text) + " " + wpm);
                setPrefInt("pref_int_wpm", wpm);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        int wpm = myPreferences.getInt("pref_int_wpm", 250);
        int minWPM = Integer.parseInt(myPreferences.getString("pref_text_minWPM", "100"));
        int maxWPM = Integer.parseInt(myPreferences.getString("pref_text_maxWPM", "1000"));
        // fix bad values
        if (wpm > maxWPM || wpm < minWPM) {
            removePref("pref_int_wpm");
            wpm = myPreferences.getInt("pref_int_wpm", 250);}
        // fix bad values
        if (minWPM >= maxWPM) {
            removePref("pref_text_minWPM"); removePref("pref_text_maxWPM");
            minWPM = myPreferences.getInt("wpm_min", 100);
            maxWPM = myPreferences.getInt("wpm_max", 1000);
        }
        // math needed to turn progress from 1 to 100 into a value between min and max
        mSeekBarWpm.setMax(maxWPM - minWPM);
        int wpmSeekBarProgress = (wpm - minWPM);
        mSeekBarWpm.setProgress(wpmSeekBarProgress);
        // this shouldn't be needed, setprogress should trigger onProgressChanged
        // wpmTV.setText(getText(R.string.seek_wpm) + " " + wpm);

        // listen for changes to size bar
        mSeekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int minTextSize = Integer.parseInt(myPreferences.getString("pref_text_minTextSize", "4"))*10;
            @Override
            public void onProgressChanged(SeekBar seekBar, int textSizeSeekBarProgress, boolean fromUser) {
                int textSize = textSizeSeekBarProgress + minTextSize;
                mSpritzerTextView.setTextSize(textSize/10);
                TextSizeTV.setText(getText(R.string.tv_textSize_text) + " " + (float) textSize/10);
                setPrefInt("pref_int_textSize", textSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int textSize;
        // catch if pref is not stored as int, probably won't happen in prod, but happened to me while developing.
        try {
            textSize = myPreferences.getInt("pref_int_textSize", 200);
        } catch (ClassCastException e) {
            removePref("pref_int_textSize");
            textSize = myPreferences.getInt("pref_int_textSize", 200);
        }

        int minTextSize = Integer.parseInt(myPreferences.getString("pref_text_minTextSize", "4"))*10;
        int maxTextSize = Integer.parseInt(myPreferences.getString("pref_text_minTextSize", "50"))*10;
        // fix bad values
        if (textSize > maxTextSize || textSize < minTextSize) {
            removePref("pref_int_textSize");
            textSize = myPreferences.getInt("pref_int_textSize", 200);
        }
        // fix bad values
        if (minTextSize >= maxTextSize) {
            removePref("pref_text_minTextSize"); removePref("pref_text_minTextSize");
            minTextSize = Integer.parseInt(myPreferences.getString("pref_text_minTextSize", "4"))*10;
            maxTextSize = Integer.parseInt(myPreferences.getString("pref_text_minTextSize", "50"))*10;
        }
        // math needed to allow .1 font sizes
        mSeekBarTextSize.setMax(maxTextSize - minTextSize);
        int sProgress = (textSize - minTextSize);
        mSeekBarTextSize.setProgress(sProgress);
        TextSizeTV.setText(getText(R.string.tv_textSize_text) + " " + (float) textSize/10);
    }

    private void setupButtons(){
        setListener(R.id.button_previousParagraph, new View.OnClickListener() {
            public void onClick(View v) {
                stopSpritzing();
                gotoPreviousParagraph();
            }
        });
        setListener(R.id.button_nextParagraph, new View.OnClickListener() {
            public void onClick(View v) {
                stopSpritzing();
                if (myParagraphIndex < myParagraphsNumber) {
                    ++myParagraphIndex;
                    gotoNextParagraph();
                }
            }
        });
        setListener(R.id.button_close, new View.OnClickListener() {
            public void onClick(View v) {
                switchOff();
                finish();
            }
        });
        setListener(R.id.button_pause, new View.OnClickListener() {
            public void onClick(View v) {
                stopSpritzing();
            }
        });
        setListener(R.id.button_play, new View.OnClickListener() {
            public void onClick(View v) {
                setActive(true);
            //    mSpritzerTextView.setSpritzText(gotoNextParagraph());
                mSpritzerTextView.play();
            }


        });
    }

    private volatile int myInitializationStatus;
    private static int API_INITIALIZED = 1;
    private static int FULLY_INITIALIZED = API_INITIALIZED;

    public void onConnected() {
        if (myInitializationStatus != FULLY_INITIALIZED) {
            myInitializationStatus |= API_INITIALIZED;
            if (myInitializationStatus == FULLY_INITIALIZED) {
                onInitializationCompleted();
            }
        }
    }

    private void onInitializationCompleted() {
        try {
            setTitle(myApi.getBookTitle());

            myParagraphIndex = myApi.getPageStart().ParagraphIndex;
            myParagraphsNumber = myApi.getParagraphsNumber();
            setActionsEnabled(true);
            setActive(false);
        } catch (ApiException e) {
            setActionsEnabled(false);
            showErrorMessage(getText(R.string.initialization_error), true);
            e.printStackTrace();
        }
        mSpritzerTextView.setSpritzText(gotoNextParagraph());
    }

    private void setPrefInt(String key, int mInt){
        myEditor = myPreferences.edit();
        myEditor.putInt(key, mInt);
        myEditor.commit();
    }

    private void removePref(String key){
        myEditor = myPreferences.edit();
        myEditor.remove(key);
        myEditor.commit();
    }

    private void gotoPreviousParagraph() {
        try {
            for (int i = myParagraphIndex - 1; i >= 0; --i) {
                if (myApi.getParagraphText(i).length() > 0) {
                    myParagraphIndex = i;
                    break;
                }
            }
            if (myApi.getPageStart().ParagraphIndex >= myParagraphIndex) {
                myApi.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
            }
            highlightParagraph(myParagraphIndex);
            runOnUiThread(new Runnable() {
                public void run() {
                    findViewById(R.id.button_nextParagraph).setEnabled(true);
                    findViewById(R.id.button_play).setEnabled(true);
                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private String gotoNextParagraph() {
        try {
            String text = "";
            for (; myParagraphIndex < myParagraphsNumber; ++myParagraphIndex) {
                final String s = myApi.getParagraphText(myParagraphIndex);
                if (s.length() > 0) {
                    text = s;
                    break;
                }
            }
            highlightParagraph(myParagraphIndex);
            if (myParagraphIndex >= myParagraphsNumber) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        findViewById(R.id.button_nextParagraph).setEnabled(false);
                        findViewById(R.id.button_play).setEnabled(false);
                    }
                });
            }
            return text;
        } catch (ApiException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void switchOff() {
        stopSpritzing();
        try {
            myApi.clearHighlighting();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        myApi.disconnect();
    }

    private synchronized void setActive(final boolean active) {
        myIsActive = active;

        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.button_play).setVisibility(active ? View.GONE : View.VISIBLE);
                findViewById(R.id.button_pause).setVisibility(active ? View.VISIBLE : View.GONE);
            }
        });

        if (active) {
            if (myWakeLock == null) {
                myWakeLock =
                        ((PowerManager) getSystemService(POWER_SERVICE))
                                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FBReader OSpritz Plugin");
                myWakeLock.acquire();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } else {
            if (myWakeLock != null) {
                myWakeLock.release();
                myWakeLock = null;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    private void setActionsEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.button_previousParagraph).setEnabled(enabled);
                findViewById(R.id.button_nextParagraph).setEnabled(enabled);
                findViewById(R.id.button_play).setEnabled(enabled);
            }
        });
    }

    private void showErrorMessage(final CharSequence text, final boolean fatal) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (fatal) {
                    setTitle(R.string.failure);
                }
                Toast.makeText(SpritzActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightParagraph(int paragraphIndex) throws ApiException {
        if (!"".equals(myApi.getParagraphText(paragraphIndex)) && !myApi.isPageEndOfText()) {
            myApi.setPageStart(new TextPosition(paragraphIndex, 0, 0));
        }
        if (0 <= paragraphIndex && paragraphIndex < myParagraphsNumber) {
            myApi.highlightArea(
                    new TextPosition(paragraphIndex, 0, 0),
                    new TextPosition(paragraphIndex, Integer.MAX_VALUE, 0)
            );
        } else {
            myApi.clearHighlighting();
        }
    }
    private void stopSpritzing() {
        setActive(false);
        mSpritzerTextView.pause();
    }

    private void reloadview() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        stopSpritzing();
        switchOff();
        super.onDestroy();
    }

    public void launchSettings(View view) {
        stopSpritzing();
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
}