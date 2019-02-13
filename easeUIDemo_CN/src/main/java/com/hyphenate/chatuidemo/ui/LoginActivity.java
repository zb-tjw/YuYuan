/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.db.DemoDBManager;
import com.hyphenate.chatuidemo.domain.PhoneLogin;
import com.hyphenate.chatuidemo.domain.QQlogin;
import com.hyphenate.chatuidemo.utils.ClearEditText;
import com.hyphenate.chatuidemo.utils.UserInfo;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * Login screen
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	private EditText usernameEditText;
	private EditText passwordEditText;
	private String currentUsername;
	private String currentPassword;
	private String nick;
	private String avatar_url;//Bmob端拿到的头像地址

	private boolean progressShow;
	private boolean autoLogin = false;
	boolean isFinish_updateNick = false;
	boolean isFinish_updateAvatar = false;//头像一直改不成功

	//用于QQ返回数据的接收
	private String uid = "";
	private String nickname = "";
	private String province = "";
	private String city = "";
	private String gender = "";
	private String location = "";
	private String icon_url = "";

	private int HX_username;

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			RegisterActivity registerActivity = new RegisterActivity();
			registerActivity.createAccount(HX_username,"123");
			ProgressDialog pd = new ProgressDialog(LoginActivity.this);
			pd.setCanceledOnTouchOutside(false);
			pd.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					Log.d(TAG, "EMClient.getInstance().onCancel");
					progressShow = false;
				}
			});
			pd.setMessage(getString(R.string.Is_landing));
			pd.show();

			currentUsername = String.valueOf(HX_username);
			currentPassword = "123";
			HX_login(pd);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enter the main activity if already logged in
		if (DemoHelper.getInstance().isLoggedIn()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.em_activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);

		// if user changed, clear the password
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (DemoHelper.getInstance().getCurrentUsernName() != null) {
			usernameEditText.setText(DemoHelper.getInstance().getCurrentUsernName());
		}
	}

	/**
	 * login
	 * 
	 * @param view
	 */
	public void login(View view) {
		if (!EaseCommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		currentUsername = usernameEditText.getText().toString().trim();
		currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentUsername)) {
			Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
			if(usernameEditText.isFocused()){
				return;
			}else{
				((ClearEditText) usernameEditText).setShakeAnimation();
				usernameEditText.requestFocus();
			}
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
			if(passwordEditText.isFocused()){
				return;
			}else{
				((ClearEditText) passwordEditText).setShakeAnimation();
				passwordEditText.requestFocus();
			}
			return;
		}

		progressShow = true;
		final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "EMClient.getInstance().onCancel");
				progressShow = false;
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
		pd.show();

		// After logout，the DemoDB may still be accessed due to async callback, so the DemoDB will be re-opened again.
		// close it before login to make sure DemoDB not overlap
        DemoDBManager.getInstance().closeDB();

        // reset current user name before login
        DemoHelper.getInstance().setCurrentUserName(currentUsername);

        
		final long start = System.currentTimeMillis();
		HX_login(pd);

	}

	private void HX_login(final ProgressDialog pd) {
		// call login method
		Log.d(TAG, "EMClient.getInstance().login");
		EMClient.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "login: onSuccess");


				// ** manually load all local groups and conversation
			    EMClient.getInstance().groupManager().loadAllGroups();
			    EMClient.getInstance().chatManager().loadAllConversations();

				// get user's info (this should be get from App's server or 3rd party service)
				DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

				BmobQuery<UserInfo> query = new BmobQuery<UserInfo>("userInfo");
				query.addWhereEqualTo("user_id",Integer.parseInt(currentUsername));
				query.findObjects(new FindListener<UserInfo>() {
					@Override
					public void done(List<UserInfo> list, BmobException e) {
						if(e == null){
							for (UserInfo data : list){
								nick = data.getUsername();
								avatar_url = data.getIcon().getUrl();
								UserProfileActivity userProfileActivity = new UserProfileActivity();
								isFinish_updateNick = userProfileActivity.updateRemoteNick(nick,false);

								Intent intent = new Intent(LoginActivity.this,
										MainActivity.class);

								if (isFinish_updateNick){
//									if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
//										pd.dismiss();
//									}
									startActivity(intent);
									pd.dismiss();


									finish();
								}

//								Bitmap bitmap = returnBitmap(avatar_url);
//								String avatarUrl = DemoHelper.getInstance().getUserProfileManager().uploadUserAvatar(Bitmap2Bytes(bitmap));
//								if (avatarUrl != null) {
//									Toast.makeText(LoginActivity.this, getString(R.string.toast_updatephoto_success),
//											Toast.LENGTH_SHORT).show();
//									isFinish_updateAvatar = true;
//								} else {
//									Toast.makeText(LoginActivity.this, getString(R.string.toast_updatephoto_fail),
//											Toast.LENGTH_SHORT).show();
//									isFinish_updateAvatar = true;
//								}

							}
						}
					}
				});

			}

			@Override
			public void onProgress(int progress, String status) {
				Log.d(TAG, "login: onProgress");
			}

			@Override
			public void onError(final int code, final String message) {
				Log.d(TAG, "login: onError: " + code);
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}


	/**
	 * register
	 * 
	 * @param view
	 */
	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DemoHelper.getInstance().getCurrentUsernName() != null) {
			usernameEditText.setText(DemoHelper.getInstance().getCurrentUsernName());
		}
		if (autoLogin) {
			return;
		}
	}


	/**
	 * 根据图片的url路径获得Bitmap对象
	 * @param url
	 * @return
	 */
	public Bitmap returnBitmap(String url) {
		URL fileUrl = null;
		Bitmap bitmap = null;

		try {
			fileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			HttpURLConnection conn = (HttpURLConnection) fileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	public byte[] Bitmap2Bytes(Bitmap bm){
		Log.i("tag","Bitmap2Bytes");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public void qqLogin(View view){
		UMShareAPI.get(this).getPlatformInfo(LoginActivity.this, SHARE_MEDIA.QQ, umAuthListener);
	}

	public void wxLogin(View view){
		Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
	}

	public void mobLogin(View view){
//		Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
		//打开注册页面
		RegisterPage registerPage = new RegisterPage();
		registerPage.setRegisterCallback(new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				// 解析注册结果
				if (result == SMSSDK.RESULT_COMPLETE) {
					@SuppressWarnings("unchecked") final
					HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
					final String country = (String) phoneMap.get("country");
					final String phone = (String) phoneMap.get("phone");

					BmobQuery<PhoneLogin> query = new BmobQuery<PhoneLogin>("PhoneLogin");
					query.addWhereEqualTo("phoneNumber",phone);
					query.findObjects(new FindListener<PhoneLogin>() {
						@Override
						public void done(List<PhoneLogin> list, BmobException e) {
							if (list.size() == 0 && e == null){
								PhoneLogin phoneLogin = new PhoneLogin();
								phoneLogin.setPhoneNumber(phone);
								phoneLogin.setCountry(country);
								phoneLogin.save(new SaveListener<String>() {
									@Override
									public void done(String s, BmobException e) {
										if (e==null){
											//同步到userinfo表中
											BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
											// 根据user_id字段升序显示数据
											query.order("-user_id");
											query.findObjects(new FindListener<UserInfo>() {
												@Override
												public void done(List<UserInfo> list, BmobException e) {
													if (e == null) {
														UserInfo lastUser = list.get(0);
														int lastUserId = lastUser.getUser_id();
														//Bmob后台注册
														HX_username = lastUserId + 1;
														BmobFile icon = new BmobFile(HX_username+"_icon.jpg","",icon_url);
														UserInfo userInfo = new UserInfo();
														userInfo.setUser_id(HX_username);
														userInfo.setUsername(phone);
														userInfo.setPassword("123");
														userInfo.setIcon(icon);
														userInfo.setAge(0);
														userInfo.setSex("");
														userInfo.setLocation("");
														userInfo.setPhoneNumber(phone);
														userInfo.save(new SaveListener<String>() {
															@Override
															public void done(String s, BmobException e) {
																if (e==null){
																	handler.sendEmptyMessage(1);
																	Toast.makeText(getApplicationContext(), R.string.Authorize_succeed, Toast.LENGTH_SHORT).show();
																}else{
																	Toast.makeText( getApplicationContext(), R.string.Authorize_fail+":"+e.toString(), Toast.LENGTH_SHORT).show();
																}
															}
														});
													}
												}
											});
										}else {
											Toast.makeText( getApplicationContext(), R.string.Authorize_fail+":"+e.toString(), Toast.LENGTH_SHORT).show();
										}
									}
								});
							}else{
								//已经用手机注册过
								BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
								query.addWhereEqualTo("phoneNumber",phone);
								query.findObjects(new FindListener<UserInfo>() {
									@Override
									public void done(List<UserInfo> list, BmobException e) {
										for (UserInfo data : list){
											int userID = data.getUser_id();
											String userPWD = data.getPassword();

											ProgressDialog pd = new ProgressDialog(LoginActivity.this);
											pd.setCanceledOnTouchOutside(false);
											pd.setOnCancelListener(new OnCancelListener() {

												@Override
												public void onCancel(DialogInterface dialog) {
													Log.d(TAG, "EMClient.getInstance().onCancel");
													progressShow = false;
												}
											});
											pd.setMessage(getString(R.string.Is_landing));
											pd.show();

											currentUsername = String.valueOf(userID);
											currentPassword = userPWD;
											HX_login(pd);
										}
									}
								});
							}
						}
					});

				}
			}
		});
		registerPage.show(LoginActivity.this);
	}

	private UMAuthListener umAuthListener = new UMAuthListener() {
		@Override
		public void onStart(SHARE_MEDIA share_media) {
			//授权开始的回调
			Toast.makeText(getApplicationContext(), R.string.Authorize_start, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
			uid = data.get("uid");
			nickname = data.get("name");
			gender = data.get("gender");
			province = data.get("province");
			city = data.get("city");
			icon_url = data.get("iconurl");
			location = province + "-" + city;

			BmobQuery<QQlogin> query = new BmobQuery<QQlogin>("QQlogin");
			query.addWhereEqualTo("uid",uid);
			query.findObjects(new FindListener<QQlogin>() {
				@Override
				public void done(List<QQlogin> list, BmobException e) {
					if (list.size() == 0 && e == null){
						QQlogin qqlogin = new QQlogin();
						qqlogin.setUid(uid);
						qqlogin.setCity(city);
						qqlogin.setGender(gender);
						qqlogin.setProvince(province);
						qqlogin.setNickname(nickname);
						qqlogin.save(new SaveListener<String>() {
							@Override
							public void done(String s, BmobException e) {
								if (e==null){
									//同步到userinfo表中
									BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
									// 根据user_id字段升序显示数据
									query.order("-user_id");
									query.findObjects(new FindListener<UserInfo>() {
										@Override
										public void done(List<UserInfo> list, BmobException e) {
											if (e == null) {
												UserInfo lastUser = list.get(0);
												int lastUserId = lastUser.getUser_id();
												//Bmob后台注册
												HX_username = lastUserId + 1;
												BmobFile icon = new BmobFile(HX_username+"_icon.jpg","",icon_url);
												UserInfo userInfo = new UserInfo();
												userInfo.setUid(uid);
												userInfo.setUser_id(HX_username);
												userInfo.setUsername(nickname);
												userInfo.setPassword("123");
												userInfo.setIcon(icon);
												userInfo.setAge(0);
												userInfo.setSex(gender);
												userInfo.setLocation(location);
												userInfo.save(new SaveListener<String>() {
													@Override
													public void done(String s, BmobException e) {
														if (e==null){
															handler.sendEmptyMessage(1);
															Toast.makeText(getApplicationContext(), R.string.Authorize_succeed, Toast.LENGTH_SHORT).show();
														}else{
															Toast.makeText( getApplicationContext(), R.string.Authorize_fail+":"+e.toString(), Toast.LENGTH_SHORT).show();
														}
													}
												});
											}
										}
									});
								}else {
									Toast.makeText( getApplicationContext(), R.string.Authorize_fail+":"+e.toString(), Toast.LENGTH_SHORT).show();
								}
							}
						});
					}else{
						//已经用QQ登陆过
						BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
						query.addWhereEqualTo("uid",uid);
						query.findObjects(new FindListener<UserInfo>() {
							@Override
							public void done(List<UserInfo> list, BmobException e) {
								for (UserInfo data : list){
									int userID = data.getUser_id();
									String userPWD = data.getPassword();

									ProgressDialog pd = new ProgressDialog(LoginActivity.this);
									pd.setCanceledOnTouchOutside(false);
									pd.setOnCancelListener(new OnCancelListener() {

										@Override
										public void onCancel(DialogInterface dialog) {
											Log.d(TAG, "EMClient.getInstance().onCancel");
											progressShow = false;
										}
									});
									pd.setMessage(getString(R.string.Is_landing));
									pd.show();

									currentUsername = String.valueOf(userID);
									currentPassword = userPWD;
									HX_login(pd);
								}
							}
						});
					}
				}
			});
		}

		@Override
		public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
			Toast.makeText( getApplicationContext(), R.string.Authorize_fail, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(SHARE_MEDIA share_media, int i) {
			Toast.makeText( getApplicationContext(), R.string.Authorize_cancel, Toast.LENGTH_SHORT).show();
		}
	};


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
	}

}
