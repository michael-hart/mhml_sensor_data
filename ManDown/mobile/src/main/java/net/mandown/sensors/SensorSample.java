package net.mandown.sensors;

/**
 * Accelerometer Sample class with timestamp of time sample was taken and x,y,z changes
 */
public class SensorSample {
    public long mTimestamp;
    public float mX, mY, mZ;
    public SensorSample(long timestamp, float x, float y, float z) {
        mTimestamp = timestamp;
        mX = x;
        mY = y;
        mZ = z;
    }
}
