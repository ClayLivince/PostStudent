package xyz.cyanclay.buptallinone.network.info;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import xyz.cyanclay.buptallinone.R;

public class InfoCategory {

    public String name;
    public String id;
    public ArrayList<InfoCategory> subCategory;

    private InfoCategory() {
    }

    private static InfoCategory category = new InfoCategory();

    public static InfoCategory getRootCategory() {
        return category;
    }

    public static void init(Context context) {
        InputStream in = context.getResources().openRawResource(R.raw.info_announcer);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Gson gson = new Gson();
        category = gson.fromJson(reader, InfoCategory.class);
    }

    public ArrayList<String> getSubNames() {
        ArrayList<String> list = new ArrayList<>();
        if (subCategory != null) {
            for (InfoCategory subCate : subCategory) {
                list.add(subCate.name);
            }
        }
        return list;
    }
}
