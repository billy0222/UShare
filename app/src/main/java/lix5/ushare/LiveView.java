package lix5.ushare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LiveView extends AppCompatActivity {
    private WebView webView;

    public LiveView(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url)
            {
                webView.loadUrl("javascript:(function() { " +
                        "jwplayer().setFullscreen(true);"+
                        "jwplayer().setControls(false);"+
                        "})()");
            }
        });
        setContentView(webView);
        webView.loadUrl("http://liveview.ust.hk/busstop/index.html");

    }



}