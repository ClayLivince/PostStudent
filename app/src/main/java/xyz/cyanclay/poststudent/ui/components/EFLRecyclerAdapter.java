package xyz.cyanclay.poststudent.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import xyz.cyanclay.poststudent.R;

/**
 * EFL refers to Empty, Failed, Loading
 * <p>
 * An universal RecyclerView adapter that can handle loading, empty, failed conditions
 *
 * @param <T> data type;
 */
public abstract class EFLRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> list = new LinkedList<>();
    protected Context context;

    protected static final int LOADING_TYPE = 1;
    protected static final int EMPTY_TYPE = 2;
    protected static final int FAILED_TYPE = 3;

    protected boolean isLoading = true;
    protected boolean isFailed = false;

    protected EFLReloadable fragment;

    public EFLRecyclerAdapter(Context context) {
        this.context = context;
    }

    public EFLRecyclerAdapter(Context context, EFLReloadable reloadable) {
        this.context = context;
        this.fragment = reloadable;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading)
            return LOADING_TYPE;
        else if (isFailed)
            return FAILED_TYPE;
        else if (list.isEmpty())
            return EMPTY_TYPE;
        else
            return getCustomViewType(position);
    }

    public void setList(List<T> list) {
        this.list = list;
        this.notifyDataSetChanged();
        isLoading = false;
    }

    public List<T> getList() {
        return list;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }

    @Override
    public int getItemCount() {
        if (isLoading | isFailed)
            return 1;
        else if (list.isEmpty())
            return 1;
        else
            return getCustomItemCount();
    }

    public int getCustomItemCount() {
        return list.size();
    }

    public int getCustomViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PicHolder) {
            PicHolder picHolder = (PicHolder) holder;
            picHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onImageClicked();
                }
            });
            switch (picHolder.type) {
                case EMPTY_TYPE: {
                    picHolder.caption.setText(getEmptyCaption());
                    picHolder.image.setImageResource(R.drawable.empty);
                    break;
                }
                case FAILED_TYPE: {
                    picHolder.caption.setText(R.string.load_failed_sim);
                    picHolder.image.setImageResource(R.drawable.load_failed);
                    break;
                }
                case LOADING_TYPE: {
                    picHolder.caption.setText(R.string.loading_sim);
                    picHolder.image.setImageResource(R.drawable.loading);
                    final MediaController mc = new MediaController(context);
                    mc.setMediaPlayer((GifDrawable) picHolder.image.getDrawable());
                    mc.setAnchorView(picHolder.image);
                    break;
                }
            }
        } else onBindCustomHolder(holder, position);
    }

    public void onImageClicked() {
    }

    public abstract @StringRes
    int getEmptyCaption();

    public abstract void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case LOADING_TYPE: {
                return new PicHolder(LayoutInflater.from(context)
                        .inflate(R.layout.piece_loading, parent, false), LOADING_TYPE);
            }
            case EMPTY_TYPE: {
                return new PicHolder(LayoutInflater.from(context)
                        .inflate(R.layout.piece_loading, parent, false), EMPTY_TYPE);
            }
            case FAILED_TYPE: {
                return generateFailedHolder(parent);
            }
            default: {
                return onCreateCustomViewHolder(parent, viewType);
            }
        }
    }

    protected PicHolder generateFailedHolder(@NonNull ViewGroup parent) {
        return new PicHolder(LayoutInflater.from(context)
                .inflate(R.layout.piece_loading, parent, false), FAILED_TYPE);
    }

    public abstract RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType);

    public Context getContext() {
        return context;
    }
}

class PicHolder extends RecyclerView.ViewHolder {
    int type;
    TextView caption;
    GifImageView image;

    public PicHolder(@NonNull View itemView, int type) {
        super(itemView);
        this.type = type;
        image = itemView.findViewById(R.id.imageViewLoading);
        caption = itemView.findViewById(R.id.textViewLoadingDescription);
    }
}
