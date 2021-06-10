package xyz.cyanclay.poststudent.entity.iclass;

/**
 * Time: 2021/4/15
 * An entity class of iclass course, stores some information while processing.
 * Only getter/setter things in this class.
 */
public class IClassCourse {

    private String courseName;
    private String courseID;
    private String url;
    private String teacherName;

    public String getCourseID() {
        return courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getUrl() {
        return url;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
