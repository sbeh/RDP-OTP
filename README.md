# RDP OTP
One time password authentication for Windows remote desktop environments

## How does it work?
Everytime you log into your remote desktop environment, your password gets automatically changed.
The new password is then encrypted with the public key of a personal RSA key pair.
The key pair's only purpose is to be used to send one time passwords with an extra level of safety.
Your encrypted password is then encoded as an QR code image.
This QR code is uploaded to any FTP, WebDAV or network share of your choice.
Then you can access the QR code image with the browser even from untrusted systems.
Your smartphone holds the private key of your RSA key pair.
It is therefor able to decrypt your one time password from the scanned QR code image.
You can read the unencrypted password from the screen of your smartphone and use it to log into your remote desktop environment using the untrusted system.
Now that you are logged in, the password has already been changed in the background.

## Installation procedure
* Install<br/>
 https://github.com/sbeh/RDP-OTP/raw/master/Android/RDP_OTP_Client/bin/RDP_OTP.apk<br/>
 onto your android device
* Run app **RDP OTP** on your android device
* Click **Generate key** which generates a RSA 1024 bit keypair<br/>
  Private key is only accessable by this app<br/>
  Public key is saved as **pubkey.pem** on the external sd card
* Copy **pubkey.pem** to your remote desktop session
* In your remote desktop session, copy<br/>
   https://github.com/sbeh/RDP-OTP/raw/master/Windows/RDP%20OTP%20Server/bin/Debug/RDP%20OTP%20Server.exe<br/> to<br/>
   C:\Users\[Your user name]\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup
* Place the **pubkey.pem** in the same folder
* Create a file **oldPass.txt** in the same folder, which contains your current password<br/>
  No line breaks please!<br/>
  Make it accessable only by you
* Create a file **uploadTo.url** in the same folder, that contains a link where the QR code is going to be uploaded to. It should look similar to one of these:<br/>
  file://N:/Storage/OTP_of_DevMachine.png<br/>
  ftp://username:P4$$w0rd@cloudspace.mycompany.net/myPersonalSpace/LoginMeIn.png<br/>
  https://ralf:ralfspassword@webdavhost.intern/OTP.png<br/>
  file://C:/Users/User/Dropbox/PublicSecrets/MyLogin.png<br/>
  No line breaks please!