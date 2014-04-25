package com.wujy.dhuhelper.view;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.actionbarsherlock.view.Menu;
import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.Util;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetConstant;

public class WebViewScreen extends WindowActivity {

	protected WebView mWebView;
	protected Context mContext;
	private String url, name;
	private String webHtml;
	ProgressDialog progressDialog;
	// private int screenHeight, screenWidth;

	private static final int PROGRESS_ID = 10;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.NEWS_DETAIL) {
			try {
				Document doc = myHttpResponse.getData();

				Element ele = doc.getElementsByAttributeValue("colspan", "8")
						.get(0);
				ele.getElementsByAttributeValue("value", "关闭窗口").remove();
				webHtml = "<html><head><meta name=\"viewport\" content=\"target-densitydpi=medium-dpi, width=device-width\"></head><body style=\"font-size:20px\">" + ele.html()
						+ "</body></html>";
				webHtml = webHtml.replace("width=\"455\"", "width=\""
						+ Globe.news_detail_images_width + "\"");
				webHtml = webHtml.replace("width=\"450\"", "width=\""
						+ Globe.news_detail_images_width + "\"");
				webHtml = webHtml.replace("height=\"5\"", "height=\"2\"");

				mWebView.loadDataWithBaseURL(url, webHtml, "text/html",
						"UTF-8", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
//		 } else if (myHttpResponse.getPipIndex() == NetConstant.NEWS_DETAIL_JW) {
//			 try {
//				Document doc = myHttpResponse.getData();
//				
//				
//				webHtml = "<body style=\"font-size:20px\">" +
//						 doc.getElementById("newsshow").html() + "</body>";
//				 //doc.getElementById("noPrint").remove();
//				 //String webHtml = doc.html().replace("width=\"600\"",
//				 //"width=\"0\"");
//				String webHtml_gb2312 = String.valueOf(webHtml.getBytes("GB2312"));
//				
//				mWebView.loadDataWithBaseURL(url, webHtml_gb2312, "text/html", "gb2312", null);
//			 } catch (Exception e) {
//				e.printStackTrace();
//			 }
		 }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		url = getIntent().getStringExtra("url");
		name = getIntent().getStringExtra("name");
		boolean needCookies = getIntent().getBooleanExtra("needCookies", false);
		
		if (url == null) finish();

		setTitle(name);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.logo_actionbar);

		mContext = this;
		mWebView = new WebView(mContext);
		mWebView.getSettings().setJavaScriptEnabled(true);
		// mWebView.getSettings().setDefaultTextEncodingName("GBK");
		mWebView.getSettings().setBlockNetworkImage(true);
		if (!Globe.news_detail_load_images) {
			mWebView.getSettings().setLoadsImagesAutomatically(false);
		}
		mWebView.getSettings().setBuiltInZoomControls(true);// 设置支持缩放
		mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常

		if (!Globe.news_detail_load_images) {
			mWebView.getSettings().setLoadsImagesAutomatically(false);
		} else {
			mWebView.getSettings().setLoadsImagesAutomatically(true);
		}

		addContentView(mWebView, new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));

		mWebView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(final String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				// 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
				
				mAlertDialog = Util.createAlertDialog(WebViewScreen.this,
						getString(R.string.tishi),
						"此通知为Office文档格式，将调用浏览器下载，\n请安装相应的办公软件，如WPS等，否则可能无法打开。",
						getString(R.string.download), getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Uri uri = Uri.parse(url);
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								startActivity(intent);
								
								finish();
							}
						},
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						});
				mAlertDialog.show();
			}
		});

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				
				//showDialog(PROGRESS_ID);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				
				if (Globe.news_detail_load_images) {
					mWebView.getSettings().setBlockNetworkImage(false);
				}
				
				dismissNetLoadingDialog();
				
				System.out.println("onPageFinished() executed");
				//dismissDialog(PROGRESS_ID);
				//mWebView.loadUrl("javascript:(function(){"+"document.getElementsByTagName('img').style.display ='none';"+"})()");
				//<img src="../../newsmanage/mf/title4.gif">
			}
		});

		if (needCookies) {
			
			SharedPreferences saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
			Globe.sCookieStringAll = saveCacheSettings.getString("CookieStringAll", "");
			
			Globe.sCookieString = Globe.sCookieStringAll.split(";");
			Globe.sCookieStringCnt = Globe.sCookieString.length;
			
			if (Globe.sCookieStringCnt > 0) {
				CookieManager cookieManager = CookieManager.getInstance();
				// cookieManager.removeSessionCookie();
				for (int i = 0; i < Globe.sCookieStringCnt; i++) {
					cookieManager.setCookie(url, Globe.sCookieString[i]);
				}
				CookieSyncManager.getInstance().sync();
			}
		}
		//
		// WindowManager manage = getWindowManager();
		// Display display = manage.getDefaultDisplay();
		// //screenHeight = display.getHeight();
		// screenWidth = display.getWidth();
		
		showNetLoadingDialog();
		
		if (Globe.news_detail_reset_style) {
			if (url.contains("http://www2.dhu.edu.cn/dhuxxxt/xinwenwang/shownews.asp")) {
				// prepareNetClient();

				MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
						url, null, true);
				req.setPipIndex(NetConstant.NEWS_DETAIL);
				mNetClient.sendRequest(req);

			} else if (url.contains("http://jw.dhu.edu.cn/dhu/news/index.jsp")) {
//				// prepareNetClient();
//				//
//				 MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
//						 url, null, true);
//				 req.setPipIndex(NetConstant.NEWS_DETAIL_JW);
//				 mNetClient.sendRequest(req);
				//mWebView.getSettings().setLoadsImagesAutomatically(false);
				mWebView.loadUrl(url);

			} else {
				mWebView.loadUrl(url);
			}
		} else {
			mWebView.loadUrl(url);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.actionbar_list, menu);
		return true;
	}

//	@Override
//	protected Dialog onCreateDialog(int id) {
//		if (id == PROGRESS_ID) {
//			progressDialog = ProgressDialog.show(mContext, null,
//					mContext.getString(R.string.loading));
//			progressDialog.setOnKeyListener(onKeyListener);
//		}
//		return super.onCreateDialog(id);
//	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			if (Globe.news_detail_reset_style) {
				if (!mWebView.canGoBack()) {
					prepareNetClient();
					if (url.contains("http://www2.dhu.edu.cn/dhuxxxt/xinwenwang/shownews.asp")) {
						MyHttpRequest req = new MyHttpRequest(
								NetConstant.TYPE_GET, url, null, true);
						req.setPipIndex(NetConstant.NEWS_DETAIL);
						mNetClient.sendRequest(req);
//					} else if (url.contains("http://jw.dhu.edu.cn/dhu/news/index.jsp")) {
//						MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET, url, null, true);
//						req.setPipIndex(NetConstant.NEWS_DETAIL_JW);
//						mNetClient.sendRequest(req);
					}
				}
			}
		} else {
			// super.onBackPressed();
			finish();
		}
	}
}
