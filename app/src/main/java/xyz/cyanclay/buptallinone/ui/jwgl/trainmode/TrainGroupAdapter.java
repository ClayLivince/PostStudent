package xyz.cyanclay.buptallinone.ui.jwgl.trainmode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.jwgl.trainmode.TrainModeCourse;
import xyz.cyanclay.buptallinone.network.jwgl.trainmode.TrainModeCourseSubGroup;
import xyz.cyanclay.buptallinone.ui.components.EFLRecyclerAdapter;

class TrainGroupAdapter extends EFLRecyclerAdapter<TrainModeCourse> {

    TrainModeAdapter parent;

    TrainGroupAdapter(TrainModeAdapter parent) {
        super(parent.getContext());
        this.parent = parent;
    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CourseHolder courseHolder = (CourseHolder) holder;
        TrainModeCourse course = list.get(position);

        if (course instanceof TrainModeCourse.PublicCoursePlaceHolder) {
            TrainModeCourseSubGroup subGroup = ((TrainModeCourse.PublicCoursePlaceHolder) course).getSubGroup();
            courseHolder.setCourseName(context.getString(R.string.public_group, subGroup.getGroupName(), subGroup.size()));

        } else {
            courseHolder.setCourseName(course.courseName);
            if (course.isPassed) {
                courseHolder.setPassed();
                courseHolder.setBasePassed();
            } else if (course.isFailed) {
                courseHolder.setFailed();
                courseHolder.setBaseFailed();
            } else if (course.isTeaching) {
                courseHolder.setBaseTeaching();
            } else if (course.isSelectable) {
                courseHolder.setBaseSelectable();
            } else if (course.isBanned) {
                courseHolder.setBanned();
                courseHolder.setBaseBanned();
            }

            holder.itemView.setTag(courseHolder);
            holder.itemView.setOnClickListener(onClickListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CourseHolder holder = (CourseHolder) v.getTag();
            holder.select();
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseHolder(LayoutInflater.from(context).inflate(R.layout.piece_train_mode_course,
                parent, false), context);
    }

    @Override
    public int getEmptyCaption() {
        return R.string.empty_train_group;
    }
}

class CourseHolder extends RecyclerView.ViewHolder {

    Context context;
    boolean isSelected = false;
    boolean selectable;

    TextView courseName;
    TextView mark;
    CardView card;
    LinearLayout container;
    BaseType baseType = BaseType.BLANK;

    CourseHolder(View itemView, Context context) {
        super(itemView);

        this.context = context;
        courseName = itemView.findViewById(R.id.tv_train_mode_course_name);
        mark = itemView.findViewById(R.id.tv_train_mode_course_status);
        container = itemView.findViewById(R.id.ll_train_mode_course);
        card = (CardView) itemView;
    }

    public CardView getCard() {
        return card;
    }

    public void setCourseName(String courseName) {
        this.courseName.setText(courseName);
    }

    public void select() {
        if (isSelected)
            setNotSelected();
        else setSelected();

        isSelected = !isSelected;
    }

    public void setSelected() {
        final float targetElevation = 100f;
        final float targetRadius = 80f;
        final View borderContainer = card.findViewById(R.id.ll_border_container);
        borderContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                /*
                if (interpolatedTime == 1){
                    // Do this after expanded
                }

                 */
                card.setRadius(interpolatedTime * targetRadius);
                card.setCardElevation(interpolatedTime * targetElevation);
                if (interpolatedTime <= 1f)
                    borderContainer.getBackground().setAlpha((int) (interpolatedTime * 255f));

                //container.setElevation();
                //card.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(500);
        a.setInterpolator(new AccelerateDecelerateInterpolator());

        card.startAnimation(a);
    }

    public void setNotSelected() {
        final float targetElevation = 100f;
        final float targetRadius = 80f;
        final View borderContainer = card.findViewById(R.id.ll_border_container);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                card.setRadius(targetRadius - (interpolatedTime * targetRadius));
                card.setCardElevation(targetElevation - (interpolatedTime * targetElevation));
                try {
                    borderContainer.getBackground().setAlpha(255 - (int) (255 * interpolatedTime));
                } catch (NullPointerException ignored) {
                }
                //container.setElevation();
                //container.requestLayout();
                //card.requestLayout();
                if (interpolatedTime == 1) {
                    // Do this after expanded
                    borderContainer.setBackground(null);
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(500);
        a.setInterpolator(new AccelerateDecelerateInterpolator());

        card.startAnimation(a);
    }

    public void setPassed() {
        this.mark.setText(R.string.mark_passed);
        this.mark.setTextColor(context.getResources().getColor(R.color.colorCorrect));
    }

    public void setFailed() {
        this.mark.setText(R.string.mark_failed);
        this.mark.setTextColor(context.getResources().getColor(R.color.colorError));
    }

    public void setBanned() {
        this.mark.setText(R.string.mark_banned);
        this.mark.setTextColor(context.getResources().getColor(R.color.colorError));
    }

    public void setBasePassed() {
        this.container.setBackgroundColor(context.getResources().getColor(R.color.passed_course));
        this.baseType = BaseType.PASSED;
    }

    public void setBaseSelectable() {
        this.container.setBackgroundColor(context.getResources().getColor(R.color.selectable_course));
        this.baseType = BaseType.SELECTABLE;
    }

    public void setBaseTeaching() {
        this.container.setBackgroundColor(context.getResources().getColor(R.color.teaching_course));
        this.baseType = BaseType.TEACHING;
    }

    public void setBaseFailed() {
        this.container.setBackgroundColor(card.getResources().getColor(R.color.failed_course));
        this.baseType = BaseType.FAILED;
    }

    public void setBaseBanned() {
        this.container.setBackgroundColor(card.getResources().getColor(R.color.banned_course));
        this.baseType = BaseType.BANNED;
    }
}

enum BaseType {
    PASSED,
    FAILED,
    BANNED,
    TEACHING,
    SELECTABLE,
    BLANK
}