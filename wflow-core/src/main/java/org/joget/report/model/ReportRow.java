package org.joget.report.model;

public class ReportRow {

    private String id;
    private String name;
    private double minDelay;
    private double maxDelay;
    private double ratioWithDelay;
    private double ratioOnTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(double maxDelay) {
        this.maxDelay = maxDelay;
    }

    public double getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(double minDelay) {
        this.minDelay = minDelay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRatioOnTime() {
        return ratioOnTime;
    }

    public void setRatioOnTime(double ratioOnTime) {
        this.ratioOnTime = ratioOnTime;
    }

    public double getRatioWithDelay() {
        return ratioWithDelay;
    }

    public void setRatioWithDelay(double ratioWithDelay) {
        this.ratioWithDelay = ratioWithDelay;
    }
}
