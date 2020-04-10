package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.util.Log;

import com.eclipsesource.v8.V8;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class VPNManager extends SiteManager {

    private Context context;

    private static String vpnURL = "http://webvpn.bupt.edu.cn";

    VPNManager(NetworkManager nm, Context context) throws IOException {
        super(nm);
        this.context = context;

        init();
    }

    private void init() throws IOException {
        Connection.Response init = Jsoup.connect(vpnURL)
                .userAgent(NetworkManager.userAgent)
                .method(Connection.Method.GET)
                .execute();
        cookies = init.cookies();
    }

    public LoginStatus doLogin() throws IOException {
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        Map<String, String> details = new HashMap<String, String>() {{
            put("auth_type", "local");
            put("username", user);
            put("sms_code", "");
            put("password", pass);
        }};

        String loginURL = "http://webvpn.bupt.edu.cn/do-login?local_login=true";
        Connection.Response login = Jsoup.connect(loginURL)
                .cookies(cookies)
                .data(details)
                .userAgent(NetworkManager.userAgent)
                .method(Connection.Method.POST)
                .execute();
        cookies.putAll(login.cookies());
        Document loginDoc = login.parse();

        if (loginDoc.getElementById("aHref") != null) {
            isLoggedIn = true;
            return LoginStatus.LOGIN_SUCCESS;
        } else {
            StringBuilder sb = new StringBuilder();
            for (Element entry : loginDoc.getElementsByTag("script")) {
                sb.append(entry.data());
            }

            String token = "";
            String[] vars = sb.toString().split("var");
            for (String var : vars) {
                if (var.contains("=")) {
                    if (var.contains("logoutOtherToken")) {
                        String[] kvp = var.split("=");
                        token = kvp[1].split("\n")[0].replace("'", "").trim();
                        break;
                    }
                }
            }

            System.out.println(token);
            if (!token.equals("")) {
                Connection.Response confirm = Jsoup.connect(vpnURL + "/do-confirm-login")
                        .data("username", user, "logoutOtherToken", token)
                        .method(Connection.Method.POST)
                        .userAgent(NetworkManager.userAgent)
                        .cookies(cookies)
                        .ignoreContentType(true)
                        .execute();

                try {
                    JSONObject confirmJSON = new JSONObject(confirm.body());
                    if (confirmJSON.getBoolean("success")) {
                        isLoggedIn = true;
                        return LoginStatus.LOGIN_SUCCESS;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException("VPN JSON Exception!");
                }
            }
            isLoggedIn = false;
            return LoginStatus.UNKNOWN_ERROR;
        }
    }

    public Connection.Response get(Connection conn) throws IOException {
        if (checkLogin()) {

            conn.cookies(cookies);
            String url = conn.request().url().toString();
            if (!url.contains("webvpn.bupt.edu.cn")) conn.url(analyseURL(url));
            Connection.Response res = conn.execute();
            cookies.putAll(res.cookies());
            Log.i("VPNConnection", "Trying to connect to " + res.url().toString());
            return res;
        }
        throw new IOException("VPN Failed to login.");
    }

    private String analyseURL(String url) {
        String protocol = url.split("://", 2)[0];
        String href = url.split("://", 2)[1];
        InputStream is = context.getResources().openRawResource(R.raw.vpn);   //获取用户名与密码加密的js代码
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            V8 runtime = V8.createV8Runtime();
            //使用J2V8运行js代码并将编码结果返回
            final String result = runtime.executeStringScript(sb.toString()
                    + ";encrypUrl('" + protocol + "','" + href + "');\n");

            return vpnURL + result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
