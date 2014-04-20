package com.wujy.dhuhelper.view;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.Util;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.db.SharedDataUtil;
import com.wujy.dhuhelper.monitor.MonitorService;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetConstant;

public class InitScreen extends WindowActivity {

	private TextView init_title;

	@SuppressWarnings("deprecation")
	private void getAPN() {
		try {
			init_title.setText(Messages.getString("InitScreen.check_network")); //$NON-NLS-1$
			// 通过context得到ConnectivityManager连接管理
			ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			// 通过ConnectivityManager得到NetworkInfo网络信息
			NetworkInfo info = manager.getActiveNetworkInfo();
			// 获取NetworkInfo中的apn信息
			if (info == null) {
				System.out.println("没有可用的网络");
				showDialog(NetConstant.MSG_NONE_NET);
			} else {
				if (Globe.news_load_when_startup) {
					init_title.setText(Messages.getString("InitScreen.loading_news")); //$NON-NLS-1$
					MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
							NetConstant.URL_NEWS_DHXW, null, false);
					req.setPipIndex(NetConstant.NEWS_DHXW);
					mNetClient.sendRequest(req);
					System.out.println("开始连接首页");
				} else {
					init_title.setText(Messages.getString("InitScreen.initialize_success")); //$NON-NLS-1$
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(2000);
								Intent it = new Intent(InitScreen.this,
										NewsScreen.class);
								it.putExtra("RadioChecked", '0');
								startActivity(it);
								
								Looper.prepare();
								Toast.makeText(InitScreen.this, Messages.getString("InitScreen.startup_hint_load_news_manually"), //$NON-NLS-1$
										Toast.LENGTH_LONG).show();
								finish();
								Looper.loop();
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handResponse(final MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.NEWS_DHXW) {
			init_title.setText(Messages.getString("InitScreen.making_news_list")); //$NON-NLS-1$

			new Thread() {
				@Override
				public void run() {
					Globe.mFirstListItems.clear();
		
					Document doc = myHttpResponse.getData();
					Elements tables = doc.select("table[width=96%]");
					Elements items = tables.select("td[width=96%]");
					for (Element item : items) {
						String[] arr = new String[3];
						arr[0] = NewsScreen.MAIN_URL_DHU
								+ item.select("a").attr("href");
						arr[1] = item.select("a").text();
						arr[2] = item.text().substring(
								item.text().lastIndexOf("(") + 1,
								item.text().length() - 1);
						Globe.mFirstListItems.add(arr);
					}
		
					System.out.println("获取首页信息成功");
					//init_title.setText(Messages.getString("InitScreen.initialize_success")); //$NON-NLS-1$
					Intent it = new Intent(InitScreen.this, NewsScreen.class);
					it.putExtra("RadioChecked", '0');
					startActivity(it);
					
					Looper.prepare();
					Toast.makeText(InitScreen.this, Messages.getString("InitScreen.startup_hint_load_news_auto"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
					finish();
					Looper.loop();
				}
			}.start();
			// mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.init_screen_layout);
				
		init_title = (TextView) findViewById(R.id.init_title);
		
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, "hrw9hwUY54DIbhYOjvd4kMVr");
				//Util.getMetaValue(InitScreen.this, "api_key"));
		//List<String> tags = Util.getTagsList(textviewGid.getText().toString());
		List<String> tags = Util.getTagsList("1.5");
        PushManager.setTags(getApplicationContext(), tags);
		
		Intent it = new Intent();
		it.setClass(this, MonitorService.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(it);
	}
	
	@Override
	protected void onResume() {
		init_title.setText(Messages.getString("InitScreen.loading_preferences")); //$NON-NLS-1$
		super.onResume();
		
		long tenMinutes = 15 * 60 * 1000;

		final SharedPreferences[] settings = new SharedPreferences[5];
		settings[0] = getSharedPreferences("dhuhelper_cache_news_dhxw", Context.MODE_MULTI_PROCESS);
		settings[1] = getSharedPreferences("dhuhelper_cache_news_xngg", Context.MODE_MULTI_PROCESS);
		settings[2] = getSharedPreferences("dhuhelper_cache_news_jwxx", Context.MODE_MULTI_PROCESS);
		
		//final SharedDataUtil settings = SharedDataUtil.getInstance(this); 
		Globe.cache_news_dhxw_time = settings[0].getLong("cache_news_dhxw_time", 0);
		Globe.cache_news_xngg_time = settings[1].getLong("cache_news_xngg_time", 0);
		Globe.cache_news_jwxx_time = settings[2].getLong("cache_news_jwxx_time", 0);
		
		System.out.println("timepast: " + (System.currentTimeMillis() - Globe.cache_news_dhxw_time));

		// 后台已刷新新闻缓存，且时间在可接受范围内（暂定15分钟）
		if (System.currentTimeMillis() - Globe.cache_news_dhxw_time < tenMinutes) {
			new Thread() {
				@Override
				public void run() {
					String saveNewsString_dhxw = settings[0].getString("cache_news_dhxw_string", "");
					String saveNewsString_xngg = settings[1].getString("cache_news_xngg_string", "");
					String saveNewsString_jwxx = settings[2].getString("cache_news_jwxx_string", "");
					
					Globe.mFirstListItems.clear();
					String[] tempList = saveNewsString_dhxw.split("@");
					for (String temp : tempList) {
						String[] item = temp.split("#");
						Globe.mFirstListItems.add(item);
					}
					
					Globe.mSecondListItems.clear();
					tempList = saveNewsString_xngg.split("@");
					for (String temp : tempList) {
						String[] item = temp.split("#");
						Globe.mSecondListItems.add(item);
					}
					
					Globe.mThirdListItems.clear();
					tempList = saveNewsString_jwxx.split("@");
					for (String temp : tempList) {
						String[] item = temp.split("#");
						Globe.mThirdListItems.add(item);
					}
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					System.out.println("获取首页信息成功");
					//init_title.setText(Messages.getString("InitScreen.initialize_success")); //$NON-NLS-1$
					Intent it = new Intent(InitScreen.this, NewsScreen.class);
					it.putExtra("RadioChecked", '0');
					startActivity(it);
					
					Looper.prepare();
					Toast.makeText(InitScreen.this, Messages.getString("InitScreen.startup_hint_load_news_auto"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
					finish();
					
					Looper.loop();

				}
			}.start();
			
		} else {
		
			getAPN();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.actionbar_list, menu);
		return true;
	}
}
