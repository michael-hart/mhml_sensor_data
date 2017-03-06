package net.mandown.db;

import android.provider.BaseColumns;

/**
 * Define table contents of pure accelerometer data
 */
public final class MagnetDataReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MagnetDataReaderContract() {}

    /* Inner class that defines the table contents */
    public static class MagnetDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "magnet";
        public static final String COLUMN_NAME_ID = "user_id";
        public static final String COLUMN_NAME_TS = "timestamp";
        public static final String COLUMN_NAME_MAGNET_X = "magnet_x";
        public static final String COLUMN_NAME_MAGNET_Y = "magnet_y";
        public static final String COLUMN_NAME_MAGNET_Z = "magnet_z";
    }
}
