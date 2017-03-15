package net.mandown.history;

/**
 * Simple class to contain a previous intoxication record with date, time, and level
 */
public class IntoxicationRecord {
    public String date;
    public String time;
    public String level;

    public IntoxicationRecord(String date, String time, String level) {
        this.date = date;
        this.time = time;
        this.level = level;
    }
}
