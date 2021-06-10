package xyz.cyanclay.poststudent.network.iclass;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.collection.ArrayMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xyz.cyanclay.poststudent.entity.iclass.IClassActivities;
import xyz.cyanclay.poststudent.entity.iclass.IClassActivity;
import xyz.cyanclay.poststudent.entity.iclass.IClassActivityFilter;
import xyz.cyanclay.poststudent.entity.iclass.IClassCourse;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.network.login.LoginStatus;

/**
 * IClassManager refers to iclass.bupt.edu.cn, which is a Blackboard based system.
 * This class handles things related to this site.
 */
public class IClassManager extends SiteManager {

    private static final String mainURL = "https://iclass.bupt.edu.cn";

    private static final String loginPart = "https://iclass.bupt.edu.cn/webapps/bb-cas_bjyddx-BBLEARN/caslogin.jsp";

    private static final String tabPart = "/webapps/portal/execute/tabs/tabAction";

    private static final String activityPart = "/webapps/streamViewer/streamViewer";

    private static final Map<String, String> courseListData = new ArrayMap<String, String>() {{
        put("action", "refreshAjaxModule");
        put("modId", "_4_1");
        put("tabId", "_1_1");
        put("tab_tab_group_id", "_1_1");
    }};

    private static final Map<String, String> activityListData = new ArrayMap<String, String>() {{
        //cmd=loadStream&streamName=alerts&providers=%7B%7D&forOverview=false
        put("cmd", "loadStream");
        put("streamName", "alerts");
        put("providers", "{}");
        put("forOverview", "false");
    }};

    private Document cachedDocument;
    private List<IClassCourse> cachedCourse = new LinkedList<>();

    public IClassManager(NetworkManager nm, Context context) {
        super(nm, context);
    }

    private void parseCourseContent(String url) throws Exception {
        Connection.Response response = get(url);


    }

    public String getCourseName(String courseID) {
        for (IClassCourse c : cachedCourse) {
            if (c.getCourseID().equals(courseID))
                return c.getCourseName();
        }
        return "";
    }

    /**
     * Parses available course list
     *
     * @return a list of iClassCourse
     * @throws Exception Networking or array bound Exception
     */
    public List<IClassCourse> parseCourseList() throws Exception {

        Connection.Response listRes = post(mainURL + tabPart, courseListData);

        // returned data was xml, so first strip it to html
        String contentStr = listRes.body().split("<contents><!\\[CDATA\\[")[1];
        contentStr = contentStr.substring(0, contentStr.length() - 14);

        Document listDom = Jsoup.parse(contentStr);

        Elements lis = listDom.getElementsByTag("li");

        LinkedList<IClassCourse> listCourse = new LinkedList<>();

        for (Element row : lis) {
            IClassCourse course = new IClassCourse();

            for (Element grid : row.children()) {
                if (grid.tag().toString().equals("a")) {
                    course.setUrl(grid.attr("href").trim());
                    String[] compound = grid.ownText().split(":");

                    try {
                        course.setCourseID(compound[0].trim());
                        course.setCourseName(compound[1].trim());
                    } catch (ArrayIndexOutOfBoundsException outBound) {
                        Log.e("iClassManager", "Unrecognizable course:" + grid.ownText());
                    }

                } else if (grid.classNames().contains("courseInformation")) {
                    try {
                        course.setTeacherName(grid.getElementsByClass("name")
                                .first().ownText().trim());
                    } catch (Exception ignore) {

                    }
                }
            }

            if (course.getUrl() != null) {
                listCourse.add(course);
            }
        }

        cachedCourse = listCourse;
        return listCourse;

    }

    /**
     * Parses how many unchecked activity are there.
     *
     * @return unchecked activity count, integer
     * @throws Exception networking and parsing exceptions
     */
    public int parseActivityCount() throws Exception {

        validateCache();

        Element badgeTotalCount = cachedDocument.getElementById("badgeTotalCount");
        String countStr = badgeTotalCount.ownText();
        int count = 0;
        if (countStr.length() != 0) {
            count = Integer.parseInt(countStr);
        }

        return count;
    }

