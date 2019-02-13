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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.utils.UserInfo;
import com.hyphenate.easeui.widget.EaseAlertDialog;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;

public class AddContactActivity extends BaseActivity implements View.OnClickListener {
	private EditText editText;
	private RelativeLayout searchedUserLayout;
	private TextView nameText;
	private TextView tv_nick_name;
	private Button searchBtn;
	private String toAddUsername;
	private ProgressDialog progressDialog;
	private ImageView user_icon;
	private String nick = "";
	private String avatar = "";
	private String username = "";
	private String user_id = "";
	private int age;
	private String sex = "";
	private String address = "";
	private String nickname = "";
	private String avator_url = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_add_contact);
		TextView mTextView = (TextView) findViewById(R.id.add_list_friends);
		
		editText = (EditText) findViewById(R.id.edit_note);
		String strAdd = getResources().getString(R.string.add_friend);
		mTextView.setText(strAdd);
		String strUserName = getResources().getString(R.string.user_name);
		editText.setHint(strUserName);
		searchedUserLayout = (RelativeLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);
		searchBtn = (Button) findViewById(R.id.search);
		tv_nick_name = (TextView) findViewById(R.id.nick_name);
		user_icon = (ImageView) findViewById(R.id.avatar);
		searchedUserLayout.setOnClickListener(this);
	}
	
	
	/**
	 * search contact
	 * @param v
	 */
	public void searchContact(View v) {
		final String name = editText.getText().toString();
		String saveText = searchBtn.getText().toString();
		
		if (getString(R.string.button_search).equals(saveText)) {
			toAddUsername = name;
			if(TextUtils.isEmpty(name)) {
				new EaseAlertDialog(this,R.string.Please_enter_a_username).show();
				return;
			}
			// TODO you can search the user from your app server here.
			BmobQuery<UserInfo> query = new BmobQuery<UserInfo>("userInfo");
			query.addWhereEqualTo("user_id",Integer.parseInt(toAddUsername));
			query.findObjects(new FindListener<UserInfo>() {
				@Override
				public void done(List<UserInfo> list, BmobException e) {
					if(e == null && list.size() > 0){
						Log.i("tag","DATA===="+list.toString());
						searchedUserLayout.setVisibility(View.VISIBLE);
						nameText.setText(toAddUsername);
						for (UserInfo data : list){
							tv_nick_name.setText("(" + data.getUsername() + ")");
							BmobFile icon_file = data.getIcon();
							user_id = String.valueOf(data.getUser_id());
							nickname = data.getUsername();
							age = data.getAge();
							sex = data.getSex();
							address = data.getLocation();
							avator_url = data.getIcon().getUrl();
							if ("".equals(avatar)){
								downloadFile(icon_file);
							}else{
							}
						}
					}else {
						Toast.makeText(AddContactActivity.this,R.string.no_user,Toast.LENGTH_LONG).show();
					}
				}
			});

		} 
	}


	private void downloadFile(BmobFile file){
		Log.i("tag","downloadFile");
		//允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
		File dir = new File("/sdcard/tjw_YuYuan/");
		dir.mkdirs();
		File saveFile = new File("/sdcard/tjw_YuYuan/", toAddUsername+".jpg");
		file.download(saveFile, new DownloadFileListener() {

			@Override
			public void onStart() {
//				Toast.makeText(AddContactActivity.this,R.string.start_download,Toast.LENGTH_SHORT).show();
			}

			@Override
			public void done(String savePath,BmobException e) {
				if(e==null){
//					toast("下载成功,保存路径:"+savePath);
//					Toast.makeText(AddContactActivity.this,R.string.download_success,Toast.LENGTH_SHORT).show();
					fileTobitmap();
					}else{
//					toast("下载失败："+e.getErrorCode()+","+e.getMessage());
					Toast.makeText(AddContactActivity.this, getString(R.string.downoad_fail)+e.getErrorCode()+","+e.getMessage(),Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onProgress(Integer value, long newworkSpeed) {
				Log.i("bmob","下载进度："+value+","+newworkSpeed);
			}

		});
	}


	public void fileTobitmap(){
		File f = new File("/sdcard/tjw_YuYuan/", toAddUsername+".jpg");
		if (f.exists()) {
			Bitmap bm = BitmapFactory.decodeFile("/sdcard/tjw_YuYuan/"+toAddUsername+".jpg");
			final BitmapDrawable bd = new BitmapDrawable(bm);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					user_icon.setImageDrawable(bd);
				}
			});
		}
	}
	
	/**
	 *  add contact
	 * @param view
	 */
	public void addContact(View view){
		if(EMClient.getInstance().getCurrentUser().equals(nameText.getText().toString())){
			new EaseAlertDialog(this,R.string.not_add_myself).show();
			return;
		}
		
		if(DemoHelper.getInstance().getContactList().containsKey(nameText.getText().toString())){
		    //let the user know the contact already in your contact list
		    if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(nameText.getText().toString())){
		        new EaseAlertDialog(this,R.string.user_already_in_contactlist).show();
		        return;
		    }
			new EaseAlertDialog(this,R.string.This_user_is_already_your_friend).show();
			return;
		}
		
		progressDialog = new ProgressDialog(this);
		String stri = getResources().getString(R.string.Is_sending_a_request);
		progressDialog.setMessage(stri);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		
		new Thread(new Runnable() {
			public void run() {
				
				try {
					//demo use a hardcode reason here, you need let user to input if you like
					String s = getResources().getString(R.string.Add_a_friend);
					EMClient.getInstance().contactManager().addContact(toAddUsername, s);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s1 = getResources().getString(R.string.send_successful);
							Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s2 = getResources().getString(R.string.Request_add_buddy_failure);
							Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
	
	public void back(View v) {
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.ll_user:
				//这里如果开启的话查找到就自动添加上了
//				Intent intent = new Intent(AddContactActivity.this, UserProfileActivity.class);
//				intent.putExtra("username", toAddUsername);
//				intent.putExtra("isShowSendMessage", false);
//				startActivity(intent);
				Intent intent = new Intent(AddContactActivity.this, BmobUserInfo.class);
				intent.putExtra("user_id", toAddUsername);
				intent.putExtra("nickname", nickname);
				intent.putExtra("age", age);
				intent.putExtra("sex", sex);
				intent.putExtra("address", address);
				intent.putExtra("avator_url", avator_url);
				Log.i("tag","toAddUsername==="+toAddUsername);
				startActivity(intent);
				break;
		}
	}
}
