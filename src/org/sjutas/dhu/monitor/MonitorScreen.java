package org.sjutas.dhu.monitor;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;
import org.sjutas.dhu.view.WebViewScreen;

public class MonitorScreen extends WindowActivity implements OnClickListener {

	private Button startService, stopService, unbindService, confirmInfoBtn,
			courseSearchBtn, openCoursePageBtn;
	private EditText courseId, courseNo, courseName;
	private CheckBox autorun;

	private boolean isBind, isfinished = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("选课监控");
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.logo_actionbar);

		setContentView(R.layout.monitor_layout);

		startService = (Button) findViewById(R.id.start_service);
		stopService = (Button) findViewById(R.id.stop_service);
		unbindService = (Button) findViewById(R.id.unbind_service);

		autorun = (CheckBox) findViewById(R.id.settings_monitor_autorun);
		
		autorun.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				Editor edit = settings.edit();
				edit.putBoolean("monitor_auto_startup", isChecked);
				edit.commit();

				Globe.monitor_auto_startup = isChecked;
			}
		});

		courseNo = (EditText) findViewById(R.id.settings_monitor_courseNo);
		courseId = (EditText) findViewById(R.id.settings_monitor_courseId);
		courseName = (EditText) findViewById(R.id.settings_monitor_courseName);

		confirmInfoBtn = (Button) findViewById(R.id.monitor_confirm_course_info);
		courseSearchBtn = (Button) findViewById(R.id.monitor_search);
		openCoursePageBtn = (Button) findViewById(R.id.monitor_open_course_page);

		startService.setOnClickListener(this);
		stopService.setOnClickListener(this);
		unbindService.setOnClickListener(this);

		confirmInfoBtn.setOnClickListener(this);
		courseSearchBtn.setOnClickListener(this);
		openCoursePageBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		autorun.setChecked(Globe.monitor_auto_startup);
		courseNo.setText(String.valueOf(Globe.monitor_courseNo));
		courseId.setText(String.valueOf(Globe.monitor_courseId));
		courseName.setText(Globe.monitor_courseName);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch (v.getId()) {
		case R.id.start_service:
			Intent startIntent = new Intent(this, MonitorService.class);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startService(startIntent);
			break;
		case R.id.stop_service:
			Log.d("MonitorService", "click Stop Service button");
			Intent stopIntent = new Intent(this, MonitorService.class);
			stopService(stopIntent);
			break;
		case R.id.monitor_confirm_course_info:
			// Intent bindIntent = new Intent(this, MonitorService.class);
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);

			Editor edit = settings.edit();
			edit.putString("monitor_courseNo", courseNo.getEditableText()
					.toString().trim());
			edit.putString("monitor_courseId", courseId.getEditableText()
					.toString().trim());
			edit.putString("monitor_courseName", courseName.getEditableText()
					.toString().trim());
			edit.commit();

			Globe.monitor_courseNo = Integer.parseInt(settings.getString(
					"monitor_courseNo", "0"));
			Globe.monitor_courseId = Integer.parseInt(settings.getString(
					"monitor_courseId", "0"));
			Globe.monitor_courseName = settings.getString("monitor_courseName",
					"");
			
			break;
		case R.id.unbind_service:
			Log.d("MonitorService", "click Unbind Service button");

			break;
		case R.id.monitor_search:
			MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
					NetConstant.URL_KCCX, null, true);
			req.setPipIndex(NetConstant.KCCX_HOMEPAGE);
			mNetClient.sendRequest(req);
			break;
		case R.id.monitor_open_course_page:
			Intent it = new Intent(this, WebViewScreen.class);
			it.putExtra("url", NetConstant.URL_COURSE_CHOOSE);
			it.putExtra("name", "选课监控");
			it.putExtra("needCookies", true);
			startActivity(it);
			break;
		default:
			break;
		}
	}

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackPressed() {
		if (Globe.monitor_auto_startup) {
			Toast.makeText(this, Messages.getString("MonitorScreen.monitor_service_still_running_background"), //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, Messages.getString("MonitorScreen.monitor_service_will_be_killed"), //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.actionbar_list, menu);
		return true;
	}

}