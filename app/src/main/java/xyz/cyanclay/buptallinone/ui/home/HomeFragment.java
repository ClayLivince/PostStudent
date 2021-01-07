package xyz.cyanclay.buptallinone.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.constant.ImageList;
import xyz.cyanclay.buptallinone.util.Utils;
import xyz.cyanclay.buptallinone.widget.BannerPager;

public class HomeFragment extends Fragment {

    private View root;
    private boolean inited = false;
    private MainActivity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (!inited) {
            root = inflater.inflate(R.layout.fragment_home, container, false);
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

            SwipeRefreshLayout srl = root.findViewById(R.id.srlHome);
            srl.setEnabled(false);

            root.findViewById(R.id.menu_calan).setOnClickListener(listener_calan);//校历
            root.findViewById(R.id.menu_score).setOnClickListener(listener_score);//课表
            root.findViewById(R.id.menu_courseList).setOnClickListener(listener_course);//正方系统
            //root.findViewById(R.id.menu_library).setOnClickListener(listener_library);//图书借阅
            //root.findViewById(R.id.menu_love).setOnClickListener(listener_love);//表白墙
            //root.findViewById(R.id.menu_money).setOnClickListener(listener_money);//消费记录
            root.findViewById(R.id.menu_info).setOnClickListener(listener_info);//学校黄历
            //root.findViewById(R.id.menu_more).setOnClickListener(listener_more);//更多功能

            inited = true;
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewGroup courseToday = root.findViewById(R.id.fragmentCourseToday);
        ViewGroup infoList = root.findViewById(R.id.fragmentSchoolInfo);
        courseToday.removeAllViews();
        infoList.removeAllViews();
    }

    private View.OnClickListener listener_calan = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Navigation.findNavController(activity, R.id.nav_host_fragment)
                    .navigate(R.id.action_nav_to_Calendar);

        }
    };

    private View.OnClickListener listener_score = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (activity.getNetworkManager().jwglManager.user != null) {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.action_nav_home_to_nav_check_score);
            } else {
                showLoginPopup();
            }
        }
    };

    private View.OnClickListener listener_course = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (activity.getNetworkManager().jwglManager.user != null) {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.action_to_nav_class_schedule);
            } else {
                showLoginPopup();
            }
        }
    };

    private View.OnClickListener listener_info = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (activity.getNetworkManager().infoManager.user != null) {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.action_to_nav_info);
                NavigationView navigationView = activity.findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_info);
                DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
                drawer.closeDrawers();
            } else {
                showLoginPopup();
            }
        }
    };

    private void showLoginPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示");
        builder.setMessage("请先登录！");
        builder.setPositiveButton("去登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.action_to_nav_user_details);
            }
        });
        builder.create().show();
    }
}