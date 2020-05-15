package xyz.cyanclay.buptallinone.ui.info;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoCategory;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItem;

public class ItemDetailFragment extends Fragment {

    private InfoItem item;
    private View root;

    static ItemDetailFragment newInstance() {
        return new ItemDetailFragment();
    }

    private ItemDetailViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_item_detail, container, false);

        mViewModel = ViewModelProviders.of(getActivity()).get(ItemDetailViewModel.class);
        mViewModel.getItem().observe(getViewLifecycleOwner(), new Observer<InfoItem>() {
            @Override
            public void onChanged(InfoItem infoItem) {
                item = infoItem;
                parseItem(root, item, false);
            }
        });
        final MainActivity activity = (MainActivity) getActivity();
        final NetworkManager nm = activity.getNetworkManager();

        SwipeRefreshLayout srl = root.findViewById(R.id.srlItemDetail);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parseItem(root, item, true);
            }
        });

        srl.setRefreshing(true);

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.share, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share_to) {
            shareItem();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareItem() {
        if (item.category == InfoCategory.SCHOOL_NOTICE | item.category == InfoCategory.SCHOOL_NEWS) {
            MainActivity activity = (MainActivity) getActivity();
            activity.getNetworkManager().shareManager.share(item, activity);
        } else {
            Snackbar.make(root, "仅能分享校内通知和校内新闻哦~", Snackbar.LENGTH_LONG).show();
        }
    }

    private static void parseItem(final View root, final InfoItem item, final boolean isRefresh) {
        DisplayMetrics metrics;
        metrics = root.getContext().getApplicationContext().getResources().getDisplayMetrics();
        final int mWidth = metrics.widthPixels - 20;
        new AsyncTask<Void, Void, InfoItem>() {
            @Override
            protected InfoItem doInBackground(Void... voids) {
                try {
                    item.parseContentSpanned(mWidth, isRefresh);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return item;
            }

            @Override
            protected void onPostExecute(InfoItem item) {

                ((TextView) root.findViewById(R.id.textViewItemTitle)).setText(item.titleFull);
                ((TextView) root.findViewById(R.id.textViewItemAnnouncer)).setText(item.announcer);
                ((TextView) root.findViewById(R.id.textViewItemTime)).setText(item.time);
                ((TextView) root.findViewById(R.id.textViewItemContent)).setText(item.contentSpanned);

                if (isRefresh) {
                    Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
                }
                ((SwipeRefreshLayout) root.findViewById(R.id.srlItemDetail)).setRefreshing(false);

                root.findViewById(R.id.layoutItem).setVisibility(View.VISIBLE);
                super.onPostExecute(item);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                ((SwipeRefreshLayout) root.findViewById(R.id.srlItemDetail)).setRefreshing(false);
                Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }

    public void setItem(InfoItem item) {
        this.item = item;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP) {
                        Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment))
                                .popBackStack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

}
