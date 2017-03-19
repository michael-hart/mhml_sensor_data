"""File containing MLStringBuilder class"""

from datetime import datetime
import inspect
import numpy as np
from OrderedSet import OrderedSet

# To add to dictionary:
# - Add a print friendly name to CSV_FRIENDLY if you want it printed, or None 
# if not
# - Add the same key to the _config with a default value
# - Add the same key to the argument list of configure WITH THE SAME SPELLING
# EXACTLY
# - Add process for printing result to build_from

CSV_FRIENDLY = {
    "username": "User Name",
    "timestamps": "Date/Time",
    "var_passive_accel": "PAV X,PAV Y,PAV Z",
    "var_walk_accel": "WAV X,WAV Y,WAV Z",
    "var_walk_gyro": "WGV X,WGV Y,WGV Z",
    "var_gamescore": "GSV X,GSV Y,GSV Z",
    "beer_score": "score",
    "var_beer_time": "vrt",
    "mean_beer_time": "mrt",
    "min_datetime": None,
    "max_datetime": None,
}

# All allowed fields containing datetimes under a user
SENSOR_FIELDS = ["accelerometer", "Drunkness", "gyro", "Magnetometer",
                 "reaction", "Sensorgame", "watch", "Whack-A-Beer Score"]

# Walk has subfields that need to be dealt with separately
WALK_FIELDS = ["accelerometer", "gyro", "Magnetometer"]


def get_datetimes(input_dict):
    """Produces a list of all date times containing data"""
    times = []
    for field in SENSOR_FIELDS:
        if field in input_dict:
            for dt_str in input_dict[field].keys():
                # Add if unique
                if dt_str not in times:
                    times += [dt_str]

    for field in WALK_FIELDS:
        if "Walk" in input_dict and field in input_dict["Walk"]:
            for dt_str in input_dict["Walk"][field].keys():
                # Add if unique
                if dt_str not in times:
                    times += [dt_str]

    # Convert to datetime objects to properly sort
    date_times = [datetime.strptime(x, "%d-%m-%Y %H:%M:%S") for x in times]
    return sorted(date_times)


