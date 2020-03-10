package xyz.cyanclay.buptallinone.ui.info;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.InfoManager;
import xyz.cyanclay.buptallinone.network.NetworkManager;

public class ItemDetailFragment extends Fragment {

    private InfoManager.InfoItem item;
    private View root;

    public static ItemDetailFragment newInstance() {
        ItemDetailFragment fragment = new ItemDetailFragment();
        return fragment;
    }

    private ItemDetailViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_item_detail, container, false);

        final MainActivity activity = (MainActivity) getActivity();
        final NetworkManager nm = activity.getNetworkManager();

        SwipeRefreshLayout srl = root.findViewById(R.id.srl);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parseItem(root, item, true);
            }
        });

        parseItem(root, item, false);

        return root;
    }

    private static void parseItem(final View root, final InfoManager.InfoItem item, final boolean isRefresh) {
        final ProgressBar pb = root.findViewById(R.id.progressBarItemDetail);
        new AsyncTask<Void, Void, InfoManager.InfoItem>() {
            @Override
            protected InfoManager.InfoItem doInBackground(Void... voids) {
                try {
                    item.parseContent();
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return item;
            }

            @Override
            protected void onPostExecute(InfoManager.InfoItem item) {
                pb.setVisibility(View.GONE);

                ((TextView) root.findViewById(R.id.textViewItemTitle)).setText(item.titleFull);
                ((TextView) root.findViewById(R.id.textViewItemAnnouncer)).setText(item.category);
                ((TextView) root.findViewById(R.id.textViewItemTime)).setText(item.time);
                ((TextView) root.findViewById(R.id.textViewItemContent)).setText(item.content);

                if (isRefresh) {
                    ((SwipeRefreshLayout) root.findViewById(R.id.srl)).setRefreshing(false);
                    Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_LONG);
                }

                root.findViewById(R.id.layoutItem).setVisibility(View.VISIBLE);
                super.onPostExecute(item);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                pb.setVisibility(View.GONE);
                ((SwipeRefreshLayout) root.findViewById(R.id.srl)).setRefreshing(false);
                Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }

    public void setItem(InfoManager.InfoItem item) {
        this.item = item;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ItemDetailViewModel.class);
        // TODO: Use the ViewModel
    }

}
