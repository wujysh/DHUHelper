package org.sjutas.dhu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener; 
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.android.feedback.FeedbackManager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.sjutas.dhu.monitor.MonitorScreen;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetClient;
import org.sjutas.dhu.net.NetConstant;
import org.sjutas.dhu.view.AboutScreen;
import org.sjutas.dhu.view.BookBorrowScreen;
import org.sjutas.dhu.view.BookSearchScreen;
import org.sjutas.dhu.view.CheckUpdateScreen;
import org.sjutas.dhu.view.CommonQueryScreen;
import org.sjutas.dhu.view.ExamListScreen;
import org.sjutas.dhu.view.LoginScreen;
import org.sjutas.dhu.view.MeetingInfoScreen;
import org.sjutas.dhu.view.NewsScreen;
import org.sjutas.dhu.view.PointSearchScreen;
import org.sjutas.dhu.view.PreferenceScreen;
import org.sjutas.dhu.view.RunningQueryScreen;
import org.sjutas.dhu.view.ScoreSearchScreen;
import org.sjutas.dhu.view.ScorelistSearchScreen;
import org.sjutas.dhu.view.StudentInfoScreen;
import org.sjutas.dhu.view.TimeTableScreen;
import org.sjutas.dhu.view.UserScreen;

/**
 * @author wujy 程序中activity的基类
 */
public abstract class WindowActivity extends SherlockActivity implements OnClickListener {
	protected static final int REQ_SYSTEM_SETTINGS = 0;
	public AlertDialog mAlertDialog;
	private Dialog progressDialog;

	protected NetClient mNetClient;

	public SlidingMenu menu;
	public ScrollView mScrollView;
	//public TextView mUserInfoTextView;
    public TextView mJwInfo, mJwStatus, mLibInfo, mLibStatus;
    public Button mJwChange, mLibChange;

	boolean isfinished = false;
	// private Dialog mNetLoadingDialog;
	protected Context mContext;

	// private static final int PROGRESS_ID = 1;

	public void dismissNetLoadingDialog() {
		// if (mNetLoadingDialog != null && mNetLoadingDialog.isShowing()) {
		// mNetLoadingDialog.dismiss();
		// }
		System.out.println("dismissNetloadingDialog() executed");
		if (isFinishing()) {
            return;
        }
		if (progressDialog != null && progressDialog.isShowing()) {
			dismissDialog(NetConstant.MSG_NET_LOADING);
		}
	}

	public abstract void handResponse(MyHttpResponse myHttpResponse);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

		prepareNetClient();

