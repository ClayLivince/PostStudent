package xyz.cyanclay.buptallinone.network.info;

public class Bus {


    /**
     * 目的地
     * 0 本部
     * 1 沙河
     */
    private int destination;

    private int weekday;

    private String time;

    private String type;

    public int getDestination() {
        return destination;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }
}
