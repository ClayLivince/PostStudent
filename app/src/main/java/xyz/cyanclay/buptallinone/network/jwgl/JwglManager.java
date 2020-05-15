package xyz.cyanclay.buptallinone.network.jwgl;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class JwglManager extends SiteManager {

    private JwglData data;
    private String token;

    private static final String jwglURL = "http://jwgl.bupt.edu.cn/app.do";

    public JwglManager(NetworkManager nm, Context context) {
        super(nm, context);
        data = new JwglData(this);
    }

    public int getWeek() throws IOException {
        if (data.currentWeek == 0) getTime();
        return data.currentWeek;
    }

    private void getTime() throws IOException {
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
                data.currentWeek = response.getInt("zc");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Course> getClassToday(boolean forceRefresh) throws IOException {
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

    public List<Course> getClassWeek(final int week, boolean forceRefresh) throws IOException {
        List<Course> list = new LinkedList<>();
        if (week == -1) {
            if (data.currentWeek == 0) getTime();
            return getClassWeek(data.currentWeek, forceRefresh);
        }
        if (!forceRefresh) {
            list = data.loadCourse(week);
        }
        if (list.isEmpty()) {
            list = fetchCourse(week);
        }

        return list;
    }

    private List<Course> fetchCourse(final int week) throws IOException {
        List<Course> list = new LinkedList<>();
        if (checkLogin()) {

            Map<String, String> details = new HashMap<String, String>() {{
                put("method", "getKbcxAzc");
                put("xh", user);
                put("zc", String.valueOf(week));
            }};

            Connection.Response res = nm.get(Jsoup.connect(jwglURL)
                    .ignoreContentType(true)
                    .data(details)
                    .header("token", token), cookies);
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
