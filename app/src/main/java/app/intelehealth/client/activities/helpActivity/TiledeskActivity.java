package app.intelehealth.client.activities.helpActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import app.intelehealth.client.R;

public class TiledeskActivity extends AppCompatActivity {

    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiledesk);
        getSupportActionBar().hide();

        WebView myWebView = (WebView) findViewById(R.id.tiledesk);


        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        myWebView.setWebChromeClient(new FileChooserWebChromeClient(this) {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("TiledeskActivity", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });

//        allow to snap photos
        webSettings.setAllowFileAccess(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        myWebView.loadUrl("https://widget.tiledesk.com/v4/assets/twp/index.html?isOpen=true&tiledesk_projectid=6077db39d7957800190137f1&tiledesk_fullscreenMode=true&tiledesk_hideHeaderCloseButton=true&tiledesk_isopen=true");

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }


    public class FileChooserWebChromeClient extends WebChromeClient {

        private Activity myActivity;

        public FileChooserWebChromeClient(TiledeskActivity myActivity) {
            this.myActivity = myActivity;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                myActivity.startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }
    }
}