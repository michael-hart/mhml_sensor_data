# ManDown

Android application and server-side code for our ManDown application, used to detect alcohol intoxication levels of the user using game data and passive sensor data. The components are as follows:

## Android Application

The Android application will be split into a Mobile project, a Wear project, and machine learning components.

### Mobile Application

#### Activities
- MainActivity: The splash screen for the user, containing navigation options and a sign-in button to sign into a Google account.
- HistoryActivity: A display of the most recent records for intoxication levels identified by the machine learning component.
- JournalActivity: An activity that allows the user to input a number of drinks consumed.
- GameMenuActivity: A menu for selecting which game to play, containing information about each game.

#### Games
- Whack-A-Beer: A game where the user taps full beer glasses or cocktails to earn points. The game score and the reaction times are stored in the database.
- Tightrope Waiter: A game where the user walks in a straight line, trying to balance the glass on the tray. The passive sensor readings are stored in the database.
- Who Am I: A game that doesn't take data, but that the user can play with friends as a fun drinking game.
- Ring of Fire: A game that doesn't take data, but that the user can play with friends as a fun drinking game.

#### Services
- SensorService: Threaded background service to continuously take data from passive sensors, depending on whether it is started by the Mobile application or the Wear application. Polls at a set rate for a set period of time, then waits an interval before another sampling session. The service then writes all the data into the database using the DBService.
- DBService: Intent-based service that provides interface to Firebase database. Stores the user ID after a sign-in and provides helper methods for inserting data of all kinds and reading out the latest intoxication level from the server.
- IntoxicationService: Threaded background service to collect latest data from user games and passive data collection, and post the results to Amazon AWS to get an intoxication classification. This service then broadcasts the result, notifies the user if above a certain level, and stores the latest result for other components to check.
- SensorBroadcastService: Simple service to take data from passive sensors and broadcast them until the service is stopped. This service will be started by an activity requiring the data immediately, such as TightropeWaiter, and stopped by the same activity.
- WalkSensorService: Simple service to take data for a fixed amount of time at a fixed poll rate and store the results in the database. This service will be started when the user requests taking walking data manually.

### Wear Components

#### Activities
- MainActivity: The watch splash screen that allows the user to select one of the other activities.
- InputDrinks: An interface for the user to select how many drinks have been consumed.
- CallSOS: Allows the user to contact the local emergency services.

#### Services
- SensorBroadcast: Operates in the same manner as the SensorBroadcastService in the mobile application.

## ML Components

The machine learning components contain a script for collecting data from the Firebase database for a given user and URL. The script allows configuration of which elements to include  in an output CSV file for use in training a model.

The remainder of the work done by the machine learning is not contained in this repository as it is done in an Amazon AWS instance.
