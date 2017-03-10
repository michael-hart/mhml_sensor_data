package net.mandown.db;

import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Michael on 07/03/2017.
 */
public class DBServiceTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void startActionPutReactionTimes() throws Exception {
        List<Long> myList = new ArrayList<Long>();
        myList.add(100l);
        myList.add(200l);
        myList.add(300l);
        DBService.startActionPutReactionTimes(InstrumentationRegistry.getTargetContext(), myList);
        Thread.sleep(100000);
    }

}