package net.mandown.db;

import android.provider.BaseColumns;

/**
 * Define table contents of pure accelerometer data
 */
public final class AccelDataReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private AccelDataReaderContract() {}

    /* Inner class that defines the table contents */
    public static class AccelDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "accel";
        public static final String COLUMN_NAME_ID = "user_id";
        public static final String COLUMN_NAME_TS = "timestamp";
        public static final String COLUMN_NAME_ACCEL_X = "accel_x";
        public static final String COLUMN_NAME_ACCEL_Y = "accel_y";
        public static final String COLUMN_NAME_ACCEL_Z = "accel_z";
    }
}
