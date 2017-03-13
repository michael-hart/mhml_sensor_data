package net.mandown.ml;

import android.util.Log;

import com.amazonaws.services.machinelearning.model.PredictResult;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test suite to check that machine learning is working correctly
 */
public class RealtimePredictionTest {

    //create and populate map containing pairs of attribute name and value - both strings
    static final Map<String,String> CLASSIFY_ME;

    static {
        CLASSIFY_ME = new HashMap<String,String>();
        CLASSIFY_ME.put("ax","0.85"); CLASSIFY_ME.put("ay","0.87"); CLASSIFY_ME.put("az","0.75");
        CLASSIFY_ME.put("gx","1.13"); CLASSIFY_ME.put("gy","-1.02"); CLASSIFY_ME.put("gz","1.01");
        //example of using a variable; cast to a string using ""+ (or .toString() depending on scope)
        CLASSIFY_ME.put("rt", ""+1.66d);
    }

    private RealtimePrediction testObj;

    @Before
    public void setUp() throws Exception {
        // Set up the test object
        testObj = new RealtimePrediction();
        assertNotNull(testObj);
    }

    @Test
    public void connect() throws Exception {
        testObj.connect();
        assertNotNull(testObj);
    }

    @Test
    public void predict() throws Exception {
        testObj.connect();
        assertNotNull(testObj);
        PredictResult p = testObj.predict(CLASSIFY_ME);
        assertNotNull(p);
    }

    @Test
    public void getPredictedLabel() throws Exception {
        testObj.connect();
        assertNotNull(testObj);
        testObj.predict(CLASSIFY_ME);
        String label = testObj.getPredictedLabel();
        assertNotNull(label);
    }

    @Test
    public void getPredictedScores() throws Exception {
        testObj.connect();
        assertNotNull(testObj);
        testObj.predict(CLASSIFY_ME);
        Map<String, Float> results = testObj.getPredictedScores();
        assertNotNull(results);
    }

    @Test
    public void getPredictedScore() throws Exception {
        testObj.connect();
        assertNotNull(testObj);
        testObj.predict(CLASSIFY_ME);
        Map<String, Float> results = testObj.getPredictedScores();
        assertNotNull(results);
        String label = testObj.getPredictedLabel();
        float result = testObj.getPredictedScore(label);
        assertNotNull(result);
        assertTrue(result > 0);
        // Check that results are within 0.001 of each other
        assertEquals(results.get(label), result, 0.001);

        Log.d("Classification:", label);
        Log.d("Confidence: ", "" + result);
    }

}