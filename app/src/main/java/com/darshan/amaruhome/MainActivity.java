package com.darshan.amaruhome;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.AlertDialog;
import android.widget.EditText;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

/** Our Home: the web app (bundled in the APK, works offline) in a WebView, plus native widgets, reminders, fingerprint and sign-in. */
public class MainActivity extends Activity {
    static final String HOST = "appassets.androidplatform.net";
    WebView web;
    ValueCallback<Uri[]> chooser;
    final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.parseColor("#FF5A5F"));
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (!HOST.equals(u.getHost())) return null;
                String p = u.getPath() == null ? "/" : u.getPath();
                if (p.equals("/") || p.isEmpty()) p = "/index.html";
                try {
                    InputStream in = getAssets().open(p.substring(1));
                    String mime = p.endsWith(".html") ? "text/html" : p.endsWith(".js") ? "text/javascript" : p.endsWith(".png") ? "image/png" : p.endsWith(".json") ? "application/json" : "application/octet-stream";
                    return new WebResourceResponse(mime, "UTF-8", in);
                } catch (Exception e) { return null; }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg).setPositiveButton(android.R.string.ok, (d, w) -> r.confirm()).setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg)
                        .setPositiveButton(android.R.string.ok, (d, w) -> r.confirm())
                        .setNegativeButton(android.R.string.cancel, (d, w) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
            @Override
            public boolean onJsPrompt(WebView v, String url, String msg, String def, final JsPromptResult r) {
                final EditText e = new EditText(MainActivity.this);
                e.setText(def);
                new AlertDialog.Builder(MainActivity.this).setMessage(msg).setView(e)
                        .setPositiveButton(android.R.string.ok, (d, w) -> r.confirm(e.getText().toString()))
                        .setNegativeButton(android.R.string.cancel, (d, w) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (chooser != null) chooser.onReceiveValue(null);
                chooser = cb;
                try { startActivityForResult(p.createIntent(), 77); } catch (Exception e) { chooser = null; return false; }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 5);
        handleAuth(getIntent());
        web.loadUrl("https://" + HOST + "/index.html");
    }

    @Override
    protected void onNewIntent(Intent in) {
        super.onNewIntent(in);
        handleAuth(in);
    }

    void handleAuth(Intent in) {
        Uri u = in == null ? null : in.getData();
        if (u == null || !"amaruhome".equals(u.getScheme())) return;
        String r = u.getQueryParameter("refresh"), uid = u.getQueryParameter("uid");
        if (r == null || r.isEmpty() || uid == null) return;
        String em = u.getQueryParameter("email");
        getSharedPreferences("auth", MODE_PRIVATE).edit().putString("refresh", r).putString("uid", uid).putString("email", em == null ? "" : em).apply();
        ui.postDelayed(() -> web.evaluateJavascript("window.agNativeInit&&agNativeInit(true)", null), 1200);
    }

    @Override
    protected void onActivityResult(int code, int res, Intent data) {
        if (code == 77 && chooser != null) {
            Uri[] out = null;
            if (res == RESULT_OK && data != null) {
                if (data.getData() != null) out = new Uri[]{data.getData()};
                else if (data.getClipData() != null) {
                    out = new Uri[data.getClipData().getItemCount()];
                    for (int i = 0; i < out.length; i++) out[i] = data.getClipData().getItemAt(i).getUri();
                }
            }
            chooser.onReceiveValue(out);
            chooser = null;
        } else super.onActivityResult(code, res, data);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(function(){if(window.agBack&&agBack())return 1;return 0})()", v -> { if ("1".equals(v)) return; finish(); });
    }

    class Bridge {
        @JavascriptInterface
        public void setWidgetData(String json) {
            getSharedPreferences("ag", MODE_PRIVATE).edit().putString("w", json).apply();
            Widgets.refresh(MainActivity.this);
        }

        @JavascriptInterface
        public void scheduleAlarms(String json) {
            getSharedPreferences("ag", MODE_PRIVATE).edit().putString("alarms", json).apply();
            Alarms.schedule(MainActivity.this);
        }

        @JavascriptInterface
        public void saveFile(String name, String data, String type) {
            try {
                File dir = new File(getCacheDir(), "statements");
                dir.mkdirs();
                File f = new File(dir, new File(name).getName());
                try (FileOutputStream o = new FileOutputStream(f)) { o.write(data.getBytes("UTF-8")); }
                Uri u = Uri.parse("content://" + getPackageName() + ".files/" + f.getName());
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType(type);
                i.putExtra(Intent.EXTRA_STREAM, u);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i, name));
            } catch (Exception e) { toast("Error: " + e.getMessage()); }
        }

        @JavascriptInterface
        public String getAuth() {
            SharedPreferences p = getSharedPreferences("auth", MODE_PRIVATE);
            try { return new JSONObject().put("refresh", p.getString("refresh", "")).put("uid", p.getString("uid", "")).put("email", p.getString("email", "")).toString(); }
            catch (Exception e) { return "{}"; }
        }

        @JavascriptInterface
        public void setRefresh(String r) { getSharedPreferences("auth", MODE_PRIVATE).edit().putString("refresh", r).apply(); }

        @JavascriptInterface
        public void signIn() { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://1darshankumarsavsani-blip.github.io/Amaru-Ghar/app-auth.html"))); }

        @JavascriptInterface
        public void signOut() { getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply(); }

        @JavascriptInterface
        public void http(final String id, final String method, final String url, final String body, final String type, final String bearer) {
            new Thread(() -> {
                int code = 0;
                String out = "";
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                    c.setConnectTimeout(15000);
                    c.setReadTimeout(20000);
                    c.setRequestMethod(method);
                    if (!bearer.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + bearer);
                    if (!body.isEmpty()) {
                        c.setDoOutput(true);
                        c.setRequestProperty("Content-Type", type);
                        try (OutputStream o = c.getOutputStream()) { o.write(body.getBytes("UTF-8")); }
                    }
                    code = c.getResponseCode();
                    InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    if (in != null) { byte[] bb = new byte[4096]; int n; while ((n = in.read(bb)) > 0) bo.write(bb, 0, n); }
                    out = bo.toString("UTF-8");
                } catch (Exception e) { out = String.valueOf(e.getMessage()); }
                final int fc = code;
                final String fo = out;
                ui.post(() -> web.evaluateJavascript("window.__httpCb&&__httpCb(" + JSONObject.quote(id) + "," + fc + "," + JSONObject.quote(fo) + ")", null));
            }).start();
        }

        @JavascriptInterface
        public boolean canBiometric() { return Build.VERSION.SDK_INT >= 28; }

        @JavascriptInterface
        public void biometric() {
            if (Build.VERSION.SDK_INT < 28) return;
            ui.post(() -> {
                try {
                    BiometricPrompt.Builder bp = new BiometricPrompt.Builder(MainActivity.this).setTitle("અમારું ઘર").setNegativeButton("PIN", getMainExecutor(), (d, w) -> { });
                    bp.build().authenticate(new CancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r) {
                            web.evaluateJavascript("window.agBioOk&&agBioOk()", null);
                        }
                    });
                } catch (Exception e) { toast("Fingerprint not available"); }
            });
        }
    }

    void toast(String s) { android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_LONG).show(); }
}
