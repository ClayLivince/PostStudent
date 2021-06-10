package xyz.cyanclay.poststudent.network.jwgl;

import android.content.Context;
import android.util.Log;

import com.eclipsesource.v8.V8;
import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.jwgl.Course;
import xyz.cyanclay.poststudent.entity.jwgl.Courses;
import xyz.cyanclay.poststudent.entity.jwgl.Score;
import xyz.cyanclay.poststudent.entity.jwgl.Scores;
import xyz.cyanclay.poststudent.entity.jwgl.trainmode.TrainModeCourse;
import xyz.cyanclay.poststudent.entity.jwgl.trainmode.TrainModeCourseGroup;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.network.login.LoginStatus;

import static org.slf4j.LoggerFactory.getLogger;

public class JwglManager extends SiteManager {

    private static Logger logger = getLogger(JwglManager.class);

    public static final String jwglURL = "https://jwgl.bupt.edu.cn/jsxsd/";

    // Part of url of logging into the jwgl
    private static final String loginPart = "xk/LoginToXk";

    private static final String framePart = "framework/xsMain.jsp";
    private static final String mainPart = "framework/xsMain_new.jsp?t1=1";
    private static final String coursePart = "xskb/xskb_list.do";
    private static final String scorePart = "kscj/cjcx_list";
    private static final String trainModePart = "pyfa/topyfamx";


    private final JwglData data;
    //private String token;
    private String encoded = "";
    private Document cachedDocument;

    public JwglManager(NetworkManager nm, Context context) {
        super(nm, context);
        data = new JwglData(this);
    }

    public int getWeek() throws Exception {
        if (data.currentWeek == 0) getTime();
        return data.currentWeek;
    }

