package net.mandown.db;

import android.provider.BaseColumns;

/**
 * Table definition for User IDs and Names
 */
public final class UserNameReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UserNameReaderContract() {}

    /* Inner class that defines the table contents */
    public static class UserNameEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_names";
        public static final String COLUMN_NAME_USER_NAME = "user_name";
    }
}
