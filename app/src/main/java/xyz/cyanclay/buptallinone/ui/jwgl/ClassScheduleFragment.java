package xyz.cyanclay.buptallinone.ui.jwgl;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.jwgl.Course;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginTask;

public class ClassScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;
    private boolean inited = false;

    private SwipeRefreshLayout srl;
    private RelativeLayout rl;
    private Spinner spinnerWeek;
    private ArrayAdapter<CharSequence> adapter;
    private boolean adapterUpdated = false;

    private NetworkManager nm;

    private int width;
    private int aveWidth;

    private int defaultHeight;
    private int courseNum = 13;

    private List<Course> courses;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_class_schedule, container, false);
        srl = root.findViewById(R.id.srlSchedule);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        rl = root.findViewById(R.id.rl_course);
        spinnerWeek = root.findViewById(R.id.spinnerScheduleWeek);
        nm = ((MainActivity) getActivity()).getNetworkManager();

        context = getContext();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!inited) {
            initFrame();
            initSpinner();
            taskFetchCourses(this, -1, false);

            srl.setOnRefreshListener(this);

            inited = true;
        }
    }

    @Override
    public void onRefresh() {
        rl.removeAllViews();
        initFrame();
        taskFetchCourses(ClassScheduleFragment.this,
                spinnerWeek.getSelectedItemPosition() + 1, true);
    }

    private void initSpinner() {
        CharSequence[] weeks = new CharSequence[25];
        for (int i = 1; i <= 25; i++) {
            weeks[i - 1] = "第" + i + "周";
        }
        adapter = new ArrayAdapter<>(context, R.layout.spinner_schedule_week);
        adapter.addAll(weeks);
        spinnerWeek.setAdapter(adapter);

        spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rl.removeAllViews();
                initFrame();
                taskFetchCourses(ClassScheduleFragment.this,
                        spinnerWeek.getSelectedItemPosition() + 1, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateAdapter(int week) {
        CharSequence item = adapter.getItem(week - 1);
        CharSequence newitem = item + "(本周)";
        adapter.remove(item);
        adapter.insert(newitem, week - 1);
        spinnerWeek.setSelection(week - 1);
    }

    private int generateID(int type, int week, int section) {
        return type * 1000 + week * 15 + section;
    }

    private void initFrame() {

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        width = dm.widthPixels;
        aveWidth = width / 8;
        int height = dm.heightPixels;
        defaultHeight = height / courseNum;
        defaultHeight = 150;
        CharSequence[] weekdays = context.getResources().getTextArray(R.array.weekdays);

        for (int section = 0; section <= courseNum; section++) {
            for (int week = 0; week <= 7; week++) {
                BorderTextView textView = new BorderTextView(context);
                textView.setId(generateID(1, week, section));

                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        defaultHeight);

                textView.setGravity(Gravity.CENTER);

                if (week == 0) {
                    textView.setTextAppearance(context, R.style.courseTableText);
                    textView.setBackground(getResources().getDrawable(R.drawable.schedule_first_column));
                    textView.setText(String.valueOf(section));
                    rp.width = aveWidth * 3 / 4;

                    if (section == 0) {
                        rp.addRule(RelativeLayout.ALIGN_PARENT_START);
                        rp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        rp.height = defaultHeight / 2;
                        textView.setText("星期");
                    } else
                        rp.addRule(RelativeLayout.BELOW, generateID(1, week, section - 1));
                } else {
                    if (section == 0) {
                        rp.height = defaultHeight / 2;
                        textView.setTextAppearance(context, R.style.courseTableText);
                        textView.setBackground(getResources().getDrawable(R.drawable.schedule_first_column));
                        textView.setText(weekdays[week - 1]);
                    }
                    rp.addRule(RelativeLayout.RIGHT_OF, generateID(1, week - 1, section));
                    rp.addRule(RelativeLayout.ALIGN_TOP, generateID(1, week - 1, section));
                }

                textView.setLayoutParams(rp);

                rl.addView(textView);
            }
        }
    }

    private void displayCourse() {
        Collections.sort(courses);

        int[] colors = new int[]{R.color.courseTable1, R.color.courseTable2,
                R.color.courseTable3, R.color.courseTable4, R.color.courseTable5};

        for (Course course : courses) {

            TextView courseInfo = new TextView(context);
            courseInfo.setId(generateID(2, course.day, course.startSection));
            courseInfo.setTextColor(Color.WHITE);
            courseInfo.setTextSize(12);
            courseInfo.setGravity(Gravity.CENTER);
            String text = course.courseName + "@" + course.classRoom;
            courseInfo.setText(text);

            CardView courseCard = new CardView(context);
            courseCard.addView(courseInfo);
            courseCard.setCardElevation(5f);
            courseCard.setRadius(10f);

            courseCard.setBackgroundColor(context.getResources().getColor(
                    colors[(((course.startSection * 8) + course.day) % colors.length)]));

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(aveWidth,
                    (course.endSection - course.startSection + 1) * defaultHeight);
            lp.addRule(RelativeLayout.ALIGN_LEFT, generateID(1, course.day, course.startSection));
            lp.addRule(RelativeLayout.ALIGN_TOP, generateID(1, course.day, course.startSection));

            courseCard.setLayoutParams(lp);

            rl.addView(courseCard);
        }
    }
    /*

    private void displayCoursezzz(){
        for(Map.Entry<String, List<Course>> entry: courseInfoMap.entrySet())
        {

                    //记录顶层课程在cInfoList中的索引位置
                    final int upperCourseIndex = index;
                    // 动态生成课程信息TextView
                    TextView courseInfo = new TextView(this.getContext());
                    courseInfo.setId(1000 + upperCourse.day * 100 + upperCourse.startSection * 10 + upperCourse.getCid());//设置id区分不同课程
                    int id = courseInfo.getId();
                    textviewCourseMap.put(id, cInfoList);
                    courseInfo.setText(String.format("%s\n@%s", upperCourse.courseName, upperCourse.classRoom));
                    //该textview的高度根据其节数的跨度来设置
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                            aveWidth * 31 / 32,
                            (defaultHeight - 5) * 2 + (upperCourse.endSection - upperCourse.startSection - 1) * defaultHeight);
                    //textview的位置由课程开始节数和上课的时间（day of week）确定
                    rlp.topMargin = 5 + (upperCourse.startSection - 1) * defaultHeight;
                    rlp.leftMargin = 1;
                    // 前面生成格子时的ID就是根据Day来设置的，偏移由这节课是星期几决定
                    rlp.addRule(RelativeLayout.RIGHT_OF, upperCourse.day);
                    //字体居中中
                    courseInfo.setGravity(Gravity.CENTER);
                    //选择一个颜色背景
                    int colorIndex = ((upperCourse.startSection - 1) * 8 + upperCourse.day) % (background.length - 1);
                    courseInfo.setBackgroundResource(background[colorIndex]);
                    courseInfo.setTextSize(12);
                    courseInfo.setLayoutParams(rlp);
                    courseInfo.setTextColor(Color.WHITE);
                    //设置不透明度
                    courseInfo.getBackground().setAlpha(200);
                    // 设置监听事件
                    courseInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Log.v("text_view", String.valueOf(arg0.getId()));
                            Map<Integer, List<Course>> map = textviewCourseMap;
                            final List<Course> tempList = map.get(arg0.getId());
                            if(tempList.size() > 1)
                            {
                                //如果有多个课程，则设置点击弹出gallery 3d 对话框
                                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View galleryView = layoutInflater.inflate(R.layout.info_gallery_layout, null);
                                final Dialog coursePopupDialog = new AlertDialog.Builder(ClassScheduleFragment.this.getContext()).create();
                                coursePopupDialog.setCanceledOnTouchOutside(true);
                                coursePopupDialog.setCancelable(true);
                                coursePopupDialog.show();
                                WindowManager.LayoutParams params = coursePopupDialog.getWindow().getAttributes();
                                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                                coursePopupDialog.getWindow().setAttributes(params);
                                CourseAdapter adapter = new CourseAdapter(CourseActivity.this, tempList, screenWidth, cw);
                                InfoGallery gallery = (InfoGallery) galleryView.findViewById(R.id.info_gallery);
                                gallery.setSpacing(10);
                                gallery.setAdapter(adapter);
                                gallery.setSelection(upperCourseIndex);
                                gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                                        Course courseInfo = tempList.get(arg2);
                                        Intent intent = new Intent();
                                        Bundle mBundle = new Bundle();
                                        mBundle.putSerializable("courseInfo", courseInfo);
                                        intent.putExtras(mBundle);
                                        intent.setClass(CourseActivity.this, CourseDetailInfoActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
                                        coursePopupDialog.dismiss();
                                        finish();
                                    }
                                });
                                coursePopupDialog.setContentView(galleryView);
                            }
                            else
                            {
                                Intent intent = new Intent();
                                Bundle mBundle = new Bundle();
                                mBundle.putSerializable("courseInfo", tempList.get(0));
                                intent.putExtras(mBundle);
                                intent.setClass(CourseActivity.this, CourseDetailInfoActivity.class);
                                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                                startActivity(intent);
                                finish();
                            }

                        }

                    });
                    rl.addView(courseInfo);
                    courseTextViewList.add(courseInfo);

                    upperCourse = null;
                }
            } while(list.size() < lastListSize && list.size() != 0);
        }

    }

     */

    private static void taskFetchCourses(final ClassScheduleFragment csf, final int week, final boolean forceRefresh) {
        final int[] currentWeek = new int[1];
        new AsyncTask<Void, Void, List<Course>>() {
            LoginException exception = null;
            @Override
            protected List<Course> doInBackground(Void... voids) {
                try {
                    currentWeek[0] = csf.nm.jwglManager.getWeek();
                    return csf.nm.jwglManager.getClassWeek(week, forceRefresh);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                } catch (LoginException e){
                    exception = e;
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Course> courses) {
                super.onPostExecute(courses);
                csf.courses = courses;
                csf.displayCourse();
                if (!csf.adapterUpdated) {
                    csf.updateAdapter(currentWeek[0]);
                    csf.adapterUpdated = true;
                }
                if (csf.srl.isRefreshing()) {
                    csf.srl.setRefreshing(false);
                    Snackbar.make(csf.root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                if (csf.srl.isRefreshing()) {
                    csf.srl.setRefreshing(false);
                }
                if (exception != null)
                    Snackbar.make(csf.root, R.string.load_failed, Snackbar.LENGTH_SHORT).show();
                else LoginTask.handleStatus(csf.getActivity(), csf.root, exception.status);
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP) {
                        Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment))
                                .popBackStack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
}
