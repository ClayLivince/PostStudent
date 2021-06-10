package xyz.cyanclay.poststudent.entity.iclass;


import android.text.SpannableStringBuilder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import xyz.cyanclay.poststudent.network.iclass.IClassManager;

/**
 * Time: 2021/4/16
 * An entity class of iclass activity, stores some information while processing.
 * Only getter/setter things in this class.
 */
public class IClassActivity {

    // Be cautious that all time that iclass returned was all GMT time.
    private static final SimpleDateFormat dueTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);

    static {
        dueTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * This filter only contains filters extracted from {@link IClassManager#parseActivity()}
     * This Map stores the key and value. if there is no attribute named as the key,
     * It will still presence in this map, with value NULL.
     */
    public Map<String, String> filter = new HashMap<>();

    private IClassManager manager;
    private String courseID;
    private String activityID;
    private String courseContentID;
    private String providerID;

    private boolean seen = false;

    private long timeStamp;
    private String url;

    private SpannableStringBuilder title;
    private String rawContent;
    private SpannableStringBuilder parsedContent;

    private String announcementFirstName;
    private String announcementLastName;

    private String extraDataType;
    private String extraDataFileURL;

    private String dueTime;
    private Date dueDate;

    public SpannableStringBuilder getValidContent() {
        if (null != parsedContent) {
            return parsedContent;
        } else if (null != rawContent)
            return new SpannableStringBuilder(rawContent);
        else if (null != title)
            return new SpannableStringBuilder(title);
        else return new SpannableStringBuilder("");
    }

    public SpannableStringBuilder getValidFrom() {
        if (null != announcementFirstName) {
            return new SpannableStringBuilder(announcementFirstName + ((announcementLastName != null ? announcementLastName : "")));
        }
        String cname = manager.getCourseName(getCourseID());
        if (!cname.isEmpty()) {
            return new SpannableStringBuilder(cname);
        } else if (null != title)
            return title;
        else return new SpannableStringBuilder();
    }

    public String getUrl() {
        return url;
    }

    public String getCourseID() {
        return courseID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public String getActivityID() {
        return activityID;
    }

    public String getAnnouncementFirstName() {
        return announcementFirstName;
    }

    public String getAnnouncementLastName() {
        return announcementLastName;
    }

    public String getCourseContentID() {
        return courseContentID;
    }

    public String getExtraDataFileURL() {
        return extraDataFileURL;
    }

    public String getExtraDataType() {
        return extraDataType;
    }

    public String getProviderID() {
        return providerID;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getDueTime() {
        return dueTime;
    }

    public SpannableStringBuilder getTitle() {
        return title;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public void setAnnouncementFirstName(String announcementFirstName) {
        this.announcementFirstName = announcementFirstName;
    }

    public void setAnnouncementLastName(String announcementLastName) {
        this.announcementLastName = announcementLastName;
    }

    public void setCourseContentID(String courseContentID) {
        this.courseContentID = courseContentID;
    }

    public void setExtraDataFileURL(String extraDataFileURL) {
        this.extraDataFileURL = extraDataFileURL;
    }

    public void setExtraDataType(String extraDataType) {
        this.extraDataType = extraDataType;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setTitle(SpannableStringBuilder title) {
        this.title = title;
    }

    public void setDueTime(String dueTime) {
        if (dueTime == null)
            return;

        if (dueTime.length() == 0)
            return;

        this.dueTime = dueTime;

        try {
            dueDate = dueTimeFormat.parse(dueTime);
        } catch (ParseException e) {
            Log.e("iClassActivity", "Unrecognizable due time: " + dueTime);
        }
    }

    public void setParsedContent(SpannableStringBuilder parsedContent) {
        this.parsedContent = parsedContent;
    }
}
