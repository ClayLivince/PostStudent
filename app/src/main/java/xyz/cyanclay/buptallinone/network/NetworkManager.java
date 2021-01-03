package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import xyz.cyanclay.buptallinone.network.info.InfoManager;
import xyz.cyanclay.buptallinone.network.jwgl.JwglManager;
import xyz.cyanclay.buptallinone.network.login.PasswordHelper;

public class NetworkManager {

    public boolean isSchoolNet = false;
    Context context;
    public JwxtManager jwxtManager;
    public JwglManager jwglManager;
    public VPNManager vpnManager;
    public AuthManager authManager;
    public InfoManager infoManager;
    public UpdateManager updateManager;
    public ShareManager shareManager;
    public PasswordHelper passwordHelper;

    private static File cacheDir;
    private static File networkCacheDir;
    private static File picDir;

    private Logger logger = LogManager.getLogger(NetworkManager.class);

    public NetworkManager(Context appContext) throws IOException {

        context = appContext;
        vpnManager = new VPNManager(this, context);
        jwxtManager = new JwxtManager(this, context);
        authManager = new AuthManager(this, context);
        infoManager = new InfoManager(this, context);
        jwglManager = new JwglManager(this, context);
        updateManager = new UpdateManager(this);
        shareManager = new ShareManager(context);
        passwordHelper = new PasswordHelper();

        init();
    }

    static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36";

    public String user = "";
    public String name = "";

    private void init() throws IOException {

        /*
        httpClient = new OkHttpClient.Builder()
                .writeTimeout(30000, TimeUnit.MILLISECONDS)
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        httpClientNoRedirect = new OkHttpClient.Builder()
                .writeTimeout(30000, TimeUnit.MILLISECONDS)
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .cookieJar(httpClient.cookieJar())
                .build();

         */

        isSchoolNet = checkNetwork();
        cacheDir = context.getApplicationContext().getCacheDir();
        networkCacheDir = new File(cacheDir, "network");
        picDir = new File(cacheDir, "pic");
        checkCache("");

        dispatchPassword(passwordHelper.loadDecrypt(context.getFilesDir()));
    }

    public void setUser(String user, String name) {
        this.user = user;
        this.name = name;
    }

    private void dispatchPassword(String[] details) {
        if (details[0] != null) this.user = details[0];
        vpnManager.setDetails(details[0], details[1]);
        if (details[2] != null) infoManager.setDetails(details[0], details[2]);
        if (details[3] != null) jwglManager.setDetails(details[0], details[3]);
        if (details[4] != null) jwxtManager.setDetails(details[0], details[4]);
    }

    private boolean checkNetwork() {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;

        return (wifiState == 3 && wifiId != null && wifiId.contains("BUPT-"));
    }

    public Connection.Response get(String url, Map<String, String> cookies,
                                   boolean ignoreContent) throws Exception {
        try {
            return get(Jsoup.connect(url).ignoreContentType(ignoreContent), cookies);
        } catch (IllegalArgumentException e) {
            logger.error("Error Happened while Constructing Connection to " + url, e);
            throw new IOException(e);
        }
    }

    public Connection.Response get(String url) throws Exception {
        return get(Jsoup.connect(url), null);
    }

    public Connection.Response get(Connection conn, Map<String, String> cookies) throws Exception {

        conn.userAgent(userAgent)
                .method(Connection.Method.GET)
                .timeout(10000);
        String url = conn.request().url().toString();
        if (url.contains("&amp;")) {
            conn.url(url.replace("&amp;", "&"));
        }
        Log.i("NetworkManager", "Trying to connect to " + conn.request().url().toString());
        if (cookies != null) conn.cookies(cookies);
        if (isSchoolNet) {
            return conn.execute();
        } else {
            return vpnManager.get(conn);
        }
    }

    public Connection.Response post(Connection conn) throws Exception {
        conn.userAgent(userAgent)
                .method(Connection.Method.POST)
                .timeout(10000);
        if (isSchoolNet) {
            return conn.execute();
        } else {
            return vpnManager.get(conn);
        }
    }

