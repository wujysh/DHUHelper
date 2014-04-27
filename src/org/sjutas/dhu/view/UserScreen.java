package org.sjutas.dhu.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpResponse;

public class UserScreen extends WindowActivity {

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		// TODO Auto-generated method stub

	}

	private TextView mJwInfo, mJwStatus, mLibInfo, mLibStatus;
	private Button mJwChange, mLibChange;
	private LinearLayout mLinearLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("账户信息");

		setContentView(R.layout.user_layout);

		setSlidingMenu();

		mJwInfo = (TextView) findViewById(R.id.user_jw_info);
		mLibInfo = (TextView) findViewById(R.id.user_lib_info);
		mJwStatus = (TextView) findViewById(R.id.user_jw_status);
		mLibStatus = (TextView) findViewById(R.id.user_lib_status);
		mJwChange = (Button) findViewById(R.id.user_jw_change);
		mLibChange = (Button) findViewById(R.id.user_lib_change);

		mJwChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it;
				if (Globe.isLoginJW) {
					Globe.isLoginJW = false;

					// MyHttpRequest req = new
					// MyHttpRequest(NetConstant.TYPE_GET,
					// NetConstant.URL_LOGOUT_JW, null, true);
					// req.setPipIndex(NetConstant.LOGOUT);
					// mNetClient.sendRequest(req);

					Toast.makeText(mContext, "注销成功", Toast.LENGTH_LONG).show();
					it = new Intent(mContext, UserScreen.class);
				} else {
					it = new Intent(mContext, LoginScreen.class);
					it.putExtra("where", "JWC");
					it.putExtra("NextScreen", UserScreen.class);
				}
				Globe.sMenuPosX = mScrollView.getScrollX();
				Globe.sMenuPosY = mScrollView.getScrollY();
				startActivity(it);
				finish();
			}
		});

		mLibChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it;
				if (Globe.isLoginLib) {
					Globe.isLoginLib = false;

					Toast.makeText(mContext, "注销成功", Toast.LENGTH_LONG).show();
					it = new Intent(mContext, UserScreen.class);
				} else {

					it = new Intent(mContext, LoginScreen.class);
					it.putExtra("where", "LIB");
					it.putExtra("NextScreen", UserScreen.class);
				}
				Globe.sMenuPosX = mScrollView.getScrollX();
				Globe.sMenuPosY = mScrollView.getScrollY();
				startActivity(it);
				finish();
			}
		});

		if (Globe.isLoginJW) {
			mJwChange.setText(R.string.logout);
			mJwStatus.setText(R.string.logged);
			mJwInfo.setText("学号：" + Globe.sId_jw + "  姓名：" + Globe.sName_jw);
		} else {
			mJwChange.setText(R.string.login);
			mJwStatus.setText(R.string.unlogged);
			mJwInfo.setText("登陆后才能使用教务处校内服务");
		}

		if (Globe.isLoginLib) {
			mLibChange.setText(R.string.logout);
			mLibStatus.setText(R.string.logged);
			mLibInfo.setText("学号：" + Globe.sId_lib + "  姓名：" + Globe.sName_lib
					+ "\n" + Globe.sMsg_lib);
		} else {
			mLibChange.setText(R.string.login);
			mLibStatus.setText(R.string.unlogged);
			mLibInfo.setText("登陆后才能使用图书馆校内服务");
		}

		mLinearLayout = (LinearLayout) findViewById(R.id.menu_user);
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
		if (item.getItemId() != R.id.user) {
			super.onOptionsItemSelected(item);
		}
		return false;
	}
}
