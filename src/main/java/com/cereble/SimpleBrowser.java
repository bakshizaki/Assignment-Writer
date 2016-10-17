package com.cereble;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SimpleBrowser extends Activity implements OnClickListener {

	EditText url;
	CustomWebView ourBrow;
	SharedPreferences getPrefs;
	String DesktopUA;
	String DefaultUA;
	ProgressBar loading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		
		//ourBrow =new ExtendedWebView(this);
		ourBrow = (CustomWebView) findViewById(R.id.wvBrowser);
		
		ourBrow.getSettings().setJavaScriptEnabled(true);
		ourBrow.getSettings().setLoadWithOverviewMode(true);
		ourBrow.getSettings().setUseWideViewPort(true);
		ourBrow.getSettings().setBuiltInZoomControls(true);
		
		
		ourBrow.loadUrl("http://www.google.com/webhp?hl=en&output=html");
		ourBrow.setWebViewClient(new OurWebViewClient());
		getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		//DesktopUA= "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0";
		DesktopUA="Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
		DefaultUA= ourBrow.getSettings().getUserAgentString().toString();
		
		
		Button go = (Button) findViewById(R.id.bGo);
		Button back = (Button) findViewById(R.id.bBack);
		Button forward = (Button) findViewById(R.id.bForward);
		Button refresh = (Button) findViewById(R.id.bRefresh);
		Button hist = (Button) findViewById(R.id.bExit);
		loading = (ProgressBar) findViewById(R.id.brow_load);
		url = (EditText) findViewById(R.id.etURL);
		go.setOnClickListener(this);
		back.setOnClickListener(this);
		forward.setOnClickListener(this);
		refresh.setOnClickListener(this);
		hist.setOnClickListener(this);
		
		//for hiding keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(url.getApplicationWindowToken(), 0);

	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bGo:
			String newurl = url.getText().toString();
			if(!newurl.startsWith("http"))
				newurl = "http://"+newurl;
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(url.getApplicationWindowToken(), 0);
			ourBrow.loadUrl(newurl);
			break;
		case R.id.bBack:
			
			if (ourBrow.canGoBack())
				ourBrow.goBack();
			break;
		case R.id.bForward:
			if (ourBrow.canGoForward())
				ourBrow.goForward();
			break;
		case R.id.bRefresh:
			ourBrow.reload();
			
			break;
		case R.id.bExit:
			//ourBrow.clearHistory();
			finish();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		 super.onCreateOptionsMenu(menu);
		MenuInflater im = getMenuInflater();
		im.inflate(R.menu.browser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		 super.onOptionsItemSelected(item);
		switch(item.getItemId())
		{
		case R.id.browser_prefs:
			Intent p = new Intent("android.intent.action.BROWSERPREFS");
			//startActivity(p);
			startActivityForResult(p, 0);
			break;
		case R.id.browser_exit:
			finish();
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK)
		{
			Bundle b = data.getExtras();
			boolean m_view = b.getBoolean("m_view");
			if(!getPrefs.getBoolean("mobile_view", true))
    		{
				ourBrow.getSettings().setUserAgentString(DesktopUA);
    		
    		}
    		else
    		{
    			
    			ourBrow.getSettings().setUserAgentString(DefaultUA);
    		}
		}
	}

	
	public class OurWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
		view.loadUrl(url);
		return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			loading.setVisibility(View.GONE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			loading.setVisibility(View.VISIBLE);
			
		}
		
		
	}	
	
}
