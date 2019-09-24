package xyz.cyanclay.buptallinone.login;

import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class LoginManager {

    private String user;
    private String pass;
    private String cap;

    private final String loginURL = "https://jwxt.bupt.edu.cn/";
    private final String headerAgent = "User-Agent";
    private final String headerAgentArg = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3562.0 Safari/537.36";
    String form = "formhash=xxx&referer=https://www.bbaaz.com/&loginfield=username&username=xxxx&password=xxxx&questionid=0&answer=";

    private byte [] buffer;
    private byte [] all;
    private int length;
    private ArrayList<byte []> byteList;
    private ArrayList<Integer> byteLength;
    private int totalLength = 0;
    private String [] content = null;

    public LoginManager(String user, String pass, String cap){
        this.user = user;
        this.pass = pass;
        this.cap = cap;
    }

    public void setLoginDetails(String user, String pass, String cap){
        this.user = user;
        this.pass = pass;
        this.cap = cap;
    }

    public void getCapImage(){

    }

    public void login(){
        try {
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
            CookieHandler.setDefault(manager);
            HttpsURLConnection connection = (HttpsURLConnection) (new URL(loginURL).openConnection());
            connection.setRequestMethod("GET");
            connection.setRequestProperty(headerAgent, headerAgentArg);
            connection.connect();
            if(connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                buffer = new byte[1024];
                byteList = new ArrayList<>();
                byteLength = new ArrayList<>();
                while ((length = inputStream.read(buffer)) != -1) {
                    byteList.add(buffer);
                    byteLength.add(length);
                    totalLength += length;
                    buffer = new byte[1024];
                }
                connection.disconnect();
                all = new byte[totalLength];
                totalLength = 0;
                while (byteList.size() != 0) {
                    System.arraycopy(byteList.get(0), 0, all, totalLength, byteLength.get(0));
                    totalLength += byteLength.get(0);
                    byteList.remove(0);
                    byteLength.remove(0);
                }
                connection = (HttpsURLConnection) (new URL(loginURL + query.replace("xxx", suffix)).openConnection());
                connection.setRequestMethod("POST");
                connection.setRequestProperty(headerAgent, headerAgentArg);
                connection.setDoOutput(true);
                connection.connect();
                connection.getOutputStream().write(form.replace("xxx", formhash).getBytes("UTF-8"));
                inputStream = connection.getInputStream();
                buffer = new byte[1024];
                byteList = new ArrayList<>();
                byteLength = new ArrayList<>();
                totalLength = 0;
                while( (length = inputStream.read(buffer)) != -1 ) {
                    byteList.add(buffer);
                    byteLength.add(length);
                    totalLength += length;
                    buffer = new byte[1024];
                }
                connection.disconnect();
                all = new byte[totalLength];
                totalLength = 0;
                while(byteList.size() != 0) {
                    System.arraycopy(byteList.get(0), 0, all, totalLength, byteLength.get(0));
                    totalLength += byteLength.get(0);
                    byteList.remove(0);
                    byteLength.remove(0);
                }
                new String(all, "UTF-8"); // 查看该页面信息，登录成功
                all = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
