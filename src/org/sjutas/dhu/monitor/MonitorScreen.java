package org.sjutas.dhu.monitor;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.Visibility;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;

import org.apache.http.NameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;
import org.sjutas.dhu.view.WebViewScreen;

import java.util.ArrayList;

public class MonitorScreen extends WindowActivity implements OnClickListener {

	private Button startService, stopService, confirmInfoBtn,
			courseSearchBtn, openCoursePageBtn, openSelectedCourseBtn;
	private EditText courseId, courseNo, courseInfo;
	private CheckBox autorun;

    private ImageView mImageView;
    private LinearLayout mLinearLayout;

	private boolean isBind, isfinished = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("选课查询");

		setContentView(R.layout.monitor_layout);

        setSlidingMenu();

        //findViewById(R.id.monitor_advanced);

		startService = (Button) findViewById(R.id.start_service);
		stopService = (Button) findViewById(R.id.stop_service);

		autorun = (CheckBox) findViewById(R.id.settings_monitor_autorun);
		
		autorun.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				Editor edit = settings.edit();
				edit.putBoolean("monitor_courses_auto_startup", isChecked);
				edit.commit();

				Globe.monitor_courses_auto_startup = isChecked;
			}
		});

		courseNo = (EditText) findViewById(R.id.settings_monitor_courseNo);
		courseId = (EditText) findViewById(R.id.settings_monitor_courseId);
        courseInfo = (EditText) findViewById(R.id.settings_monitor_courseInfo);

		confirmInfoBtn = (Button) findViewById(R.id.monitor_confirm_course_info);
		courseSearchBtn = (Button) findViewById(R.id.monitor_search);
		openCoursePageBtn = (Button) findViewById(R.id.monitor_open_course_page);
        openSelectedCourseBtn = (Button) findViewById(R.id.monitor_open_selected_course);

		startService.setOnClickListener(this);
		stopService.setOnClickListener(this);

		confirmInfoBtn.setOnClickListener(this);
		courseSearchBtn.setOnClickListener(this);
		openCoursePageBtn.setOnClickListener(this);
        openSelectedCourseBtn.setOnClickListener(this);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);

        Globe.monitor_courseNo = Integer.parseInt(settings.getString(
                "monitor_courseNo", "0"));
        Globe.monitor_courseId = Integer.parseInt(settings.getString(
                "monitor_courseId", "0"));
        Globe.monitor_courseName = settings.getString("monitor_courseName",
                "");
        Globe.monitor_url = settings.getString("monitor_url", "");

        confirmInfoBtn.setEnabled(false);
        startService.setEnabled(false);

        mImageView = (ImageView) findViewById(R.id.menu_icon_course);
        mImageView.setImageResource(R.drawable.ic_action_location_found);
        mLinearLayout = (LinearLayout) findViewById(R.id.menu_course_choose);
        mLinearLayout.setBackgroundResource(R.drawable.menu2);
        mLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.showContent();
            }
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		autorun.setChecked(Globe.monitor_auto_startup);
        if (Globe.monitor_courseNo != 0) {
            courseNo.setText(String.valueOf(Globe.monitor_courseNo));
        }
        if (Globe.monitor_courseId != 0) {
            courseId.setText(String.valueOf(Globe.monitor_courseId));
        }
		//courseName.setText(Globe.monitor_courseName);
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
            Globe.monitor_courses = true;
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
			//edit.putString("monitor_courseName", courseName.getEditableText()
			//		.toString().trim());
            edit.putString("monitor_url", "http://jw.dhu.edu.cn/dhu/student/selectcourse/selectcourse2.jsp?" +
                    "courseNo=" + courseNo.getEditableText() + "&courseId="+courseId.getEditableText() + "&courseName=" + Globe.monitor_courseName);
			edit.commit();

			Globe.monitor_courseNo = Integer.parseInt(settings.getString(
					"monitor_courseNo", "0"));
			Globe.monitor_courseId = Integer.parseInt(settings.getString(
					"monitor_courseId", "0"));
			Globe.monitor_courseName = settings.getString("monitor_courseName",
					"");
            Globe.monitor_url = settings.getString("monitor_url", "");

            startService.setEnabled(true);
			break;
		case R.id.monitor_search:
            //System.out.println("Button clicked!");
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET, "http://jw.dhu.edu.cn/dhu/student/selectcourse/selectcourse2.jsp?" +
                    "courseNo=" + courseNo.getEditableText() + "&courseId="+courseId.getEditableText() + "&courseName=", null, true);
			req.setPipIndex(NetConstant.XKCX_SEARCH);
			mNetClient.sendRequest(req);
            //mNetClient.start();
			break;
		case R.id.monitor_open_course_page:
			Intent it = new Intent(this, WebViewScreen.class);
			it.putExtra("url", NetConstant.URL_COURSE_CHOOSE);
			it.putExtra("name", "选课对照表");
			it.putExtra("needCookies", true);
			startActivity(it);
			break;
        case R.id.monitor_open_selected_course:
            Intent it2 = new Intent(this, WebViewScreen.class);
            it2.putExtra("url", NetConstant.URL_COURSE_SELECTED);
            it2.putExtra("name", "选课情况");
            it2.putExtra("needCookies", true);
            startActivity(it2);
            break;
		default:
			break;
		}
	}

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		// TODO Auto-generated method stub
        System.out.println("hand Response");
        if (myHttpResponse.getPipIndex() == NetConstant.XKCX_SEARCH) {
            try {
                Document doc = myHttpResponse.getData();
                Element ele = doc.body();
                ele = ele.getElementById("AutoNumber2");

                Elements eles = ele.select("td");
                //System.out.println(eles.html());

                String max_admit = eles.get(10).text();
                String already_select = eles.get(11).text();
                String already_admit = eles.get(12).text();

                courseInfo.setText("最大录取人数:"+max_admit+"\n已选人数:"+already_select+"\n已录取人数:"+already_admit);

                confirmInfoBtn.setEnabled(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

//	@Override
//	public void onBackPressed() {
//        if (Globe.monitor_courses) {
//            if (Globe.monitor_auto_startup) {
//                Toast.makeText(this, Messages.getString("MonitorScreen.monitor_service_still_running_background"), //$NON-NLS-1$
//                        Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, Messages.getString("MonitorScreen.monitor_service_will_be_killed"), //$NON-NLS-1$
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//        finish();
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.actionbar_list, menu);
		return true;
	}

}