package net.mandown.db;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Michael on 02/03/2017.
 */
public class DBServiceTest {

    DBService mStaticInstance;

    @Before
    public void setUp() throws Exception {

        // Get context and start service
        Context c = InstrumentationRegistry.getTargetContext();
        DBService.startActionResetDatabase(c);
        Thread.sleep(1000);

        // Check static instance is not filled
        assertNotNull(DBService.sInstance);
        mStaticInstance = DBService.sInstance;
    }

    @After
    public void tearDown() throws Exception {
        if (mStaticInstance != null) {
            // mStaticInstance.onDestroy();
        }
    }

    @Test
    public void startActionResetDatabase() throws Exception {
        assertNotNull(mStaticInstance);
        // Insert an extra user name
        SQLiteDatabase w_db = mStaticInstance.getDbHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserNameReaderContract.UserNameEntry.COLUMN_NAME_USER_NAME, "Gwen Stacey");
        long row_id = w_db.insert(UserNameReaderContract.UserNameEntry.TABLE_NAME, null, values);
        assertTrue(row_id > 1);

        // Reset the database
        DBService.startActionResetDatabase(InstrumentationRegistry.getTargetContext());
        Thread.sleep(1000);

        // Check how many usernames there are
        SQLiteDatabase r_db = mStaticInstance.getDbHelper().getReadableDatabase();

        // Get the IDs and user names currently in the database
        String countQuery = "SELECT * FROM " + UserNameReaderContract.UserNameEntry.TABLE_NAME;
        Cursor cursor = r_db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        assertEquals(cnt, 1);
        assertNotEquals(cnt, row_id);
    }

    @Test
    public void startActionPutPassive() throws Exception {
        assertNotNull(mStaticInstance);
        // Assumes that getNumPassiveReadings works
        int before_readings = mStaticInstance.getNumPassiveReadings();
        assertNotNull(before_readings);
        Context c = InstrumentationRegistry.getTargetContext();
        for (int i = 0; i < 5; i++) {
            DBService.startActionPutPassive(c, i, i, i);
        }
        int after_readings = mStaticInstance.getNumPassiveReadings();
        assertNotNull(after_readings);
        assertEquals(before_readings + 5, after_readings);
    }

    @Test
    public void getUserNames() throws Exception {
        // Reset the database to be sure that Joe Bloggs is present
        DBService.startActionResetDatabase(InstrumentationRegistry.getTargetContext());
        // Insert Gwen Stacey to be sure that string arrays function correctly
        SQLiteDatabase w_db = mStaticInstance.getDbHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserNameReaderContract.UserNameEntry.COLUMN_NAME_USER_NAME, "Gwen Stacey");
        long row_id = w_db.insert(UserNameReaderContract.UserNameEntry.TABLE_NAME, null, values);
        assertTrue(row_id > 1);

        // Get the list of user names
        List<String> names = DBService.sInstance.getUserNames();
        assertTrue(names.size() == 2);
        assertTrue(names.get(0) == "Joe Bloggs");
        assertTrue(names.get(1) == "Gwen Stacey");
    }

    @Test
    public void getNumPassiveReadings() throws Exception {
        // Check how many usernames there are
        SQLiteDatabase r_db = mStaticInstance.getDbHelper().getReadableDatabase();

        // Get the IDs and user names currently in the database
        String countQuery = "SELECT * FROM " +
                            PassiveDataReaderContract.PassiveDataEntry.TABLE_NAME;
        Cursor cursor = r_db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        assertEquals(cnt, mStaticInstance.getNumPassiveReadings());
    }

}