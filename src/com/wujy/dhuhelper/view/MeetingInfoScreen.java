package com.wujy.dhuhelper.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import org.jsoup.nodes.Document;

public class MeetingInfoScreen extends WindowActivity {

	protected WebView mWebView;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;
	private String webHtml;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("讲座信息"); //$NON-NLS-1$

		mWebView = new WebView(mContext);
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // solve the
																// white content
																// bug when
																// sliding menu
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDefaultTextEncodingName("gb18030");
		mWebView.getSettings().setBlockNetworkImage(true);
        mWebView.getSettings().setUserAgentString("DHUHelper");
		// mWebView.getSettings().setLoadsImagesAutomatically(false);

		addContentView(mWebView, new LayoutParams(
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

		mWebView.loadUrl(NetConstant.URL_MEETING_INFO);

		mImageView = (ImageView) findViewById(R.id.menu_icon_meeting_info);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_meeting_info);
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
        } else {
            super.onBackPressed();
        }
	}

}
