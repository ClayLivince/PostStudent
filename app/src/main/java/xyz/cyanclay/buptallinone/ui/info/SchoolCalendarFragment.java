package xyz.cyanclay.buptallinone.ui.info;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;
import xyz.cyanclay.buptallinone.util.Utils;

public class SchoolCalendarFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private View root;
    SwipeRefreshLayout srl;

    public SchoolCalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_school_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view;
        srl = view.findViewById(R.id.srlSchoolCalendar);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        srl.setOnRefreshListener(this);
        srl.setRefreshing(true);
        fetchCalendarImg(this);
    }

    @Override
    public void onRefresh() {
        fetchCalendarImg(this);
    }

    void setCalendar(Bitmap img) {
        srl.setRefreshing(false);

        ImageView iv = root.findViewById(R.id.imageCalendar);
        iv.setImageBitmap(img);
    }

    static void fetchCalendarImg(final SchoolCalendarFragment fragment) {
        new TryAsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    NetworkManager nm = Utils.getNetworkManager((MainActivity) fragment.requireActivity());
                    assert nm != null;
                    Bitmap origin = nm.webAppManager.getCalendar();

                    int width = origin.getWidth();
                    int height = origin.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);
                    // 围绕原地进行旋转
                    Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);

                    origin.recycle();

                    int maxWidth = Utils.getScreenWidth(fragment.requireContext());

                    double ratio = (double) maxWidth / (double) newBM.getWidth();
                    double scaledHeight = (double) newBM.getHeight() * ratio;

                    if (scaledHeight <= 0) scaledHeight = 1;

                    return Bitmap.createScaledBitmap(newBM, maxWidth, (int) scaledHeight, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void postExecute(Bitmap img) throws Exception {
                fragment.setCalendar(img);
                Snackbar.make(fragment.root, R.string.fetched, BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }.execute();
    }
}