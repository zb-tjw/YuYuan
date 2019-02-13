/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.utils.ChangeAddressPopwindow;
import com.hyphenate.chatuidemo.utils.ClearEditText;
import com.hyphenate.chatuidemo.utils.UserInfo;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * register screen
 *
 */
public class RegisterActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText confirmPwdEditText;
    private Spinner userSexSpinner;
    private Spinner userAgeSpinner;
    private TextView userWhereTextView;
    private String userSex;
    private int userAge;
    private int lastUserId;
    private int HX_username;
    private String location = null;
    private String username;
    private String pwd;
    private String userWhere;
    private ProgressDialog pd;
    private static final String[] Icon = {
            "http://img03.taobaocdn.com/poster_pic/i3/T1.IJzXlXEXXaH.X6X.JPEG",
            "http://www.3fantizi.com/Article/pic/2013121622416693.jpg",
            "http://www.qq1234.org/uploads/allimg/120509/1_120509171458_7.jpg",
            "http://img0.imgtn.bdimg.com/it/u=3959292123,1789415419&fm=206&gp=0.jpg"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_register);
        userNameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
        userSexSpinner = (Spinner) findViewById(R.id.userSex);
        userAgeSpinner = (Spinner) findViewById(R.id.userAge);
        userSexSpinner.setOnItemSelectedListener(this);
        userAgeSpinner.setOnItemSelectedListener(this);
        userAgeSpinner.setSelection(8, true);
        userWhereTextView = (TextView) findViewById(R.id.userWhere);
        userWhereTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeAddressPopwindow mChangeAddressPopwindow = new ChangeAddressPopwindow(RegisterActivity.this);
                mChangeAddressPopwindow.setAddress("山东","淄博","临淄区");
                mChangeAddressPopwindow.showAtLocation(userWhereTextView, Gravity.BOTTOM, 0, 0);
                mChangeAddressPopwindow
                        .setAddresskListener(new ChangeAddressPopwindow.OnAddressCListener() {

                            @Override
                            public void onClick(String province, String city, String area) {
                                // TODO Auto-generated method stub
//								Toast.makeText(RegisterActivity.this,
//										province + "-" + city + "-" + area,
//										Toast.LENGTH_LONG).show();
                                if ("".equals(area)) {
                                    location = province + "-" + city;
                                    userWhereTextView.setText(location);
                                } else {
                                    location = province + "-" + city + "-" + area;
                                    userWhereTextView.setText(location);
                                }
                            }
                        });
            }
        });

    }

    public void register(View view) {
        username = userNameEditText.getText().toString().trim();
        pwd = passwordEditText.getText().toString().trim();
        userWhere = userWhereTextView.getText().toString().trim();
        String confirm_pwd = confirmPwdEditText.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            if (userNameEditText.isFocused()) {
                return;
            } else {
                ((ClearEditText) userNameEditText).setShakeAnimation();
                userNameEditText.requestFocus();
            }
            return;
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            if (passwordEditText.isFocused()) {
                return;
            } else {
                ((ClearEditText) passwordEditText).setShakeAnimation();
                passwordEditText.requestFocus();
            }
            return;
        } else if (TextUtils.isEmpty(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            if (confirmPwdEditText.isFocused()) {
                return;
            } else {
                ((ClearEditText) confirmPwdEditText).setShakeAnimation();
                confirmPwdEditText.requestFocus();
            }
            return;
        } else if (!pwd.equals(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
            if (passwordEditText.isFocused()) {
                return;
            } else {
                ((ClearEditText) passwordEditText).setShakeAnimation();
                ((ClearEditText) confirmPwdEditText).setShakeAnimation();
                passwordEditText.requestFocus();
            }
            return;
        } else if (TextUtils.isEmpty(userWhere)) {
            Toast.makeText(this, getResources().getString(R.string.tjw_userWhere_empty), Toast.LENGTH_SHORT).show();
            if (userWhereTextView.isFocused()) {
                return;
            } else {
                userWhereTextView.requestFocus();
            }
            return;
        }

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            pd = new ProgressDialog(this);
            pd.setMessage(getResources().getString(R.string.Is_the_registered));
            pd.show();

            //查询表中是否有数据
            BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
            query.findObjects(new FindListener<UserInfo>() {
                @Override
                public void done(List<UserInfo> list, BmobException e) {
                    if (list.size() == 0) {
                        //Bmob后台注册
                        HX_username = 1000;
                        int label = (int) (Math.random() * (Icon.length - 1));
                        final BmobFile icon = new BmobFile(HX_username + "_icon.jpg", "", Icon[label]);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUser_id(HX_username);
                        userInfo.setUsername(username);
                        userInfo.setPassword(pwd);
                        userInfo.setIcon(icon);
                        userInfo.setAge(userAge);
                        userInfo.setSex(userSex);
                        userInfo.setLocation(location);
                        userInfo.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e == null) {
                                    Toast.makeText(RegisterActivity.this, R.string.tjw_bmobRegister_succeed, Toast.LENGTH_SHORT).show();
                                    mHandler.sendEmptyMessage(1);
                                } else {
                                    Toast.makeText(RegisterActivity.this, R.string.tjw_bmobRegister_failed + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // 根据user_id字段升序显示数据
                        BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
                        query.order("-user_id");
                        query.findObjects(new FindListener<UserInfo>() {
                            @Override
                            public void done(List<UserInfo> list, BmobException e) {
                                if (e == null) {
                                    UserInfo lastUser = list.get(0);
                                    lastUserId = lastUser.getUser_id();
                                    //Bmob后台注册
                                    HX_username = lastUserId + 1;
                                    int label = (int) (Math.random() * (Icon.length - 1));
                                    final BmobFile icon = new BmobFile(HX_username + "_icon.jpg", "", Icon[label]);
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.setUser_id(HX_username);
                                    userInfo.setUsername(username);
                                    userInfo.setPassword(pwd);
                                    userInfo.setIcon(icon);
                                    userInfo.setAge(userAge);
                                    userInfo.setSex(userSex);
                                    userInfo.setLocation(location);
                                    userInfo.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String objectId, BmobException e) {
                                            if (objectId != "") {
                                                Toast.makeText(RegisterActivity.this, R.string.tjw_bmobRegister_succeed, Toast.LENGTH_SHORT).show();
                                                mHandler.sendEmptyMessage(1);
                                            } else {
                                                Toast.makeText(RegisterActivity.this, R.string.tjw_bmobRegister_failed + e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });


        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            createAccount(HX_username,pwd);
                        }
                    }).start();
                    ;
                    break;
            }
            super.handleMessage(msg);
        }

    };

    public void createAccount(int user_id, String pwd) {
        try {
            // call method in SDK
            EMClient.getInstance().createAccount(String.valueOf(HX_username), pwd);
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!RegisterActivity.this.isFinishing())
                        pd.dismiss();
                    // save current user
                    DemoHelper.getInstance().setCurrentUserName(String.valueOf(HX_username));
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();

//					DemoApplication.currentUserNick = username;
                    new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getResources().getString(R.string.tjw_userId_message_remember))
                            .setContentText(getResources().getString(R.string.tjw_userId_message_loginUse))
                            .setConfirmText(getResources().getString(R.string.tjw_userId_message_next))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    // reuse previous dialog instance
                                    sDialog.setTitleText(getResources().getString(R.string.tjw_userId_message) + String.valueOf(HX_username))
                                            .setContentText(getResources().getString(R.string.tjw_userId_message_start))
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    finish();
                                                }
                                            })
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                }
                            })
                            .show();
                }
            });
        } catch (final HyphenateException e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!RegisterActivity.this.isFinishing())
                        pd.dismiss();
                    int errorCode = e.getErrorCode();
                    if (errorCode == EMError.NETWORK_ERROR) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                    } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                    } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                    } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void back(View view) {
        finish();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.userSex:
                userSex = RegisterActivity.this.getResources().getStringArray(R.array.sex)[position];
//				userSex = userSexSpinner.getSelectedItem().toString();
                break;
            case R.id.userAge:
                userAge = Integer.valueOf(RegisterActivity.this.getResources().getStringArray(R.array.age)[position].toString());
//				userAge = Integer.valueOf(userAgeSpinner.getSelectedItem().toString());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
