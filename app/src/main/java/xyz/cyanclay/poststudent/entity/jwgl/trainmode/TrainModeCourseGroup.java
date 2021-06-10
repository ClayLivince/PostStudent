package xyz.cyanclay.poststudent.entity.jwgl.trainmode;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Author : ClayLivince
 * <p>
 * This is the class of course group for courses in train method.
 */
public class TrainModeCourseGroup implements Iterable<TrainModeCourse> {

    public String groupName;
    float totalPoint = 0f;
    float passedPoint = 0f;
    //boolean hasPublic = false;
    float minimumPoint = -1f;
    String groupID = "";

    boolean isPublic;
    LinkedList<TrainModeCourse> contentCourse = new LinkedList<>();
    LinkedList<TrainModeCourse> passedPublic = null;

    public TrainModeCourseGroup(String groupName, String groupID, boolean isPublic) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.isPublic = isPublic;
        if (isPublic) this.passedPublic = new LinkedList<>();
    }

    public TrainModeCourseGroup(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<TrainModeCourse> asList() {
        if (isPublic) {
            return passedPublic;
        } else {
            return contentCourse;
        }
    }

    public int getItemCount() {
        if (isPublic) {
            return getPassedPublicSize() + 1;
        } else {
            return size();
        }

    }

    public boolean add(TrainModeCourse trainModeCourse) {
        if (isPublic) {
            if (trainModeCourse.isPassed) {
                passedPublic.add(trainModeCourse);
                return true;
            }
        }
        return contentCourse.add(trainModeCourse);
    }

    public int getPassedPublicSize() {
        return passedPublic.size();
    }

    public float getTotalPoint() {
        if (totalPoint == -1f) {
            for (TrainModeCourse course : this) {
                totalPoint += course.point;
            }
        }
        return totalPoint;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public float getMinimumPoint() {
        return minimumPoint;
    }

    public void setMinimumPoint(float minimumPoint) {
        this.minimumPoint = minimumPoint;
    }

    public float getPassedPoint() {
        if (passedPoint == -1f) {
            passedPoint = 0f;
            if (isPublic) {
                for (TrainModeCourse c : this.passedPublic) {
                    passedPoint += c.point;
                }
            } else {
                for (TrainModeCourse c : this) {
                    if (c.isPassed)
                        passedPoint += c.point;
                }
            }
        }
        return passedPoint;
    }

    public int size() {
        return contentCourse.size();
    }

    public boolean isEmpty() {
        return contentCourse.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<TrainModeCourse> iterator() {
        return this.contentCourse.iterator();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void forEach(@NonNull Consumer<? super TrainModeCourse> action) {
        this.contentCourse.forEach(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Spliterator<TrainModeCourse> spliterator() {
        return this.contentCourse.spliterator();
    }
}
