"""Read data from Firebase and generate a raw csv of machine learning data"""

# Global imports
import argparse
from firebase import firebase
import json
import os
import time

# Local imports
from MLStringBuilder import MLStringBuilder

# Constant definitions
DEFAULT_DB_URL = "https://mandown-application.firebaseio.com/"
DEFAULT_CSV = "testdata.csv"
CACHE_FILE = "db_cache.json"


def connect_db(db_url):
    """Return connection object to given URL"""
    return firebase.FirebaseApplication(db_url, None)


def put_test_data(db, username):
    """Put some data like passive, accelerometer, gyro"""

    # Construct a dictionary containing all of our test data
    user_data = {username: {"accelerometer": {"15-03-2017 09:00:00": {"x": list(range(5)),
                                                                      "y": list(range(5)),
                                                                      "z": list(range(5)),
                                                                      "timestamp": list(range(5))}}}}
    user_data[username]["gyro"] = user_data[username]["accelerometer"]
    db.put(DEFAULT_DB_URL, '/Users', user_data)


def collect_data():
    """Connects to Firebase and returns the required data as a dictionary of lists"""

    start_time = time.time()
    username = "Santiago Rubio"

    # Check for cached file
    use_cache = True
    if use_cache and os.path.exists(CACHE_FILE):
        user_data = {}
        print("Cache file found, accessing...")
        with open(CACHE_FILE) as f:
            try:
                user_data = json.load(f)
            except ValueError:
                print("Error while reading from cache. Exiting... ")
                return
    else:
        print("Connecting to Firebase")
        db = connect_db(DEFAULT_DB_URL)

        # Get data from server and time the connection
        user_data = db.get("/Users/" + username, None)
        if user_data == None:
            print("No user data found. Exiting...")
        # Write data if using cache
        if use_cache:
            print("Writing collected data to cache file...")
            with open(CACHE_FILE, 'w') as f:
                json.dump(user_data, f)

    # Create instance of builder and configure
    builder = MLStringBuilder()
    builder.configure(username=True, timestamps=True, 
                      var_passive_accel=True, var_walk_accel=True, 
                      var_walk_gyro=True, var_gamescore=True, beer_score=True,
                      var_beer_time=True, mean_beer_time=True)

    print("Collected data in {}s. Writing to CSV file..."
          .format(time.time() - start_time))
    with open(DEFAULT_CSV, 'w') as out_f:
        out_f.write(builder.csv_headers())
        out_f.write(builder.build_from(user_data, username))

    print("Written to file. Exiting...")


def main():
    """Entry point of application"""

    # Set up command line arguments
    # parser = argparse.ArgumentParser(description="Select features to output.")
    # Want username, data type list out of a list of options
    # Can't be arsed with this right now

    collect_data()


if __name__ == '__main__':
    main()