    public Connection.Response get(String url, Map<String, String> cookies) throws Exception {
        return get(Jsoup.connect(url), cookies);
    }

    public Connection.Response getNoVPN(Connection conn) throws IOException {
        return conn.timeout(5000).userAgent(userAgent).method(Connection.Method.GET).execute();
    }

    /**
     * 获取目标url网页内容，如有缓存优先读取缓存
     *
     * @param url     目标url
     * @param cookies 需要携带的cookies
     * @return {@link Document} parse过的html document对象
     */
    public Document getContent(String url, Map<String, String> cookies) throws Exception {
        return getContent(url, cookies, false);
    }

    public byte[] getByteStream(String url, Map<String, String> cookies, boolean forceRefresh) throws Exception {
        if (!forceRefresh && checkCache(url)) {
            File resFile = new File(picDir, String.valueOf(url.hashCode()));
            InputStream in = new FileInputStream(resFile);
            byte[] data;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            data = out.toByteArray();
            in.close();
            return data;
        }
        return refreshResponse(url, cookies);
    }

    /**
     * 获取目标url网页内容
     *
     * @param url          目标url
     * @param cookies      需要携带的cookies
     * @param forceRefresh 是否强制刷新缓存
     * @return {@link Document} parse过的html document对象
     */
    public Document getContent(String url, Map<String, String> cookies, boolean forceRefresh) throws Exception {

        if (!forceRefresh && checkCache(url)) {
            File docFile = new File(networkCacheDir, String.valueOf(url.hashCode()));
            BufferedReader bufReader = null;
            try {
                bufReader = new BufferedReader(new FileReader(docFile));
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line);
                    line = bufReader.readLine();
                }
                return Jsoup.parse(sb.toString());
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) {
                    Log.e("FileNotFoundException: ", "File / " + docFile.getPath() + " / not found. The process should not goes here.");
                }
                e.printStackTrace();
                return refreshContent(url, cookies);
            } finally {
                try {
                    if (bufReader != null) {
                        bufReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return refreshContent(url, cookies);

    }

    private byte[] refreshResponse(String url, Map<String, String> cookies) throws Exception {

        FileOutputStream outStream = null;
        Connection.Response response = get(url, cookies, true);

        if (response != null) {
            try {
                File resFile = new File(picDir, String.valueOf(url.hashCode()));

                if (resFile.exists()) {
                    if (!resFile.delete()) throw new IOException("Failed to delete old cache.");
                }
                if (resFile.createNewFile()) {
                    outStream = new FileOutputStream(resFile);
                    outStream.write(response.bodyAsBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
            }
        }
        if (response != null) return response.bodyAsBytes();
        else throw new IOException("Failed to load " + url);

    }

    private Document refreshContent(String url, Map<String, String> cookies) throws Exception {
        FileOutputStream outStream = null;

        Connection.Response response = get(url, cookies);
        Document document = null;
        if (response != null) {
            document = response.parse();

            try {
                File docFile = new File(networkCacheDir, String.valueOf(url.hashCode()));
                if (docFile.exists()) {
                    if (!docFile.delete()) throw new IOException("Failed to delete old cache.");
                }
                if (docFile.createNewFile()) {
                    outStream = new FileOutputStream(docFile);
                    outStream.write(document.outerHtml().getBytes());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (document != null) return document;
        else throw new IOException("Unable to load Page : " + url);
    }

    private static boolean checkCache(String url) throws IOException {
        if (cacheDir.list() != null) {
            if (!picDir.exists()) {
                if (!picDir.mkdir()) throw new IOException("Failed to create PicCache Dir.");
            }
            if (networkCacheDir.exists() && networkCacheDir.isDirectory()) {
                if (Arrays.asList(Objects.requireNonNull(networkCacheDir.list())).contains(String.valueOf(url.hashCode())))
                    return true;
            }
        }
        File network = new File(cacheDir, "network");
        if (network.mkdir()) networkCacheDir = network;
        return false;
    }
}