    /**
     * Extract filter from json. This method is an extracted part from
     * {@link #parseActivity()} for pretty reading.
     *
     * @param extrasObj the sv_extras json object. {@link JsonObject}.
     * @param filterMap the container map.
     */
    private void extractFilter(JsonObject extrasObj, Map<String, IClassActivityFilter> filterMap) {
        JsonArray filterEntries = extrasObj.get("sv_filters").getAsJsonArray();
        for (JsonElement entry : filterEntries) {
            try {
                JsonObject entryObj = entry.getAsJsonObject();
                IClassActivityFilter filter = new IClassActivityFilter();

                filter.setAttribute(entryObj.get("attribute").getAsString());
                filter.setExclusive(entryObj.get("isExclusive").getAsBoolean());
                filter.setName(entryObj.get("name").getAsString());

                JsonObject choiceObj = entryObj.get("choices").getAsJsonObject();

                for (Map.Entry<String, JsonElement> e : choiceObj.getAsJsonObject().entrySet()) {
                    filter.choices.put(e.getKey(), e.getValue().getAsString());
                }

                filterMap.put(filter.getAttribute(), filter);
            } catch (IllegalStateException illegalState) {
                Log.e("iClassManager", "Unrecognizable Filters:\n" +
                        entry.toString());
            }
        }
    }

