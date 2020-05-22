package xyz.cyanclay.buptallinone.ui.info;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoCategory;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItem;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItems;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginTask;

import static xyz.cyanclay.buptallinone.network.info.InfoCategory.INNER_CONTROL_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.NOTICE_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.PARTY_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_NEWS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_NOTICE;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_OVERTNESS;

public class ItemListFragment extends Fragment {
    private boolean infoLoaded = false;
    private View root = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.fragment_info_item_list, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!infoLoaded)
            loadInfo(root, this, false);
    }

    private static void loadInfo(final View root, final ItemListFragment fragment, final boolean isRefresh) {

        final Context context = fragment.getContext();
        final MainActivity activity = (MainActivity) fragment.getActivity();
        final Map<InfoCategory, LinearLayout> ref = new HashMap<InfoCategory, LinearLayout>() {{
            put(SCHOOL_NOTICE, (LinearLayout) root.findViewById(R.id.NoticeContainer));
            put(SCHOOL_NEWS, (LinearLayout) root.findViewById(R.id.NewsContainer));
            put(PARTY_OVERTNESS, (LinearLayout) root.findViewById(R.id.PartyContainer));
            put(SCHOOL_OVERTNESS, (LinearLayout) root.findViewById(R.id.SchoolOvertContainer));
            put(INNER_CONTROL_OVERTNESS, (LinearLayout) root.findViewById(R.id.InnerControlContainer));
            put(NOTICE_OVERTNESS, (LinearLayout) root.findViewById(R.id.OvertnessContainer));
        }};

        new AsyncTask<Void, Void, InfoItems[]>() {
            LoginException exception = null;
            @Override
            protected InfoItems[] doInBackground(Void... voids) {
                NetworkManager nm = null;
                while (nm == null){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nm = activity.getNetworkManager();
                }
                InfoItems[] items = new InfoItems[6];
                for (InfoCategory cate : InfoCategory.values()) {
                    try {
                        items[cate.ordinal()] = nm.infoManager.parseMainpage(cate);
                    } catch (IOException e) {
                        cancel(true);
                        e.printStackTrace();
                    } catch (LoginException e) {
                        cancel(true);
                        exception = e;
                    }
                }
                for (InfoItems itemList : items) {
                    if (itemList == null) {
                        cancel(true);
                    }
                }

                return items;
            }

            @Override
            protected void onPostExecute(final InfoItems[] infoItems) {
                super.onPostExecute(infoItems);
                for (InfoCategory cate : InfoCategory.values()) {

                    Objects.requireNonNull(ref.get(cate)).removeAllViews();

                    for (int i = 0; i <= 5; i++) {
                        final View itemView = View.inflate(context, R.layout.fragment_info_item, null);
                        final InfoItem item = infoItems[cate.ordinal()].get(i);
                        itemView.setTag(item);
                        ((TextView) itemView.findViewById(R.id.textViewItemTitle)).setText(item.title);
                        ((TextView) itemView.findViewById(R.id.textViewItemTime)).setText(item.time);

                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemDetailViewModel vm = ViewModelProviders.of(activity).get(ItemDetailViewModel.class);
                                vm.setItem(item);
                                Navigation.findNavController(activity.findViewById(R.id.nav_host_fragment))
                                        .navigate(R.id.action_to_nav_info_item_detail);
                            }
                        });

                        Objects.requireNonNull(ref.get(cate)).addView(itemView);
                    }
                }
                if (isRefresh) {
                    Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
                }

                root.findViewById(R.id.layoutItemLists).setVisibility(View.VISIBLE);
                fragment.infoLoaded = true;
            }

            @Override
            protected void onCancelled() {
                root.findViewById(R.id.layoutItemLists).setVisibility(View.GONE);
                if (exception == null)
                    Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
                else LoginTask.handleStatus(activity, root, exception.status);
            }
        }.execute();
    }
}