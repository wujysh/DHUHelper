package org.sjutas.dhu.view;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.android.pushservice.PushManager;
import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.Util;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetConstant;

/**
 * 登陆待完成
 * 
 * @author wujy
 */
public class LoginScreen extends WindowActivity {
	private ArrayList<String[]> hide_params = Globe.sHideParams;

	private Button mLoginButton, mClearUser;

	private SharedPreferences mLoginInfoPreferences;

	private CheckBox mSavePasswordBox, mSaveIdBox, mAutoLoginBox;

	private EditText mUserName, mPassword;

	private String where;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		System.out.println("处理一个响应");
		Document doc;
		if (myHttpResponse.getPipIndex() == NetConstant.LOGIN) {
			doc = myHttpResponse.getData();
			// System.out.println(doc);
			// dismissNetLoadingDialog();

			if (where.contentEquals("JWC")) {
				if (doc.select("body:contains(用户名或密码不能为空)").isEmpty()) {
					if (doc.select("font:contains(错误的用户名和密码)").isEmpty()) {
						System.out.println(Messages.getString("LoginScreen.login_success")); //$NON-NLS-1$

						Globe.sId_jw = mUserName.getEditableText().toString()
								.trim(); // 学号
						Elements name = doc.getElementById("login")
								.getElementsByTag("font");
						Globe.sName_jw = name.text().toString(); // 姓名
						// Globe.sClassName =; // 班级

						changePreferences();

						Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.login_success"), //$NON-NLS-1$
								Toast.LENGTH_SHORT).show();

						Globe.isLoginJW = true;

						Intent it = new Intent(LoginScreen.this,
								(Class<?>) getIntent().getSerializableExtra(
										"NextScreen"));
						// it.putExtra("RadioChecked", "0");
						if (getIntent().getStringExtra("url") != null) {
							it.putExtra("url", getIntent()
									.getStringExtra("url"));
							it.putExtra("name",
									getIntent().getStringExtra("name"));
							it.putExtra("needCookies", true);
							startActivity(it);
						} else {
							if (it.equals(UserScreen.class)) {
								
							} else {
								
							}
							
							startActivity(it);
							
							sendFeedback();

							finish();
						}
					} else {
						Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.wrong_id_or_password"), //$NON-NLS-1$
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.blank_id_or_password"), //$NON-NLS-1$
							Toast.LENGTH_SHORT).show();
				}
			} else if (where.contentEquals("LIB")) {
				if (doc.select("font:contains(请输入正确的读者证件号)").isEmpty()) {
					if (doc.select("font:contains(对不起，密码错误，请查实！)").isEmpty()) {
						System.out.println(Messages.getString("LoginScreen.login_success")); //$NON-NLS-1$

						Globe.sId_lib = mUserName.getEditableText().toString()
								.trim(); // 学号
						Globe.sMsg_lib = doc
								.getElementsByAttributeValue("class",
										"mylib_msg").text().split("最近")[0];
                        Globe.sMsg_lib = Globe.sMsg_lib.replace("]，", "]\n");
                        //Globe.sMsg_lib = Globe.sMsg_lib.replace("，", "");

						Globe.sName_lib = doc.getElementById("mylib_info")
								.select("td:contains(姓名：)").text().substring(3);
						Globe.sIdentityId_lib = doc
								.getElementById("mylib_info")
								.select("td:contains(身份证号：)").text()
								.substring(6);

						changePreferences();

						Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.login_success"), //$NON-NLS-1$
								Toast.LENGTH_SHORT).show();

						Globe.isLoginLib = true;

						Intent it = new Intent(LoginScreen.this,
								(Class<?>) getIntent().getSerializableExtra(
										"NextScreen"));
						if (getIntent().getStringExtra("url") != null) {
							it.putExtra("url", getIntent()
									.getStringExtra("url"));
							it.putExtra("name",
									getIntent().getStringExtra("name"));
							it.putExtra("needCookies", true);
							startActivity(it); // WebViewScreen doesn't use
												// slidingmenu, should back to
												// previous activity
						} else {
							startActivity(it);

							sendFeedback();

							finish();
						}
					} else {
						Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.wrong_id_or_password"), //$NON-NLS-1$
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.blank_id_or_password"), //$NON-NLS-1$
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void sendFeedback() {
		new Thread() {
			@Override
			public void run() {
				try {
                    List<String> tags = Util.getTagsList("[DHU]"+mUserName.getEditableText().toString().trim());
                    PushManager.setTags(getApplicationContext(), tags);
				} catch (Exception e) {
					//System.out.print("{网络超时}");
                    e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("登录");

		setContentView(R.layout.login_layout);

		setSlidingMenu();

		mLoginButton = (Button) findViewById(R.id.login_btn);
		mClearUser = (Button) findViewById(R.id.clear_user);
		mUserName = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mSavePasswordBox = (CheckBox) findViewById(R.id.remember_password);
		mSaveIdBox = (CheckBox) findViewById(R.id.remember_id);
		mAutoLoginBox = (CheckBox) findViewById(R.id.auto_login);

		where = getIntent().getStringExtra("where");
		if (where == null) where = "JWC";
		if (where.contentEquals("JWC")) {
			mLoginInfoPreferences = getSharedPreferences("logininfo_jw",
					MODE_PRIVATE);
			setTitle("登录教务处");
			mAutoLoginBox.setChecked(Globe.autologin_jw);
		} else if (where.contentEquals("LIB")) {
			mLoginInfoPreferences = getSharedPreferences("logininfo_lib",
					MODE_PRIVATE);
			setTitle("登录图书馆");
			mAutoLoginBox.setChecked(Globe.autologin_lib);
		}

		mUserName.setText(mLoginInfoPreferences.getString("loginname", ""));
		mPassword.setText(mLoginInfoPreferences.getString("loginpassword", ""));
		mSavePasswordBox.setChecked(mLoginInfoPreferences.getBoolean(
				"savepassword", false));
		mSaveIdBox
				.setChecked(mLoginInfoPreferences.getBoolean("saveid", false));
		// mAutoLoginBox.setChecked(mLoginInfoPreferences.getBoolean("autologin",
		// false));

		mAutoLoginBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mSavePasswordBox.setChecked(true);
					mSaveIdBox.setChecked(true);
				}
			}
		});
		mSavePasswordBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							mSaveIdBox.setChecked(true);
						} else {
							mAutoLoginBox.setChecked(false);
						}
					}
				});
		mSaveIdBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked) {
					mAutoLoginBox.setChecked(false);
					mSavePasswordBox.setChecked(false);
				}
			}
		});

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

				if (where.contentEquals("JWC")) {
					params.add(new BasicNameValuePair("userName", mUserName
							.getEditableText().toString().trim()));
					params.add(new BasicNameValuePair("userPwd", mPassword
							.getEditableText().toString().trim()));

					MyHttpRequest req = new MyHttpRequest(
							NetConstant.TYPE_POST, NetConstant.URL_LOGIN_JW,
							params, true);
					req.setPipIndex(NetConstant.LOGIN);
					mNetClient.sendRequest(req);

				} else if (where.contentEquals("LIB")) {
					params.add(new BasicNameValuePair("select", "cert_no"));
					params.add(new BasicNameValuePair("number", mUserName
							.getEditableText().toString().trim()));
					params.add(new BasicNameValuePair("passwd", mPassword
							.getEditableText().toString().trim()));
					params.add(new BasicNameValuePair("returnUrl", ""));
					params.add(new BasicNameValuePair("submit.x", "13"));
					params.add(new BasicNameValuePair("submit.y", "12"));

					MyHttpRequest req = new MyHttpRequest(
							NetConstant.TYPE_POST, NetConstant.URL_LOGIN_LIB,
							params, true);
					req.setPipIndex(NetConstant.LOGIN);
					mNetClient.sendRequest(req);
				}

				// LoginScreen.this.showNetLoadingDialog();

			}
		});

		if (mAutoLoginBox.isChecked()
				&& mUserName.getEditableText().length() > 0) {
			mLoginButton.performClick();
			Toast.makeText(LoginScreen.this, Messages.getString("LoginScreen.autologining"), Toast.LENGTH_LONG) //$NON-NLS-1$
					.show();
		}

		mClearUser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mUserName.setText("");
				mPassword.setText("");
				mSavePasswordBox.setChecked(false);
				mSaveIdBox.setChecked(false);
				mAutoLoginBox.setChecked(false);
				Editor edit = mLoginInfoPreferences.edit();
				edit.putString("loginname", "");
				edit.putString("loginpassword", "");
				edit.putBoolean("savepassword", false);
				edit.putBoolean("saveid", false);
				edit.putBoolean("autologin", false);
				edit.commit();
			}
		});
		
		
