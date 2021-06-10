package xyz.cyanclay.poststudent.ui.info;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.info.InfoCategory;
import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItem;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.network.login.LoginTask;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;
import xyz.cyanclay.poststudent.ui.home.WebViewViewModel;
import xyz.cyanclay.poststudent.util.Utils;

public class ItemDetailFragment extends Fragment {

    private InfoItem item;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_info_detail, container, false);

        ItemDetailViewModel mViewModel = ViewModelProviders.of(requireActivity()).get(ItemDetailViewModel.class);
        mViewModel.getItem().observe(getViewLifecycleOwner(), new Observer<InfoItem>() {
            @Override
            public void onChanged(InfoItem infoItem) {
                item = infoItem;
                parseItem(ItemDetailFragment.this, item, false);
            }
        });

        SwipeRefreshLayout srl = root.findViewById(R.id.srlItemDetail);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parseItem(ItemDetailFragment.this, item, true);
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
        } else if (item.getItemId() == R.id.action_open_in_webview) {
            openInWebView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareItem() {
        if (item.category.id.equals(InfoCategory.getRootCategory().getSubCategory(0).id) |
                item.category.id.equals(InfoCategory.getRootCategory().getSubCategory(1).id)) {
            MainActivity activity = (MainActivity) requireActivity();
            activity.getNetworkManager().shareManager.share(item, activity);
        } else {
            Snackbar.make(root, "仅能分享校内通知和校内新闻哦~", Snackbar.LENGTH_LONG).show();
        }
    }

    private void openInWebView() {
        WebViewViewModel viewModel = ViewModelProviders.of(requireActivity()).get(WebViewViewModel.class);

        viewModel.setUrl(item.getUrl());

        Navigation.findNavController(requireActivity().findViewById(R.id.nav_host_fragment))
                .navigate(R.id.action_to_nav_web_view);
        /*
        Fragment mMainNavFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //获取指定的fragment
        Fragment fragment = mMainNavFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (fragment instanceof MediaDetailsFragment) {
            return (MediaDetailsFragment) fragment;
        }
        ItemDetailViewModel vm = ViewModelProviders.of(activity).get(ItemDetailViewModel.class);
                    vm.setItem(item);
         */

    }

    private void layoutItem() {

        ((TextView) root.findViewById(R.id.textViewItemTitle)).setText(item.titleFull);
        ((TextView) root.findViewById(R.id.textViewItemAnnouncer)).setText(item.announcer);
        ((TextView) root.findViewById(R.id.textViewItemTime)).setText(item.time);
        ((TextView) root.findViewById(R.id.textViewItemContent)).setText(item.contentSpanned);


        ((SwipeRefreshLayout) root.findViewById(R.id.srlItemDetail)).setRefreshing(false);

        root.findViewById(R.id.layoutItem).setVisibility(View.VISIBLE);

        if (!item.attachments.isEmpty()) {
            LinearLayout container = root.findViewById(R.id.layoutAtachment);
            for (final String name : item.attachments.keySet()) {
                TextView textView = new TextView(requireContext());
                textView.setText(String.format(" ◈ %s", name));
                textView.setClickable(true);
                textView.setPadding(0, 15, 0, 15);
                textView.setTextSize(12);
                final String url = item.attachments.get(name);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(requireContext(), String.format((String) getText(R.string.start_download_attachment), name), Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(Utils.getNetworkManager((MainActivity) getActivity()))
                                .updateManager.download(url, name);
                    }
                });
                container.addView(textView);
            }
        }
    }

    private static void parseItem(final ItemDetailFragment idf, final InfoItem item, final boolean isRefresh) {
        DisplayMetrics metrics;
        metrics = idf.root.getContext().getApplicationContext().getResources().getDisplayMetrics();
        final int mWidth = metrics.widthPixels - 20;
        new TryAsyncTask<Void, Void, InfoItem>() {
            LoginException exception = null;

            @Override
            protected InfoItem doInBackground(Void... voids) {
                try {
                    item.parseContentSpanned(mWidth, isRefresh);
                } catch (LoginException e) {
                    exception = e;
                    cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return item;
            }

            @Override
            protected void postExecute(InfoItem item) {
                idf.layoutItem();
                if (isRefresh) {
                    Snackbar.make(idf.root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void cancelled(InfoItem infoItem) throws Exception {
                super.onCancelled();
                ((SwipeRefreshLayout) idf.root.findViewById(R.id.srlItemDetail)).setRefreshing(false);
                if (exception == null)
                    Snackbar.make(idf.root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
                else LoginTask.handleStatus(idf.getActivity(), idf.root, exception.status);
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
        View view = requireView();
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP) {
                        Navigation.findNavController(requireActivity().findViewById(R.id.nav_host_fragment))
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
