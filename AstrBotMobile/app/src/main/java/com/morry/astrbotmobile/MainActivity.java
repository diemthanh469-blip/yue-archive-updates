package com.morry.astrbotmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "astrbot_settings";
    private static final String KEY_SERVER = "server_url";
    private static final int FILE_PICK_REQUEST = 7124;

    private LinearLayout root;
    private WebView webView;
    private String serverUrl = "";
    private ValueCallback<Uri[]> fileChooserCallback;
    private boolean pageFailed = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(23, 25, 35));
        getWindow().setNavigationBarColor(Color.rgb(23, 25, 35));
        serverUrl = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_SERVER, "");
        buildLayout();
        configureWebView();
        if (serverUrl.isEmpty()) showServerDialog(true);
        else openChat();
    }

    private void buildLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(8), dp(6), dp(6), dp(6));
        toolbar.setBackgroundColor(Color.rgb(23, 25, 35));

        TextView title = new TextView(this);
        title.setText("AstrBot");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, dp(42), 1f));
        toolbar.addView(toolbarButton("聊天", v -> openChat()));
        toolbar.addView(toolbarButton("后台", v -> openDashboard()));
        toolbar.addView(toolbarButton("刷新", v -> refreshPage()));
        toolbar.addView(toolbarButton("地址", v -> showServerDialog(false)));

        webView = new WebView(this);
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    private Button toolbarButton(String text, View.OnClickListener action) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(47, 51, 68)));
        button.setOnClickListener(action);
        return button;
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                pageFailed = false;
                super.onPageStarted(view, url, icon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                if ("http".equals(scheme) || "https".equals(scheme)) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(MainActivity.this, "没有可以打开此链接的应用", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame() && !pageFailed) {
                    pageFailed = true;
                    Toast.makeText(MainActivity.this,
                            "连接失败：请检查服务器地址、网络和 AstrBot 是否运行", Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择要上传的文件"), FILE_PICK_REQUEST);
                } catch (ActivityNotFoundException error) {
                    fileChooserCallback = null;
                    Toast.makeText(MainActivity.this, "找不到文件选择器", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
    }

    private void showServerDialog(boolean firstLaunch) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(serverUrl);
        input.setHint("http://192.168.1.100:6185");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(22), dp(8), dp(22), 0);
        body.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView help = new TextView(this);
        help.setText("填写运行 AstrBot 的电脑或服务器地址。手机和电脑需要能互相访问。\n\n不要填 127.0.0.1，除非 AstrBot 就运行在手机本机。示例：http://192.168.1.100:6185");
        help.setTextSize(13);
        help.setTextColor(Color.rgb(90, 95, 110));
        body.addView(help);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("设置 AstrBot 服务地址")
                .setView(body)
                .setPositiveButton("连接", null)
                .setNegativeButton(firstLaunch ? "退出" : "取消", (d, w) -> {
                    if (firstLaunch && serverUrl.isEmpty()) finish();
                })
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String candidate = normalizeUrl(input.getText().toString());
            if (candidate == null) {
                input.setError("请输入有效地址，例如 http://192.168.1.100:6185");
                return;
            }
            serverUrl = candidate;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_SERVER, serverUrl).apply();
            dialog.dismiss();
            openChat();
        });
    }

    private String normalizeUrl(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.isEmpty()) return null;
        if (!value.matches("(?i)^https?://.*")) value = "http://" + value;
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) return null;
        if (uri.getHost() == null || uri.getHost().isEmpty()) return null;
        if (uri.getQuery() != null || uri.getFragment() != null) return null;
        return value;
    }

    private void openChat() {
        if (serverUrl.isEmpty()) {
            showServerDialog(true);
            return;
        }
        webView.loadUrl(serverUrl + "/#/chat");
    }

    private void openDashboard() {
        if (serverUrl.isEmpty()) showServerDialog(true);
        else webView.loadUrl(serverUrl + "/");
    }

    private void refreshPage() {
        if (webView.getUrl() != null) webView.reload();
        else openChat();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST && fileChooserCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    result = new Uri[count];
                    for (int i = 0; i < count; i++) result[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    result = new Uri[] { data.getData() };
                }
            }
            fileChooserCallback.onReceiveValue(result);
            fileChooserCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
