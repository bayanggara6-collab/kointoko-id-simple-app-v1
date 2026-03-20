package id.kointoko.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://kointoko.id";

    private WebView              webView;
    private ProgressBar          progressBar;
    private SwipeRefreshLayout   swipeRefresh;
    private RelativeLayout       noInternetLayout;
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView          = findViewById(R.id.webView);
        progressBar      = findViewById(R.id.progressBar);
        swipeRefresh     = findViewById(R.id.swipeRefresh);
        noInternetLayout = findViewById(R.id.noInternetLayout);

        setupFileChooser();
        setupWebView();
        setupSwipeRefresh();

        String targetUrl = resolveUrl(getIntent());
        if (isConnected()) {
            loadUrl(targetUrl);
        } else {
            showNoInternet();
        }

        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            if (isConnected()) { hideNoInternet(); loadUrl(BASE_URL); }
            else Toast.makeText(this, "Masih tidak ada koneksi", Toast.LENGTH_SHORT).show();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " KoinTokoApp/1.0");

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (!url.startsWith("https://kointoko.id") && !url.startsWith("http://kointoko.id")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                    catch (Exception e) { Toast.makeText(MainActivity.this, "Tidak dapat membuka link", Toast.LENGTH_SHORT).show(); }
                    return true;
                }
                return false;
            }

            @Override public void onPageStarted(WebView v, String url, Bitmap fav) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override public void onPageFinished(WebView v, String url) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                CookieManager.getInstance().flush();
                // Vibrate on success pages
                if (url.contains("sukses") || url.contains("success")) vibrate();
            }

            @Override public void onReceivedError(WebView v, int code, String desc, String url) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (!isConnected()) showNoInternet();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                progressBar.setProgress(p);
                if (p == 100) progressBar.setVisibility(View.GONE);
            }

            @Override public boolean onShowFileChooser(WebView wv,
                    ValueCallback<Uri[]> cb, FileChooserParams params) {
                filePathCallback = cb;
                try { fileChooserLauncher.launch(params.createIntent()); }
                catch (Exception e) { filePathCallback = null; return false; }
                return true;
            }
        });

        webView.setDownloadListener((url, ua, cd, mime, length) -> {
            String fileName = URLUtil.guessFileName(url, cd, mime);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setMimeType(mime);
            req.addRequestHeader("User-Agent", ua);
            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) req.addRequestHeader("Cookie", cookie);
            req.setTitle(fileName);
            req.setDescription("Mengunduh...");
            req.allowScanningByMediaScanner();
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) { dm.enqueue(req); Toast.makeText(this, "Mengunduh " + fileName, Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setupFileChooser() {
        fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (filePathCallback == null) return;
                Uri[] results = null;
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    results = new Uri[]{ result.getData().getData() };
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.green_primary));
        swipeRefresh.setOnRefreshListener(() -> {
            if (isConnected()) webView.reload();
            else { swipeRefresh.setRefreshing(false); Toast.makeText(this, "Tidak ada koneksi", Toast.LENGTH_SHORT).show(); }
        });
    }

    private String resolveUrl(Intent intent) {
        if (intent != null && intent.hasExtra("url")) {
            String url = intent.getStringExtra("url");
            if (url != null && !url.isEmpty()) return url;
        }
        if (intent != null && intent.getData() != null) return intent.getData().toString();
        return BASE_URL;
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void loadUrl(String url) { hideNoInternet(); webView.loadUrl(url); }
    private void showNoInternet()   { noInternetLayout.setVisibility(View.VISIBLE); webView.setVisibility(View.GONE); }
    private void hideNoInternet()   { noInternetLayout.setVisibility(View.GONE); webView.setVisibility(View.VISIBLE); }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(50);
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) { webView.goBack(); return; }
        long now = System.currentTimeMillis();
        if (now - backPressedTime < 2000) { super.onBackPressed(); }
        else { backPressedTime = now; Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show(); }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = resolveUrl(intent);
        if (!url.equals(BASE_URL)) loadUrl(url);
    }

    @Override protected void onResume()  { super.onResume();  webView.onResume(); }
    @Override protected void onPause()   { super.onPause();   webView.onPause(); }
    @Override protected void onDestroy() { webView.destroy(); super.onDestroy(); }
}
