package org.sjutas.dhu.view;

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
import org.sjutas.dhu.net.MyHttpResponse;

public class AboutScreen extends WindowActivity {

	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(Messages.getString("AboutScreen.about_dhuhelper")); //$NON-NLS-1$

		setContentView(R.layout.about_layout);

		setSlidingMenu();

		((TextView) findViewById(R.id.about_version)).setText(Messages.getString("AboutScreen.version") //$NON-NLS-1$
				+ getVersion(this));

		mLinearLayout = (LinearLayout) findViewById(R.id.menu_about);
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
		//if (item.getItemId() != R.id.about) {
			super.onOptionsItemSelected(item);
		//}
		return false;
	}

	public String getVersion(Context context) {
		try {
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return manager.versionName + " (" + manager.versionCode + ")";
		} catch (NameNotFoundException e) {
			return Messages.getString("AboutScreen.unknown"); //$NON-NLS-1$
		}
	}

}
