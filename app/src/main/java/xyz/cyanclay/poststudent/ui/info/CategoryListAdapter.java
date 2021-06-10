package xyz.cyanclay.poststudent.ui.info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItem;
import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItems;

public class CategoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * static ViewHolder Types
     */
    private static final int footType = 1;
    private static final int normalType = 0;
    private static final int headerType = -1;

    private InfoItems items;

    private boolean fadeTips = false;

    private MainActivity activity;

    CategoryListAdapter(MainActivity activity) {
        this.activity = activity;
    }

    void setItems(InfoItems items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items == null ? 1 : items.size() + 1;
    }

    // 根据条目位置返回ViewType，以供onCreateViewHolder方法内获取不同的Holder
    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return footType;
        } else {
            return normalType;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == headerType) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_info_recycler_header, parent, false));
        } else if (viewType == normalType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.piece_info_item, parent, false));
        } else {
            return new FootHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.piece_recycler_bottom, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            final InfoItem item = items.get(position);
            holder.itemView.setTag(item);

            ((TextView) holder.itemView.findViewById(R.id.textViewItemTitle)).setText(item.title);
            ((TextView) holder.itemView.findViewById(R.id.textViewItemTime)).setText(item.time);
            ((TextView) holder.itemView.findViewById(R.id.textViewItemAnnouncer)).setText(item.announcer);
            holder.itemView.findViewById(R.id.textViewItemAnnouncer).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.cardItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemDetailViewModel vm = ViewModelProviders.of(activity).get(ItemDetailViewModel.class);
                    vm.setItem(item);
                    Navigation.findNavController(activity.findViewById(R.id.nav_host_fragment))
                            .navigate(R.id.action_to_nav_info_item_detail);
                }
            });
        } else {
            if (items != null) {
                ((FootHolder) holder).tips.setVisibility(View.VISIBLE);
                if (!items.bottom) {
                    fadeTips = false;
                    if (items.size() > 0) {
                        ((FootHolder) holder).tips.setText("正在加载...");
                        ((FootHolder) holder).pb.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (items.size() > 0) {
                        ((FootHolder) holder).tips.setText("没有更多数据了");
                        ((FootHolder) holder).pb.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    boolean isFadeTips() {
        return fadeTips;
    }

    InfoItems getItems() {
        return items;
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    static class FootHolder extends RecyclerView.ViewHolder {
        private TextView tips;
        private ProgressBar pb;

        FootHolder(View itemView) {
            super(itemView);
            tips = itemView.findViewById(R.id.textViewLoadingTips);
            pb = itemView.findViewById(R.id.progressBarRecyclerBottom);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        ItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
