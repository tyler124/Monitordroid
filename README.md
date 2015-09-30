Monitordroid

Basic instructions:

1. If you haven't yet, first set up your Monitordroid open-source web server following the directions in the README.md in    the Monitordroid-Web-Application repository. 

2. Import the Monitordroid project into an IDE suited for Android development such as Android Studio or Eclipse. 

3. Open the CommonUtilities.java file in an editor. Set the 'DOMAIN' variable on line 40 to point to the link of your       active Monitordroid web server. This can be a domain or an IP address. If you are using a LAN IP address, you will not    be able to use Monitordroid if your device leaves your local network. To solve this, you can forward port 80 from your    router to the local machine on your network running your web server and use your router's public IP address in this      field.

4. Set the 'SENDER_ID' variable on line 44 to the Sender ID you retrieved from step 1 of the instructions for the           Monitordroid-Web-Application

5. Export an APK and install it onto your intended device.  
