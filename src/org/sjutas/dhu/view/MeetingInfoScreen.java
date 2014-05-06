package org.sjutas.dhu.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MeetingInfoScreen extends WindowActivity {

	protected WebView mWebView;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;
	private String webHtml;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
        if (myHttpResponse.getPipIndex() == NetConstant.MEETING_INFO) {
            try {
                Document doc = myHttpResponse.getData();

                Element ele = doc.body();
                ele.getElementsByAttributeValue("href", "###").remove();
                ele.getElementsByAttributeValue("align", "right").remove();
                ele.getElementsByTag("span").remove();
                ele.getElementsByTag("b").remove();
                ele.getElementsByTag("h1").remove();
                ele.getElementsByTag("script").remove();
                ele.getElementsByTag("br").remove();
                webHtml = ele.html();

                webHtml = webHtml.replace("东华科技部&nbsp; &nbsp; ", "");

                String[] week = {"一","二","三","四","五","六","七","八","九","十","十一","十二","十三","十四","十五","十六","十七","十八","十九","二十"};
                int week_index = 0;
                for (int i = 0; i < 20; i++) {
                    if (webHtml.contains("【讲座】第"+week[i]+"周讲座公示-东华科技部")) {
                        webHtml = webHtml.replace("【讲座】第"+week[i]+"周讲座公示-东华科技部", "");
                        week_index = i;
                        break;
                    }
                }
                webHtml = webHtml.replace("2014-4-20 02:28", "");
                webHtml = "<html><head><meta name=\"viewport\" content=\"target-densitydpi=medium-dpi, width=device-width\"></head><body style=\"font-size:20px\"><h3>PuPu-东华</h3>第"+week[week_index]+"周讲座公示-东华科技部"
                        + webHtml + "</body></html>";

                //System.out.println(webHtml);

                mWebView.loadDataWithBaseURL(NetConstant.URL_MEETING_INFO, webHtml, "text/html",
                        "UTF-8", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        //mWebView.getSettings().setUserAgentString("DHUHelper");
        //mWebView.getSettings().setUseWideViewPort(true);
        //mWebView.getSettings().setLoadWithOverviewMode(true);
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

        MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
                NetConstant.URL_MEETING_INFO, null, true);
        req.setPipIndex(NetConstant.MEETING_INFO);
        mNetClient.sendRequest(req);

		//mWebView.loadUrl(NetConstant.URL_MEETING_INFO);

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
