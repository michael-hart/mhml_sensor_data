package net.mandown.db;

import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for database service
 */
public class DBServiceTest {

    @Test
    public void startActionPutReactionTimes() throws Exception {
        ArrayList<Long> myList = new ArrayList<Long>();
        myList.add(100l);
        myList.add(200l);
        myList.add(300l);
        DBService.startActionPutReactionTimes(InstrumentationRegistry.getTargetContext(), myList);
        Thread.sleep(100000);
    }

}