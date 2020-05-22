package xyz.cyanclay.buptallinone.ui.jwgl;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.jwgl.Course;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginTask;

public class CourseTodayFragment extends Fragment {

    private View root;
    private boolean inited = false;
    private RecyclerView rv;
    private CourseListAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CourseTodayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.fragment_course_today_list, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);
            rv = root.findViewById(R.id.listCourseToday);
            adapter = new CourseListAdapter(this);
            rv.setAdapter(adapter);
            fetchCourse(this, false);
            inited = true;
        }
        if (adapter.courseList.isEmpty()){
            fetchCourse(this, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.courseList.isEmpty()) {
            fetchCourse(this, false);
        }
    }

    private void displayCourse(List<Course> courses) {
        adapter.courseList = courses;
        adapter.notifyDataSetChanged();
    }

    private static void fetchCourse(final CourseTodayFragment fragment, final boolean force) {
        new AsyncTask<Void, Void, List<Course>>() {
            LoginException exception = null;
            @Override
            protected List<Course> doInBackground(Void... voids) {
                NetworkManager nm = null;
                MainActivity activity = (MainActivity) fragment.getActivity();
                while (nm == null){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nm = activity.getNetworkManager();
                }
                try {
                    return nm.jwglManager.getClassToday(force);
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
            protected void onPostExecute(List<Course> list) {
                super.onPostExecute(list);
                fragment.displayCourse(list);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                fragment.rv.setVisibility(View.GONE);
                if (exception == null)
                    Snackbar.make(fragment.root, "加载今日课程表失败！", Snackbar.LENGTH_LONG).show();
                else LoginTask.handleStatus(fragment.getActivity(), fragment.root, exception.status);
            }
        }.execute();
    }
}
