package xyz.cyanclay.buptallinone.network.jwgl;

import java.util.HashMap;
import java.util.Map;

class CourseTime {

    static String getStartTime(int section) {
        return startTime.get(section);
    }

    static String getEndTime(int section) {
        return endTime.get(section);
    }

    private static final Map<Integer, String> startTime = new HashMap<Integer, String>() {{
        put(1, "8:00");
        put(2, "8:50");
        put(3, "9:50");
        put(4, "10:40");
        put(5, "11:30");
        put(6, "13:00");
        put(7, "13:50");
        put(8, "14:45");
        put(9, "15:40");
        put(10, "16:35");
        put(11, "17:25");
        put(12, "18:30");
        put(13, "19:20");
        put(14, "20:10");
    }};

    private static final Map<Integer, String> endTime = new HashMap<Integer, String>() {{
        put(1, "8:45");
        put(2, "9:35");
        put(3, "10:35");
        put(4, "11:25");
        put(5, "12:15");
        put(6, "13:45");
        put(7, "14:35");
        put(8, "15:30");
        put(9, "16:25");
        put(10, "17:20");
        put(11, "18:10");
        put(12, "19:15");
        put(13, "20:05");
        put(14, "20:55");
    }};

}
