package xyz.cyanclay.buptallinone.network.jwgl;

import java.util.LinkedList;

public class Scores extends LinkedList<Score> {

    private int rank;
    private float avg;
    private float weightedAvg;
    private float points;
    private float avgGpa;
    private float weightedGpa;

    public void setAvg(float avg) {
        this.avg = avg;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setWeightedAvg(float weightedAvg) {
        this.weightedAvg = weightedAvg;
    }

    public void setPoints(float points) {
        this.points = points;
    }

    public void setAvgGpa(float avgGpa) {
        this.avgGpa = avgGpa;
    }

    public void setWeightedGpa(float weightedGpa) {
        this.weightedGpa = weightedGpa;
    }

    public float getAvg() {
        return avg;
    }

    public float getWeightedAvg() {
        return weightedAvg;
    }

    public int getRank() {
        return rank;
    }

    public float getAvgGpa() {
        return avgGpa;
    }

    public float getPoints() {
        return points;
    }

    public float getWeightedGpa() {
        return weightedGpa;
    }
}
