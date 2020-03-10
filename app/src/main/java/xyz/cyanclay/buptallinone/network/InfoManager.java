package xyz.cyanclay.buptallinone.network;

import android.text.Html;
import android.text.SpannableStringBuilder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static xyz.cyanclay.buptallinone.network.NetworkManager.userAgent;

public class InfoManager {

    public static final Integer SCHOOL_NOTICE = 0,
            SCHOOL_NEWS = 1,
            PARTY_OVERTNESS = 2,
            SCHOOL_OVERTNESS = 3,
            INNER_CONTROL_OVERTNESS = 4,
            NOTICE_OVERTNESS = 5;
    private static final String infoURL = "http://my.bupt.edu.cn/",
            loginURL = "https://auth.bupt.edu.cn/authserver/login?service=http%3A%2F%2Fmy.bupt.edu.cn%2Flogin.portal";
    private static final Map<Integer, String> URL_INDEX = new HashMap<Integer, String>() {{
        put(SCHOOL_NOTICE, detachURL(".pen=pe1144"));
        put(SCHOOL_NEWS, detachURL(".pen=pe1142"));
        put(PARTY_OVERTNESS, detachURL(".pen=pe1942"));
        put(SCHOOL_OVERTNESS, detachURL(".pen=pe1941"));
        put(INNER_CONTROL_OVERTNESS, detachURL(".pen=pe1962"));
        put(NOTICE_OVERTNESS, detachURL(".pen=pe1921"));

    }};
    static private Map<String, String> infoCookies;
    private String user, pass;
    private Document cachedDocument;
    private boolean isLoggedIn;

    static private String packageURL(String href) {
        return infoURL + href;
    }

    static private String detachURL(String href) {
        return "http://my.bupt.edu.cn/detach.portal?" + href;
    }

    /**
     * 获取信息门户主页显示的通知项目
     *
     * @param index 通知的类别；
     *              下列内容之一 {@link #SCHOOL_NOTICE 校内通知}, {@link #SCHOOL_NEWS 校内新闻},
     *              {@link #PARTY_OVERTNESS 党务公开}, {@link #SCHOOL_OVERTNESS 校务公开},
     *              {@link #INNER_CONTROL_OVERTNESS 内控信息公开}, {@link #NOTICE_OVERTNESS 公示公告},
     * @return items 返回获取到的项目列表，包含 标题、日期、url以及正文简介;
     */
    public InfoItems parseMainpage(Integer index) throws IOException {
        if (verifyIndex(index)) {
            if (checkLogin()) {
                if (cachedDocument != null) {
                    InfoItems items = new InfoItems();
                    for (Element entry : cachedDocument.getElementsByClass("mainhome").first().getElementsByClass("listnotice").get(index).child(0).child(0).children()) {
                        if (entry.className().equals("more")) continue;
                        if (entry.tag().getName().equals("div")) continue;
                        //Log.e("Entry: ", entry.toString());
                        InfoItem item = new InfoItem();
                        item.url = packageURL(entry.child(0).attr("href"));
                        item.title = entry.child(0).ownText().trim();
                        item.time = entry.child(0).child(0).ownText().trim();
                        if (index.equals(SCHOOL_NOTICE) || index.equals(SCHOOL_NEWS))
                            item.introduction = entry.child(1).child(1).child(1).child(0).ownText();
                        items.add(item);
                    }
                    return items;
                } else {
                    if (refreshCachedDocument()) return parseMainpage(index);
                }
            }
        }
        return null;
    }

    private boolean verifyIndex(int index) {
        return index >= 0 && index <= 5;
    }

    public InfoItems parseNoticeFull(Integer index) throws IOException {
        if (verifyIndex(index)) {
            if (checkLogin()) {
                Document document = getContent(URL_INDEX.get(index));
                Elements notices = document.getElementsByClass("newslist").first().children();
                InfoItems items = new InfoItems();
                for (Element entry : notices) {
                    InfoItem item = new InfoItem();
                    item.url = packageURL(entry.child(0).attr("href"));
                    item.title = entry.child(0).ownText().trim();
                    item.category = entry.child(1).ownText().trim();
                    item.time = entry.child(2).ownText().trim();
                    items.add(item);
                }
                return items;
            }
        }
        return null;
    }

    private boolean refreshCachedDocument() throws IOException {
        if (checkLogin()) {
            cachedDocument = NetworkManager.getContent(infoURL, infoCookies, true);
            return cachedDocument.title().equals("欢迎访问信息服务门户");
        }
        return false;
    }

