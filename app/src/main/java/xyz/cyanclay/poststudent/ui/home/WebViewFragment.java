package xyz.cyanclay.poststudent.ui.home;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.LinkedList;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.NetworkManager;

public class WebViewFragment extends Fragment {

    private View root;
    private String url;

    public WebViewFragment() {
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
        root = inflater.inflate(R.layout.fragment_web_view, container, false);
        return root;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SwipeRefreshLayout srl = root.findViewById(R.id.srlWebView);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        srl.setRefreshing(true);

        final WebView webView = root.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);

        final ProgressBar progressBar = root.findViewById(R.id.progressBarWebView);

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl(webView.getUrl());
            }
        });
        // 设置子视图是否允许滚动到顶部
        srl.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                return webView.getScrollY() > 0;
            }
        });


        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        NetworkManager nm = ((MainActivity) requireActivity()).getNetworkManager();
        LinkedList<Pair<String, String>> cookies = nm.bundleCookies();

        for (Pair<String, String> pair : cookies) {
            cookieManager.setCookie(pair.first, pair.second);
            Log.e(pair.first, pair.second);
        }

        cookieManager.flush();

        //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                srl.setRefreshing(false);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                srl.setRefreshing(true);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }
        });

        WebViewViewModel mViewModel = ViewModelProviders.of(requireActivity()).get(WebViewViewModel.class);
        mViewModel.getURL().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String info) {
                url = info;
                webView.loadUrl(url);
            }
        });
    }
}