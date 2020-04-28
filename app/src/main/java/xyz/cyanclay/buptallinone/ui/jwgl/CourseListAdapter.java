package xyz.cyanclay.buptallinone.ui.jwgl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.jwgl.Course;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.CourseHolder> {

    List<Course> courseList = new LinkedList<>();
    private CourseTodayFragment fragment;

    CourseListAdapter(CourseTodayFragment fragment) {
        super();
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public CourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseHolder(LayoutInflater.from(fragment.getContext())
                .inflate(R.layout.fragment_course_today, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CourseHolder holder, int position) {
        View v = holder.itemView;
        Course course = courseList.get(position);
        ((TextView) v.findViewById(R.id.textViewStartTime)).setText(course.startTime);
        ((TextView) v.findViewById(R.id.textViewEndTime)).setText(course.endTime);
        ((TextView) v.findViewById(R.id.textViewCourseName)).setText(course.courseName);
        ((TextView) v.findViewById(R.id.textViewClassroom)).setText(course.classRoom);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    class CourseHolder extends RecyclerView.ViewHolder {

        public CourseHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
