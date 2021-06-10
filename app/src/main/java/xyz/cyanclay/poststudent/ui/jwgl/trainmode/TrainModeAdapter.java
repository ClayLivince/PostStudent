package xyz.cyanclay.poststudent.ui.jwgl.trainmode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.jwgl.trainmode.TrainModeCourseGroup;
import xyz.cyanclay.poststudent.ui.components.EFLRecyclerAdapter;

public class TrainModeAdapter extends EFLRecyclerAdapter<TrainModeCourseGroup> {

    TrainModeFragment fragment;

    List<TrainGroupAdapter> childAdapters = new LinkedList<>();

    public TrainModeAdapter(TrainModeFragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupHolder groupHolder = (GroupHolder) holder;
        TrainModeCourseGroup group = list.get(position);

        groupHolder.setGroupName(group.getGroupName());
        groupHolder.setGroupPoint(context.getString(R.string.group_point, group.getPassedPoint(), group.getMinimumPoint()));

        TrainGroupAdapter childAdapter = new TrainGroupAdapter(this);
        groupHolder.groupContainer.setAdapter(childAdapter);
        childAdapter.setList(getList().get(position).asList());
        childAdapters.add(childAdapter);

        holder.itemView.findViewById(R.id.llGroupName).setTag(position);
        holder.itemView.findViewById(R.id.llGroupName).setOnClickListener(onClickListener);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_key_value, null);
            LinearLayout kvContainer = dialogView.findViewById(R.id.llKeyValueContainer);
            for (final TrainModeCourseGroup group : list) {

                View nameView = inflater.inflate(R.layout.piece_kv_1to1, kvContainer, false);
                ((TextView) nameView.findViewById(R.id.textViewDetailKey)).setText(R.string.group_name);
                ((TextView) nameView.findViewById(R.id.textViewDetailValue)).setText(group.getGroupName());
                kvContainer.addView(nameView);

                View IDView = inflater.inflate(R.layout.piece_kv_1to1, kvContainer, false);
                ((TextView) IDView.findViewById(R.id.textViewDetailKey)).setText(R.string.group_id);
                ((TextView) IDView.findViewById(R.id.textViewDetailValue)).setText(group.getGroupID());
                kvContainer.addView(IDView);

                View pointView = inflater.inflate(R.layout.piece_kev_1to1, kvContainer, false);
                ((TextView) pointView.findViewById(R.id.textViewDetailKey)).setText(R.string.min_point);
                ((EditText) pointView.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(group.getMinimumPoint()));
                ((EditText) pointView.findViewById(R.id.textViewDetailValue)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            group.setMinimumPoint(Float.parseFloat(s.toString()));
                        } catch (Exception e) {
                            if (!(e instanceof NumberFormatException))
                                e.printStackTrace();
                        }

                    }
                });
                kvContainer.addView(pointView);


            }

            builder.setView(dialogView);

            builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

        }
    };


    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(LayoutInflater.from(context).inflate(R.layout.piece_train_mode_group,
                parent, false), context);
    }

    @Override
    public int getEmptyCaption() {
        return R.string.empty_train_mode;
    }


}

class GroupHolder extends RecyclerView.ViewHolder {

    RecyclerView groupContainer;
    ImageButton toggleButton;
    TextView groupName;
    TextView groupPoint;
    Context context;

    GroupHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        groupContainer = itemView.findViewById(R.id.rv_train_mode_courses);
        groupName = itemView.findViewById(R.id.tv_train_mode_groupName);
        groupPoint = itemView.findViewById(R.id.tv_train_mode_groupPoint);
        toggleButton = itemView.findViewById(R.id.btn_train_mode_visibility);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContent();
            }
        });
    }

    public void setGroupName(String groupName) {
        this.groupName = itemView.findViewById(R.id.tv_train_mode_groupName);
        this.groupName.setText(groupName);
    }

    public void setGroupPoint(String groupPoint) {
        this.groupPoint = itemView.findViewById(R.id.tv_train_mode_groupPoint);
        this.groupPoint.setText(groupPoint);
    }

    void toggleContent() {
        groupContainer = itemView.findViewById(R.id.rv_train_mode_courses);
        toggleButton = itemView.findViewById(R.id.btn_train_mode_visibility);
        boolean visible = (groupContainer.getVisibility() == View.VISIBLE);
        if (visible) {
            Animation rotateAnim = AnimationUtils.loadAnimation(context, R.anim.arrow_rotate_back);
            LinearInterpolator li = new LinearInterpolator();
            rotateAnim.setInterpolator(li);
            rotateAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //toggleButton.setImageDrawable();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //toggleButton.setAnimation(rotateAnim);
            toggleButton.startAnimation(rotateAnim);

            groupContainer.setVisibility(View.GONE);
        } else {
            Animation rotateAnim = AnimationUtils.loadAnimation(context, R.anim.arrow_rotate);
            LinearInterpolator li = new LinearInterpolator();
            rotateAnim.setInterpolator(li);

            toggleButton.startAnimation(rotateAnim);

            groupContainer.setVisibility(View.VISIBLE);
        }
    }
}
