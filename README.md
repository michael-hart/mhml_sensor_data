# ManDown

Android application and server-side code for our ManDown application, used to detect alcohol intoxication levels of the user using game data and passive sensor data. The components are as follows:

## Android Application

The Android application will be split into a Mobile project, a Wear project, and a common folder containing components used for both projects.

### Common Components

#### SensorService
Threaded background service to continuously take data from passive sensors, depending on whether it is started by the Mobile application or the Wear application. Polls at a set rate for a set period of time, then waits an interval before another sampling session. The service then writes all the data into the database using the DBService.

#### DBService
Intent-based service that provides interface to Firebase database. Stores the user ID after a sign-in and provides helper methods for inserting data of all kinds and reading out the latest intoxication level from the server.

### Wear Components

The only Android Wear component not in the common components folder is a Service to listen for messages.

#### WearListenerService
Listens for commands from the Mobile application to begin taking data, and stop taking data. If not connected, the mobile application will continue to run without it.

### Mobile Components

The mobile application contains the same background data collection as in Wear, but also includes all the UI components and the games, making it substantially more complex.

#### MainActivity

The first screen to be displayed to the user. Checks with the DBService to see if the user is signed in, and if not, redirects to the SignInActivity. Once the user is signed in, triggers the SensorService to begin collection, and allows the user access to the app, including games and current intoxication level.

#### Game Activities

This includes all games to be written as part of the application, such as Ring of Fire and Whack-A-Beer. These games will be used as normal, using DBService to write data into Firebase. As Intent Services are threaded by default, the games can continue to write data uninterrupted.

## Server-side Application

The server-side application will include code to filter database results, use machine learning to adapt a general model to each user, and calculate intoxication levels for recent data. Once the intoxication level is obtained, it will be inserted back into the database so that the Android Application can retrieve it during its next network connection.
