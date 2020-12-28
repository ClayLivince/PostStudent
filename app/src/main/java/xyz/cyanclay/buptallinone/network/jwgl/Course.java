package xyz.cyanclay.buptallinone.network.jwgl;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static xyz.cyanclay.buptallinone.network.jwgl.CourseTime.getEndTime;
import static xyz.cyanclay.buptallinone.network.jwgl.CourseTime.getStartTime;

public class Course implements Comparable<Course> {

    public int day = -1;
    public int startSection = -1;
    public int endSection = -1;
    @SerializedName("kcsj") //课程时间 a0b0c, 星期a的b到c节课
    public String courseTime;

    @SerializedName("kssj") //开课时间
    public String startTime;
    @SerializedName("jssj") //结束时间
    public String endTime;
    @SerializedName("sjbz") //时间备注 0 = 每周 1 = 单周 2 = 双周
    public byte weekType;
    @SerializedName("kkzc") //开课周次 a-b, a, b, c, a-b c-d
    public String weekHas;

    @SerializedName("kcmc") //课程名称
    public String courseName;
    @SerializedName("jsmc") //教师名称
    public String classRoom;
    @SerializedName("jsxm") //教师姓名
    public String teacherName;

    public String userMark;
    public int userColor;

    Course() {
    }

    Course(JSONObject object) {
        parseClass(object);
    }

    void verifyWeekday() {
        if (day == -1) {
            day = Byte.parseByte(courseTime.substring(0, 1));
            startSection = Byte.parseByte(courseTime.substring(1, 3));
            endSection = Byte.parseByte(courseTime.substring(3, 5));
        }
    }

    public void setStartSection(int startSection) {
        this.startSection = startSection;
        this.startTime = getStartTime(startSection);
    }

    public void setEndSection(int endSection) {
        this.endSection = endSection;
        this.endTime = getEndTime(endSection);
    }

    private void parseClass(JSONObject object) {
        try {
            courseName = object.getString("kcmc");
            classRoom = object.getString("jsmc");
            teacherName = object.getString("jsxm");
            startTime = object.getString("kssj");
            endTime = object.getString("jssj");
            weekHas = object.getString("kkzc");
            weekType = Byte.parseByte(object.getString("sjbz"));

            String section = object.getString("kcsj");
            day = Integer.parseInt(section.substring(0, 1));
            startSection = Integer.parseInt(section.substring(1, 3));
            endSection = Integer.parseInt(section.substring(3, 5));

            if (object.has("userMark")) {
                userMark = object.getString("userMark");
            }
            if (object.has("userColor")) {
                userColor = object.getInt("userColor");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Course o) {
        if (this.day > o.day) {
            return 10;
        } else if (this.day < o.day) {
            return -10;
        } else {
            if (this.startSection > o.startSection) {
                return 1;
            } else if (this.startSection < o.startSection) {
                return -1;
            } else {
                if (this.endSection > o.endSection) {
                    return -2;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return day == course.day &&
                startSection == course.startSection &&
                endSection == course.endSection &&
                weekType == course.weekType &&
                weekHas.equals(course.weekHas) &&
                courseName.equals(course.courseName) &&
                classRoom.equals(course.classRoom) &&
                teacherName.equals(course.teacherName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startSection, endSection, weekType, weekHas, courseName, classRoom, teacherName);
    }
}
