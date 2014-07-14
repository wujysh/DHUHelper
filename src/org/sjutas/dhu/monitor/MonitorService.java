package org.sjutas.dhu.monitor;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;
import org.sjutas.dhu.view.WebViewScreen;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class MonitorService extends Service {

	public static final String TAG = "MonitorService";

	private MonitorNetClient mNetClient;
	private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	private MyHttpRequest req;

	private String PreContent, CurContent;

	private int interval = Globe.monitor_refresh_interval;
	
	////////////
	private MessageThread messageThread_dhxw = null, messageThread_xngg = null, 
			messageThread_jwxx = null, messageThread_scorelist = null, messageThread_courses = null;
	
	private int notificationID = 1000;
	private Notification notification = null, notification_main = null;
	private NotificationManager notificationManager = null;
	
	private PendingIntent pendingIntent = null, monitorScreenPendingIntent = null;
	private Intent intent = null, monitorScreenIntent = null;
	
	private CharSequence monitorScreenContentTitle ="东华助手选课监控"; // 通知栏标题   
    private CharSequence monitorScreenContentText ="课程信息暂无变化"; // 通知栏内容 
    private CharSequence contentTitle = "东华助手";
    private CharSequence[] contentText = {"有新的东华新闻，点击查看详情","有新的校内公告，点击查看详情","有新的教务信息，点击查看详情", "您的成绩单更新了，点击查看详情", "您关注的课程有余额，请抓紧时间"};
    
    private String[] cache, cache_string;
    private long[] cache_last_update_time;
    
    private Map <String, String> cookies = new HashMap <String, String>();
    
    //SharedDataUtil saveCacheSettings = SharedDataUtil.getInstance(this); 
    private SharedPreferences[] saveCacheSettings = new SharedPreferences[5];

	@SuppressWarnings("deprecation")
	@Override
	public synchronized void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate() executed");

		// 创建一个NotificationManager的引用
		notificationManager = (NotificationManager)    
	            this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);   
		
		// 定义Notification的各种属性   
        notification = new Notification(R.drawable.ic_launcher, "东华助手", System.currentTimeMillis());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
        
        // TODO: 增加通用性，跳转到相对应的Activity
        intent = new Intent(MonitorService.this, WebViewScreen.class); // 点击该通知后要跳转的Activity   
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
              
        // 获取设置信息
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		PreContent = settings.getString("monitor_webpage_content", "");
		Globe.monitor_auto_startup = settings.getBoolean("monitor_auto_startup", true);
        Globe.monitor_courses_auto_startup = settings.getBoolean("monitor_courses_auto_startup", false);
		interval = Globe.monitor_refresh_interval = Integer.parseInt(settings.getString("monitor_refresh_interval", "60000"));
		Globe.monitor_courseNo = Integer.parseInt(settings.getString("monitor_courseNo", "0"));
		Globe.monitor_courseId = Integer.parseInt(settings.getString("monitor_courseId", "0"));
		Globe.monitor_courseName = settings.getString("monitor_courseName", "");
		Globe.monitor_auto_select_when_available = settings.getBoolean("monitor_auto_select_when_available", false);
		Globe.monitor_courses = settings.getBoolean("monitor_courses", false);
		
		// 获取缓存和相关设置
		saveCacheSettings[0] = getSharedPreferences("dhuhelper_cache_news_dhxw", Context.MODE_MULTI_PROCESS);
		saveCacheSettings[1] = getSharedPreferences("dhuhelper_cache_news_xngg", Context.MODE_MULTI_PROCESS);
		saveCacheSettings[2] = getSharedPreferences("dhuhelper_cache_news_jwxx", Context.MODE_MULTI_PROCESS);
		saveCacheSettings[3] = getSharedPreferences("dhuhelper_cache_scorelist", Context.MODE_MULTI_PROCESS);
		saveCacheSettings[4] = getSharedPreferences("dhuhelper_cache_courses", Context.MODE_MULTI_PROCESS);

		cache = new String[5];
		cache_string = new String[5];
		cache_last_update_time = new long[5];
		long lasttime = System.currentTimeMillis() - 10 * 60 * 1000;
		cache[0] = saveCacheSettings[0].getString("cache_news_dhxw", "");
		cache_string[0] = saveCacheSettings[0].getString("cache_news_dhxw_string", "");
		cache_last_update_time[0] = saveCacheSettings[0].getLong("cache_news_dhxw_time", lasttime);
		cache[1] = saveCacheSettings[1].getString("cache_news_xngg", "");
		cache_string[1] = saveCacheSettings[1].getString("cache_news_xngg_string", "");
		cache_last_update_time[1] = saveCacheSettings[1].getLong("cache_news_xngg_time", lasttime);
		cache[2] = saveCacheSettings[2].getString("cache_news_jwxx", "");
		cache_string[2] = saveCacheSettings[2].getString("cache_news_jwxx_string", "");
		cache_last_update_time[2] = saveCacheSettings[2].getLong("cache_news_jwxx_time", lasttime);
		cache[3] = saveCacheSettings[3].getString("cache_scorelist", "");
		cache_string[3] = saveCacheSettings[3].getString("cache_scorelist_string", "");
		cache_last_update_time[3] = saveCacheSettings[3].getLong("cache_scorelist_time", lasttime);
		
		if (Globe.monitor_courses) {  // 是否监控选课，如监控选课则挂出常驻通知且startForeground，否则仅在有新消息时挂通知提醒
			
			cache[4] = saveCacheSettings[4].getString("cache_courses", "");
			cache_string[4] = saveCacheSettings[4].getString("cache_courses_string", "");
			cache_last_update_time[4] = saveCacheSettings[4].getLong("cache_courses_time", lasttime);
			
			// 设置通知的事件消息   
	        CharSequence monitorScreenContentTitle ="东华助手选课监控"; // 通知栏标题   
	        CharSequence monitorScreenContentText ="课程信息暂无变化"; // 通知栏内容   
	        monitorScreenIntent = new Intent(MonitorService.this, MonitorScreen.class); // 点击该通知后要跳转的Activity   
	        monitorScreenPendingIntent = PendingIntent.getActivity(this, 0, monitorScreenIntent, 0);   
	        notification_main = new Notification(R.drawable.ic_launcher, "东华助手选课监控已启动", System.currentTimeMillis());
	        notification_main.setLatestEventInfo(this, monitorScreenContentTitle, monitorScreenContentText, monitorScreenPendingIntent);   
			//notification_main.defaults = Notification.DEFAULT_ALL;
	        startForeground(1, notification_main);
            Log.d("MonitorService", "startForeground!");

	        if (mNetClient == null) {
				mNetClient = new MonitorNetClient(this);
			}
			if (mNetClient.isRunning == false) {
				mNetClient.getmRequestTemp().clear();
				mNetClient.isRunning = true;
				mNetClient.start();
			}
			mNetClient.setCurrentService(this);

			params.add(new BasicNameValuePair("courseNo", String.valueOf(Globe.monitor_courseNo)));
			params.add(new BasicNameValuePair("courseId", String.valueOf(Globe.monitor_courseId)));
			params.add(new BasicNameValuePair("courseName", Globe.monitor_courseName));
			req = new MyHttpRequest(NetConstant.TYPE_GET, NetConstant.URL_COURSE_SEARCH, params, false);
			req.setPipIndex(NetConstant.MONITOR);

			mNetClient.sendRequest(req);
        }

	}
	

	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand() executed");
		//return super.onStartCommand(intent, flags, startId);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Globe.monitor_dhxw = settings.getBoolean("monitor_dhxw", true);
		Globe.monitor_xngg = settings.getBoolean("monitor_xngg", true);
		Globe.monitor_jwxx = settings.getBoolean("monitor_jwxx", true);
		Globe.monitor_scorelist = settings.getBoolean("monitor_scorelist", false);
        Globe.monitor_courses = settings.getBoolean("monitor_courses", false);
		
		SharedPreferences saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
		Globe.sCookieStringAll = saveCacheSettings.getString("CookieStringAll", "");
		
		//开启线程，防止重复开启，造成失控
		if (Globe.monitor_dhxw) {
			if (messageThread_dhxw == null) {
		        messageThread_dhxw = new MessageThread();
		        messageThread_dhxw.url = NetConstant.URL_NEWS_DHXW;
		        messageThread_dhxw.id = 0;
		        messageThread_dhxw.isRunning = true;
		        messageThread_dhxw.start();
			}
		} else if (messageThread_dhxw != null) {
			messageThread_dhxw.isRunning = false;
		}

		if (Globe.monitor_xngg) {
			if (messageThread_xngg == null) {
				messageThread_xngg = new MessageThread();
				messageThread_xngg.url = NetConstant.URL_NEWS_XNGG;
			    messageThread_xngg.id = 1;
		        messageThread_xngg.isRunning = true;
		        messageThread_xngg.start();
			}
		} else if (messageThread_xngg != null) {
			messageThread_xngg.isRunning = false;
		}
		
		if (Globe.monitor_xngg) {
			if (messageThread_jwxx == null) {
				messageThread_jwxx = new MessageThread();
				messageThread_jwxx.url = NetConstant.URL_NEWS_JWXX;
			    messageThread_jwxx.id = 2;
		        messageThread_jwxx.isRunning = true;
		        messageThread_jwxx.start();
			}
		} else if (messageThread_jwxx != null) {
			messageThread_jwxx.isRunning = false;
		}
		
		// Add cookies
		if (Globe.monitor_courses || Globe.monitor_scorelist) {
			if (cookies != null) cookies.clear();
			
			//Log.i(TAG, "Thread #3|4 cookiesAll: " + Globe.sCookieStringAll);
			if (Globe.sCookieStringAll == "") return START_STICKY;
			
			Globe.sCookieString = Globe.sCookieStringAll.split(";");
			Globe.sCookieStringCnt = Globe.sCookieString.length;
			
			//Log.i(TAG, "Thread #3|4 cookiesCnt: " + Globe.sCookieStringCnt);
			
			for (int i = 0; i < Globe.sCookieStringCnt; i++) {
				//Globe.sCookieString[i].replace(";", "");
				String[] temp = Globe.sCookieString[i].split("=");
				//Log.i(TAG, "Thread #3|4 cookie: " + Globe.sCookieString[i]);
				cookies.put(temp[0], temp[1]);
			}
		}
		
		if (Globe.monitor_scorelist) {
			if (messageThread_scorelist == null) {
				messageThread_scorelist = new MessageThread();
				messageThread_scorelist.url = NetConstant.URL_CJDCX;
			    messageThread_scorelist.id = 3;
		        messageThread_scorelist.isRunning = true;
		        messageThread_scorelist.start();
			}
		} else if (messageThread_scorelist != null) {
			messageThread_scorelist.isRunning = false;
		}
        
		if (Globe.monitor_courses){
			if (messageThread_courses == null) {
				messageThread_courses = new MessageThread();
				messageThread_courses.url = Globe.monitor_url;
				messageThread_courses.id = 4;
				messageThread_courses.isRunning = true;
				messageThread_courses.start();
			}
		} else if (messageThread_courses != null) {
			messageThread_courses.isRunning = false;
		}

		return START_STICKY;
	}
	
	/**
     * 信息推送服务 - 监测通用线程
     *
     */
    class MessageThread extends Thread {

        public boolean isRunning = true;
        public String url;
        public int id;
        public long interval = 10 * 60 * 1000;
        private Document doc;
        private String MAIN_URL_DHU = "http://www2.dhu.edu.cn/dhuxxxt/xinwenwang/";
        private boolean firstTime;
        
		public void run() {
            while (isRunning) {
            	try {
            		Log.i(TAG, "Thread #"+id+" started");
            		
            		long timepast =  System.currentTimeMillis() - cache_last_update_time[id];
            		Log.i(TAG, "Thread #"+id+" time_past: " + timepast + " interval: " + interval);
            		
            		if (cache[id] != null && !cache[id].equals("")) {
            			if (cache[id].length() > 100) {
            				Log.i(TAG, "Thread #"+id+" cache: " + cache[id].substring(0, 100));
            			} else {
            				Log.i(TAG, "Thread #"+id+" cache: " + cache[id]);
            			}
            		} else {
            			Log.i(TAG, "Thread #"+id+" no cache!");
            		}
            		
            		if (cache[id].equals("")) {
            			firstTime = true;
            		} else {
            			firstTime = false;
            		}
            		
            		if (timepast >= interval) {
            			String serverMessage;
            			if (id == 3 || id == 4) {
            				doc = Jsoup.connect(url).cookies(cookies).timeout(10000).get();
            				serverMessage = doc.text();
            			} else {
            				doc = Jsoup.connect(url).timeout(10000).get();
            				// 正则去除所有数字，即排除访问量的变化
                			String regEx = "[0-9]";  
                	        Pattern p = Pattern.compile(regEx);  
                	        Matcher m = p.matcher(doc.text());  
                	        serverMessage = m.replaceAll("").trim();
            			}
    					Log.i(TAG, "Thread #"+id+" fetch data success");
    					
    					if (serverMessage.length() > 100) {
    						Log.i(TAG, "Thread #"+id+" document: " + serverMessage.substring(0, 100));
    					} else {
    						Log.i(TAG, "Thread #"+id+" document: " + serverMessage);
    					}
    					
    					if (serverMessage != null && !serverMessage.contentEquals(cache[id])) {
    						cache[id] = serverMessage;
    						
    						new Thread() {
    							@Override
    							public void run() {
    								Log.i(TAG, "Thread #"+id+"(edit) start");
    								
    								boolean isChanged = false;
    								ArrayList<String[]> mListItems = new ArrayList<String[]>();
    								ArrayList<String[]> mNotificationListItems = new ArrayList<String[]>();
    								String saveNewsString = "";
    								
    								// 生成列表
    								mListItems.clear();
    								if (id == 0 || id == 1) {
			    						Elements tables = doc.select("table[width=96%]");
			    						Elements items = tables.select("td[width=96%]");
			    						for (Element item : items) {
			    							String[] arr = new String[3];
			    							arr[0] = MAIN_URL_DHU + item.select("a").attr("href");
			    							arr[1] = item.select("a").text();
			    							arr[2] = item.text().substring(
			    									item.text().lastIndexOf("(") + 1,
			    									item.text().length() - 1);
			    							saveNewsString += arr[0] + "#" + arr[1] + "#" + arr[2] + "@";
			    							mListItems.add(arr);
			    						}
    								} else if (id == 2) {
    									Elements tables = doc.select("table[id=table1]");
    									Elements items = tables.select("tr[align=center]");
    									for (Element item : items) {
    										String[] arr = new String[3];
    										arr[0] = item.select("a").attr("href");
    										arr[1] = item.select("a").text();
    										arr[2] = item.text().substring(item.text().length() - 10);
    										saveNewsString += arr[0] + "#" + arr[1] + "#" + arr[2] + "@";
    										mListItems.add(arr);
    									}
    								} else if (id == 3) {
    									Elements eles = doc.select("table:contains(课程名称)").select("tr");
    									for (Element ele : eles) {
    										Elements items = ele.getElementsByTag("td");
    										if (items.get(0).text().contentEquals("课程名称")) continue;
    										if (items.get(0).text().contentEquals("学业警告")) break;
    										String[] str = new String[items.size()];
    										for (int i = 0; i < items.size(); i++) {
    											str[i] = items.get(i).text();
    											if (i > 0) saveNewsString += "#";
    											saveNewsString += str[i];
    										}
    										saveNewsString += "@";
    										mListItems.add(str);
    									}
    								} else if (id == 4) {
                                        Elements eles = doc.getElementById("AutoNumber2").select("td");

                                        String max_admit = eles.get(10).text();
                                        String already_select = eles.get(11).text();
                                        String already_admit = eles.get(12).text();

                                        saveNewsString = max_admit + "@" + already_select + "@" + already_admit;
                                    }
    								
    								Log.i(TAG, "Thread #"+id+"(edit) saveNewsString: " + saveNewsString);

    								if ((id == 4 || saveNewsString.length() > 50) && !saveNewsString.equals(cache_string[id])) {

                                        if (id != 4) {
                                            if (!firstTime) {
                                                int maxNotification = 5;// 单次最大通知数
                                                if (id == 3) {
                                                    maxNotification = mListItems.size();
                                                } else {
                                                    maxNotification = (mListItems.size() < 5) ? mListItems.size() : 5;
                                                }
                                                for (int i = 0; i < maxNotification; i++) {
                                                    String[] mListItem = mListItems.get(i);
                                                    String compareString = "";
                                                    if (id == 3) {
                                                        compareString = mListItem[0] + "#" + mListItem[1] + "#" + mListItem[2] + "#" + mListItem[3];
                                                    } else {
                                                        compareString = mListItem[0];
                                                    }

                                                    if (compareString.length() > 20 && !cache_string[id].contains(compareString)) {
                                                        // 显示通知
                                                        intent = new Intent(MonitorService.this, WebViewScreen.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        if (id != 3) {
                                                            contentTitle = mListItem[1];
                                                            intent.putExtra("url", mListItem[0]);
                                                            intent.putExtra("name", contentTitle);
                                                        } else {
                                                            contentTitle = mListItem[0] + "：" + mListItem[3];
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// Disable for cookie...
                                                            intent.putExtra("url", NetConstant.URL_CJDCX);
                                                            intent.putExtra("name", "学生个人成绩单");
                                                            intent.putExtra("needCookies", true);  // 学生个人成绩单需要登录
                                                        }
                                                        pendingIntent = PendingIntent.getActivity(MonitorService.this, notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                        notification.tickerText = contentTitle;
                                                        notification.when = System.currentTimeMillis();
                                                        notification.setLatestEventInfo(MonitorService.this, contentTitle, contentText[id], pendingIntent);
                                                        notificationManager.notify(notificationID, notification);
                                                        notificationID++;
                                                    }
                                                }
                                            }
                                        } else {

                                        }
    									
    									synchronized (MonitorService.class) {
	    									// 保存缓存
	    									cache_string[id] = saveNewsString;
	    									
			    	                        //SharedPreferences saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
	    									Editor edit = saveCacheSettings[id].edit();
	    									//SharedDataUtil.SharedDataEditor edit = saveCacheSettings.getSharedDataEditor();
			    	                        switch (id) {
			    							case 0:
	//		    								Globe.mFirstListItems = mListItems;
			    								edit.putString("cache_news_dhxw", cache[id]);
			    								edit.putString("cache_news_dhxw_string", cache_string[id]);
			    								break;
			    							case 1:
	//		    								Globe.mSecondListItems = mListItems;
			    								edit.putString("cache_news_xngg", cache[id]);
			    								edit.putString("cache_news_xngg_string", cache_string[id]);
			    								break;
			    							case 2:
	//		    								Globe.mThirdListItems = mListItems;
			    								edit.putString("cache_news_jwxx", cache[id]);
			    								edit.putString("cache_news_jwxx_string", cache_string[id]);
			    								break;
			    							case 3:
	//		    								Globe.mScoreListItems = mListItems;
			    								edit.putString("cache_scorelist", cache[id]);
			    								edit.putString("cache_scorelist_string", cache_string[id]);
			    								break;
                                            case 4:
                                                edit.putString("cache_courses", cache[id]);
                                                edit.putString("cache_courses_string", cache_string[id]);
                                                break;
			    							}
			    	                        edit.commit();
    									}
    								}
	    	                        
	    	                        Log.i(TAG, "Thread #"+id+"(edit) finished");
    							}
    						}.start();
                        
    					}
    					
    					cache_last_update_time[id] = System.currentTimeMillis();
    					
    					new Thread() {
							@Override
							public void run() {
								synchronized (MonitorService.class) {
									Log.i(TAG, "Thread #"+id+"(save time) start");
									
									//SharedPreferences saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
			    					Editor edit = saveCacheSettings[id].edit();
									//SharedDataUtil.SharedDataEditor edit = saveCacheSettings.getSharedDataEditor();
			                        switch (id) {
									case 0:
	//									Globe.cache_news_dhxw_time = cache_last_update_time[id];
										edit.putLong("cache_news_dhxw_time", cache_last_update_time[id]);
										break;
									case 1:
	//									Globe.cache_news_xngg_time = cache_last_update_time[id];
										edit.putLong("cache_news_xngg_time", cache_last_update_time[id]);
										break;
									case 2:
	//									Globe.cache_news_jwxx_time = cache_last_update_time[id];
										edit.putLong("cache_news_jwxx_time", cache_last_update_time[id]);
										break;
									case 3:
	//									Globe.cache_scorelist_time = cache_last_update_time[id];
										edit.putLong("cache_scorelist_time", cache_last_update_time[id]);
										break;
                                    case 4:
                                        edit.putLong("cache_courses_time", cache_last_update_time[id]);
                                        break;
									}
			                        edit.commit();
			                        
			                        Log.i(TAG, "Thread #"+id+"(save time) finished");
								}
							}
    					}.start();
    					
    					Log.i(TAG, "Thread #"+id+" entering sleep");
                        //休息10分钟
                        Thread.sleep(interval);
            			

            		} else {
            			Thread.sleep(interval - timepast);
            		}
					
                    
                } catch (SocketTimeoutException e) {
                	Log.i(TAG, "Thread #"+id+" network timeout");
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                	e.printStackTrace();
                	try {
						Thread.sleep(interval);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
                }
            }
        }
    }

	public void handResponse(MyHttpResponse myHttpResponse) {
		Document doc = myHttpResponse.getData();
		System.out.println(doc.text());

		// Elements ele = doc.select("table");
		Elements ele = doc.getAllElements();

		if (ele != null) {
			CurContent = ele.text();
			System.out.println(CurContent);
		}

		if (PreContent == null) {
			PreContent = CurContent;

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			mNetClient.sendRequest(req);

		} else {
			if (!PreContent.contentEquals(CurContent)) {
				
//				Notification notification = new Notification(R.drawable.logo,
//						Globe.monitor_courseName + Messages.getString("MonitorService.alert_webpage_differences"), //$NON-NLS-1$
//						System.currentTimeMillis());
//				PendingIntent contentIntent = PendingIntent.getActivity(this,
//						0, new Intent(this, MonitorScreen.class), 0);
//				notification.defaults = Notification.DEFAULT_SOUND;
				notification.setLatestEventInfo(this, Messages.getString("MonitorService.monitor_service"), //$NON-NLS-1$
						Globe.monitor_courseName + Messages.getString("MonitorService.alert_webpage_differences"), pendingIntent); //$NON-NLS-1$
				notificationManager.notify(notificationID, notification);
				notificationID++;
				//startForeground(1, notification);

				// Intent it = new Intent(this, MonitorScreen.class);
				// it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(it);

				Intent it = new Intent(this, WebViewScreen.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				it.putExtra("url", NetConstant.URL_COURSE_SEARCH + "?courseId="
						+ Globe.monitor_courseId + "&courseNo="
						+ Globe.monitor_courseNo + "&courseName="
						+ Globe.monitor_courseName);
				it.putExtra("name", "选课监控");
				// it.putExtra("needCookies", true);
				startActivity(it);

				mNetClient.isRunning = false;

				PreContent = CurContent;
				//SharedPreferences saveCacheSettings = getSharedPreferences("dhuhelper_cache", Context.MODE_MULTI_PROCESS);
				Editor edit = saveCacheSettings[4].edit();
				//SharedDataUtil.SharedDataEditor edit = saveCacheSettings.getSharedDataEditor();
				edit.putString("monitor_webpage_content", PreContent);
				edit.commit();

			} else {

				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mNetClient.sendRequest(req);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() executed");
		stopForeground(true);
		if (mNetClient != null) {
			mNetClient.isRunning = false;
		}
		if (messageThread_dhxw != null) {
			messageThread_dhxw.isRunning = false;
		}
		if (messageThread_xngg != null) {
			messageThread_xngg.isRunning = false;
		}
		if (messageThread_jwxx != null) {
			messageThread_jwxx.isRunning = false;
		}
		if (messageThread_scorelist != null) {
			messageThread_scorelist.isRunning = false;
		}
        if (messageThread_courses != null) {
            messageThread_courses.isRunning = false;
        }
	}

	@Override
	public IBinder onBind(Intent intent) {

        return null;
    }

}
