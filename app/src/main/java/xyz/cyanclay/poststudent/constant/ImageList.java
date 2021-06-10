package xyz.cyanclay.poststudent.constant;

import java.util.ArrayList;

import xyz.cyanclay.poststudent.R;

public class ImageList {

    public static ArrayList<Integer> getDefault() {
        ArrayList<Integer> imageList = new ArrayList<Integer>();
        imageList.add(R.drawable.banner_1);
        imageList.add(R.drawable.banner_2);
        imageList.add(R.drawable.banner_3);
        imageList.add(R.drawable.banner_4);
        imageList.add(R.drawable.banner_5);
        return imageList;
    }
}
