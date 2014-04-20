package com.wujy.dhuhelper.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.db.SharedDataUtil;

/**
 * @author wujy 用来执行网络事件的类
 */
public class NetClient {
	private DefaultHttpClient client = new DefaultHttpClient();

	private HttpGet get;

	public boolean isRunning;

	private WindowActivity mActivity;

	public Handler mRefreshHandler;

	private ArrayList<MyHttpRequest> mRequestTemp = new ArrayList<MyHttpRequest>();

	private ArrayList<MyHttpResponse> mResponseTemp = new ArrayList<MyHttpResponse>();

	private HttpPost post;

	private HttpResponse response;

	public MyHttpRequest mRetryRequest;
	
	public SharedPreferences saveCacheSettings;

	@SuppressLint("HandlerLeak")
	public NetClient(Activity activity) {
		this.mActivity = (WindowActivity) activity;
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 4000);
		client.getParams()
				.setParameter(
						HTTP.USER_AGENT,
						"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36");

		mRefreshHandler = new Handler() {
			@SuppressWarnings("deprecation")
			@Override
			public void handleMessage(Message msg) {
				if (!mActivity.isFinishing()) {
					switch (msg.what) {
					case NetConstant.MSG_REFRESH:
						if (mResponseTemp.size() != 0) {
							System.out.println("temp中已有数据,交给activity");
							mActivity.handResponse(mResponseTemp.get(0));
							if (mResponseTemp.size() != 0) {
								mResponseTemp.remove(0);
							}
							if (mRequestTemp.size() == 0) {
								mActivity.dismissNetLoadingDialog();
							}
						}
						break;
					case NetConstant.MSG_SERVER_ERROR:
						mActivity.dismissNetLoadingDialog();
						mActivity.showDialog(NetConstant.MSG_SERVER_ERROR);
						break;
					case NetConstant.MSG_NET_ERROR:
						mActivity.dismissNetLoadingDialog();
						mActivity.showDialog(NetConstant.MSG_NET_ERROR);
						mRequestTemp.add(mRetryRequest); // Retry
						break;
					case NetConstant.MSG_NONE_NET:
						mActivity.dismissNetLoadingDialog();
						mActivity.showDialog(NetConstant.MSG_NONE_NET);
						break;

					default:
						break;
					}
				}
			}
		};
		isRunning = true;
		start();
	}

	public ArrayList<MyHttpRequest> getmRequestTemp() {
		return mRequestTemp;
	}

	public void sendRequest(MyHttpRequest req) {
		try {
			if (!isRunning) {
				mRequestTemp.clear();
				isRunning = true;
				start();
			}
			if (req.isBlock) {
				mActivity.showNetLoadingDialog();
			}
			System.out.println("添加一个请求至temp");
			mRequestTemp.add(req);
		} catch (Exception e) {
		}
	}

	public void setCurrentWindow(WindowActivity activity) {
		this.mActivity = activity;
	}

	public void setmRequestTemp(ArrayList<MyHttpRequest> mRequestTemp) {
		this.mRequestTemp = mRequestTemp;
	}

	public void start() {

		new Thread() {
			@Override
			public void run() {
				while (isRunning) {
					if (mRequestTemp.size() != 0) {
						MyHttpRequest req = mRequestTemp.get(0);
						if (req.getType() == NetConstant.TYPE_GET) {// 1为get
							System.out.println("准备发送一个get请求");
							get = new HttpGet(req.getUrl());
							// System.out.println("url:"+req.getUrl());
							// get.addHeader("Cookie", Globe.sCookieString);
							// System.out.println("添加cookie:"+Globe.sCookieString);
							try {
								System.out.println("执行前");
								if (client == null) return;  // prevent the NullPointerException when activity has been finished
								else response = client.execute(get);
								System.out.println("执行后");
								if (response.getStatusLine().getStatusCode() == 200) {
									String strResult = EntityUtils.toString(
											response.getEntity(), "UTF-8");
									Document doc = Jsoup.parse(strResult);
									MyHttpResponse resp = new MyHttpResponse(
											req.getPipIndex(), doc);
									mResponseTemp.add(resp);
									if (mRequestTemp.size() != 0)
										mRequestTemp.remove(0);
								} else {
									mRefreshHandler
											.sendEmptyMessage(NetConstant.MSG_SERVER_ERROR);
								}
							} catch (ClientProtocolException e) {
								mRetryRequest = req;
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_NET_ERROR);
								e.printStackTrace();
							} catch (ConnectTimeoutException e) {
								mRetryRequest = req;
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_NET_ERROR);
							} catch (IOException e) {
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_OTHER_ERROR);
								e.printStackTrace();
							} catch (NullPointerException e) {
								e.printStackTrace();
							} finally {
								get.abort();
							}
						} else {// 为post请求
							System.out.println("准备发送一个post请求");
							post = new HttpPost(req.getUrl());
							try {
								if (req.getPipIndex() != NetConstant.LOGIN) {
									// post.addHeader("Cookie",
									// Globe.sCookieString);
									// System.out.println("添加cookie:" +
									// Globe.sCookieString);
								}
								post.setEntity(new UrlEncodedFormEntity(req
										.getParams(), "gb2312"));
								System.out.println("执行前");
								if (client == null) return;  // prevent the NullPointerException when activity has been finished
								else response = client.execute(post);
								System.out.println("执行后");
								
								if (response.getStatusLine().getStatusCode() == 200) {
									String strResult = EntityUtils.toString(
											response.getEntity(), "UTF-8");
									Document doc = Jsoup.parse(strResult);
									if (req.getPipIndex() == NetConstant.LOGIN) {
									
										List<Cookie> cookies = client
												.getCookieStore().getCookies();
										String CookieStringAll = "";
										Globe.sCookieString = new String[cookies
												.size()];
										for (int i = 0; i < cookies.size(); i++) {
											Globe.sCookieString[i] = cookies
													.get(i).getName()
													+ "="
													+ cookies.get(i).getValue()
													+ ";";
											CookieStringAll += Globe.sCookieString[i];
										}
										Globe.sCookieStringCnt = cookies.size();
										if (req.getUrl() == NetConstant.URL_LOGIN_JW) {
											// TODO:
											Editor edit = saveCacheSettings.edit();
											//SharedDataUtil.SharedDataEditor edit = saveCacheSettings.getSharedDataEditor();
											edit.putString("CookieStringAll",
													CookieStringAll);
											edit.commit();
											System.out.println(CookieStringAll);
										}
									}
									System.out.println("取出数据,放入temp");
									MyHttpResponse resp = new MyHttpResponse(
											req.getPipIndex(), doc);
									mResponseTemp.add(resp);
									if (mRequestTemp.size() != 0)
										mRequestTemp.remove(0);
								} else {
									mRefreshHandler
											.sendEmptyMessage(NetConstant.MSG_SERVER_ERROR);
								}
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								mRetryRequest = req;
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_NET_ERROR);
								e.printStackTrace();
							} catch (ConnectTimeoutException e) {
								mRetryRequest = req;
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_NET_ERROR);
							} catch (IOException e) {
								mRefreshHandler
										.sendEmptyMessage(NetConstant.MSG_OTHER_ERROR);
								e.printStackTrace();
							} catch (NullPointerException e) {
								e.printStackTrace();
							} finally {
								post.abort();
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}.start();
		new Thread() {
			@Override
			public void run() {
				while (isRunning) {
					mRefreshHandler.sendEmptyMessage(NetConstant.MSG_REFRESH);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

}
