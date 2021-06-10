package xyz.cyanclay.poststudent.ui.jwgl.score;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.jwgl.Score;
import xyz.cyanclay.poststudent.entity.jwgl.Scores;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    Scores scoreList = new Scores();
    private CheckScoreFragment fragment;

    ScoreAdapter(CheckScoreFragment fragment) {
        super();
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (scoreList.isEmpty()) {
            return 0;
        } else return 1;
    }

    public void setScoreList(Scores scoreList) {
        this.scoreList = scoreList;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1: {
                return createScoreHolder(parent);
            }
            case 0: {
                return createEmptyHolder(parent);
            }
        }
        return createEmptyHolder(parent);
    }

    private ScoreHolder createScoreHolder(@NonNull ViewGroup parent) {
        return new ScoreHolder(LayoutInflater.from(fragment.getContext())
                .inflate(R.layout.piece_score, parent, false));
    }

    private EmptyHolder createEmptyHolder(@NonNull ViewGroup parent) {
        return new EmptyHolder(LayoutInflater.from(fragment.getContext())
                .inflate(R.layout.piece_empty_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreAdapter.ViewHolder holder, final int position) {
        View v = holder.itemView;
        if (holder instanceof ScoreHolder) {
            Score score = scoreList.get(position);
            ((TextView) v.findViewById(R.id.textViewCourseName)).setText(score.courseName);
            ((TextView) v.findViewById(R.id.textViewScore)).setText(score.score);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        processScoreDetail(position);
                    } catch (Exception e) {
                        Snackbar.make(v, R.string.problem_on_display_score, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        } else if (holder instanceof EmptyHolder) {

        }
    }

    private void processScoreDetail(int position) throws IllegalAccessException {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScoreAdapter.this.fragment.getContext());
        builder.setTitle(R.string.score_detail);

        Score score = scoreList.get(position);

        LayoutInflater inflater = fragment.getLayoutInflater();
        View dialogScrollView = inflater.inflate(R.layout.dialog_key_value, null);
        LinearLayout dialogView = dialogScrollView.findViewById(R.id.llKeyValueContainer);

        View courseName = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseName.findViewById(R.id.textViewDetailKey)).setText(R.string.course_name);
        ((TextView) courseName.findViewById(R.id.textViewDetailValue)).setText(score.courseName);
        dialogView.addView(courseName);

        View courseID = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseID.findViewById(R.id.textViewDetailKey)).setText(R.string.course_id);
        ((TextView) courseID.findViewById(R.id.textViewDetailValue)).setText(score.courseID);
        dialogView.addView(courseID);

        /*
        View courseEnglish = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseEnglish.findViewById(R.id.textViewDetailKey)).setText(R.string.course_name_english);
        ((TextView) courseEnglish.findViewById(R.id.textViewDetailValue)).setText(score.courseEnglish);
        dialogView.addView(courseEnglish);

         */

        View courseType = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseType.findViewById(R.id.textViewDetailKey)).setText(R.string.course_type);
        ((TextView) courseType.findViewById(R.id.textViewDetailValue)).setText(score.courseType);
        dialogView.addView(courseType);

        View courseCategory = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseCategory.findViewById(R.id.textViewDetailKey)).setText(R.string.course_category);
        ((TextView) courseCategory.findViewById(R.id.textViewDetailValue)).setText(score.courseCategory);
        dialogView.addView(courseCategory);

        View coursePoint = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) coursePoint.findViewById(R.id.textViewDetailKey)).setText(R.string.course_point);
        ((TextView) coursePoint.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(score.point));
        dialogView.addView(coursePoint);

        View courseTerm = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) courseTerm.findViewById(R.id.textViewDetailKey)).setText(R.string.course_term);
        ((TextView) courseTerm.findViewById(R.id.textViewDetailValue)).setText(score.termID);
        dialogView.addView(courseTerm);

        View examType = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) examType.findViewById(R.id.textViewDetailKey)).setText(R.string.exam_type);
        ((TextView) examType.findViewById(R.id.textViewDetailValue)).setText(score.examType);
        dialogView.addView(examType);

        View mark = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) mark.findViewById(R.id.textViewDetailKey)).setText(R.string.mark);
        ((TextView) mark.findViewById(R.id.textViewDetailValue)).setText(score.mark);
        dialogView.addView(mark);

        View scoreMark = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) scoreMark.findViewById(R.id.textViewDetailKey)).setText(R.string.score_mark);
        ((TextView) scoreMark.findViewById(R.id.textViewDetailValue)).setText(score.scoreMark);
        dialogView.addView(scoreMark);

        View reTerm = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) reTerm.findViewById(R.id.textViewDetailKey)).setText(R.string.re_term);
        ((TextView) reTerm.findViewById(R.id.textViewDetailValue)).setText(score.reTermID);
        dialogView.addView(reTerm);

        View scoreView = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) scoreView.findViewById(R.id.textViewDetailKey)).setText(R.string.total_score);
        ((TextView) scoreView.findViewById(R.id.textViewDetailValue)).setText(score.score);
        dialogView.addView(scoreView);

        View gpaView = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
        ((TextView) gpaView.findViewById(R.id.textViewDetailKey)).setText(R.string.bupt_gpa);
        DecimalFormat df = new DecimalFormat("#0.00");
        ((TextView) gpaView.findViewById(R.id.textViewDetailValue)).setText(df.format(score.gpa));
        dialogView.addView(gpaView);

        builder.setView(dialogScrollView);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public int getItemCount() {
        if (scoreList.size() != 0)
            return scoreList.size();
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ScoreHolder extends ViewHolder {
        public ScoreHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class EmptyHolder extends ViewHolder {
        public EmptyHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}