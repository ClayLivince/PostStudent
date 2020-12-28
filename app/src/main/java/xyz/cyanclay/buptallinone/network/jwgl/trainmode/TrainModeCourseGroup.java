package xyz.cyanclay.buptallinone.network.jwgl.trainmode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TrainModeCourseGroup {

    public String groupName;
    public Map<String, TrainModeCourseSubGroup> groupMap = new HashMap<>();
    float totalPoint = 0f;
    float passedPoint = 0f;
    //boolean hasPublic = false;
    float minimumPoint = -1f;

    public TrainModeCourseSubGroup getSubGroup(String id) {
        return groupMap.get(id);
    }

    public Collection<TrainModeCourseSubGroup> getSubGroups() {
        return groupMap.values();
    }

    public List<TrainModeCourse> asList() {
        LinkedList<TrainModeCourse> list = new LinkedList<>();
        for (TrainModeCourseSubGroup subGroup : groupMap.values()) {
            if (subGroup.isPublic) {
                list.addAll(subGroup.passedPublic);
                list.add(new TrainModeCourse.PublicCoursePlaceHolder(subGroup));
            } else list.addAll(subGroup.contentCourse);
        }
        return list;
    }

    public int getItemCount() {
        int size = 0;
        for (TrainModeCourseSubGroup subGroup : groupMap.values()) {
            if (subGroup.isPublic) {
                size += 1;
                size += subGroup.getPassedPublicSize();
            } else size += subGroup.size();
        }
        return size;
    }

    public void putSubGroup(TrainModeCourseSubGroup group) {
        this.groupMap.put(group.groupID, group);
    }

    public float getTotalPoint() {
        if (totalPoint == 0f) {
            for (TrainModeCourseSubGroup subGroup : groupMap.values()) {
                totalPoint += subGroup.getTotalPoint();
            }
        }
        return totalPoint;
    }

    public boolean isEmpty() {
        return this.groupMap.isEmpty();
    }

    public String getGroupName() {
        return groupName;
    }

    public float getMinimumPoint() {
        return minimumPoint;
    }

    public float getPassedPoint() {
        if (passedPoint == 0f) {
            for (TrainModeCourseSubGroup g : groupMap.values()) {
                passedPoint += g.getPassedPoint();
            }
        }
        return passedPoint;
    }
}
