package net.mandown.db;

import android.support.test.InstrumentationRegistry;

import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for DBService
 */
public class DBServiceTest {
    @Test
    public void startActionPutSensorList() throws Exception {
        ArrayList<SensorSample> myList = new ArrayList<>();
        myList.add(new SensorSample(10000, 1f, 1f, 1f));
        myList.add(new SensorSample(20000, 2f, 2f, 2f));
        myList.add(new SensorSample(30000, 3f, 3f, 3f));
        // Use the same test set to test all sensors
        DBService.startActionPutSensorList(InstrumentationRegistry.getTargetContext(), myList,
                SensorType.ACCELEROMETER);
        DBService.startActionPutSensorList(InstrumentationRegistry.getTargetContext(), myList,
                SensorType.GYROSCOPE);
        DBService.startActionPutSensorList(InstrumentationRegistry.getTargetContext(), myList,
                SensorType.MAGNETOMETER);
        // Allow worker thread to finish
        Thread.sleep(1000);
    }

    @Test
    public void startActionPutReactionTimes() throws Exception {
        ArrayList<Long> myList = new ArrayList<Long>();
        myList.add(100l);
        myList.add(200l);
        myList.add(300l);
        DBService.startActionPutReactionTimes(InstrumentationRegistry.getTargetContext(), myList);
        // Allow worker thread to finish
        Thread.sleep(1000);
    }

    @Test
    public void startActionPutML() throws Exception {
        String testML = "0.12";
        DBService.startActionPutML(InstrumentationRegistry.getTargetContext(), testML);
        // Allow worker thread to finish
        Thread.sleep(1000);
    }

    @Test
    public void getIntoxHistory() throws Exception {
        String testML = "0.12";
        DBService.startActionPutML(InstrumentationRegistry.getTargetContext(), testML);
        // Allow time to work
        Thread.sleep(200);

        // Get the new data
        List<String[]> results = DBService.getIntoxHistory();

        // Check for equality
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals(results.get(results.size() - 1)[1], testML);

        // Allow worker thread to finish
        Thread.sleep(1000);
    }

}