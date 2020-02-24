package xyz.cyanclay.buptallinone.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;

import xyz.cyanclay.buptallinone.CheckScoreActivity;
import xyz.cyanclay.buptallinone.constant.ImageList;
import xyz.cyanclay.buptallinone.util.Utils;
import xyz.cyanclay.buptallinone.widget.BannerPager;

import android.widget.LinearLayout.LayoutParams;

import xyz.cyanclay.buptallinone.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        /*homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        // 从布局文件中获取名叫banner_pager的横幅轮播条
        BannerPager banner = root.findViewById(R.id.banner_pager);
        // 获取横幅轮播条的布局参数
        LayoutParams params = (LayoutParams) banner.getLayoutParams();
        params.height = (int) (Utils.getScreenWidth(getContext()) * 250f / 640f);
        // 设置横幅轮播条的布局参数
        banner.setLayoutParams(params);
        // 设置横幅轮播条的广告图片队列
        banner.setImage(ImageList.getDefault());
        // 设置横幅轮播条的广告点击监听器
        // banner.setOnBannerListener(this);
        // 开始广告图片的轮播滚动
        banner.start();

        //root.findViewById(R.id.menu_calan).setOnClickListener(listener_calan);//校历
        root.findViewById(R.id.menu_course).setOnClickListener(listener_course);//课表
        //root.findViewById(R.id.menu_zfxt).setOnClickListener(listener_zfxt);//正方系统
        //root.findViewById(R.id.menu_library).setOnClickListener(listener_library);//图书借阅
        //root.findViewById(R.id.menu_love).setOnClickListener(listener_love);//表白墙
        //root.findViewById(R.id.menu_money).setOnClickListener(listener_money);//消费记录
        //root.findViewById(R.id.menu_yellow).setOnClickListener(listener_yellow);//学校黄历
        //root.findViewById(R.id.menu_more).setOnClickListener(listener_more);//更多功能
        return root;
    }

    View.OnClickListener listener_course = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CheckScoreActivity.class);
            startActivity(intent);
        }
    };
}