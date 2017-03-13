package net.mandown.ml;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.machinelearning.AmazonMachineLearningClient;
import com.amazonaws.services.machinelearning.model.EntityStatus;
import com.amazonaws.services.machinelearning.model.GetMLModelRequest;
import com.amazonaws.services.machinelearning.model.GetMLModelResult;
import com.amazonaws.services.machinelearning.model.PredictRequest;
import com.amazonaws.services.machinelearning.model.PredictResult;
import com.amazonaws.services.machinelearning.model.RealtimeEndpointStatus;

import java.util.Map;

/**
 * Class used to connect to Amazon services and create a prediction based on data
 */
public class RealtimePrediction {

    // Class Constants
    private static final String ACCESS_KEY = "AKIAJQJKGOLKOOFNATNQ";
    private static final String SECRET_KEY = "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";
    private static final String MODEL_ID = "ml-SE2VPcgnZdb";

    private final String mlModelId;
    private String endpoint;
    private AmazonMachineLearningClient client;

    // Member variables
    private PredictResult mPrediction;
    private AWSCredentials mCredentials;


    /*
     * Overloaded constructors for object
     */

    public RealtimePrediction(String mlModelId, AWSCredentials credentials) {
        this.mlModelId = mlModelId;
        this.mCredentials = credentials;
    }
    public RealtimePrediction() {
        this(MODEL_ID, new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
    }
    public RealtimePrediction(String mlModelId) {
        this(mlModelId, new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
    }
    public RealtimePrediction(AWSCredentials credentials) {
        this(MODEL_ID, credentials);
    }


    /*
     * Method Definitions
     */

    //connect to the realtime endpoint of the model
    public void connect() throws PredictionException {

        GetMLModelResult result;

        if (mCredentials == null) {
            throw new PredictionException("Invalid credentials in exception");
        }

        try {
            this.client = new AmazonMachineLearningClient(mCredentials);
            GetMLModelRequest request = new GetMLModelRequest();
            request.setMLModelId(mlModelId);
            result = client.getMLModel(request);
        } catch (AmazonServiceException ase) {
            Log.e("RealtimePrediction", "Connection error: " + ase.getStackTrace());
            throw new PredictionException("Connection Error");
        } catch (AmazonClientException ace) {
            Log.e("RealtimePrediction", "Connection error: " + ace.getStackTrace());
            throw new PredictionException("Connection Error");
        }

        //some debugging
        if (!result.getStatus().equals(EntityStatus.COMPLETED.toString())) {
            throw new PredictionException("ML model " + mlModelId + " needs to be completed.");
        }
        if (!result.getEndpointInfo().getEndpointStatus().equals(
                RealtimeEndpointStatus.READY.toString())) {
            throw new PredictionException("ML model " + mlModelId +
                    "'s real-time endpoint is not yet ready or needs to be created.");
        }

        this.endpoint = result.getEndpointInfo().getEndpointUrl();
    }

    //connect and predict, return full prediction as a PredictResult object
    public PredictResult predict(Map<String, String> record) {
        PredictRequest request = new PredictRequest();
        request.setMLModelId(mlModelId);
        request.setPredictEndpoint(endpoint);

        // Populate record with data relevant to the ML model
        request.setRecord(record);
        PredictResult result = client.predict(request);

        mPrediction = result;

        return result;
    }

    /*
     * Getter methods for prediction results
     */
    public String getPredictedLabel() throws PredictionException {
        if (mPrediction != null) {
            return mPrediction.getPrediction().getPredictedLabel();
        }
        throw new PredictionException("No prediction yet made.");
    }

    public Map<String, Float> getPredictedScores() throws PredictionException {
        if (mPrediction != null) {
            return mPrediction.getPrediction().getPredictedScores();
        }
        throw new PredictionException("No prediction yet made.");
    }

    public float getPredictedScore(String label) throws PredictionException {
        if (mPrediction != null) {
            return getPredictedScores().get(label);
        }
        throw new PredictionException("No prediction yet made.");
    }
}

