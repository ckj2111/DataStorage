-----------------------------
README for DataStorage
Cathy Jin
-----------------------------

There are two classes in this app: main.java and OtherMethods.java.
The program also depends on a third file called template.smali.

To run this program, build the two classes and then run as follows:

arg 0:	apktool.jar file path
arg 1: .apk file path
arg 2: decompile file path (output file)
arg 3: write to internal file name
arg 4: template.smali location
arg 5: keystore location

For example,

main /Users/Cathy/Desktop/apktool.jar /Users/Desktop/AndroidSharedPreferences.apk /Users/Desktop/test/ tracker /Users/Desktop/template.smali /Users/Desktop/

This app will decompile the .apk file, edit the code and insert the template file in the necessary places, recompile the file, and use keytool to create a certificate and jarsigner to sign the app. However, this is for demo purposes, as the passwords for keytool and jarsigner generally shouldn’t be automated into the code.  