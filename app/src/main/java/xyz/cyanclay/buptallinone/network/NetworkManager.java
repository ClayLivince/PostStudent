package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;

public class NetworkManager {
    public Context context;
    public JwxtManager jwxtManager;
    public JwglManager jwglManager;
    //public VPNManager vpnManager;
    public InfoManager infoManager;


    private static File cacheDir;
    private static File networkCacheDir;

    public NetworkManager(Context appContext) throws IOException {

        context = appContext;
        jwxtManager = new JwxtManager(this);
        infoManager = new InfoManager();

        init();

        //jwglManager = new JwglManager();
    }

    boolean isSchoolNet = true;
    static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36";

    public static String user = "";
    public static String name = "";

    private void init() throws IOException {
        cacheDir = context.getApplicationContext().getCacheDir();
        networkCacheDir = new File(cacheDir, "network");

        dispatchPassword(PasswordHelper.loadDecrypt(context.getFilesDir()));
    }

    static void setUser(String user, String name) {
        NetworkManager.user = user;
        NetworkManager.name = name;
    }

    private void dispatchPassword(String[] details) {
        infoManager.setLoginDetails(details[0], details[1]);
        if (details[2] != null) jwxtManager.setJwDetails(details[0], details[2]);
        if (details[3] != null) ;
    }

    public boolean checkNetwork(){
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;

        return (wifiState == 3 && wifiId != null && wifiId.contains("BUPT-"));
    }

    static void handleException(Exception e) {
        //TODO: handle the exception;
        if (e instanceof HttpStatusException){
            ((HttpStatusException) e).getStatusCode();
        } else if(e instanceof SocketTimeoutException){

        }
    }

    /**
     * 获取目标url网页内容，如有缓存优先读取缓存
     * @param url 目标url
     * @param cookies 需要携带的cookies
     * @return {@link Document} parse过的html document对象
     */
    static Document getContent(String url, Map<String, String> cookies){
        return getContent(url, cookies, false);
    }

    /**
     * 获取目标url网页内容
     * @param url 目标url
     * @param cookies 需要携带的cookies
     * @param forceRefresh 是否强制刷新缓存
     * @return {@link Document} parse过的html document对象
     */
    static Document getContent(String url, Map<String, String> cookies, boolean forceRefresh){
        if (!forceRefresh && checkCache(url)){
            File docFile = new File(networkCacheDir, String.valueOf(url.hashCode()));
            BufferedReader bufReader = null;
            try {
                bufReader = new BufferedReader(new FileReader(docFile));
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null){
                    sb.append(line);
                    line = bufReader.readLine();
                }
                return Jsoup.parse(sb.toString());
            } catch (IOException e) {
                if (e instanceof FileNotFoundException){
                    Log.e("FileNotFoundException: ", "File / " + docFile.getPath() + " / not found. The process should not goes here.");
                }
                e.printStackTrace();
            } finally {
                try {
                    if (bufReader != null){
                        bufReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } return refreshContent(url, cookies);
    }

    static private Document refreshContent(String url, Map<String, String> cookies){
        FileOutputStream outStream = null;
        try {
            Connection connection = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .userAgent(userAgent);

            Connection.Response response = connection.execute();

            if (response != null){
                Document document = response.parse();

                File docFile = new File(networkCacheDir, String.valueOf(url.hashCode()));
                if (docFile.exists()){
                    docFile.delete();
                }
                if (docFile.createNewFile()){
                    outStream = new FileOutputStream(docFile);
                    outStream.write(document.outerHtml().getBytes());
                }
                return document;
            }

        } catch (Exception e) {
            e.printStackTrace();
            handleException(e);
        } finally {
            try {
                if (outStream != null){
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static private boolean checkCache(String url){
        if (cacheDir.list() != null){
            if (networkCacheDir.exists() && networkCacheDir.isDirectory()){
               if (Arrays.asList(networkCacheDir.list()).contains(String.valueOf(url.hashCode()))) return true;
            }
        }
        File network = new File(cacheDir, "network");
        if (network.mkdir()) networkCacheDir = network;
        return false;
    }
}
