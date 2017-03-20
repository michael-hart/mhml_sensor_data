package net.mandown.db;

import com.google.gson.internal.Primitives;

import net.mandown.sensors.SensorSample;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Methods for computing ML features from given data
 */
public class IntoxAlgs {

    /* Methods for Long lists */
    public static class LongAlgs {
        public static long sum(List<Long> list) {
            long result = 0;
            if (list != null && list.size() > 0) {
                for (Long l : list) {
                    result += l;
                }
            }
            return result;
        }

        public static double mean(List<Long> list) {
            double result = -1;
            if (list != null && list.size() > 0) {
                result = sum(list) / list.size();
            }
            return result;
        }

        public static double variance(List<Long> list) {
            if (list == null || list.size() == 0) {
                return -1;
            } else if (list.size() == 1) {
                return 0;
            } else {
                double sumDiffsSquared = 0.0;
                double avg = mean(list);
                for (double value : list) {
                    double diff = value - avg;
                    diff *= diff;
                    sumDiffsSquared += diff;
                }
                return sumDiffsSquared / (list.size() - 1);
            }
        }
    }

    /* Methods for Float lists */
    public static class FloatAlgs {
        public static float sum(List<Float> list) {
            float result = 0;
            if (list != null && list.size() > 0) {
                for (Float f : list) {
                    result += f;
                }
            }
            return result;
        }

        public static double mean(List<Float> list) {
            double result = -1;
            if (list != null && list.size() > 0) {
                result = sum(list) / list.size();
            }
            return result;
        }

        public static double variance(List<Float> list) {
            if (list == null || list.size() == 0) {
                return -1;
            } else if (list.size() == 1) {
                return 0;
            } else {
                double sumDiffsSquared = 0.0;
                double avg = mean(list);
                for (double value : list) {
                    double diff = value - avg;
                    diff *= diff;
                    sumDiffsSquared += diff;
                }
                return sumDiffsSquared / (list.size() - 1);
            }
        }
    }

    /* Methods for extracting ts,x,y,z from SensorSample */

    public static List<Long> getSensorTs(List<SensorSample> list) {
        if (list == null || list.size() == 0) {
            return null;
        } else {
            List<Long> result = new ArrayList<>();
            for (SensorSample ss : list) {
                result.add(ss.mTimestamp);
            }
            return result;
        }
    }

    public static List<Float> getSensorX(List<SensorSample> list) {
        if (list == null || list.size() == 0) {
            return null;
        } else {
            List<Float> result = new ArrayList<>();
            for (SensorSample ss : list) {
                result.add(ss.mX);
            }
            return result;
        }
    }

    public static List<Float> getSensorY(List<SensorSample> list) {
        if (list == null || list.size() == 0) {
            return null;
        } else {
            List<Float> result = new ArrayList<>();
            for (SensorSample ss : list) {
                result.add(ss.mY);
            }
            return result;
        }
    }

    public static List<Float> getSensorZ(List<SensorSample> list) {
        if (list == null || list.size() == 0) {
            return null;
        } else {
            List<Float> result = new ArrayList<>();
            for (SensorSample ss : list) {
                result.add(ss.mZ);
            }
            return result;
        }
    }

}