    public List<TrainModeCourseGroup> getTrainMode(boolean force) throws Exception {

        // Read cached trainmode from local cache
        if (!force) {
            try {
                List<TrainModeCourseGroup> list = data.loadTrainMode();
                if (!list.isEmpty())
                    return list;
            } catch (IOException ignored) {

            }
        }

        // Refresh trainmode from server
        Connection.Response trainModeres = get(jwglURL + trainModePart).bufferUp();

        Document trainModeDoc = trainModeres.parse();

        Element courseTable = trainModeDoc.getElementById("mxh").child(0);
        Elements courseEntries = courseTable.children();

        // First four numbers indicates when user in university
        int userYear = Integer.parseInt(user.substring(0, 4));
        int currentTerm = (Integer.parseInt(data.getCurrentTerm().substring(0, 4)) - userYear) * 2 +
                data.getCurrentTerm().charAt(data.getCurrentTerm().length() - 1) - '0';

        // Trace current term for debug
        // Log.e("CurrentTerm", data.getCurrentTerm() + " " + currentTerm);
        Courses courses = data.getCourses(false);

        // Initiate
        LinkedList<TrainModeCourseGroup> list = new LinkedList<>();
        TrainModeCourseGroup group = new TrainModeCourseGroup(false);

        /*
        This has already flooded in history

            String reg = "(.*?) 课组号(?:.*?)(\\d.*?)方案计划号(?:.*?)(\\d.*)";
            Pattern pattern = Pattern.compile(reg);
        */

        for (int i = 2; i < courseEntries.size(); i++) {

            Element courseEntry = courseEntries.get(i);
            boolean head = false;

            try {
                // Jump error or empty tags

                if (courseEntry.children().isEmpty())
                    continue;

                if (courseEntry.child(0).hasAttr("colspan"))
                    continue;

                // Have this attribute means a new group starts

                if (courseEntry.child(0).hasAttr("rowspan")) {
                    if (!group.isEmpty()) {
                        list.add(group);
                    }
                    group = new TrainModeCourseGroup(false);

                    // if this is the first column, the column number should
                    // be +1 due to column header

                    head = true;

                    String title = courseEntry.child(0).ownText();
                    group.setGroupName(title.split("应修")[0].trim());
                }

                TrainModeCourse course = new TrainModeCourse();

                course.courseID = courseEntry.child(head ? 1 : 0).ownText().trim();
                course.courseName = courseEntry.child(head ? 2 : 1).ownText().trim();

                String status = courseEntry.child(head ? 3 : 2).ownText().trim();
                if (status.contains("不及格"))
                    course.isFailed = true;
                else if (status.contains("已修"))
                    course.isPassed = true;

                course.courseCategory = courseEntry.child(head ? 4 : 3).ownText().trim();
                course.courseType = courseEntry.child(head ? 5 : 4).ownText().trim();
                course.point = Float.parseFloat(courseEntry.child(head ? 6 : 5).ownText().trim());
                course.courseHour = Integer.parseInt(courseEntry.child(head ? 7 : 6).ownText().trim());
                course.practiceHour = Integer.parseInt(courseEntry.child(head ? 8 : 7).ownText().trim());
                course.lectureHour = Integer.parseInt(courseEntry.child(head ? 9 : 8).ownText().trim());
                course.experimentHour = Integer.parseInt(courseEntry.child(head ? 10 : 9).ownText().trim());
                course.otherHour = Integer.parseInt(courseEntry.child(head ? 11 : 10).ownText().trim());
                course.totalHour = Integer.parseInt(courseEntry.child(head ? 12 : 11).ownText().trim());
                course.term = Integer.parseInt(courseEntry.child(head ? 13 : 12).ownText().trim());

                for (Course c : courses) {
                    if (c.courseName.split(" ")[0].equals(course.courseName)) {
                        course.isTeaching = true;
                        break;
                    }
                }

                if (course.term == currentTerm)
                    course.isSelectable = true;
                else if (course.term < currentTerm) {
                    course.isBanned = true;
                }

                group.add(course);

                // Public group has become history

                /*
                String groupEntry = courseEntry.child(head ? 1 : 0).ownText();
                Matcher matcher = pattern.matcher(groupEntry);

                if (matcher.find()) {
                    try {
                        String groupName = Objects.requireNonNull(matcher.group(1)).trim();
                        String groupID = Objects.requireNonNull(matcher.group(2)).trim();

                        boolean isPublic = groupID.contains("ggkz");
                        if (isPublic) {
                            if (course.term % 2 == currentTerm % 2)
                                course.isSelectable = true;

                        } else {
                            if (course.term == currentTerm)
                                course.isSelectable = true;
                            else if (course.term < currentTerm) {
                                course.isBanned = true;
                            }
                        }

                        TrainModeCourseSubGroup subGroup = group.getSubGroup(groupID);
                        if (subGroup != null) {
                            //course.subGroup = subGroup;
                            subGroup.add(course);
                        } else {
                            subGroup = new TrainModeCourseSubGroup(groupName, groupID, isPublic);
                            subGroup.add(course);
                            group.putSubGroup(subGroup);
                        }

                    } catch (NullPointerException | IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }

                 */

            } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
                e.printStackTrace();
            }

        }

        data.saveTrainMode(list);

