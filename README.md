FB-Ospritz
============

A Open Spritz plugin for FB Reader.

Looking for feedback from beta testers. Get the beta in [Google Play](https://play.google.com/apps/testing/net.clintarmstrong.fbreader.plugin.ospritz)
or [directly](https://github.com/clinta/FB-Ospritz/raw/master/net.clintarmstrong.fbreader.plugin.ospritz/net.clintarmstrong.fbreader.plugin.ospritz.apk).

Recent changes. Lots of new options available in the settings menu. Detailed descriptions are [here](https://github.com/clinta/FB-Ospritz/blob/master/preferences.md).
New features include: Custom delays for short and long words, custom delays for punctuation and paragraph splits. Custom window positions.

Latest version Implements my own [TextSpritzer](https://github.com/clinta/TextSpritzer) library, a fork of [SpritzerTextView](https://github.com/andrewgiang/SpritzerTextView) that adds a multithreaded
queue where strings can be pre-parsed while they're being read, as well as additional callbacks needed for managing the queue. This design should allow for more complicated parsing rules in the future.

Note: This project has nothing to do with SpritzInc.

License
------------
```
Copyright [2014] [Clint Armstrong]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
