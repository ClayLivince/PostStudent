package xyz.cyanclay.buptallinone.network.jwgl;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class JwglManager extends SiteManager {

    private String token;


    private static final String jwglURL = "http://jwgl.bupt.edu.cn/app.do";

    public JwglManager(NetworkManager nm) {
        super(nm);
    }

    private void getTime() {

    }

    public LoginStatus doLogin() throws IOException {

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
                return LoginStatus.LOGIN_SUCCESS;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LoginStatus.UNKNOWN_ERROR;
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

    private boolean encodeDetail() throws IOException{
        InputStream is= context.getResources().openRawResource(R.raw.conwork);   //获取用户名与密码加密的js代码
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            V8 runtime = V8.createV8Runtime();      //使用J2V8运行js代码并将编码结果返回
            final String encodename = runtime.executeStringScript(sb.toString()
                    + ";encodeInp('"+user+"');\n");
            final String encodepwd=runtime.executeStringScript(sb.toString()+";encodeInp('"+pass+"');\n");
            runtime.release();

            final String encoded = encodename + "%%%" + encodepwd;
            Log.e("Encoded", encoded);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to encode jwgl details!");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyEncoded() throws IOException{
        if (encoded != null){
            if (encoded.length() != 0){
                return true;
            }
        }
        return encodeDetail();
    }

     */

}
