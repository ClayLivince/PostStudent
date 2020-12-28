package xyz.cyanclay.buptallinone.ui.jwgl.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.jwgl.Course;
import xyz.cyanclay.buptallinone.ui.components.EFLRecyclerAdapter;

public class CourseListAdapter extends EFLRecyclerAdapter<Course> {

    private CourseTodayFragment fragment;

    CourseListAdapter(CourseTodayFragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }

    @Override
    public int getEmptyCaption() {
        return R.string.empty_courses;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseHolder(LayoutInflater.from(context)
                .inflate(R.layout.piece_course_today, parent, false));

    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CourseHolder) {
            View v = holder.itemView;
            Course course = list.get(position);
            ((TextView) v.findViewById(R.id.textViewStartTime)).setText(course.startTime);
            ((TextView) v.findViewById(R.id.textViewEndTime)).setText(course.endTime);
            ((TextView) v.findViewById(R.id.textViewCourseName)).setText(course.courseName);
            ((TextView) v.findViewById(R.id.textViewClassroom)).setText(String.format("%s %s", course.classRoom, course.teacherName));
        }
    }

    @Override
    public void onImageClicked() {
        CourseTodayFragment.fetchCourse(fragment, true);
    }
}

class CourseHolder extends RecyclerView.ViewHolder {
    public CourseHolder(@NonNull View itemView) {
        super(itemView);
    }
}
