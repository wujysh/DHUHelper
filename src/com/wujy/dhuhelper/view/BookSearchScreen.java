package com.wujy.dhuhelper.view;

import org.jsoup.nodes.Document;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetConstant;

public class BookSearchScreen extends WindowActivity {

	protected WebView mWebView;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;
	private String webHtml;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		try {
			Document doc = myHttpResponse.getData();

			doc.getElementById("header_opac").remove();
			doc.getElementById("menubar").remove();
			doc.getElementById("submenu").remove();
			doc.getElementById("search_container").remove();
			doc.getElementById("footer").remove();
			webHtml = doc.html().replace("class=\"search_bgimg\"", "");
			webHtml = webHtml.replace("style=\"text-indent:2em;margin:4px\"",
					"");
			webHtml = webHtml.replace("style=\"width:100px;\"", "");
			webHtml = webHtml.replace("fieldset",
					"fieldset style=\"width:100%;padding:0;margin:0\"");

			mWebView.loadDataWithBaseURL(NetConstant.URL_BOOK_SEARCH, webHtml,
					"text/html", "UTF-8", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(Messages.getString("BookSearchScreen.book_search")); //$NON-NLS-1$

		mWebView = new WebView(mContext);
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // solve the
																// white content
																// bug when
																// sliding menu
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDefaultTextEncodingName("gb18030");
		mWebView.getSettings().setBlockNetworkImage(true);
		// mWebView.getSettings().setLoadsImagesAutomatically(false);

		addContentView(mWebView, new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));

		setSlidingMenu();
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				showNetLoadingDialog();
				// showDialog(PROGRESS_ID);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				dismissNetLoadingDialog();
				mWebView.getSettings().setBlockNetworkImage(false);
				// dismissDialog(PROGRESS_ID);
			}

		});

		if (Globe.sCookieStringCnt > 0) {
			CookieManager cookieManager = CookieManager.getInstance();
			// cookieManager.removeSessionCookie();
			for (int i = 0; i < Globe.sCookieStringCnt; i++) {
				cookieManager.setCookie(NetConstant.URL_BOOK_SEARCH,
						Globe.sCookieString[i]);
			}
			CookieSyncManager.getInstance().sync();
		}

		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_BOOK_SEARCH, null, true);
		req.setPipIndex(NetConstant.BOOK_SEARCH);
		mNetClient.sendRequest(req);

		// mWebView.loadUrl(NetConstant.URL_BOOK_SEARCH);

		mImageView = (ImageView) findViewById(R.id.menu_icon_book_search);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_book_search);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			if (!mWebView.canGoBack()) {
				MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
						NetConstant.URL_BOOK_SEARCH, null, true);
				req.setPipIndex(NetConstant.BOOK_SEARCH);
				mNetClient.sendRequest(req);
			}
		} else {
			super.onBackPressed();
		}
	}

}