		mContext = this;
	}

	public void prepareNetClient() {
		if (Globe.sNetClient == null) {
			Globe.sNetClient = new NetClient(this);
		}
		if (Globe.sNetClient.isRunning == false) {
			Globe.sNetClient.getmRequestTemp().clear();
			Globe.sNetClient.isRunning = true;
			Globe.sNetClient.start();
		}
		Globe.sNetClient.saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
		//Globe.sNetClient.saveCacheSettings = SharedDataUtil.getInstance(this); 
		Globe.sNetClient.setCurrentWindow(this);
		mNetClient = Globe.sNetClient;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == NetConstant.MSG_NONE_NET) {
			mAlertDialog = Util.createAlertDialog(this,
					this.getString(R.string.tishi),
					this.getString(R.string.none_net),
					this.getString(R.string.quit), null,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mNetClient.isRunning = false;
							finish();
						}
					}, null);
			return mAlertDialog;
		} else if (id == NetConstant.MSG_NET_ERROR) {
			mAlertDialog = Util.createAlertDialog(this,
					this.getString(R.string.tishi),
					this.getString(R.string.net_error),
					this.getString(R.string.wait),
					this.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							if (mNetClient.mRetryRequest.isBlock) {
								showNetLoadingDialog();
							}
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mNetClient.isRunning = false;
						}
					});
			return mAlertDialog;
		} else if (id == NetConstant.MSG_PASSWORD_ERROR) {
			mAlertDialog = Util.createAlertDialog(this,
					this.getString(R.string.tishi),
					this.getString(R.string.password_error),
					this.getString(R.string.confirm), null,
					// this.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// mNetClient.isRunning = false;
							// finish();
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			return mAlertDialog;
		} else if (id == NetConstant.MSG_SERVER_ERROR) {
			mAlertDialog = Util.createAlertDialog(this,
					this.getString(R.string.tishi),
					this.getString(R.string.server_error),
					this.getString(R.string.confirm), null,
					// this.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// mNetClient.isRunning = false;
							// finish();
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			return mAlertDialog;
		} else if (id == NetConstant.MSG_NET_LOADING) {
			if(null == progressDialog) {
				progressDialog = ProgressDialog.show(mContext, null,
						mContext.getString(R.string.loading));
				progressDialog.setCancelable(false);
			} else {
				progressDialog.show();
			}
			progressDialog.setOnKeyListener(onKeyListener);
			return progressDialog;
		} else {
			mAlertDialog = Util.createAlertDialog(this,
					this.getString(R.string.tishi),
					this.getString(R.string.other_errors),
					this.getString(R.string.confirm), null,
					// this.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// mNetClient.isRunning = false;
							// finish();
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
		}
		return mAlertDialog;
	}

	/** 
     * add a key listener for progress dialog to cancel web request
     */  
    public OnKeyListener onKeyListener = new OnKeyListener() {  
        @Override  
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {  
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {  
            	if (isfinished) {
            		if (mNetClient != null) {
            			mNetClient.isRunning = false;
            			//mNetClient = null;
            		}
            		dismissNetLoadingDialog();
            		if (menu != null) {
        				menu.toggle();
        			} else {
        				finish();
        			}
            	} else {
	            	Toast toast = Toast.makeText(mContext, "再按一次取消操作", Toast.LENGTH_SHORT);
	            	toast.setDuration(2000);
	    			toast.show();
	    			
	            	new Thread() {
	    				@Override
	    				public void run() {
	    					isfinished = true;
	    					try {
	    						Thread.sleep(2000);
	    						isfinished = false;
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
	    				};
	    			}.start();
            	}
            	
            	//dismissNetLoadingDialog();  
            }  
            return false;  
        }  
    };
	
	@Override
	protected void onDestroy() {
		// if (!(this instanceof HomePageScreen)) {
		// Globe.sNetClient.getmRequestTemp().clear();
		// }
		dismissNetLoadingDialog();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
		}
		return super.onKeyDown(keyCode, event);

	}

	public void showNetErrorDialog() {

	}

	public void showNetLoadingDialog() {
		showDialog(NetConstant.MSG_NET_LOADING);
	}

	public void showTipDialog(String str) {

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		if (isfinished) {
			Globe.clearAll();
			finish();
		} else {
			Toast toast = Toast.makeText(this, Messages.getString("WindowActivity.confirm_exit"), Toast.LENGTH_SHORT);
			toast.setDuration(2000);
			toast.show(); //$NON-NLS-1$

			if (menu != null) {
				menu.toggle();
			}

			new Thread() {
				@Override
				public void run() {
					isfinished = true;
					try {
						Thread.sleep(2000);
						isfinished = false;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	}

	public void setSlidingMenu() {
		// configure the Home icon
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.logo_actionbar);

		// configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT_RIGHT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		
		menu.setMenu(R.layout.menu);
		menu.setSecondaryMenu(R.layout.menu2);
		
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setSecondaryShadowDrawable(R.drawable.shadow2);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		
		mScrollView = (ScrollView) findViewById(R.id.menu_all);

		// mUserInfoTextView = (TextView) findViewById(R.id.menu_title_user);
		// if (Globe.sName_jw != null) {
		// mUserInfoTextView.setText(Globe.sName_jw);
		// } else {
		// mUserInfoTextView.setText("账户信息");
		// }
		
		// set OnClickListener to the SlidingMenu
		// User
		//findViewById(R.id.menu_user).setOnClickListener(this);
		findViewById(R.id.menu_user).setBackgroundResource(R.drawable.menu2);
		
		// News
		findViewById(R.id.menu_dhxw).setOnClickListener(this);
		findViewById(R.id.menu_xngg).setOnClickListener(this);
		findViewById(R.id.menu_jwxx).setOnClickListener(this);
		// Jwc
		findViewById(R.id.menu_score_search).setOnClickListener(this);
		findViewById(R.id.menu_scorelist_search).setOnClickListener(this);
		findViewById(R.id.menu_point_search).setOnClickListener(this);
		findViewById(R.id.menu_exam_search).setOnClickListener(this);
		findViewById(R.id.menu_timetable).setOnClickListener(this);
		findViewById(R.id.menu_course_choose).setOnClickListener(this);
		findViewById(R.id.menu_userinfo).setOnClickListener(this);
		findViewById(R.id.menu_common_query).setOnClickListener(this);
		// Library
		findViewById(R.id.menu_book_borrow).setOnClickListener(this);
		findViewById(R.id.menu_book_search).setOnClickListener(this);
		// Others
        findViewById(R.id.menu_meeting_info).setOnClickListener(this);
        findViewById(R.id.menu_running_query).setOnClickListener(this);

		findViewById(R.id.menu_settings).setOnClickListener(this);
		findViewById(R.id.menu_update_check).setOnClickListener(this);
        findViewById(R.id.menu_feedback).setOnClickListener(this);
		findViewById(R.id.menu_about).setOnClickListener(this);

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
					//it = new Intent(mContext, UserScreen.class);
					it = new Intent(mContext, NewsScreen.class);
                    //it.putExtra("RadioChecked", '0');
				} else {
					it = new Intent(mContext, LoginScreen.class);
					it.putExtra("where", "JWC");
					//it.putExtra("NextScreen", UserScreen.class);
					it.putExtra("NextScreen", mContext.getClass());
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
					//it = new Intent(mContext, UserScreen.class);
					it = new Intent(mContext, NewsScreen.class);
                    //it.putExtra("RadioChecked", '0');
				} else {
					it = new Intent(mContext, LoginScreen.class);
					it.putExtra("where", "LIB");
					//it.putExtra("NextScreen", UserScreen.class);
					it.putExtra("NextScreen", mContext.getClass());
				}
				Globe.sMenuPosX = mScrollView.getScrollX();
				Globe.sMenuPosY = mScrollView.getScrollY();
				startActivity(it);
				finish();
			}
		});

        refreshMenuInfo();
	}

    public void refreshMenuInfo() {
        if (Globe.isLoginJW) {
            mJwChange.setText(R.string.logout);
            mJwStatus.setText(R.string.logged);
            mJwInfo.setText("学号：" + Globe.sId_jw + "\n姓名：" + Globe.sName_jw);
        } else {
            mJwChange.setText(R.string.login);
            mJwStatus.setText(R.string.unlogged);
            mJwInfo.setText("校内服务需要登陆");
        }

        if (Globe.isLoginLib) {
            mLibChange.setText(R.string.logout);
            mLibStatus.setText(R.string.logged);
            mLibInfo.setText("学号：" + Globe.sId_lib + "\n姓名：" + Globe.sName_lib
                    + "\n" + Globe.sMsg_lib);
        } else {
            mLibChange.setText(R.string.login);
            mLibStatus.setText(R.string.unlogged);
            mLibInfo.setText("校内服务需要登陆");
        }
    }



	@Override
	public void onClick(View v) {
		Intent it = null;
		
		// User
		switch (v.getId()) {
		case R.id.menu_user:
			it = new Intent(mContext, UserScreen.class);
			break;
		case R.id.menu_dhxw:
			it = new Intent(mContext, NewsScreen.class);
			it.putExtra("RadioChecked", '0');
			break;
		case R.id.menu_xngg:
			it = new Intent(mContext, NewsScreen.class);
			it.putExtra("RadioChecked", '1');
			break;
		case R.id.menu_jwxx:
			it = new Intent(mContext, NewsScreen.class);
			it.putExtra("RadioChecked", '2');
			break;
		}
		if (it != null) {
			startActivity(it);
			finish();
			return;
		}
		
		// Jwc
		if (Globe.isLoginJW) {
			switch (v.getId()) {
			case R.id.menu_score_search:
				it = new Intent(mContext, ScoreSearchScreen.class);
				break;
			case R.id.menu_scorelist_search:
				it = new Intent(mContext, ScorelistSearchScreen.class);
				break;
			case R.id.menu_point_search:
				it = new Intent(mContext, PointSearchScreen.class);
				break;
			case R.id.menu_exam_search:
				it = new Intent(mContext, ExamListScreen.class);
				break;
			case R.id.menu_timetable:
				it = new Intent(mContext, TimeTableScreen.class);
				break;
			case R.id.menu_course_choose:
				// TODO: Finish course choose activity
				//Toast.makeText(mContext, Messages.getString("WindowActivity.developing"), //$NON-NLS-1$
				//		Toast.LENGTH_LONG).show();
				it = new Intent(mContext, MonitorScreen.class);
				//startActivity(it);
				//it = new Intent(mContext, MonitorScreen.class);
				break;
				//return;
			case R.id.menu_userinfo:
				it = new Intent(mContext, StudentInfoScreen.class);
				break;
			case R.id.menu_common_query:
				it = new Intent(mContext, CommonQueryScreen.class);
				break;
			}
		} else {
			it = new Intent(mContext, LoginScreen.class);
			it.putExtra("where", "JWC");
			switch (v.getId()) {
			case R.id.menu_score_search:
				it.putExtra("NextScreen", ScoreSearchScreen.class);
				break;
			case R.id.menu_scorelist_search:
				it.putExtra("NextScreen", ScorelistSearchScreen.class);
				break;
			case R.id.menu_point_search:
				it.putExtra("NextScreen", PointSearchScreen.class);
				break;
			case R.id.menu_exam_search:
				it.putExtra("NextScreen", ExamListScreen.class);
				break;
			case R.id.menu_timetable:
				it.putExtra("NextScreen", TimeTableScreen.class);
				break;
			case R.id.menu_course_choose:
				// TODO: Finish course choose activity
				//Toast.makeText(mContext, Messages.getString("WindowActivity.developing"), //$NON-NLS-1$
				//		Toast.LENGTH_LONG).show();
                it.putExtra("NextScreen", MonitorScreen.class);
				//it = new Intent(mContext, MonitorScreen.class);
				//startActivity(it);
				//it.putExtra("NextScreen", MonitorScreen.class);
				break;
				//return;
			case R.id.menu_userinfo:
				it.putExtra("NextScreen", StudentInfoScreen.class);
				break;
			case R.id.menu_common_query:
				it.putExtra("NextScreen", CommonQueryScreen.class);
				break;
			default:
				it = null;
				break;
			}
		}
		if (it != null) {
			startActivity(it);
			finish();
			return;
		}
		
		// Library
		if (Globe.isLoginLib) {
			switch (v.getId()) {
			case R.id.menu_book_borrow:
				it = new Intent(mContext, BookBorrowScreen.class);
				break;
			case R.id.menu_book_search:
				it = new Intent(mContext, BookSearchScreen.class);
				break;
			}
		} else {
			it = new Intent(mContext, LoginScreen.class);
			it.putExtra("where", "LIB");
			switch (v.getId()) {
			case R.id.menu_book_borrow:
				it.putExtra("NextScreen", BookBorrowScreen.class);
				break;
			case R.id.menu_book_search: // 无需登录
				it = new Intent(mContext, BookSearchScreen.class);
				break;
			default:
				it = null;
				break;
			}
		}
		if (it != null) {
			startActivity(it);
			finish();
			return;
		}

        // Others
        if (Globe.isLoginJW || Globe.isLoginLib) {
            switch (v.getId()) {
                case R.id.menu_running_query:
                    it = new Intent(mContext, RunningQueryScreen.class);
                    break;
            }
        } else {
            it = new Intent(mContext, LoginScreen.class);
            it.putExtra("where", "JWC");
            switch (v.getId()) {
                case R.id.menu_running_query:
                    it.putExtra("NextScreen", RunningQueryScreen.class);
                    break;
                default:
                    it = null;
                    break;
            }
        }
        if (it != null) {
            startActivity(it);
            finish();
            return;
        }

		switch (v.getId()) {
        case R.id.menu_meeting_info:
            it = new Intent(mContext, MeetingInfoScreen.class);
            break;

		case R.id.menu_settings:
			it = new Intent(mContext, PreferenceScreen.class);
			startActivityForResult(it, REQ_SYSTEM_SETTINGS);
			return;
		case R.id.menu_update_check:
			it = new Intent(mContext, CheckUpdateScreen.class);
			break;
        case R.id.menu_feedback:
            //it = new Intent(mContext, FeedbackScreen.class);
            FeedbackManager.getInstance(this).startFeedbackActivity();
            return;
		case R.id.menu_about:
			it = new Intent(mContext, AboutScreen.class);
			break;
		}
		if (it != null) {
            System.out.println(it.toURI());
			startActivity(it);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.actionbar_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent it = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			menu.toggle();
			return false;
		case R.id.user:
			//it = new Intent(mContext, UserScreen.class);
			//break;
			menu.showSecondaryMenu();
			return false;
		//case R.id.settings:
		//	it = new Intent(mContext, PreferenceScreen.class);
		//	startActivityForResult(it, REQ_SYSTEM_SETTINGS);
		//	return false;
		//case R.id.update:
		//	it = new Intent(mContext, CheckUpdateScreen.class);
		//	break;
		//case R.id.about:
		//	it = new Intent(mContext, AboutScreen.class);
		//	break;
		//default:
		//	return false;
		}

		if (mScrollView != null) {
			Globe.sMenuPosX = mScrollView.getScrollX();
			Globe.sMenuPosY = mScrollView.getScrollY();
		} else {
			Globe.sMenuPosX = Globe.sMenuPosY = 0;
		}

		startActivity(it);
		finish();

		return false;
	}

	// Settings设置界面返回的结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SYSTEM_SETTINGS) {
			setGlobeSettings();
		} else {
			// 其他Intent返回的结果
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		setGlobeSettings();

		if (mScrollView != null) {
			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.smoothScrollTo(Globe.sMenuPosX, Globe.sMenuPosY);
				}
			});
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (mScrollView != null) {
			Globe.sMenuPosX = mScrollView.getScrollX();
			Globe.sMenuPosY = mScrollView.getScrollY();
		}
	}
	
	protected void setGlobeSettings() {
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

			Globe.news_load_when_startup = settings.getBoolean(
					"news_load_when_startup", true);
			Globe.news_detail_load_images = settings.getBoolean(
					"news_detail_load_images", true);
			Globe.news_detail_reset_style = settings.getBoolean(
					"news_detail_reset_style", true);
			Globe.news_detail_images_width = Integer.parseInt(settings
					.getString("news_detail_images_width", "300"));

			Globe.autologin_jw = settings.getBoolean("autologin_jw", false);
			Globe.autologin_lib = settings.getBoolean("autologin_lib", false);

			Globe.monitor_auto_startup = settings.getBoolean(
					"monitor_auto_startup", true);
            Globe.monitor_courses_auto_startup = settings.getBoolean(
                    "monitor_courses_auto_startup", false);
			Globe.monitor_refresh_interval = Integer.parseInt(settings
					.getString("monitor_refresh_interval", "60000"));
			Globe.monitor_courseNo = Integer.parseInt(settings.getString(
					"monitor_courseNo", "0"));
			Globe.monitor_courseId = Integer.parseInt(settings.getString(
					"monitor_courseId", "0"));
			Globe.monitor_courseName = settings.getString("monitor_courseName",
					"");
			Globe.monitor_auto_select_when_available = settings.getBoolean(
					"monitor_auto_select_when_available", false);

			//Globe.sCookieStringAll = settings.getString("CookieStringAll", "");
			
			Globe.monitor_courses = settings.getBoolean("monitor_courses", false);
			
			Globe.monitor_dhxw = settings.getBoolean("monitor_dhxw", true);
			Globe.monitor_xngg = settings.getBoolean("monitor_xngg", true);
			Globe.monitor_jwxx = settings.getBoolean("monitor_jwxx", true);
			Globe.monitor_scorelist = settings.getBoolean("monitor_scorelist", false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
