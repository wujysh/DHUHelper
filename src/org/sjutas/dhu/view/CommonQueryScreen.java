package org.sjutas.dhu.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;

public class CommonQueryScreen extends WindowActivity {

	protected WebView mWebView;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("公共查询");

		mWebView = new WebView(mContext);
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // solve the
																// white content
																// bug when
																// sliding menu
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDefaultTextEncodingName("gb18030");
		mWebView.getSettings().setLoadsImagesAutomatically(false);

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
				// dismissDialog(PROGRESS_ID);
			}
		});

		if (Globe.sCookieStringCnt > 0) {
			CookieManager cookieManager = CookieManager.getInstance();
			// cookieManager.removeSessionCookie();
			for (int i = 0; i < Globe.sCookieStringCnt; i++) {
				cookieManager.setCookie(NetConstant.URL_COMMON_QUERY,
						Globe.sCookieString[i]);
			}
			CookieSyncManager.getInstance().sync();
		}

		mWebView.loadUrl(NetConstant.URL_COMMON_QUERY);

		mImageView = (ImageView) findViewById(R.id.menu_icon_common_query);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_common_query);
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
