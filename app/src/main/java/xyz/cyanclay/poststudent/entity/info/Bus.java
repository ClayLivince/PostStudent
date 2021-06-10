package xyz.cyanclay.poststudent.entity.info;

/**
 * Time: 2021/1/4
 * An entity class of Bus.
 */
public class Bus {


    /**
     * Destinations
     * 0 本部 main campus
     * 1 沙河 shahe campus
     */
    private int destination;

    private int weekday;

    private String time;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWeekday() {
        return weekday;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
