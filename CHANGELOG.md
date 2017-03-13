# Change Log
All notable changes to this project will be documented in this file.

The format is based on [keep-a-changelog](https://github.com/olivierlacan/keep-a-changelog)
but does NOT adhere to [Semantic Versioning](http://semver.org/) as it is too
short a project. Version numbers will simply be MAJOR.MINOR, where MINOR is a
merged branch, and MAJOR is a change signalling a release.

Each change will list Added, Changed, and Fixed.

## 0.8.0 - 2017-03-13
### Added
- Additional database methods for storing walking data

## 0.7.0 - 2017-03-13
### Added
- Calls to get intoxication level from machine learning
- Updates to beer glass with intoxication level
- Notifications if the user is too intoxicated

## 0.6.0 - 2017-03-09
### Changed
- Modified SensorService to schedule tasks using a timer instead of AlarmManager

## 0.5.0 - 2017-03-09
### Added
- Added disclaimer on app start-up

## 0.4.0 - 2017-03-08
### Changed
- Firebase has been moved into DBService

### Removed
- All SQLite functionality has been removed

## 0.3.0 - 2017-03-06
### Changed
- UI has more controls and theme is coloured

## 0.2.0 - 2017-03-05
### Added
- SensorService to poll sensors at intervals and put data into database

### Changed
- Database has new tables for accelerometer, gyroscope, and magnetometer data

## 0.1.0 - 2017-03-05
### Added
- Entire project in its current status
- Changelog file