    private boolean checkLogin() throws IOException {
        if (isLoggedIn && infoCookies != null) {
            return true;
        } else return infoLogin() == LoginStatus.LOGIN_SUCCESS;
    }

    public void setLoginDetails(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public LoginStatus infoLogin() throws IOException {

        Connection.Response infoInit = Jsoup.connect(infoURL)
                .method(Connection.Method.GET)
                .userAgent(userAgent)
                .execute();
        Connection.Response authInit = Jsoup.connect(loginURL)
                .method(Connection.Method.GET)
                .userAgent(userAgent)
                .execute();

        //Connection.Response infoInitRes = networkTask(infoInit);
        //Connection.Response authInitRes = networkTask(authInit);

        if (infoInit != null && authInit != null) {
            infoCookies = infoInit.cookies();
            Map<String, String> authCookies = authInit.cookies();
            final Document auth = authInit.parse();

            Map<String, String> loginDetail = new HashMap<String, String>() {{
                put("username", user);
                put("password", pass);
                put("lt", auth.getElementsByAttributeValue("name", "lt").first().attr("value"));
                put("execution", auth.getElementsByAttributeValue("name", "execution").first().attr("value"));
                put("_eventId", auth.getElementsByAttributeValue("name", "_eventId").first().attr("value"));
                put("rmShown", auth.getElementsByAttributeValue("name", "rmShown").first().attr("value"));
            }};

            Connection.Response authLogin = Jsoup.connect(loginURL)
                    .method(Connection.Method.POST)
                    .userAgent(userAgent)
                    .cookies(authCookies)
                    .data(loginDetail)
                    .execute();
            // Connection.Response authLoginRes = NetworkManager.networkTask(authLogin);
            infoCookies = authLogin.cookies();
            Document login = Jsoup.parse(authLogin.body());
            if (login.getElementsByClass("errors").first() != null) {
                String status = login.getElementsByClass("errors").first().ownText();
                if (status != null) {
                    isLoggedIn = false;
                    if (status.equals("Username is a required field.")) {
                        return LoginStatus.EMPTY_USERNAME;
                    } else if (status.equals("Password is a required field.")) {
                        return LoginStatus.EMPTY_PASSWORD;
                    } else if (status.equals("The username or password you provided cannot be determined to be authentic.")) {
                        return LoginStatus.INCORRECT_DETAIL;
                    } else if (status.equals("Please enter captcha.")) {
                        return LoginStatus.EMPTY_CAPTCHA;
                    } else if (status.equals("invalid captcha.")) {
                        return LoginStatus.INCORRECT_CAPTCHA;
                    } else if (status.length() != 0) {
                        return LoginStatus.UNKNOWN_ERROR;
                    }
                }
            } else if (login.title().contains("欢迎")) {
                NetworkManager.setUser(user, login.getElementsByClass("topinner").first().ownText().split(",")[0]);
                isLoggedIn = true;
                cachedDocument = login;
                return LoginStatus.LOGIN_SUCCESS;
            }
        }
        return LoginStatus.UNKNOWN_ERROR;
    }

    private Document getContent(String url) throws IOException {
        if (checkLogin()) {
            return NetworkManager.getContent(url, infoCookies);
        } else return null;
    }

    public class InfoItem {
        public String title;
        public String titleFull;
        public String introduction;
        public String time;
        public String url;
        public String category;
        public SpannableStringBuilder content;
        //public ArrayList<Drawable> contentPictures;

        /**
         * 附件列表，Key为附件名称，Value为附件下载URL。
         */
        public HashMap<String, String> attachments;

        public Document getContent() throws IOException {
            return InfoManager.this.getContent(url);
        }

        public void parseContent() throws IOException {
            Document document = getContent();
            titleFull = document.getElementsByClass("singlemainbox").first().getElementsByTag("h1").first().ownText();
            category = document.getElementsByClass("pdept").first().ownText();
            Elements paragraphs = document.getElementsByClass("singleinfo");
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            StringBuilder sb = new StringBuilder();
            for (Element entry : paragraphs) {
                sb.append(entry.text());
                ssb.append("\t").append(String.valueOf(Html.fromHtml(entry.html())));
            }
            content = ssb;
        }
    }

    public class InfoItems extends ArrayList<InfoItem> {

    }
}
