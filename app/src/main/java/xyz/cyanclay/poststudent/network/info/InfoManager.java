package xyz.cyanclay.poststudent.network.info;

import android.content.Context;
import android.text.SpannableStringBuilder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.VPNManager;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.network.login.LoginStatus;
import xyz.cyanclay.poststudent.network.spanner.BuptSpanner;

public class InfoManager extends SiteManager {

    private static final boolean useSSL = false;

    public static final String domain = useSSL ? "https://my.bupt.edu.cn" : "http://my.bupt.edu.cn";
    private static final String infoURL = "/index.jsp";
    private static final String indexURL = "/xs_index.jsp?urltype=tree.TreeTempUrl&wbtreeid=1541";
    private String owner = "";
    private String loginRedirect = "";
    //loginURL = "https://auth.bupt.edu.cn/authserver/login?service=http%3A%2F%2Fmy.bupt.edu.cn%2Flogin.jsp";

    private Document cachedDocument;
    private BuptSpanner buptSpanner = new BuptSpanner(this);

    public InfoManager(NetworkManager nm, Context context) {
        super(nm, context);

        InfoCategory.init(context);
    }

    public LoginStatus doLogin() throws Exception {

        Connection.Response infoInit = nm.get(Jsoup.connect(domain + infoURL)
                .followRedirects(false), cookies);
        cookies.putAll(infoInit.cookies());
        infoInit = nm.get(Jsoup.connect(domain + "/xs_index.jsp?urltype=tree.TreeTempUrl&wbtreeid=1541")
                .followRedirects(false), cookies);
        cookies.putAll(infoInit.cookies());
        String partMoved = infoInit.header("Location");
        try {
            owner = partMoved.split("owner=")[1];
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            throw new IOException("Failed to parse owner.", aiobe);
        }
        partMoved = partMoved.replace("http/77726476706e69737468656265737421fdee0f9e32207c1e7b0c9ce29b5b/", "");
        loginRedirect = domain + partMoved;
        Connection.Response init = nm.get(Jsoup.connect(loginRedirect), cookies);
        nm.authManager.setDetails(this.user, this.pass);

        String initUrl = init.url().toString();

        // If the returned url contains anything related to auth.bupt.edu.cn
        // 777264 is the vpn encoded one of auth
        // Then login with authManager
        if (initUrl.contains("auth.bupt.edu.cn") |
                initUrl.contains("77726476706e69737468656265737421f1e2559469327d406a468ca88d1b203b")) {
            return nm.authManager.login(loginRedirect);
        } else {
            Document initDoc = init.parse();
            if (initDoc.title().contains("欢迎")) {
                nm.setUser(user, initDoc.getElementsByClass("topinner").first().ownText().split(",")[0]);
                setLoggedIn(true);
                cachedDocument = initDoc;
                return LoginStatus.LOGIN_SUCCESS;
            }
        }
        return LoginStatus.UNKNOWN_ERROR;
    }

    @Override
    public LoginStatus doCaptchaLogin(String captcha) throws Exception {
        return nm.authManager.loginWithCaptcha(loginRedirect, captcha);
    }

    private Document getContent(String url, boolean refresh) throws Exception {
        if (checkLogin()) {
            return nm.getContent(url, cookies, refresh);
        } else throw new IOException("Info Login Failed.");
    }

    public byte[] getBytes(String url, boolean force) throws Exception {
        if (checkLogin()) {
            return nm.getByteStream(url, cookies, force);
        } else throw new IOException("Info Login Failed.");
    }