    /**
     * Extract activity entry from json. This method is an extracted part from
     * {@link #parseActivity()} for pretty reading.
     * <p>
     * This method runs at low efficiency, it may requires more work.
     *
     * @param entryObj  the activity entry json object. {@link JsonObject}.
     * @param filterMap the filter map, contains all possible filters.
     * @return The extracted activity entity. {@link IClassActivity}.
     */
    private IClassActivity extractActivity(JsonObject entryObj, Map<String, IClassActivityFilter> filterMap) {
        IClassActivity activity = new IClassActivity();

        // Basically for strip message we do not need to traverse each JsonEntry
        // but we need to strip filters and save it , so let us traverse.
        for (Map.Entry<String, JsonElement> entry : entryObj.entrySet()) {
            if (entry.getValue().isJsonNull())
                continue;
            HtmlSpanner spanner = new HtmlSpanner();
            switch (entry.getKey()) {
                case "extraAttribs": {
                    JsonObject extraObj = entry.getValue().getAsJsonObject();
                    for (Map.Entry<String, JsonElement> extraEntry : extraObj.entrySet()) {
                        if (filterMap.containsKey(extraEntry.getKey()))
                            activity.filter.put(extraEntry.getKey(), extraEntry.getValue().getAsString());
                    }
                    break;
                }
                case "providerId": {
                    activity.setProviderID(entry.getValue().getAsString());
                    break;
                }
                case "se_timestamp": {
                    activity.setTimeStamp(entry.getValue().getAsLong());
                    break;
                }
                case "se_id": {
                    activity.setActivityID(entry.getValue().getAsString());
                    break;
                }
                case "se_details": {
                    if (!entry.getValue().isJsonNull() && activity.getRawContent() == null) {
                        activity.setRawContent(entry.getValue().getAsString());
                        activity.setParsedContent(new SpannableStringBuilder().append(spanner.fromHtml(entry.getValue().getAsString())));
                    }
                    break;
                }
                case "se_context": {
                    if (activity.getTitle() == null) {
                        activity.setTitle(new SpannableStringBuilder().append(spanner.fromHtml(entry.getValue().getAsString())));
                    } else if (activity.getRawContent() == null) {
                        activity.setRawContent(entry.getValue().getAsString());
                        activity.setParsedContent(new SpannableStringBuilder().append(spanner.fromHtml(entry.getValue().getAsString())));
                    }
                }
                case "se_courseId": {
                    activity.setCourseID(entry.getValue().getAsString());
                    break;
                }
                case "se_itemUri": {
                    activity.setUrl(entry.getValue().getAsString());
                    break;
                }
                case "itemSpecificData": {
                    // use try here to prevent complete cut off of this entity parsing process.
                    try {
                        JsonObject dataObj = entry.getValue().getAsJsonObject();

                        if (!dataObj.get("title").isJsonNull())
                            activity.setTitle(new SpannableStringBuilder().append(spanner.fromHtml(dataObj.get("title").getAsString())));
                        if (!dataObj.get("courseContentId").isJsonNull())
                            activity.setCourseContentID(dataObj.get("courseContentId").getAsString());

                        JsonObject detailObj = dataObj.get("notificationDetails").getAsJsonObject();
                        activity.setSeen(detailObj.get("seen").getAsBoolean());
                        if (!detailObj.get("courseId").isJsonNull())
                            activity.setCourseID(detailObj.get("courseId").getAsString());
                        if (!detailObj.get("announcementFirstName").isJsonNull())
                            activity.setAnnouncementFirstName(detailObj.get("announcementFirstName").getAsString());
                        if (!detailObj.get("announcementLastName").isJsonNull())
                            activity.setAnnouncementLastName(detailObj.get("announcementLastName").getAsString());
                        if (!detailObj.get("dueDate").isJsonNull())
                            activity.setDueTime(detailObj.get("dueDate").getAsString());
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e("iClassManager", "Malformed Activity item specific data:" +
                                entry.getValue().toString());
                    } catch (UnsupportedOperationException euo) {
                        euo.printStackTrace();
                    }
                    break;
                }
                case "contentDetails": {
                    if (!entry.getValue().isJsonObject())
                        break;

                    JsonObject fileObj = entry.getValue().getAsJsonObject();
                    activity.setExtraDataType(fileObj.get("contentSpecificExtraData").getAsString());
                    activity.setExtraDataFileURL(fileObj.get("contentSpecificFileData").getAsString());
                }
            }

            // If it is a filter key then let us save it in it's filter map.
            if (filterMap.containsKey(entry.getKey())) {
                activity.filter.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        return activity;
    }

    /**
     * Parses the activity list.
     * There is an example returned json in this folder, named as ExampleActivityList.json
     *
     * @return a list of activity entity with a modified class extended with Filters.
     * @throws Exception network and json exceptions.
     * @see IClassActivities
     * @see IClassActivityFilter
     */
    public IClassActivities parseActivity() throws Exception {

        Connection.Response activityRes = post(mainURL + activityPart, activityListData);

        //returned data was json, need to be formatted and tokenized
        JsonObject jsonObject = JsonParser.parseString(activityRes.body()).getAsJsonObject();

        // First let us strip filters
        // This was all available filters
        // key: attributeName value: filter
        Map<String, IClassActivityFilter> filterMap = new HashMap<>();

        // Be cautious here for any uncompleted json
        if (jsonObject.has("sv_extras")) {
            JsonObject extrasObj = jsonObject.get("sv_extras").getAsJsonObject();
            if (extrasObj.has("sv_filters")) {
                extractFilter(extrasObj, filterMap);
            }
        }

        // Parse Activity Entry
        IClassActivities activities = new IClassActivities();
        activities.filters = filterMap;

        if (jsonObject.has("sv_streamEntries")) {
            JsonArray entryArray = jsonObject.get("sv_streamEntries").getAsJsonArray();

            for (JsonElement entryElement : entryArray) {
                JsonObject entryObj = entryElement.getAsJsonObject();

                activities.add(extractActivity(entryObj, filterMap));
            }
        }

        return activities;
    }

    private void validateCache() throws Exception {
        if (cachedDocument == null) {
            LoginStatus result = doLogin();
            Log.e("iclass", result.toString());
        }
    }

    @Override
    protected LoginStatus doLogin() throws Exception {
        Connection.Response infoInit = nm.get(Jsoup.connect(mainURL)
                .followRedirects(true), cookies);
        cookies.putAll(infoInit.cookies());

        Document infoinitDoc = infoInit.parse();
        Log.e("IclassManager", infoinitDoc.title());
        if (infoinitDoc.title().contains("欢迎")) {
            setLoggedIn(true);
            cachedDocument = infoinitDoc;
            return LoginStatus.LOGIN_SUCCESS;
        }
        String loginRedirect = loginPart;
        LoginStatus status = nm.authManager.checkLogin(loginRedirect);
        if (status == LoginStatus.LOGIN_SUCCESS) {
            Connection.Response init = nm.authManager.getAuth(loginRedirect);
            Document initDoc = init.parse();
            if (initDoc.title().contains("Insert title here")) {

                Element form = initDoc.body().getElementsByTag("form").first();
                String targetURL = form.attr("action");
                Map<String, String> datas = new HashMap<>();
                for (Element e : form.children()) {
                    if ("input".equals(e.tagName())) {
                        datas.put(e.attr("name"), e.attr("value"));
                    }
                }

                Document loginDoc = plainPost(targetURL, datas).parse();

                setLoggedIn(true);
                cachedDocument = loginDoc;
                Log.e("IclassManager", loginDoc.title());
                return LoginStatus.LOGIN_SUCCESS;
            }

        }

        return status;
    }

    /*
    @Override
    protected LoginStatus doLogin() throws Exception {
        if (user == null || pass == null){
            this.setDetails(nm.user, nm.vpnManager.getPass());
        }

        Connection.Response initRes = nm.get(mainURL);
        cookies.putAll(initRes.cookies());

        final String[] encoded = encodePassword();

        final Map<String, String> details = new HashMap<String, String>(){{
            put("user_id", user);
            put("password", pass);
            put("action", "login");
            put("remote-user", "");
            put("new_loc", "");
            put("auth_type", "");
            put("one_time_token", "");
            put("encoded_pw", encoded[0]);
            put("encoded_pw_unicode", encoded[1]);
        }};

        Connection.Response loginRes = nm.post(Jsoup.connect(mainURL + loginPart)
                .data(details)
                .cookies(cookies)
        ).bufferUp();

        cookies.putAll(loginRes.cookies());
        Document loginDom = loginRes.parse();
        if (loginDom.title().contains("欢迎")){
            setLoggedIn(true);
            return LoginStatus.LOGIN_SUCCESS;

            //For some strange reason, this site do not return any useful information
            //on incorrect password or something, so overall return INCORRECT_DETAIL for convenience.
        } else return LoginStatus.INCORRECT_DETAIL;

    }

     */


    /**
     * iClass do not has captcha, so do not use this method.
     *
     * @param captcha dumb captcha
     * @return doLogin();
     * @throws Exception Any exception happened in networking or parsing
     */
    @Override
    protected LoginStatus doCaptchaLogin(String captcha) throws Exception {
        return doLogin();
    }

    private Connection.Response get(String url) throws Exception {
        if (checkLogin()) {
            Connection.Response response = nm.get(url, cookies);

            cookies.putAll(response.cookies());
            return response;
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
    }

    private Connection.Response post(String url, Map<String, String> data) throws Exception {
        if (checkLogin()) {
            Connection.Response response = nm.post(Jsoup.connect(url)
                    .cookies(cookies)
                    .data(data)
                    .ignoreContentType(true)
            );

            cookies.putAll(response.cookies());
            return response;
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
    }

    private Connection.Response plainPost(String url, Map<String, String> data) throws Exception {
        Connection.Response response = nm.post(Jsoup.connect(url)
                .cookies(cookies)
                .data(data)
                .ignoreContentType(true)
        );

        cookies.putAll(response.cookies());
        return response;
    }

    private String[] encodePassword() {
        String[] encoded = new String[2];


        return encoded;
    }
}
