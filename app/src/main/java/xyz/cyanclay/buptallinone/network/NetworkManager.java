package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import java.util.concurrent.ExecutionException;

public class NetworkManager {
    public Context context;
    public JwxtManager jwxtManager;
    public JwglManager jwglManager;
    public VPNManager vpnManager;
    public InfoManager infoManager;


    private static File cacheDir;
    private static File networkCacheDir;

    public NetworkManager(Context appContext) {
        context = appContext;
        cacheDir = context.getApplicationContext().getCacheDir();
        networkCacheDir = new File(cacheDir, "network");
        jwxtManager = new JwxtManager(this);
        infoManager = new InfoManager();

        //jwglManager = new JwglManager();

    }

    public boolean isSchoolNet = true;
    static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36";

    public String user = "";
    public String name = "";

    public void setUser(String user, String name) {
        this.user = user;
        this.name = name;
    }

    public boolean checkNetwork(){
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;

        return (wifiState == 3 && wifiId != null && wifiId.contains("BUPT-"));
    }

    static void handleException(IOException e){
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

    static Connection.Response networkTask(Connection connection){
        AsyncTask<Connection, Integer, Connection.Response> tsk = new AsyncTask<Connection, Integer, Connection.Response>() {
            @Override
            protected Connection.Response doInBackground(Connection... conn) {
                try {
                    return conn[0].execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        tsk.execute(connection);
        try {
            return tsk.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Document parseTask(Connection.Response response){
        AsyncTask<Connection.Response, Integer, Document> tsk = new AsyncTask<Connection.Response, Integer, Document>() {
            @Override
            protected Document doInBackground(Connection.Response... res) {
                try {
                    return res[0].parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        tsk.execute(response);
        try {
            return tsk.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    static private Document refreshContent(String url, Map<String, String> cookies){
        FileOutputStream outStream = null;
        try {
            Connection connection = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .userAgent(userAgent);

            Connection.Response response = networkTask(connection);

            if (response != null){
                Document document = parseTask(response);

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

        } catch (IOException e){
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

    public static final int CONNECTION_MSG = 0;
    public static final int CONNECTION_RESPONSE_MSG = 1;
    public static final int DOCUMENT_MSG = 2;
    public static final int EXCEPTION_MSG = -1;

    public static class NetworkMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == CONNECTION_RESPONSE_MSG){

            }
        }
    }
}
