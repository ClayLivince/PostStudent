package xyz.cyanclay.poststudent.entity.iclass;

import java.util.LinkedList;
import java.util.Map;

public class IClassActivities extends LinkedList<IClassActivity> {

    public Map<String, IClassActivityFilter> filters;

    /**
     * Get a filtered activities list with filter settings.
     * returns a Empty List if the attribute key was not found in the filter map
     * or an invalid position was returned.
     * <p>
     * This method was low at efficiency, so may require more work here.
     *
     * @param selected a map of selected filters, key was attribute, value was an array
     *                 of selected values.
     * @return filtered activities
     */
    public IClassActivities filter(Map<String, String[]> selected) {
        IClassActivities activities = new IClassActivities();

        // Let us start from each activity, it may reduces work.
        // As activity list may be much longer than filter list.
        for (IClassActivity activity : this) {
            boolean added = false;
            // An single activity only contains a little available filters, so it may be faster.
            for (String key : activity.filter.keySet()) {

                if (added)
                    break;

                if (selected.containsKey(key)) {
                    for (String value : selected.get(key)) {

                        if (activity.filter.get(key).equals(value)) {
                            activities.add(activity);
                            added = true;
                            break;
                        }
                    }
                }
            }
        }
        activities.filters = this.filters;
        return activities;
    }
}
