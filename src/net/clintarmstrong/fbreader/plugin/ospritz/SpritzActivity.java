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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.SeekBar;
import android.telephony.PhoneStateListener;
import android.widget.TextView;
import android.widget.Toast;

import org.geometerplus.android.fbreader.api.*;
import com.andrewgiang.textspritzer.lib.*;

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

    private void setListener(int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_panel);

        wpmTV = (TextView) findViewById(R.id.wpm_text);
        TextSizeTV = (TextView) findViewById(R.id.text_size_text);

        myPreferences = getSharedPreferences("FBReaderOSpritz", MODE_PRIVATE);

        mSpritzerTextView = (SpritzerTextView) findViewById(R.id.spritzTV);
        mSpritzer = mSpritzerTextView.getSpritzer();
        mSeekBarTextSize = (SeekBar) findViewById(R.id.seekBarTextSize);
        mSeekBarWpm = (SeekBar) findViewById(R.id.seekBarWpm);

        setupSeekBars();
        // mSeekBarWpm.setProgress(mSpritzerTextView.getWpm());
        mSeekBarWpm.setProgress(myPreferences.getInt("mSeekBarWpm", 250));
        wpmTV.setText(getText(R.string.seek_wpm) + " " + myPreferences.getInt("mSeekBarWpm", 250));

                // mSeekBarTextSize.setProgress((int) mSpritzerTextView.getTextSize());
        mSeekBarTextSize.setProgress(myPreferences.getInt("mSeekBarTextSize", (int) mSpritzerTextView.getTextSize()));
        TextSizeTV.setText(getText(R.string.seek_text_size) + " " + myPreferences.getInt("mSeekBarTextSize", (int) mSpritzerTextView.getTextSize()));

        setListener(R.id.button_previous_paragraph, new View.OnClickListener() {
            public void onClick(View v) {
                stopSpritzing();
                gotoPreviousParagraph();
            }
        });
        setListener(R.id.button_next_paragraph, new View.OnClickListener() {
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
                mSpritzerTextView.setSpritzText(gotoNextParagraph());
                mSpritzerTextView.play();
                new Thread(new Runnable() {
                    public void run() {
                        while (myIsActive) {
                            if (!mSpritzer.isPlaying()) {
                                if (myParagraphIndex < myParagraphsNumber) {
                                    ++myParagraphIndex;
                                    mSpritzerTextView.setSpritzText(gotoNextParagraph());
                                    mSpritzerTextView.play();
                                    SystemClock.sleep(500);
                                }
                            }
                        }
                    }
                }).start();
            }


        });

            ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(
                new PhoneStateListener() {
                    public void onCallStateChanged(int state, String incomingNumber) {
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            stopSpritzing();
                        }
                    }
                },
                PhoneStateListener.LISTEN_CALL_STATE
        );

        setActive(false);
        setActionsEnabled(false);

        myApi = new ApiClientImplementation(this, this);
        setTitle(R.string.initializing);
    }

    private void setupSeekBars() {
        if (mSeekBarWpm != null && mSeekBarTextSize != null) {
            // mSeekBarWpm.setMax(mSpritzerTextView.getWpm() * 2);
            mSeekBarWpm.setMax(1000);

            mSeekBarTextSize.setMax((int) mSpritzerTextView.getTextSize() * 2);
            mSeekBarWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress > 0) {
                        mSpritzerTextView.setWpm(progress);
                        wpmTV.setText(getText(R.string.seek_wpm) + " " + progress);
                        // getText(R.string.initialization_error)
                        myEditor = myPreferences.edit();
                        myEditor.putInt("mSeekBarWpm", progress);
                        myEditor.commit();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            mSeekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mSpritzerTextView.setTextSize(progress);
                    TextSizeTV.setText(getText(R.string.seek_text_size) + " " + myPreferences.getInt("mSeekBarTextSize", (int) mSpritzerTextView.getTextSize()));
                    myEditor = myPreferences.edit();
                    myEditor.putInt("mSeekBarTextSize", progress);
                    myEditor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    // implements ApiClientImplementation.ConnectionListener

    private volatile int myInitializationStatus;
    private static int API_INITIALIZED = 1;
    // private static int TTS_INITIALIZED = 2;
    private static int FULLY_INITIALIZED = API_INITIALIZED; // | TTS_INITIALIZED;

    public void onConnected() {
        if (myInitializationStatus != FULLY_INITIALIZED) {
            myInitializationStatus |= API_INITIALIZED;
            if (myInitializationStatus == FULLY_INITIALIZED) {
                onInitializationCompleted();
            }
        }
    }

    private void onInitializationCompleted() {
        final SpritzerTextView view = (SpritzerTextView) findViewById(R.id.spritzTV);
        try {
            setTitle(myApi.getBookTitle());

            myParagraphIndex = myApi.getPageStart().ParagraphIndex;
            myParagraphsNumber = myApi.getParagraphsNumber();
            setActionsEnabled(true);
            setActive(false);
            // mSpritzerTextView.setSpritzText(gotoNextParagraph());
        } catch (ApiException e) {
            setActionsEnabled(false);
            showErrorMessage(getText(R.string.initialization_error), true);
            e.printStackTrace();
        }
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
            highlightParagraph();
            runOnUiThread(new Runnable() {
                public void run() {
                    findViewById(R.id.button_next_paragraph).setEnabled(true);
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
            if (!"".equals(text) && !myApi.isPageEndOfText()) {
                myApi.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
            }
            highlightParagraph();
            if (myParagraphIndex >= myParagraphsNumber) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        findViewById(R.id.button_next_paragraph).setEnabled(false);
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
        final SpritzerTextView view = (SpritzerTextView) findViewById(R.id.spritzTV);
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
            }
        } else {
            if (myWakeLock != null) {
                myWakeLock.release();
                myWakeLock = null;
            }
        }
    }

    private void setActionsEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.button_previous_paragraph).setEnabled(enabled);
                findViewById(R.id.button_next_paragraph).setEnabled(enabled);
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

    private void highlightParagraph() throws ApiException {
        if (0 <= myParagraphIndex && myParagraphIndex < myParagraphsNumber) {
            myApi.highlightArea(
                    new TextPosition(myParagraphIndex, 0, 0),
                    new TextPosition(myParagraphIndex, Integer.MAX_VALUE, 0)
            );
        } else {
            myApi.clearHighlighting();
        }
    }
    private void stopSpritzing() {
        setActive(false);
        if (mSpritzer.isPlaying()) {
            mSpritzerTextView.pause();
        }
    }
}