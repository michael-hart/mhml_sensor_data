package net.mandown.db;

import android.provider.BaseColumns;

/**
 * Define table contents of pure accelerometer data
 */
public final class GyroDataReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private GyroDataReaderContract() {}

    /* Inner class that defines the table contents */
    public static class GyroDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "gyro";
        public static final String COLUMN_NAME_ID = "user_id";
        public static final String COLUMN_NAME_TS = "timestamp";
        public static final String COLUMN_NAME_GYRO_X = "gyro_x";
        public static final String COLUMN_NAME_GYRO_Y = "gyro_y";
        public static final String COLUMN_NAME_GYRO_Z = "gyro_z";
    }
}