    public InfoItems parseMainpage(InfoCategory category) throws Exception {
        if (checkLogin()) {
            if (cachedDocument != null) {
                InfoItems items = new InfoItems();
                Elements entries = cachedDocument.getElementsByClass("mainhome").first()
                        .getElementsByClass("listnotice").get(category.getOrdinal()).getElementsByTag("dd");
                for (Element entry : entries) {
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

    public InfoItems parseNotice(InfoCategory category, boolean refresh) throws Exception {
        return parseNotice(category, buildURL(category, -1), refresh);
    }

    public InfoItems parseNotice(InfoCategory category, int page, boolean refresh) throws Exception {
        return parseNotice(category, buildURL(category, page), refresh);
    }

    public InfoItems parseNotice(InfoCategory category, int page, String searchWord) throws Exception {
        return parseNotice(category, buildURL(category, page), searchWord);
    }

    private InfoItems parseNotice(InfoCategory category, String url, String searchWord) throws Exception {
        if (checkLogin()) {
            Document document = nm.post(Jsoup.connect(url)
                    .data("v_title", searchWord)).parse();
            return parseNotice(category, document, url, true, searchWord);
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
    }

    private InfoItems parseNotice(InfoCategory category, String url, boolean refresh) throws Exception {
        if (checkLogin()) {
            Document document = getContent(url, refresh);
            return parseNotice(category, document, url, false, null);
        }
        throw new LoginException(this, LoginStatus.UNKNOWN_ERROR);
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
        if (items.size() < 20) items.bottom = true;
        return items;
    }

    private String buildURL(InfoCategory category, int page) throws IOException {

        StringBuilder sb = new StringBuilder();

        sb.append(domain);
        sb.append("/list.jsp?urltype=tree.TreeTempUrl&wbtreeid=");
        sb.append(category.id);
        if (page > 0) {
            sb.append("&PAGENUM=");
            sb.append(page);
        }

        /*
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

         */
        return sb.toString();
    }

    private boolean refreshCachedDocument() throws Exception {
        if (checkLogin()) {
            cachedDocument = nm.getContent(domain + indexURL, cookies, true);
            return cachedDocument.title().contains("欢迎访问信息服务门户");
        }
        return false;
    }

    public static String packageURL(String href) {
        if (href.charAt(0) == '/')
            href = href.substring(1);
        return domain + '/' + href;
    }

    @Deprecated
    static String detachURL(String href) {
        return "http://my.bupt.edu.cn/detach.portal?" + href;
    }


    public class InfoItem {

        public String title = "";
        public String titleFull = "";
        public String time = "";
        String url;
        String urlVPNPacked;
        public String id;
        public String announcer = "";
        public InfoCategory category;
        public SpannableStringBuilder contentSpanned;

        InfoItem(String url, InfoCategory category) {
            this.category = category;
            this.url = url;
            this.urlVPNPacked = nm.vpnManager.analyseURL(url);
            String[] queries = url.split("&");
            for (String query : queries) {
                if (query.contains("wbnewsid")) {
                    this.id = query.split("=")[1];
                    break;
                }
            }
        }

        /**
         * 附件列表，Key为附件名称，Value为附件下载URL。
         */
        public HashMap<String, String> attachments = new HashMap<>();

        private Document getContentDocument(boolean refresh) throws Exception {
            return InfoManager.this.getContent(url, refresh);
        }

        public void parseContentSpanned(int maxWidth, boolean force) throws Exception {
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
            parseAttachment(document);
        }

        public void parseAttachment(Document document) {
            Element attachmentE = document.getElementsByClass("battch").first();
            if (attachmentE != null) {
                for (Element linkItem : attachmentE.getElementsByTag("li")) {
                    Element link = linkItem.getElementsByTag("a").first();
                    if (nm.isSchoolNet)
                        attachments.put(link.ownText(), packageURL(link.attr("href")));
                    else
                        attachments.put(link.ownText(), VPNManager.packageURL(link.attr("href")));
                }
            }
        }

        public String getUrl() {
            if (nm.isSchoolNet) return url;
            else return urlVPNPacked;
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

        public void getMore() throws Exception {
            if (url != null) {
                page++;
                InfoItems more;
                if (isSearch) {
                    more = InfoManager.this.parseNotice(this.get(0).category, buildURL(this.get(0).category, page), searchWord);
                } else {
                    more = InfoManager.this.parseNotice(this.get(0).category, buildURL(this.get(0).category, page), false);
                }
                this.addAll(more);
                this.bottom = more.bottom;
            } else {
                bottom = true;
            }
        }
    }
}