//		// Prepare for Oauth
//		OAuthService service = new ServiceBuilder()
//        .provider(TwitterApi.SSL.class)
//        .apiKey("your_api_key")
//        .apiSecret("your_api_secret")
//        .build();
//		
//		Token requestToken = service.getRequestToken();
//		
//		String authUrl = service.getAuthorizationUrl(requestToken);
//		
//		Verifier v = new Verifier("verifier you got from the user");
//		Token accessToken = service.getAccessToken(requestToken, v); // the requestToken you had from step 2
//	
//		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.twitter.com/1/account/verify_credentials.xml");
//		service.signRequest(accessToken, request); // the access token from step 4
//		Response response = request.send();
//		System.out.println(response.getBody());
	}

	private void changePreferences() {
		if (mAutoLoginBox.isChecked()) {
			Editor edit = mLoginInfoPreferences.edit();
			edit.putString("loginname", mUserName.getEditableText().toString()
					.trim());
			edit.putString("loginpassword", mPassword.getEditableText()
					.toString().trim());
			edit.putBoolean("savepassword", true);
			edit.putBoolean("saveid", true);
			// edit.putBoolean("autologin", true);
			edit.commit();
		} else if (mSavePasswordBox.isChecked()) {
			Editor edit = mLoginInfoPreferences.edit();
			edit.putString("loginname", mUserName.getEditableText().toString()
					.trim());
			edit.putString("loginpassword", mPassword.getEditableText()
					.toString().trim());
			edit.putBoolean("savepassword", true);
			edit.putBoolean("saveid", true);
			// edit.putBoolean("autologin", false);
			edit.commit();
		} else if (mSaveIdBox.isChecked()) {
			Editor edit = mLoginInfoPreferences.edit();
			edit.putString("loginname", mUserName.getEditableText().toString()
					.trim());
			edit.putBoolean("savepassword", false);
			edit.putBoolean("saveid", true);
			// edit.putBoolean("autologin", false);
			edit.commit();
		} else {
			Editor edit = mLoginInfoPreferences.edit();
			edit.putBoolean("savepassword", false);
			edit.putBoolean("saveid", false);
			// edit.putBoolean("autologin", false);
			edit.commit();
		}

		Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		if (where.contentEquals("JWC")) {
			edit.putBoolean("autologin_jw", mAutoLoginBox.isChecked());
			Globe.autologin_jw = mAutoLoginBox.isChecked();
		} else if (where.contentEquals("LIB")) {
			edit.putBoolean("autologin_lib", mAutoLoginBox.isChecked());
			Globe.autologin_lib = mAutoLoginBox.isChecked();
		}
		edit.commit();

	}

}
