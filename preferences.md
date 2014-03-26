Preferences
============

The latest version includes several advanced preferences

- Themes Settings
    - Theme Choices
        - Dark: This is the default theme as set by your device manufacturer.
        - Light: This is the default light theme as set by  your device manufacturer.
        - Black: This is a modification of the dark theme, but with the background set to black and the text set to white.
    - Custom Colors
        - Text Color
        - Background Color
- Slider Preferences
    - WPM
        - Enable WPM Bar: Enables or disables the WPM slider. Not sure why you'd want to disable it, but it's not my job to tell you how you should use the program.
        - Min WPM: Sets the minimum WPM of the slider displayed on the main window
        - Max WPM: Sets the maximum WPM of the slider displayed on the main window
    - Text Size
        - Enable Text Size: Enables or disables the Text Slider slider. Since the text size slider now also controls the entire window size, you might want to enable
        it long enough to configure it then turn it off.
        - Min Text Size: Sets the minimum text size of the slider displayed on the main window
        - Max Text Size: Sets the maximum text size of the slider displayed on the main window.
- Speed Settings
    - Delay By Letter: This will calculate the delay for each word based on the number of letters. The base wpm will be for a 4.5 character average English word.
        - Maximum Letter Delay Multiplier: This delay multiplier will be applied for a 13 character word. This setting affects all words longer than 4.5 characters.
        By default, a 13 character word will be displayed 2.8 times longer than your set WPM. An 8 character word will be displayed 1.15 times longer than your set WPM.
        If you only want long words to display shorter, closer to your set WPM, set this number closer to 1.
        - Minimum Letter Delay Multiplier: This delay multiplier will be applied for a 1 character word. This setting affects all words with less than 4.5 characters.
        By default, a 1 character word will be displayed at .22 of your set WPM. A 3 character word will be displayed at .66 of your set WPM.
        If you want short words to display longer, closer to your set WPM, set this number closer to 1.
    - Long Word Delay: This is a simpler setting for simply delaying longer words more. This is additive with the Delay By Letter setting.
    This means that if both options are enabled, the delay will first be calculated based on the number of letters, then long words will be delayed even longer by this setting.
    You probably do not want both enabled at once.
        - Number of Characters: The number of characters to signify a long word.
        - Long Word Delay Multiplier: This is the multiplier that will always be applied for long words. Any word longer than the number of characters you choose will be
        delayed this much longer than your base WPM. Any word shorter than the numeber of characters you choose will not be delayed at all.
    - Punctuation Delay: This delay is not based on the word, but is an additive delay for the punctuation itself. So if the last word in a sentence is 13 characters, and it
    is delayed 3*wpm because of it's length, and the punctuation triggers a 2*wpm delay, the word will be displayed for 5*wpm, not 6*wpm.
        - Punctuation Characters: Any characters in this field will trigger a punctuation delay
        - Punctuation Delay Multiplier: Words with punctuation will have an additional delay of this * WPM.
    - Paragraph Delay: This delay is not based on the word, but is an additive delay for the paragraph itself. So if the last word in a paragraph is 13 characters, and it
    is delayed 3*wpm because of it's length, and is delayed 2*wpm because of punctuation, and the paragraph triggers a 2*wpm delay, the word will be displayed for 7*wpm.
        - Paragraph Delay Multiplier: The last word in the paragraph will have an additional delay of this * WPM.
