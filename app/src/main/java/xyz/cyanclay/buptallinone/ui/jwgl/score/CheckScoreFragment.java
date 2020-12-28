package xyz.cyanclay.buptallinone.ui.jwgl.score;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.jwgl.AverageScore;
import xyz.cyanclay.buptallinone.network.jwgl.Score;
import xyz.cyanclay.buptallinone.network.jwgl.Scores;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginTask;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;
import xyz.cyanclay.buptallinone.util.Utils;

public class CheckScoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;
    private RecyclerView rv;
    private boolean inited = false;
    private ScoreAdapter adapter;
    private Spinner spinner;
    private SwipeRefreshLayout srl;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheckScoreFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.fragment_score_list, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);
            rv = root.findViewById(R.id.score_list);
            srl = root.findViewById(R.id.srlScore);
            spinner = root.findViewById(R.id.spinnerTerm);

            adapter = new ScoreAdapter(this);
            rv.setAdapter(adapter);
            rv.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

            srl.setOnRefreshListener(this);
            srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
            srl.setRefreshing(true);
            fetchTerm(this);
            initButtonMore();
            inited = true;
        }
        if (adapter.scoreList.isEmpty()) {
            fetchScore(this, null);
        }
    }

    private void initButtonMore() {
        Button button = root.findViewById(R.id.buttonScoreMore);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.score_detail);

                LayoutInflater inflater = getLayoutInflater();
                ScrollView dialogScrollView = (ScrollView) inflater.inflate(R.layout.dialog_key_value, null);
                LinearLayout dialogView = dialogScrollView.findViewById(R.id.llKeyValueContainer);

                View weightedAvg = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) weightedAvg.findViewById(R.id.textViewDetailKey)).setText(R.string.weighted_avg);
                ((TextView) weightedAvg.findViewById(R.id.textViewDetailValue)).setText(R.string.calcing);
                weightedAvg.setTag("wAvg");
                dialogView.addView(weightedAvg);

                View buptGPA = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) buptGPA.findViewById(R.id.textViewDetailKey)).setText(R.string.bupt_gpa);
                ((TextView) buptGPA.findViewById(R.id.textViewDetailValue)).setText(R.string.calcing);
                buptGPA.setTag("buptGPA");
                dialogView.addView(buptGPA);

                View sumPoints = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) sumPoints.findViewById(R.id.textViewDetailKey)).setText(R.string.sum_points_jwgl);
                ((TextView) sumPoints.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getPoints()));
                dialogView.addView(sumPoints);

                View avg = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) avg.findViewById(R.id.textViewDetailKey)).setText(R.string.all_average_jwgl);
                ((TextView) avg.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getAvg()));
                dialogView.addView(avg);

                View weightedAvgWeb = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) weightedAvgWeb.findViewById(R.id.textViewDetailKey)).setText(R.string.weighted_avg_jwgl);
                ((TextView) weightedAvgWeb.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getWeightedAvg()));
                dialogView.addView(weightedAvgWeb);

                View avgGPAWeb = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) avgGPAWeb.findViewById(R.id.textViewDetailKey)).setText(R.string.avg_gpa_jwgl);
                ((TextView) avgGPAWeb.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getAvgGpa()));
                dialogView.addView(avgGPAWeb);

                View weightedGPAWeb = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) weightedGPAWeb.findViewById(R.id.textViewDetailKey)).setText(R.string.gpa_jwgl);
                ((TextView) weightedGPAWeb.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getWeightedGpa()));
                dialogView.addView(weightedGPAWeb);

                View rank = inflater.inflate(R.layout.piece_kv_4to5, dialogView, false);
                ((TextView) rank.findViewById(R.id.textViewDetailKey)).setText(R.string.rank_jwgl);
                ((TextView) rank.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(adapter.scoreList.getRank()));
                dialogView.addView(rank);

                builder.setView(dialogScrollView);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

                fetchAverage(dialogView, adapter.scoreList);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.scoreList.isEmpty()) {
            fetchScore(this, null);
        }
    }

    @Override
    public void onRefresh() {
        fetchScore(this, (String) spinner.getSelectedItem());
    }

    private void initSpinner(final Set<String> termNames) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.piece_dialog_dropdown, R.id.textViewDropdown);
        TreeSet<String> sortedTermNames = new TreeSet<>(termNames);
        adapter.addAll(sortedTermNames);
        spinner.setSelection(sortedTermNames.size() - 1, true);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchScore(CheckScoreFragment.this, adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private static void fetchTerm(final CheckScoreFragment fragment) {
        new TryAsyncTask<Void, Void, Set<String>>() {
            LoginException exception = null;

            @Override
            protected Set<String> doInBackground(Void... voids) {
                MainActivity activity = (MainActivity) fragment.getActivity();
                NetworkManager nm = Utils.getNetworkManager(activity);
                if (nm == null) {
                    cancel(true);
                    return new HashSet<>();
                }

                try {
                    return nm.jwglManager.getData().getTerms().keySet();
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                } catch (LoginException e) {
                    exception = e;
                    cancel(true);
                }

                return null;
            }

            @Override
            protected void postExecute(Set<String> list) {
                fragment.initSpinner(list);
            }

            @Override
            protected void cancelled() throws Exception {
                fragment.rv.setVisibility(View.GONE);
                if (exception == null)
                    Snackbar.make(fragment.root, "加载学期列表失败！", Snackbar.LENGTH_LONG).show();
                else
                    LoginTask.handleStatus(fragment.getActivity(), fragment.root, exception.status);
            }
        }.execute();
    }

    private static void fetchScore(final CheckScoreFragment fragment, final String termName) {
        fragment.srl.setRefreshing(true);
        new TryAsyncTask<Void, Void, Scores>() {
            LoginException exception = null;

            @Override
            protected Scores doInBackground(Void... voids) {
                MainActivity activity = (MainActivity) fragment.getActivity();
                NetworkManager nm = Utils.getNetworkManager(activity);
                if (nm == null) {
                    cancel(true);
                    return new Scores();
                }

                try {
                    return nm.jwglManager.getScore(termName);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                } catch (LoginException e) {
                    exception = e;
                    cancel(true);
                }

                return null;
            }

            @Override
            protected void postExecute(Scores list) {
                fragment.adapter.setScoreList(list);
                fragment.srl.setRefreshing(false);
            }

            @Override
            protected void cancelled() throws Exception {
                fragment.srl.setRefreshing(false);
                if (exception == null)
                    Snackbar.make(fragment.root, "加载成绩失败！", Snackbar.LENGTH_LONG).show();
                else
                    LoginTask.handleStatus(fragment.getActivity(), fragment.root, exception.status);
            }
        }.execute();
    }

    private static void fetchAverage(final View dialogView, final List<Score> scores) {
        new TryAsyncTask<Void, Void, AverageScore>() {
            @Override
            protected AverageScore doInBackground(Void... voids) {
                return AverageScore.calcAvg(scores);
            }

            @Override
            protected void postExecute(AverageScore averageScore) throws Exception {
                DecimalFormat df = new DecimalFormat("#0.00");
                TextView avg = dialogView.findViewWithTag("wAvg").findViewById(R.id.textViewDetailValue);
                avg.setText(df.format(averageScore.weightedAverage));
                avg.invalidate();

                TextView buptGPA = dialogView.findViewWithTag("buptGPA").findViewById(R.id.textViewDetailValue);
                buptGPA.setText(df.format(averageScore.gpaBUPT));
                buptGPA.invalidate();
            }
        }.execute();
    }
}