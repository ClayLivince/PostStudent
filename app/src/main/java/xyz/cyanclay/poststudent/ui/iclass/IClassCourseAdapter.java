package xyz.cyanclay.poststudent.ui.iclass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.iclass.IClassCourse;
import xyz.cyanclay.poststudent.ui.components.EFLRecyclerAdapter;
import xyz.cyanclay.poststudent.ui.components.EFLReloadable;

public class IClassCourseAdapter extends EFLRecyclerAdapter<IClassCourse> {

    private final EFLReloadable fragment;

    public IClassCourseAdapter(Context context, EFLReloadable fragment) {
        super(context);
        this.fragment = fragment;
    }


    @Override
    public int getEmptyCaption() {
        return R.string.load_failed_sim;
    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CourseHolder) {
            View v = holder.itemView;
            IClassCourse course = list.get(position);
            ((TextView) v.findViewById(R.id.textViewIClassCourseName)).setText(course.getCourseName());
            ((TextView) v.findViewById(R.id.textViewIClassCourseTeacher)).setText(course.getTeacherName());
            ((TextView) v.findViewById(R.id.textViewIClassCourseID)).setText(course.getCourseID());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseHolder(LayoutInflater.from(context).inflate(
                R.layout.piece_iclass_course, parent, false));
    }

    @Override
    public void onImageClicked() {
        fragment.reload();
    }
}

class CourseHolder extends RecyclerView.ViewHolder {
    public CourseHolder(@NonNull View itemView) {
        super(itemView);
    }
}
