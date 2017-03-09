package net.mandown.ml;

/**
 * Simple extension of Exception for ML exceptions
 */
public class PredictionException extends Exception {
    public PredictionException(String message) {
        super(message);
    }
}
