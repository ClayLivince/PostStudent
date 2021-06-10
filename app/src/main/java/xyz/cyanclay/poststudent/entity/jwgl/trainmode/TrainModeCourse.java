package xyz.cyanclay.poststudent.entity.jwgl.trainmode;

import java.util.Objects;

public class TrainModeCourse {

    //public TrainModeCourseSubGroup subGroup;

    public String courseID;
    public String courseName;
    public boolean isPassed = false;
    public boolean isFailed = false;
    public boolean isBanned = false;
    public boolean isTeaching = false;
    public boolean isSelectable = false;

    public String courseCategory;
    public String courseType;

    public float point;
    public int courseHour;
    public int practiceHour;
    public int lectureHour;
    public int experimentHour;
    public int otherHour;
    public int totalHour;

    public int term;

    public TrainModeCourse() {
    }

    public TrainModeCourse(String courseID) {
        this.courseID = courseID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainModeCourse that = (TrainModeCourse) o;
        return Float.compare(that.point, point) == 0 &&
                courseID.equals(that.courseID) &&
                Objects.equals(courseCategory, that.courseCategory) &&
                Objects.equals(courseType, that.courseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID, courseCategory, courseType, point);
    }

    public static class PublicCoursePlaceHolder extends TrainModeCourse {
        TrainModeCourseGroup group;

        public PublicCoursePlaceHolder(TrainModeCourseGroup group) {
            this.group = group;
            this.courseID = "public_place_holder";
        }

        public TrainModeCourseGroup getGroup() {
            return group;
        }
    }
}


