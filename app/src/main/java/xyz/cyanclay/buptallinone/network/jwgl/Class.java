package xyz.cyanclay.buptallinone.network.jwgl;

import org.json.JSONException;
import org.json.JSONObject;

public class Class {

    public byte day;
    public byte startSection;
    public byte endSection;
    public String startTime;
    public String endTime;
    public byte weekType;
    public String weekHas;

    public String className;
    public String classRoom;
    public String teacherName;


    Class(JSONObject object) {
        parseClass(object);
    }

    void parseClass(JSONObject object) {
        try {
            className = object.getString("kcmc");
            classRoom = object.getString("jsmc");
            teacherName = object.getString("jsxm");
            startTime = object.getString("kssj");
            endTime = object.getString("jssj");
            weekHas = object.getString("kkzc");

            String section = object.getString("kcsj");
            day = Byte.parseByte(section.split("0")[0]);
            startSection = Byte.parseByte(section.split("0")[1]);
            endSection = Byte.parseByte(section.split("0")[2]);
            weekType = Byte.parseByte(object.getString("sjbz"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
