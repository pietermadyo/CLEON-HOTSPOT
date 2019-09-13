package com.sims.cleonhotspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class LoginActivity extends Activity {

    private WebView mWebView;
    Button scan;
    public final static String E_MESSAGE="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        final String email = intent.getStringExtra(SplashActivity.EXTR_MESSAGE);

        Bundle bundle = getIntent().getExtras();
        final String username = bundle.getString("data1");
        final String password = bundle.getString("data2");

        scan = (Button) findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),ScanBarcodeActivity.class);
                i.putExtra(E_MESSAGE, email);
                startActivityForResult(i,0);
            }
        });

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("http://megacleon.com");
        //  mWebView.loadUrl("http://restopedia.890m.com/restopedia-dev/index.php/welcome/login");

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(username!=null && password!=null) {
                    final String js = "javascript:document.getElementById('username').value = '" + username +
                            "';document.getElementById('password').value='" + password + "';"
                            + "document.getElementById('formlogin').submit();"
                            ;

                    if (Build.VERSION.SDK_INT >= 19) {
                        view.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        });
                        view.evaluateJavascript("javascript:function(){" +
                                "l=document.getElementById('btnlogin');" +
                                "e=document.createEvent('HTMLEvents');"+
                                "e.initEvent('click', true, true);" +
                                "l.dispatchEvent(e);"+
                                "})()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        });

                    } else {
                        view.loadUrl(js);
                        view.loadUrl("javascript:function(){" +
                                "l=document.getElementById('btnlogin');" +
                                "e=document.createEvent('HTMLEvents');"+
                                "e.initEvent('click', true, true);" +
                                "l.dispatchEvent(e);"+
                                "})()");
                    }
                }
            }
        });


        // Stop local links and redirects from opening in browser instead of WebView
        // mWebView.setWebViewClient(new MyAppWebViewClient());

        // Use local resource
        // mWebView.loadUrl("file:///android_asset/www/index.html");
    }

    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}