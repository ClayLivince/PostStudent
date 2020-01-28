package xyz.cyanclay.buptallinone.network;

import android.content.Context;

import com.eclipsesource.v8.V8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;

public class JwglManager {

    private VPNManager vpnManager;
    private String user;
    private String pass;

    private static final String mainURL = "http://jwgl.bupt.edu.cn/jsxsd/";
    /*

    public void login(){
        try {
            Context context = getApplicationContext();
            InputStream is= context.getResources().openRawResource(R.id.);   //获取用户名与密码加密的js代码
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                V8 runtime = V8.createV8Runtime();      //使用J2V8运行js代码并将编码结果返回
                final String encodename = runtime.executeStringScript(sb.toString()
                        + "encodeInp('"+userin.getText().toString()+"');\n");
                final String encodepwd=runtime.executeStringScript(sb.toString()+"encodeInp('"+pwdin.getText().toString()+"');\n");
                runtime.release();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     */
}