        return list;
    }

    public Map<String, String> getTerms() throws Exception {
        //get(jwglURL + "kscj/cjcx_frm");
        Document scoreTabDom = get(jwglURL + "kscj/cjcx_query").parse();

        // kksj may refers to "开课时间"
        Element termsSelect = scoreTabDom.getElementById("kksj");
        Map<String, String> terms = new HashMap<>();

        int userYear = Integer.parseInt(user.substring(0, 4));
        int thisYear = data.year;
        int month = data.today.get(Calendar.MONTH) + 1;
        char subTerm = ((month >= 3) && (month < 9)) ? '2' : '1';
        for (Element option : termsSelect.children()) {
            try {
                String termID = option.attr("value");
                char termNO = termID.charAt(termID.length() - 1);
                int termYear = Integer.parseInt(termID.substring(0, 4));

                if (termYear < userYear | termYear > thisYear) {
                    continue;
                }

                switch (termNO) {
                    case '1': {
                        if (termYear == thisYear && subTerm == '1')
                            data.currentTerm = termID;
                        break;
                    }
                    case '2': {
                        if ((termYear + 1) == thisYear && subTerm == '2')
                            data.currentTerm = termID;
                        break;
                    }

                    case '3': {
                        continue;
                    }
                }

                terms.put(option.ownText(), termID);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        terms.put("全部", "");

        Log.i("termMap", terms.toString());

        return terms;
    }

    public Scores getScore(String termName) throws Exception {
        if (termName == null)
            termName = "全部";

        String termID = data.getTerms().get(termName);
        if (termID != null && !termID.isEmpty()) {
            char termNO = termID.charAt(termID.length() - 1);
            if (termNO == '2') {
                Scores list = getScore(termID, 0);
                list.addAll(getScore(termID.substring(0, termID.length() - 1) + "3", 0));
                return list;
            }
        }

        if (termID == null) termID = "";
        return getScore(termID, 0);
    }

    private Scores getScore(final String termID, int dummy) throws Exception {

        Map<String, String> data = new HashMap<String, String>() {{
            put("kksj", termID);
            put("kczx", "");
            put("kcmc", "");
            put("xsfs", "all");
        }};

        Document scoreDom = post(Jsoup.connect(jwglURL + scorePart)
                .data(data)
                .referrer("https://jwgl.bupt.edu.cn/jsxsd/kscj/cjcx_query")).parse();

        Scores scores = new Scores();
        Element dataList = scoreDom.getElementById("dataList");
        Element dataListContainer = dataList.parent();
        Elements scoreTable = dataList.child(0).children();

        parseScores(scores, dataListContainer.ownText().trim());

        scoreTable.remove(0);
        for (Element scoreItem : scoreTable) {
            try {
                Score score = new Score();
                score.termID = scoreItem.child(1).ownText().trim();
                score.courseID = scoreItem.child(2).ownText().trim();
                score.courseName = scoreItem.child(3).ownText().trim();
                score.groupName = scoreItem.child(4).ownText().trim();
                score.score = scoreItem.child(5).ownText().trim();
                score.scoreMark = scoreItem.child(6).ownText().trim();
                score.point = Float.parseFloat(scoreItem.child(7).ownText().trim());
                score.pointHour = Integer.parseInt(scoreItem.child(8).ownText().trim());
                score.gpaString = scoreItem.child(9).ownText().trim();
                score.reTermID = scoreItem.child(10).ownText().trim();
                score.examType = scoreItem.child(11).ownText().trim();
                score.courseType = scoreItem.child(12).ownText().trim();
                score.courseCategory = scoreItem.child(13).ownText().trim();
                score.commonType = scoreItem.child(14).ownText().trim();

                try {
                    score.gpa = Double.parseDouble(score.gpaString);
                } catch (NumberFormatException ignored) {

                }

                scores.add(score);
            } catch (Exception e) {
                if (!(e instanceof IndexOutOfBoundsException))
                    e.printStackTrace();
            }
        }

        return scores;
    }

    private void parseScores(Scores scores, String text) {
        String reg = "所修门数\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "所修总学分\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "平均学分绩点\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "平均成绩\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "绩点\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "加权平均分\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9])).*?" +
                "排名\\s*?:\\s*?((?:[1-9]\\d*\\.?\\d*)|(?:0\\.\\d*[1-9]))";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            //Log.e("COUNT::" , matcher.group());
            try {
                scores.setPoints(Float.parseFloat(Objects.requireNonNull(matcher.group(2))));
                scores.setAvgGpa(Float.parseFloat(Objects.requireNonNull(matcher.group(3))));
                scores.setAvg(Float.parseFloat(Objects.requireNonNull(matcher.group(4))));
                scores.setWeightedGpa(Float.parseFloat(Objects.requireNonNull(matcher.group(5))));
                scores.setWeightedAvg(Float.parseFloat(Objects.requireNonNull(matcher.group(6))));
                scores.setRank(Integer.parseInt(Objects.requireNonNull(matcher.group(7))));
            } catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void initWeekday() {
        Calendar calendar = Calendar.getInstance();
        data.today = calendar;
        data.weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        data.year = calendar.get(Calendar.YEAR);
    }

    public Courses getClassToday(boolean force) throws Exception {
        if (data.weekday == -1) initWeekday();

        Courses courses = new Courses();

        for (Course c : getClassWeek(-1, false)) {
            if (data.weekday == c.day)
                courses.add(c);
        }

        return courses;
    }

    public Courses getClassWeek(int week, boolean forceRefresh) throws Exception {

        if (week == -1) {
            week = getWeek();
        }

        Courses courses = new Courses();

        for (Course c : data.getCourses(forceRefresh)) {
            try {
                String[] weeks = c.weekHas.split(",");
                if (weeks.length >= 2) {
                    for (String s : weeks) {
                        if (s.contains("-")) {
                            String[] weekPeriod = s.split("-");
                            if (week >= Integer.parseInt(weekPeriod[0]) && week <= Integer.parseInt(weekPeriod[1])) {
                                courses.add(c);
                                break;
                            }
                        } else {
                            if (week == Integer.parseInt(s)) {
                                courses.add(c);
                                break;
                            }
                        }
                    }
                } else if (c.weekHas.contains("-")) {
                    String[] weekPeriod = c.weekHas.split("-");
                    if (week >= Integer.parseInt(weekPeriod[0]) && week <= Integer.parseInt(weekPeriod[1])) {
                        courses.add(c);
                    }
                } else {
                    if (week == Integer.parseInt(c.weekHas)) {
                        courses.add(c);
                    }
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        return courses;
    }

    Courses getCourses() throws Exception {
        Courses courses = new Courses();
        Connection.Response courseRes = get(jwglURL + coursePart);
        Document courseDom = courseRes.parse();

        Element courseTable = courseDom.getElementById("kbtable");
        Elements grids = courseTable.getElementsByClass("kbcontent");
        HashMap<Element, Integer> extraGrids = new HashMap<>();

        //mark for weekday
        int i = 1;
        Gson gson = new Gson();

        for (Element e : grids) {

            //Element that has this class was empty
            if (e.hasClass("sykb2"))
                continue;

            if (e.ownText().length() == 0) {
                i++;
                if (i > 7) i = 1;
                continue;
            }

            // 当同一格中有多节课时的处理：分为多个小element，插入grids中

            if (e.outerHtml().contains("---------------------")) {
                String[] outerHtml = e.outerHtml().split("---------------------");
                outerHtml[0] = outerHtml[0].concat("</div>");
                extraGrids.put(Jsoup.parse(outerHtml[0]).body().child(0), i);

                for (int j = 1; j < outerHtml.length - 1; j++) {
                    outerHtml[j] = outerHtml[j].replaceFirst("<br>", "");
                    outerHtml[j] = "<div>" + outerHtml[j];
                    outerHtml[j] = outerHtml[j].concat("</div>");
                    extraGrids.put(Jsoup.parse(outerHtml[j]).body().child(0), i);
                }

                outerHtml[outerHtml.length - 1] = outerHtml[outerHtml.length - 1].replaceFirst("<br>", "");
                outerHtml[outerHtml.length - 1] = "<div>" + outerHtml[outerHtml.length - 1];
                extraGrids.put(Jsoup.parse(outerHtml[outerHtml.length - 1]).body().child(0), i);

                i++;
                if (i > 7) i = 1;
                continue;
            }

            try {
                addCourse(e, i, courses, gson);
            } catch (Exception e1) {
                e1.printStackTrace();
                Log.e("HTML:::", e.outerHtml());
            } finally {
                i++;
                if (i > 7) i = 1;
            }
        }

        for (Element extra : extraGrids.keySet()) {
            try {
                addCourse(extra, extraGrids.get(extra), courses, gson);
            } catch (Exception e1) {
                e1.printStackTrace();
                Log.e("HTML:::", extra.outerHtml());
            } finally {
                i++;
                if (i > 7) i = 1;
            }
        }

        return courses;
    }

    private void addCourse(Element e, int day, Courses courses, Gson gson) {
        Course course = new Course();
        course.courseName = e.ownText().trim();
        course.classRoom = e.getElementsByAttributeValue("title", "教室").first().ownText().trim();
        course.teacherName = e.getElementsByAttributeValue("title", "老师").first().ownText()
                .replace("--", "")
                .replace("副教授", "")
                .replace("讲师", "")
                .replace("（高校）", "")
                .replace("教授", "")
                .trim();
        course.day = day;

        String weeks = e.getElementsByAttributeValue("title", "周次(节次)").first().ownText().trim();
        String[] metas = weeks.split("\\(周\\)");
        course.weekHas = metas[0];
        String[] sections = metas[1].substring(1, metas[1].length() - 1).split("-");
        course.setStartSection(Integer.parseInt(sections[0].replace("节", "")));
        course.setEndSection(Integer.parseInt(sections[sections.length - 1].replace("节", "")));

        courses.add(course);
    }

    private void getDept() {

        Elements entries = cachedDocument.getElementsByClass("middletopdwxxcont");
        try {
            if (entries.get(2).text().contains(user)) {
                data.school = entries.get(3).ownText().trim();
                data.profession = entries.get(4).ownText().trim();
                data.classID = entries.get(5).ownText().trim();
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            System.out.println("Jwgl Parse Error ::: \n" + entries.html());
        }

    }

    private void getTime() throws Exception {
        if (cachedDocument == null)
            initCache();

        initWeekday();

        Element weekView = cachedDocument.getElementById("li_showWeek");
        String text = weekView.text();
        String reg = "(\\d+)(.*)(\\d+)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(text);

        if (text.contains("当前日期不在教学周历内")) {
            data.currentWeek = 1;
            return;
        }

        if (matcher.find()) {
            try {
                data.currentWeek = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                data.totalWeeks = Integer.parseInt(Objects.requireNonNull(matcher.group(3)));
            } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
                e.printStackTrace();
                data.currentWeek = -1;
                data.totalWeeks = -1;
            }
        } else {
            data.currentWeek = -1;
            data.totalWeeks = -1;
        }
    }

    public void initData() throws Exception {
        if (cachedDocument != null) {
            getTime();
            getDept();
        } else {
            initCache();
            if (cachedDocument != null) initData();
        }
    }

    private void initCache() throws Exception {
        cachedDocument = get(jwglURL + mainPart).parse();
    }

    @Override
    protected LoginStatus doLogin() throws Exception {

        if (cookies.isEmpty()) {
            Connection.Response init = nm.get(jwglURL);
            cookies = init.cookies();
        }

        encodeDetail();

        Map<String, String> details = new HashMap<String, String>() {{
            put("userAccount", user);
            put("userPassword", pass);
            put("encoded", encoded);
        }};

        Connection.Response response = nm.post(Jsoup.connect(jwglURL + loginPart)
                .data(details)
                .cookies(cookies));
        String url = response.url().toString();

        if (url.contains(loginPart)) {
            setLoggedIn(false);
            Document responseDoc = response.parse();
            Element msgElement = responseDoc.getElementById("showMsg");
            if (msgElement != null) {
                String msg = msgElement.ownText();
                if (msg.contains("用户名或密码错误"))
                    return LoginStatus.INCORRECT_DETAIL;
            }
            return LoginStatus.UNKNOWN_ERROR;
        } else {
            cookies.putAll(response.cookies());
            if (url.contains(framePart)) {
                setLoggedIn(true);
                Connection.Response mainResponse = get(jwglURL + mainPart);
                cachedDocument = mainResponse.parse();
                initData();
                return LoginStatus.LOGIN_SUCCESS;
            }
        }

        setLoggedIn(false);
        return LoginStatus.UNKNOWN_ERROR;
    }

    @Override
    protected LoginStatus doCaptchaLogin(String captcha) throws IOException {
        return null;
    }

    private boolean encodeDetail() throws IOException {
        InputStream is = context.getResources().openRawResource(R.raw.conwork);   //获取用户名与密码加密的js代码
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            V8 runtime = V8.createV8Runtime();      //使用J2V8运行js代码并将编码结果返回
            final String encodedName = runtime.executeStringScript(sb.toString()
                    + ";encodeInp('" + user + "');\n");
            final String encodedPwd = runtime.executeStringScript(sb.toString() + ";encodeInp('" + pass + "');\n");
            runtime.release();

            encoded = encodedName + "%%%" + encodedPwd;
            Log.e("Encoded", encoded);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            encoded = "";
            throw new IOException("Failed to encode jwgl details!");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection.Response get(String url) throws Exception {
        if (checkLogin()) {
            return nm.get(url, cookies);
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
    }

    private Connection.Response post(Connection conn) throws Exception {
        if (checkLogin()) {
            return nm.post(conn.cookies(cookies));
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
    }

    public JwglData getData() {
        return data;
    }

    /*
     * 当学校启用多服务器时使用

    void selectServer(String uName) {
        boolean enableServers = true;//是否启用多服务器 true/false
        String[] serversArray = new String[8];//服务器列表

        serversArray[0] = "http://10.3.58.10:8080/jsxsd/";
        serversArray[1] = "http://10.3.58.11:8080/jsxsd/";
        serversArray[2] = "http://10.3.58.12:8080/jsxsd/";
        serversArray[3] = "http://10.3.58.13:8080/jsxsd/";
        serversArray[4] = "http://10.3.58.14:8080/jsxsd/";
        serversArray[5] = "http://10.3.58.15:8080/jsxsd/";
        serversArray[6] = "http://10.3.58.16:8080/jsxsd/";
        serversArray[7] = "http://10.3.58.17:8080/jsxsd/";


        String loginUrl = "xk/LoginToXk";
    }

*
* 接口封闭前的操作
*
*
    public LoginStatus doLogin() throws IOException, LoginException {

        Map<String, String> details = new HashMap<String, String>() {{
            put("method", "authUser");
            put("xh", user);
            put("pwd", pass);
        }};
        Connection.Response res = nm.get(Jsoup.connect(jwglURL)
                .data(details)
                .ignoreContentType(true), null);
        try {
            JSONObject response = new JSONObject(res.body());
            boolean success = response.getBoolean("success");
            if (success) {
                token = response.getString("token");
                setLoggedIn(true);
                return LoginStatus.LOGIN_SUCCESS;
            } else {
                String msg = response.getString("msg");
                if (msg.contains("密码错误")) return LoginStatus.INCORRECT_DETAIL;
                else {
                    LoginStatus error = LoginStatus.UNKNOWN_ERROR;
                    error.errorMsg = msg;
                    return error;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LoginStatus.UNKNOWN_ERROR;
    }
    *
    private void getTime() throws IOException, LoginException {
        if (checkLogin()) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            data.today = calendar.getTime();
            data.weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (data.weekday == 0) data.weekday = 7;

            final String time = dateFormat.format(calendar.getTime());
            Map<String, String> details = new HashMap<String, String>() {{
                put("method", "getCurrentTime");
                put("currDate", time);
            }};

            Connection.Response res = nm.get(Jsoup.connect(jwglURL)
                    .data(details)
                    .header("token", token)
                    .ignoreContentType(true), cookies);

            try {
                JSONObject response = new JSONObject(res.body());
                data.currentTerm = response.getString("xnxqh");
                try {
                    data.currentWeek = response.getInt("zc");
                } catch (JSONException e){
                    data.currentWeek = 1;
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public HashMap<String, String> getTerms() throws IOException, LoginException {
        if (checkLogin() & data.terms.isEmpty()) {
            Map<String, String> details = new HashMap<String, String>() {{
                put("method", "getXnxq");
                put("xh", user);
            }};

            HashMap<String, String> terms = new HashMap<>();
            Connection.Response res = get(jwglURL, details);
            try {
                JSONArray array = new JSONArray(res.body());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject term = array.getJSONObject(i);
                    String termID = term.getString("xnxq01id");
                    char termNO = termID.charAt(termID.length() - 1);

                    if (termNO == '3'){
                        continue;
                    }

                    int termYear = Integer.parseInt(termID.substring(0, 4));
                    int userYear = Integer.parseInt(user.substring(0, 4));
                    if (termYear < userYear){
                        continue;
                    }
                    terms.put(term.getString("xqmc"), termID);
                    if (term.getString("isdqxq").equals("1"))
                        data.currentTerm = termID;
                }
                data.terms = terms;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data.terms;
    }

    public List<Course> getClassToday(boolean forceRefresh) throws IOException, LoginException {
        List<Course> today = new LinkedList<>();
        if (data.courseWeek.isEmpty()) {
            getClassWeek(-1, forceRefresh);
        }
        for (Course course : data.courseWeek) {
            course.verifyWeekday();
            if (course.day == data.weekday) {
                today.add(course);
            }
        }
        return today;
    }

    public List<Course> getClassWeek(final int week, boolean forceRefresh) throws IOException, LoginException {
        List<Course> list = new LinkedList<>();
        if (week == -1) {
            if (data.currentWeek == 0) getTime();
            return getClassWeek(data.currentWeek, forceRefresh);
        }
        if (!forceRefresh) {
            list = data.loadCourse(week);
        }
        if (list.isEmpty()) {
            list = getCourses(week);
        }

        for (Course course : list) {
            course.verifyWeekday();
        }

        return list;
    }

    private List<Course> getCourses(final int week) throws IOException, LoginException {
        List<Course> list = new LinkedList<>();
        if (checkLogin()) {

            Map<String, String> details = new HashMap<String, String>() {{
                put("method", "getKbcxAzc");
                put("xh", user);
                put("zc", String.valueOf(week));
            }};

            Connection.Response res = get(jwglURL, details);
            try {
                JSONArray courseArray = new JSONArray(res.body());
                Gson gson = new Gson();
                for (int i = 0; i < courseArray.length(); i++) {
                    list.add(gson.fromJson(courseArray.get(i).toString(), Course.class));
                }
                data.courseWeek = list;
                data.saveCourse(week);
                for (Course course : list) course.verifyWeekday();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public List<Score> getScore(String termName) throws IOException, LoginException {

        if (termName != null && termName.equals("全部")){
            return getAllScores();
        } else {
            String termID;
            if (termName == null) termID = data.currentTerm;
            else termID = data.terms.get(termName);
            if (termID != null && termID.endsWith("3")) {
                LinkedList<Score> list = new LinkedList<>(getScoreWithID(termID));
                list.addAll(getScoreWithID(termID.substring(0, termID.length() - 1) + "3"));
                return list;
            }
            return getScoreWithID(termID);
        }
    }

    private List<Score> getAllScores() throws IOException, LoginException{
        TreeSet<String> sortedTerms = new TreeSet<>(data.terms.values());
        LinkedList<Score> scores = new LinkedList<>();
        for (String term : sortedTerms){
            scores.addAll(getScore(term));
        }
        return scores;
    }

    private List<Score> getScoreWithID(final String termID) throws IOException, LoginException{
        LinkedList<Score> list = new LinkedList<>();

        if (checkLogin()) {
            Map<String, String> details = new HashMap<String, String>() {{
                put("method", "getCjcx");
                put("xh", user);
                if (termID == null)
                    put("xnxqid", "");
                else
                    put("xnxqid", termID);
            }};

            //77726476706e69737468656265737421fae0469069327d406a468ca88d1b203b
            Connection.Response res = get(jwglURL, details);

            try {
                JSONObject object = new JSONObject(res.body());
                if (object.getBoolean("success")) {
                    JSONArray scoreArray = object.getJSONArray("result");
                    Gson gson = new Gson();
                    for (int i = 0; i < scoreArray.length(); i++) {
                        Score score = gson.fromJson(scoreArray.get(i).toString(), Score.class);
                        score.verifyGPA();
                        list.add(score);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    *
    * private Connection.Response get(String url, Map<String, String> details) throws IOException, LoginException {
        return nm.get(Jsoup.connect(url)
                .ignoreContentType(true)
                .data(details)
                .header("token", token), cookies);
    }

     */

}
