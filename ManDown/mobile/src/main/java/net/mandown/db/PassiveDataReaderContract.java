package net.mandown.db;

import android.provider.BaseColumns;

/**
 * Created by Michael on 27/02/2017.
 */
public final class PassiveDataReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PassiveDataReaderContract() {}

    /* Inner class that defines the table contents */
    public static class PassiveDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "passive";
        public static final String COLUMN_NAME_ID = "user_id";
        public static final String COLUMN_NAME_ACCEL = "accel";
        public static final String COLUMN_NAME_GYRO = "gyro";
        public static final String COLUMN_NAME_MAGNET = "magnet";
    }
}
