package xyz.cyanclay.buptallinone.network.info;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Pair;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;
import xyz.cyanclay.buptallinone.network.spanner.BuptSpanner;

public class InfoManager extends SiteManager {

    private static final String infoURL = "http://my.bupt.edu.cn/",
            loginURL = "https://auth.bupt.edu.cn/authserver/login?service=http%3A%2F%2Fmy.bupt.edu.cn%2Flogin.portal";

    private Document cachedDocument;
    private InfoAnnouncer announcer;
    BuptSpanner buptSpanner = new BuptSpanner(this);

    public InfoManager(NetworkManager nm, Context context) {
        super(nm, context);
        announcer = new InfoAnnouncer(context);
    }

    public LoginStatus doLogin() throws IOException {

        if (user == null) {
            return LoginStatus.EMPTY_USERNAME;
        }
        if (pass == null) {
            return LoginStatus.EMPTY_PASSWORD;
        }

        Connection.Response infoInit = nm.get(infoURL);
        Connection.Response authInit = nm.get(loginURL);

        if (infoInit != null && authInit != null) {

            cookies = infoInit.cookies();
            Map<String, String> authCookies = authInit.cookies();
            final Document auth = authInit.parse();

            if (auth.title().contains("欢迎")) {
                nm.setUser(user, auth.getElementsByClass("topinner").first().ownText().split(",")[0]);
                isLoggedIn = true;
                cachedDocument = auth;
                return LoginStatus.LOGIN_SUCCESS;
            }

            Map<String, String> loginDetail = new HashMap<String, String>() {{
                put("username", user);
                put("password", pass);
                put("lt", auth.getElementsByAttributeValue("name", "lt").first().attr("value"));
                put("execution", auth.getElementsByAttributeValue("name", "execution").first().attr("value"));
                put("_eventId", auth.getElementsByAttributeValue("name", "_eventId").first().attr("value"));
                put("rmShown", auth.getElementsByAttributeValue("name", "rmShown").first().attr("value"));
            }};

            Connection.Response authLogin = nm.post(Jsoup.connect(loginURL)
                    .cookies(authCookies)
                    .data(loginDetail));

            cookies = authLogin.cookies();
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
                nm.setUser(user, login.getElementsByClass("topinner").first().ownText().split(",")[0]);
                isLoggedIn = true;
                cachedDocument = login;
                return LoginStatus.LOGIN_SUCCESS;
            }
        }
        return LoginStatus.UNKNOWN_ERROR;
    }

    private Document getContent(String url, boolean refresh) throws IOException {
        if (checkLogin()) {
            return nm.getContent(url, cookies, refresh);
        } else throw new IOException("Info Login Failed.");
    }

    public byte[] getBytes(String url, boolean force) throws IOException {
        if (checkLogin()) {
            return nm.getByteStream(url, cookies, force);
        } else throw new IOException("Info Login Failed.");
    }

    public InfoItems parseMainpage(InfoCategory category) throws IOException {
        if (checkLogin()) {
            if (cachedDocument != null) {
                InfoItems items = new InfoItems();
                for (Element entry : cachedDocument.getElementsByClass("mainhome").first().getElementsByClass("listnotice").get(category.ordinal()).child(0).child(0).children()) {
                    if (entry.className().equals("more")) continue;
                    if (entry.tag().getName().equals("div")) continue;
                    String url = packageURL(entry.child(0).attr("href"));
                    InfoItem item = new InfoItem(url, category);
                    item.title = entry.child(0).ownText().trim();
                    item.time = entry.child(0).child(0).ownText().trim();
                    //            if (index.equals(SCHOOL_NOTICE) || index.equals(SCHOOL_NEWS))
                    //                item.introduction = entry.child(1).child(1).child(1).child(0).ownText();
                    items.add(item);
                }
                return items;
            } else if (refreshCachedDocument()) return parseMainpage(category);
        }
        return null;
    }

    public InfoItems parseNotice(InfoCategory category, boolean refresh) throws IOException {
        return parseNotice(category, buildURL(category, -1, -1, 1), refresh);
    }

    public InfoItems parseNotice(InfoCategory category, int cate, int id, int page, boolean refresh) throws IOException {
        return parseNotice(category, buildURL(category, cate, id, page), refresh);
    }

    public InfoItems parseNotice(InfoCategory category, int cate, int id, int page,
                                 String searchWord) throws IOException {
        return parseNotice(category, buildURL(category, cate, id, page), searchWord);
    }

    private InfoItems parseNotice(InfoCategory category, String url, String searchWord) throws IOException {
        if (checkLogin()) {
            Document document = nm.post(Jsoup.connect(url)
                    .data("v_title", searchWord)).parse();
            return parseNotice(category, document, url, true, searchWord);
        }
        throw new LoginException(this, url);
    }

    private InfoItems parseNotice(InfoCategory category, String url, boolean refresh) throws IOException {
        if (checkLogin()) {
            Document document = getContent(url, refresh);
            return parseNotice(category, document, url, false, null);
        }
        throw new LoginException(this, url);
    }

    private InfoItems parseNotice(InfoCategory category, Document document, String url,
                                  boolean isSearch, String searchWord) {
        Elements notices = document.getElementsByClass("newslist").first().children();
        InfoItems items;
        if (isSearch) items = new InfoItems(searchWord);
        else items = new InfoItems();
        items.url = url;
        for (Element entry : notices) {
            String urlItem = packageURL(entry.child(0).attr("href"));
            InfoItem item = new InfoItem(urlItem, category);
            item.title = entry.child(0).ownText().trim();
            item.announcer = entry.child(1).ownText().trim();
            item.time = entry.child(2).ownText().trim();
            items.add(item);
        }
        if (items.size() < 30) items.bottom = true;
        return items;
    }

    private String buildURL(InfoCategory category, int cate, int id, int page) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("http://my.bupt.edu.cn/detach.portal?")
                .append(InfoCategory.pens.get(category));
        if (cate >= 0 & id >= 0) {
            Pair in = announcer.getIDAndName(cate, id);
            sb.append("&groupid=").append(in.first)
                    .append("&groupname=").append(in.second);
        }
        if (page > 1) {
            sb.append("&pageIndex=").append(page);
        }
        return sb.toString();
    }

    private boolean refreshCachedDocument() throws IOException {
        if (checkLogin()) {
            cachedDocument = nm.getContent(infoURL, cookies, true);
            return cachedDocument.title().equals("欢迎访问信息服务门户");
        }
        return false;
    }

    private static String packageURL(String href) {
        return infoURL + href;
    }

    static String detachURL(String href) {
        return "http://my.bupt.edu.cn/detach.portal?" + href;
    }


    public class InfoItem {

        public String title = "";
        public String titleFull = "";
        public String time = "";
        String url;
        public String id;
        public String announcer = "";
        public InfoCategory category;
        public SpannableStringBuilder contentSpanned;

        InfoItem(String url, InfoCategory category) {
            this.category = category;
            this.url = url;
            String[] queries = url.split("&");
            for (String query : queries) {
                if (query.contains("bulletinId")) {
                    this.id = query.split("=")[1];
                    break;
                }
            }
        }

        /**
         * 附件列表，Key为附件名称，Value为附件下载URL。
         */
        public HashMap<String, String> attachments;

        private Document getContentDocument(boolean refresh) throws IOException {
            return InfoManager.this.getContent(url, refresh);
        }

        public void parseContentSpanned(int maxWidth, boolean force) throws IOException {
            Document document = getContentDocument(force);
            titleFull = document.getElementsByClass("singlemainbox").first().getElementsByTag("h1").first().ownText();
            announcer = document.getElementsByClass("pdept").first().ownText().split("：")[1];

            BuptSpanner htmlSpanner = InfoManager.this.buptSpanner;
            htmlSpanner.setImageScheme(maxWidth, force);

            Elements paragraphs = document.getElementsByClass("singleinfo").first().children();
            SpannableStringBuilder ssb = new SpannableStringBuilder();

            for (Element para : paragraphs) {
                ssb.append(htmlSpanner.fromHtml(para.outerHtml()));
            }
            contentSpanned = ssb;
        }
    }

    public class InfoItems extends ArrayList<InfoItem> {

        String url;
        boolean isSearch = false;
        String searchWord;
        private int page = 1;
        public boolean bottom = false;

        InfoItems() {
        }

        InfoItems(String searchWord) {
            this.isSearch = true;
            this.searchWord = searchWord;
        }

        public void getMore() throws IOException {
            if (url != null) {
                page++;
                InfoItems more;
                if (isSearch) {
                    more = InfoManager.this.parseNotice(this.get(0).category, url.concat("&pageIndex=" + page), searchWord);
                } else {
                    more = InfoManager.this.parseNotice(this.get(0).category, url.concat("&pageIndex=" + page), false);
                }
                this.addAll(more);
                this.bottom = more.bottom;
            } else {
                bottom = true;
            }
        }

    }
}
