package org.sjutas.dhu.view;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;

public class CheckUpdateScreen extends WindowActivity {

	private TextView mInfoView, mUrlView, mConclusionView, mLatestVersionView,
			mCurrentVersionView;
	private LinearLayout mLinearLayout;

	private int mCurrentVersionCode, mLatestVersionCode;
	String[] CurVerStr;
	String LatestVersion, versionStr, urlStr, news;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.UPDATE) {
			Document doc = myHttpResponse.getData();
			// System.out.println(doc.toString());
			Element ele = doc.getElementById("content");

			if (ele != null) {
				String[] updateStrs = ele.text().split("@");
				versionStr = updateStrs[0];
				urlStr = updateStrs[1];
				LatestVersion = updateStrs[2];
				news = updateStrs[3];
			} else {
				LatestVersion = CurVerStr[0] + " (" + CurVerStr[1] + ")";
				versionStr = CurVerStr[1];
			}

			mLatestVersionView.setText(Messages.getString("CheckUpdateScreen.latest_version") + LatestVersion); //$NON-NLS-1$
			mLatestVersionCode = Integer.parseInt(versionStr);

			if (mLatestVersionCode > mCurrentVersionCode) {
				mConclusionView.setText(Messages.getString("CheckUpdateScreen.find_new_version")); //$NON-NLS-1$
				mUrlView.setText(urlStr);
				mInfoView.setText(Messages.getString("CheckUpdateScreen.update_content") + news); //$NON-NLS-1$
			} else {
				mConclusionView.setText(Messages.getString("CheckUpdateScreen.already_latest_version")); //$NON-NLS-1$
				mUrlView.setText("");
				mInfoView.setText("");
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		setTitle(Messages.getString("CheckUpdateScreen.check_update")); //$NON-NLS-1$

		setContentView(R.layout.update_layout);

		setSlidingMenu();

		mInfoView = (TextView) findViewById(R.id.update_info);
		mUrlView = (TextView) findViewById(R.id.update_url);
		mConclusionView = (TextView) findViewById(R.id.update_conclusion);
		mLatestVersionView = (TextView) findViewById(R.id.update_latest_version);
		mCurrentVersionView = (TextView) findViewById(R.id.update_current_version);

		CurVerStr = getVersion(this);
		mCurrentVersionCode = Integer.parseInt(CurVerStr[1]);
		mCurrentVersionView.setText(Messages.getString("CheckUpdateScreen.current_version") + CurVerStr[0] + " (" //$NON-NLS-1$
				+ CurVerStr[1] + ")");

		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_UPDATE, null, true);
		req.setPipIndex(NetConstant.UPDATE);
		mNetClient.sendRequest(req);

		mLinearLayout = (LinearLayout) findViewById(R.id.menu_update_check);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//if (item.getItemId() != R.id.update) {
			super.onOptionsItemSelected(item);
		//}
		return false;
	}

	public String[] getVersion(Context context) {
		String[] ver = new String[2];
		try {
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			ver[0] = manager.versionName;
			ver[1] = String.valueOf(manager.versionCode);
		} catch (NameNotFoundException e) {
			ver[0] = ver[1] = "UnKnown";
		}
		return ver;
	}

}
