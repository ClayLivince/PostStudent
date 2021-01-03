package xyz.cyanclay.buptallinone.ui.jwgl.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.jwgl.Course;
import xyz.cyanclay.buptallinone.network.jwgl.Courses;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginTask;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;
import xyz.cyanclay.buptallinone.util.Utils;

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
            adapter.setLoading(true);
            rv.setAdapter(adapter);
            fetchCourse(this, false);
            inited = true;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.getList().isEmpty())
            fetchCourse(this, false);
    }

    public void reload() {
        if (adapter.getList().isEmpty()) {
            fetchCourse(this, false);
        }
    }

    private void displayCourse(List<Course> courses) {
        adapter.setList(courses);
    }

    static void fetchCourse(final CourseTodayFragment fragment, final boolean force) {
        fragment.adapter.setLoading(true);
        fragment.adapter.setFailed(false);
        new TryAsyncTask<Void, Void, List<Course>>() {
            LoginException exception = null;

            @Override
            protected List<Course> doInBackground(Void... voids) {
                MainActivity activity = (MainActivity) fragment.getActivity();
                NetworkManager nm = Utils.getNetworkManager(activity);
                if (nm == null) {
                    cancel(true);
                    return null;
                }
                try {
                    Courses courses = nm.jwglManager.getClassToday(force);
                    ArrayList<Course> list = new ArrayList<>(courses);
                    Collections.sort(list);
                    return list;
                } catch (LoginException e) {
                    exception = e;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cancel(true);
                return null;
            }

            @Override
            protected void postExecute(List<Course> list) {
                fragment.adapter.setFailed(false);
                fragment.adapter.setLoading(false);
                fragment.displayCourse(list);
            }

            @Override
            protected void cancelled() throws Exception {
                super.onCancelled();
                fragment.rv.setVisibility(View.GONE);
                fragment.adapter.setLoading(false);
                fragment.adapter.setFailed(true);
                if (exception == null)
                    Snackbar.make(fragment.root, "加载今日课程表失败！", Snackbar.LENGTH_LONG).show();
                else
                    LoginTask.handleStatus(fragment.getActivity(), fragment.root, exception.status);
            }
        }.execute();
    }
}