class MLStringBuilder(object):
    """Allows configuration of which parameters to write to string"""

    _config = {"username": False,
               "timestamps": False,
               "var_passive_accel": False,
               "var_walk_accel": False,
               "var_walk_gyro": False,
               "var_gamescore": False,
               "beer_score": False,
               "var_beer_time": False,
               "mean_beer_time": False,
               "min_datetime": None,
               "max_datetime": None,
              }

    def __init__(self):
        """Initialisation function for MLStringBuilder"""
        pass


    def configure(self, username=None, timestamps=None, 
                  var_passive_accel=None, var_walk_accel=None, 
                  var_walk_gyro=None, var_gamescore=None, beer_score=None,
                  var_beer_time=None, mean_beer_time=None, min_datetime=None,
                  max_datetime=None):
        """Configure any single parameter in the allowed parameters"""
        argspec = inspect.getargspec(self.configure)

        # Check each conf in dictionary and change it if necessary
        for conf in argspec.args[1:]:
            current = locals()[conf]
            if current != None:
                self._config[conf] = current


    def csv_headers(self):
        """Returns a CSV string with the currently configured headers"""
        build_str = ""
        for field, val in self._config.items():
            lookup = CSV_FRIENDLY[field]
            if val and lookup != None:
                build_str += lookup + ","

        if len(build_str) == 0:
            # TODO: Raise an error for lack of configuration
            return None
        else:
            # Remove trailing comma and add newline char
            return build_str[:-1] + '\n'


    def build_from(self, input_dict, user):
        """Calculates data from input_str and builds string with all rows"""

        # Total construction string of all rows
        total_str = ""

        # Get list of datetimes
        for dt in get_datetimes(input_dict):

            # User name string and current row string
            user_str = ""
            build_str = ""

            # Get string from datetime object
            dt_str = dt.strftime("%d-%m-%Y %H:%M:%S")
                
            # Check minimum date
            if (self._config["min_datetime"] != None and 
                    self._config["min_datetime"] > dt):
                continue

            # Check maximum date
            if (self._config["max_datetime"] != None and 
                    self._config["max_datetime"] < dt):
                continue

            # Add given username if required
            if self._config["username"]:
                if user:
                    user_str += user + ","
                else:
                    user_str += "Unknown,"

            # Calculate passive accelerometer variance
            if (self._config["var_passive_accel"] 
                    and "accelerometer" in input_dict):

                if dt_str in input_dict["accelerometer"]:

                    # Get variance for x, y, z
                    variance = []
                    for axis in ["x", "y", "z"]:
                        data = input_dict["accelerometer"]\
                                         [dt_str].get(axis, [])
                        if data and len(data) > 0:
                            variance += [np.var(data)]
                        else:
                            variance += [-1]

                    # Write the results to the output string
                    build_str += ",".join([str(x) for x in variance]) + ","

                else:
                    # Insert empty cells if datetime is not present
                    build_str += ",,,"

            # Calculate walk accelerometer variance
            if (self._config["var_walk_accel"] 
                    and "Walk" in input_dict 
                    and "accelerometer" in input_dict["Walk"]):

                if dt_str in input_dict["Walk"]["accelerometer"]:

                    # Get variance for x, y, z
                    variance = []
                    for axis in ["x", "y", "z"]:
                        data = input_dict["Walk"]["accelerometer"]\
                                         [dt_str].get(axis, [])
                        if data and len(data) > 0:
                            variance += [np.var(data)]
                        else:
                            variance += [-1]

                    # Write the results to the output string
                    build_str += ",".join([str(x) for x in variance]) + ","

                else:
                    # Insert empty cells if datetime is not present
                    build_str += ",,,"

            # Calculate walk gyro variance
            if (self._config["var_walk_gyro"] 
                    and "Walk" in input_dict):

                if dt_str in input_dict["Walk"]["gyro"]:

                    # Get variance for x, y, z
                    variance = []
                    for axis in ["x", "y", "z"]:
                        data = input_dict["Walk"]["gyro"][dt_str][axis]
                        if data and len(data) > 0:
                            variance += [np.var(data)]
                        else:
                            variance += [-1]

                    # Write the results to the output string
                    build_str += ",".join([str(x) for x in variance]) + ","

                else:
                    # Insert empty cells if datetime is not present
                    build_str += ",,,"

            # Calculate game score accelerometer variance
            if self._config["var_gamescore"] and "Sensorgame" in input_dict:

                if dt_str in input_dict["Sensorgame"]:

                    # Get variance for x, y, z
                    variance = []
                    for axis in ["x", "y", "z"]:
                        data = input_dict["Sensorgame"][dt_str].get(axis, [])
                        if data and len(data) > 0:
                            variance += [np.var(data)]
                        else:
                            variance += [-1]

                    # Write the results to the output string
                    build_str += ",".join([str(x) for x in variance]) + ","

                else:
                    # Insert empty cells if datetime is not present
                    build_str += ",,,"

            # Get the beer score if available
            if (self._config["beer_score"] 
                    and "Whack-A-Beer Score" in input_dict):

                if dt_str in input_dict["Whack-A-Beer Score"]:
                    build_str += str(input_dict["Whack-A-Beer Score"][dt_str])
                build_str += ","

            # Calculate mean/variance of reaction times
            if ((self._config["var_beer_time"] 
                 or self._config["mean_beer_time"])
                    and "reaction" in input_dict):

                if dt_str in input_dict["reaction"]:

                    # Get variance of score
                    if self._config["var_beer_time"]: 
                        data = input_dict["reaction"][dt_str]
                        if data and len(data) > 0:
                            build_str += str(np.var(data))
                        build_str += ","

                    # Get mean of score
                    if self._config["mean_beer_time"]: 
                        data = input_dict["reaction"][dt_str]
                        if data and len(data) > 0:
                            build_str += str(np.var(data))
                        build_str += ","


            # Only add the timestamp if we're sure there's other data
            if (len(build_str.replace(',', '')) > 0 
                    and self._config["timestamps"]):
                build_str = dt_str + "," + build_str

            # Add user string, done at the end to allow timestamp insertion
            if len(build_str.replace(',', '')) > 0:
                build_str = user_str + build_str
                # Concatenate the built string to the total string
                total_str += build_str[:-1] + '\n'

        return total_str


if __name__ == '__main__':
    builder = MLStringBuilder()
    builder.configure(username=True, var_passive_accel=True)
