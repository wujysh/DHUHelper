package org.sjutas.dhu.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import org.sjutas.dhu.Globe;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;

/**
 * @author wujy 用来执行网络事件的类
 */
public class MonitorNetClient {
	private DefaultHttpClient client = new DefaultHttpClient();

	private HttpGet get;

	public boolean isRunning;

	// private WindowActivity mActivity;
	private MonitorService mService;

	public Handler mRefreshHandler;

	private ArrayList<MyHttpRequest> mRequestTemp = new ArrayList<MyHttpRequest>();

	private ArrayList<MyHttpResponse> mResponseTemp = new ArrayList<MyHttpResponse>();

	private HttpPost post;

	private HttpResponse response;

	public MyHttpRequest mRetryRequest;

	@SuppressLint("HandlerLeak")
	// public MonitorNetClient(WindowActivity activity) {
	public MonitorNetClient(MonitorService service) {
		// this.mActivity = activity;
		this.mService = service;
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 4000);

		mRefreshHandler = new Handler() {
			@SuppressWarnings("deprecation")
			@Override
			public void handleMessage(Message msg) {
				// if (!mService.isFinishing()) {
				switch (msg.what) {
				case NetConstant.MSG_REFRESH:
					if (mResponseTemp.size() != 0) {
						System.out.println("temp中已有数据,交给activity");
						mService.handResponse(mResponseTemp.get(0));
						if (mResponseTemp.size() != 0) {
							mResponseTemp.remove(0);
						}
					}
					// if (mRequestTemp.size() == 0) {
					// mService.dismissNetLoadingDialog();
					// }
					break;
				case NetConstant.MSG_SERVER_ERROR:
					// mService.dismissNetLoadingDialog();
					// mService.showDialog(NetConstant.MSG_SERVER_ERROR);
					break;
				case NetConstant.MSG_NET_ERROR:
					// mService.dismissNetLoadingDialog();
					// mService.showDialog(NetConstant.MSG_NET_ERROR);
					// mRequestTemp.add(mRetryRequest); // Retry
					break;
				case NetConstant.MSG_NONE_NET:
					// mService.dismissNetLoadingDialog();
					// mService.showDialog(NetConstant.MSG_NONE_NET);
					break;

				default:
					break;
				}
				// }
			}
		};
		// isRunning = true;
		// start();
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
			// if (req.isBlock) {
			// mService.showNetLoadingDialog();
			// }
			System.out.println("添加一个请求至temp");
			mRequestTemp.add(req);
		} catch (Exception e) {
		}
	}

	public void setCurrentService(MonitorService service) {
		this.mService = service;
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
							System.out.println("url:" + req.getUrl());
							// for (int i = 0; i < Globe.sCookieStringCnt; i++)
							// {
							// get.addHeader("Cookie", Globe.sCookieString[i]);
							// }
							get.addHeader("Cookie", Globe.sCookieStringAll);
							System.out.println("添加cookie:"
									+ Globe.sCookieStringAll);
							try {
								System.out.println("执行前");
								response = client.execute(get);
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
								response = client.execute(post);
								System.out.println("执行后");
								if (response.getStatusLine().getStatusCode() == 200) {
									String strResult = EntityUtils.toString(
											response.getEntity(), "UTF-8");
									Document doc = Jsoup.parse(strResult);
									if (req.getPipIndex() == NetConstant.LOGIN) {
										List<Cookie> cookies = client
												.getCookieStore().getCookies();
										Globe.sCookieString = new String[cookies
												.size()];
										for (int i = 0; i < cookies.size(); i++) {
											Globe.sCookieString[i] = cookies
													.get(i).getName()
													+ "="
													+ cookies.get(i).getValue()
													+ ";";
										}
										Globe.sCookieStringCnt = cookies.size();
										// System.out.println(Globe.sCookieString);
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
