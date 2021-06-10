package xyz.cyanclay.poststudent.ui.iclass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.iclass.IClassActivity;
import xyz.cyanclay.poststudent.ui.components.EFLRecyclerAdapter;
import xyz.cyanclay.poststudent.ui.components.EFLReloadable;

class IClassNoticeAdapter extends EFLRecyclerAdapter<IClassActivity> {

    IClassNoticeAdapter(Context context, EFLReloadable fragment) {
        super(context, fragment);
    }

    @Override
    public int getEmptyCaption() {
        return R.string.load_failed_sim;
    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActivityHolder) {
            View v = holder.itemView;
            IClassActivity activity = list.get(position);
            ((TextView) v.findViewById(R.id.textViewNoticeContent)).setText(activity.getValidContent());
            ((TextView) v.findViewById(R.id.textViewNoticeFrom)).setText(activity.getValidFrom());
            ((TextView) v.findViewById(R.id.textViewNoticeTime)).setText(activity.getDueTime());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActivityHolder(LayoutInflater.from(context).inflate(
                R.layout.piece_iclass_notice, parent, false));
    }

    @Override
    public void onImageClicked() {
        fragment.reload();
    }
}

class ActivityHolder extends RecyclerView.ViewHolder {
    public ActivityHolder(@NonNull View itemView) {
        super(itemView);
    }
}
