# Power Outage Alert APP ⚡

## Mobile application that notifies about power outage

### Functioning
The application will send a message via telegram when it detects that the device has stopped charging. In the event that the electric current returns, it will send a message notifying the user.

### Requirements
- Phone, Tablet or Android Device with Telegram.
- Android Studio to insert the .env constants and build the APK.

### Installation
- 1 Create a Telegram bot with the help of BotFather.
- 2 Create a group with the bot and the user who will receive the alerts.
- 3 Save the Bot Token: Provided by BotFathe.
- 4 Save the ChatID: Use this link to find out the 'chat_id' - https://api.telegram.org/bot'YOUR_BOT_TOKEN'/getUpdates - Ex: https://api.telegram.org/botABC:123/getUpdates
- 5 Some programming (ATTENTION):
    - Clone the repository.
    - Open the directory with Android Studio.
    - Open the 'PowerConnectionReceiver.java' file and copy the bot token and chat id inside the quotes "". Example: String token = "ABC:123";
    - Save changes with Ctrl+S.
    - Go to the top, and create the APK: 'Build' -> 'Build Bundle(s) / APK(s)' -> 'Build APK(s)'
- 6 Once the APK is created, save the file on the device that will serve as an alert.
- 7 Install the APK on the device.
- 8 Connect the charger and make sure the device charges.
- 9 Keep the app running.

<p align="center">
<img width="186" alt="ex" src="https://user-images.githubusercontent.com/83615514/216787214-93de96a7-e3f7-4dd9-b512-2b43747634ec.png">
</p>

<p align="center">
<img width="396" alt="ex1" src="https://user-images.githubusercontent.com/83615514/216787359-15db47ff-ac1c-4780-b24c-f9e579a56bba.png">
</p>
