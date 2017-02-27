package net.mandown.db;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

import net.mandown.db.UserNameReaderContract.UserNameEntry;

/**
 * Created by Michael on 27/02/2017.
 */
public class ManDownDbHelperTest {

    private ManDownDbHelper mDbHelper;

    @Test
    public void onCreate() throws Exception {
        // Create the context
        mDbHelper = new ManDownDbHelper(InstrumentationRegistry.getTargetContext());
        // If no exception is thrown, test passes
    }

    @Test
    public void testOneRow() throws Exception {
        SQLiteDatabase r_db = mDbHelper.getReadableDatabase();
        String countQuery = "SELECT count(*) FROM " + UserNameEntry.TABLE_NAME;
        Cursor cursor = r_db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        assertEquals(cnt, 1);
    }

}