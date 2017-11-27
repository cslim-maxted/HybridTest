package com.example.cslim.hybridtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.cslim.hybridtest.util.Configs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG";
    private boolean isKITKAT = false;

    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            isKITKAT = true;
        }

        initializeWebView();
    }

    @SuppressLint("JavascriptInterface")
    private void initializeWebView() {

        // mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        // mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDomStorageEnabled(true);
        webview.clearCache(true);
        webview.setWebChromeClient(new MyChromeClient());
        webview.setWebViewClient(new MyWebViewClient());
        webview.addJavascriptInterface(new MyJavaScriptInterface(this), "HybridTest");
        webview.setHorizontalScrollBarEnabled(false);
        webview.setVerticalScrollBarEnabled(false);

        // 버전 관리용 사용자 User Agent 설정
        userAgent();

        if (Configs.isDevelop) {
            webview.loadUrl(Configs.DEVELOP_HOST_URL, customHeader());
        }
        else {
            webview.loadUrl(Configs.RELEASE_HOST_URL, customHeader());
        }
    }

    // 버전 관리용 사용자 User Agent 설정
    public void userAgent() {
        String userAgent = webview.getSettings().getUserAgentString();
        webview.getSettings().setUserAgentString(userAgent + "APP_Android");
    }

    // 커스텀 헤더를 설정
    public Map<String, String> customHeader() {
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("device-only","true");

        return extraHeaders;
    }

    /**
     * 자바스크립트 인터페이스 클래스
     * 참조 - https://developer.android.com/guide/webapps/webview.html
     */
    private class MyJavaScriptInterface {

        Context mContext;

        MyJavaScriptInterface(Context c) {
            mContext = c;
        }

       @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        // 추가되는 이벤트 처리 메서드를 계속 작성한다.

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("chromium",""+url);

            boolean shouldOverride = true;

            // Kakao
            if(Build.VERSION.SDK_INT >= 19) {
                if (url.startsWith(Configs.INTENT_PROTOCOL_START)) {
                    final int customUrlStartIndex = Configs.INTENT_PROTOCOL_START.length();
                    final int customUrlEndIndex = url.indexOf(Configs.INTENT_PROTOCOL_INTENT);
                    if (customUrlEndIndex < 0) {
                        shouldOverride=false;
                    } else {
                        final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)));
                        } catch (Exception e) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Configs.GOOGLE_PLAY_STORE_PREFIX+Configs.PACKAGE_KAKAO_TALK));
                            startActivity(intent);
                        }
                        shouldOverride=true;
                    }
                }
                else {
                    urlLoadAction(url, view);
                }
                return shouldOverride;
            }
            else {
                if(url.startsWith("kakaolink:")){
                    if (isPackageExists(Configs.PACKAGE_KAKAO_TALK)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        view.goBack();
                    }
                    else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Configs.GOOGLE_PLAY_STORE_PREFIX+Configs.PACKAGE_KAKAO_TALK));
                        startActivity(intent);
                        view.goBack();
                    }
                }
                else{
                    urlLoadAction(url, view);
                }
                return shouldOverride;
            }
        }

        private void urlLoadAction(String url, WebView view) {

            if(url.startsWith("mailto:")){
                MailTo mt = MailTo.parse(url);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, mt.getTo());
                intent.putExtra(Intent.EXTRA_TEXT, mt.getBody());
                intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
                intent.putExtra(Intent.EXTRA_CC, mt.getCc());
                intent.setType("message/rfc822");
                startActivity(intent);
                view.reload();
            }
            else if(url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
            }
            else{
                view.loadUrl(url, customHeader());
            }
        }

        /** isPackageExists - 패키지 유무 체크 */
        public boolean isPackageExists(String targetPackage){
            List<ApplicationInfo> packages;
            PackageManager pm;
            pm = getPackageManager();
            packages = pm.getInstalledApplications(0);
            for (ApplicationInfo packageInfo : packages) {
                if(packageInfo.packageName.equals(targetPackage)) return true;
            }
            return false;
        }

        // Back or Forward를 통한 이동시 Back & Forward 상태 변화 업데이트
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e("onPageStarted",""+url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e("onPageFinished",""+url);
        }

        // 웹페이지 링크를 통한 이동시 Back & Forward 상태 변화 업데이트
        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                                           boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        // 페이지 오류
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.e("onReceivedError", errorCode + "  /// " + description);
        }
    }

    /**
     * WebChromeClient
     */
    private class MyChromeClient extends WebChromeClient {


        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            // TODO Auto-generated method stub

            WebView.HitTestResult result = view.getHitTestResult();
            String url = result.getExtra();

            if(url != null && url.indexOf("about:blank")>-1){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }else{
                WebView newWebView = new WebView(MainActivity.this);
                view.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;

                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }

        }

        @Override
        public void onCloseWindow(WebView window) {
            // TODO Auto-generated method stub
            window.setVisibility(View.GONE);
            webview.removeView(window);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            setProgress(newProgress * 100);
            if (0 != newProgress) {
                setProgressBarIndeterminateVisibility(true);
            } else if (100 == newProgress) {
                setProgressBarIndeterminateVisibility(false);
            }
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final android.webkit.JsResult result) {
            return false;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, final android.webkit.JsPromptResult result) {
            return false;
        }

        // JS Alert 다이얼로그
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final android.webkit.JsResult result) {

            new AlertDialog.Builder(view.getContext())
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    result.confirm();
                                }
                            }).setCancelable(false).create().show();
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public void onBackPressed() {
        if (!webview.canGoBack()) {
            new AlertDialog.Builder(this)
                    .setTitle("앱 종료")
                    .setMessage("앱을 종료하시겠습니까")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }
    }
}
