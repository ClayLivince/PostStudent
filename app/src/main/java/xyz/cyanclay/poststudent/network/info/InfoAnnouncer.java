package xyz.cyanclay.poststudent.network.info;

import android.content.Context;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import xyz.cyanclay.poststudent.R;

@Deprecated
public class InfoAnnouncer {

    //detach.portal?.pen=pe1142&groupid=183105000&groupname=宣传部&action=bulletinPageList&pageIndex=3

    InfoAnnouncer(Context context) {
        this.context = context;
        initGroups();
    }

    private JSONObject groups = null;
    private Context context;

    boolean initGroups() {
        InputStream in = context.getResources().openRawResource(R.raw.info_announcer);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            groups = new JSONObject(sb.toString());
            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    String getID(int cate, int id) throws JSONException, IOException {
        if (verifyGroups()) {
            return groups.getJSONArray("category")
                    .getJSONObject(cate)
                    .getJSONArray("group")
                    .getJSONObject(id)
                    .getString("id");
        } else throw new IOException("Failed to load JSON data.");
    }

    Pair<String, String> getIDAndName(int cate, int id) throws IOException {
        try {
            if (verifyGroups()) {
                JSONObject object = groups.getJSONArray("category")
                        .getJSONObject(cate)
                        .getJSONArray("group")
                        .getJSONObject(id);
                return new Pair<>(object.getString("id"), object.getString("name"));
            }
            throw new IOException("Failed to load JSON data.");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException("JSONException when getting cate, id:" + cate + ", " + id);
        }
    }

    boolean verifyGroups() {
        if (groups != null) return true;
        else return initGroups();
    }
}
