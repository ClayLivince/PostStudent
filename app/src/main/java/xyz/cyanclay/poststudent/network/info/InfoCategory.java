package xyz.cyanclay.poststudent.network.info;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import xyz.cyanclay.poststudent.R;

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
        //Log.e("InfoCategory", category.toString());
    }

    public InfoCategory getSubCategory(int index) {
        return subCategory.get(index);
    }

    public List<String> getSubNames() {
        LinkedList<String> list = new LinkedList<>();
        if (subCategory != null) {
            for (InfoCategory subCate : subCategory) {
                list.add(subCate.name);
            }
        }
        return list;
    }

    public int getOrdinal() {
        switch (id) {
            case "1154":
                return 0;
            case "1221":
                return 1;
            case "1302":
                return 2;
            case "1303":
                return 3;
            case "1304":
                return 4;
            case "1305":
                return 5;
        }
        return -1;
    }
}
