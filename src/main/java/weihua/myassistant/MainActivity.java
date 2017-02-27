package weihua.myassistant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import weihua.myassistant.context.Context;
import weihua.myassistant.context.TopicDataLoadUtil;
import weihua.myassistant.request.RequestType;
import weihua.myassistant.response.MediaType;
import weihua.myassistant.service.HelpAssistant;
import weihua.myassistant.ui.CustomerWebChromeClient;
import weihua.myassistant.ui.alarm.AlarmService;
import weihua.myassistant.ui.common.Constans;
import weihua.myassistant.ui.util.AlarmUtil;
import weihua.myassistant.ui.util.Log4JUtil;
import weihua.myassistant.ui.util.MediaUtil;
import weihua.myassistant.ui.util.MediaUtil.MusicPlaySource;
import weihua.myassistant.ui.util.ServiceUtil;
import weihua.myassistant.util.DateUtil;
import weihua.myassistant.util.ExceptionUtil;

public class MainActivity extends Activity {
	private WebView webView;
	private static String viewFilePath = "index.html";
	private Context assistantContext;

	@JavascriptInterface
	public String getResponse(String request, String requestType) {
		String msg = "";
		try {
			msg = assistantContext.getResponse(request, RequestType.fromCode(requestType));
		} catch (Exception e) {
			msg = ExceptionUtil.getStackTrace(e);
		}
		return msg;
	}

	/**
	 * @param musicPlaySource
	 * @param mediaLink
	 *            local_demo:/test.mp3;
	 *            web_demo:http://42.81.26.18/mp3.9ku.com/m4a/637791.m4a
	 */
	@JavascriptInterface
	public void playMusic(String musicPlaySource, String mediaLink) {
		MediaUtil.playMusic(getApplicationContext(), MusicPlaySource.fromCode(musicPlaySource), mediaLink, true, null);
	}

	@JavascriptInterface
	public String backHome() {
		String msg = "";
		try {
			msg = assistantContext.backHome();
		} catch (Exception e) {
			msg = ExceptionUtil.getStackTrace(e);
		}
		return msg;
	}

	@JavascriptInterface
	public void showMedia(String mediaLink, String mediaType) {
		Intent it = MediaUtil.getMediaIntent(mediaLink, MediaType.fromCode(mediaType));
		startActivity(it);
	}

	@JavascriptInterface
	public String loadDataFile(String topicName) {
		String msg = "";
		try {
			msg = HelpAssistant.generateResponse(topicName);
		} catch (Exception e) {
			msg = ExceptionUtil.getStackTrace(e);
		}
		return msg;
	}

	@JavascriptInterface
	public void showMsg(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * app package name can get from .apk file by apkhelper.exe
	 * 
	 * @param packageName
	 *            open wikiHow demo:com.wikihow.wikihowapp
	 */
	@JavascriptInterface
	public void startAppByPackageName(String packageName) {
		PackageManager packageManager = getPackageManager();
		Intent intent = new Intent();
		intent = packageManager.getLaunchIntentForPackage(packageName);
		startActivity(intent);
	}

	public void initView() {
		webView = (WebView) findViewById(R.id.wv1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(this, "mainActivity");
		webView.loadUrl("file:///android_asset/" + viewFilePath);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.setWebChromeClient(new CustomerWebChromeClient());
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
	}

	private long exitTime = 0;

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - exitTime > 2000) {
			Toast.makeText(this, "Press once more exit", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			// System.exit(0);
			// android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.isCheckable()) {
			menuItem.setChecked(true);
		}
		try {
			switch (menuItem.getItemId()) {
			case R.id.action_loadresponse:
				alarmBind();
				break;
			case R.id.action_edittopic:
				alarmCancel();
				break;
			case R.id.action_aboutme:
				TopicDataLoadUtil.loadAllTopicDataFromWeb();
				showMsg("topic refreshed");
				break;
			default:
				break;
			}
		} catch (Exception e) {
			showMsg(ExceptionUtil.getStackTrace(e));
		}
		return true;
	}

	private void alarmBind() {
		AlarmService.config();
		AlarmUtil.startAlarmOnce(this.getApplicationContext(), Constans.ALARM_SERVICE_ID,
				DateUtil.getTimeFromCurrent(0), String.valueOf(Constans.ALARM_SERVICE_ID), false);
		showMsg("service started");
	}

	private void alarmCancel() {
		AlarmUtil.stopAlarm(this, Constans.ALARM_SERVICE_ID);
		ServiceUtil.stopService(this, AlarmService.class);
		showMsg("service canceled");
		finish();
		System.exit(0);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);
		String serviceName = newIntent.getStringExtra("serviceName");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		String serviceName = getIntent().getStringExtra("serviceName");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		Log4JUtil.configure();

		try {
			assistantContext = new Context();
		} catch (Exception e) {
			showMsg(ExceptionUtil.getStackTrace(e));
		}

		initView();

	}

}
