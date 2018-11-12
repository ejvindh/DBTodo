TodoDiary - Android
===================

Regain the power of your own life documentary!

TodoDiary combines your Calendar/Todo-list with a Diary. The entries in your Calendar/Todo-list are saved as ordinary txt-files that can be read on any device. Furthermore it is saved in Dropbox, which makes it possible for you to save it on your local drives, if you wish. This makes it possible for you to access your previous life and future in the database-files, and it will never disappear. Not even if you switch to another Calendar-system later on. Therefore it makes sense to have both ordinary calendar-entries and more reflective diary-scribles.

Desktop-clients are available at https://github.com/ejvindh/TodoDiary
They are written in Java8 and can thus (if Java8 is installed) be run directly inside Windows (post XP), Linux and MacOs. The desktop-clients are not integrated with Dropbox, but if you have Dropbox installed on your desktop, you may simply direct the attention of the client to the Dropbox-folder in which the Android-app saves the databases (Dropbox/Apps/TodoDiary/), and then you have full integration between the systems!

Attention: Be carefull with the Raw-DB-View. Don't mess with the lines containing the date-indications. If they do not have the right format, TodoDiary will not be able to show and save content in the right places. And they may also risk becomming invisible in the "Single-Day-View".
	  
Notice: A similar project can be found here: http://todotxt.com/
Several Android-apps are created around this project. I'm not affiliated with that project, and my project has been developed independently.

* I will not publish this app on Google Play, since I'm against having to pay for helping Google improve their system. In order to install it, you will thus have to do this:

1) Accept installation from "unknown sources": That is done in "Settings" -- "Security" (after having installed this app, I recommend that you remove the checkmark again).

2) Download and install the apk-file found here:
https://github.com/ejvindh/DBTodo/releases

---------------
v1.0: December 2014
- First public release

v1.1: June 2015
- Bugfix: New authentications now work again
- Authentication now works without having Dropbox installed (using the Web-interface instead).
(however, beware: If you have on other systems typed content in existing databases, conten in the "Today"-entry will be wiped...sorry for now I'm too lazy to fix this bug...).

v1.2: Autumn 2017
- Bugfix: Now works with the new v2 of Dropbox SDK.
- Bugfix: Download during screenrotation works.
- Bugfix: Webauthentication now works properly.
- Now works with new post SDK23 permissions.
- Introduced ProcessDialog during save/load.
- And several other minor fixes.

v1.3: Autumn 2018
- Due to Dropbox' decision of not supporting ecryptfs-encrypted drives I've written a Dropbox-detached version of the app. It can be found here:
https://github.com/ejvindh/AndTodo
I will probably not update the Dropbox version anymore.
