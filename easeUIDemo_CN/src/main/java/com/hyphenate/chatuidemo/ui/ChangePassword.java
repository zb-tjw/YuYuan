package com.hyphenate.chatuidemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.utils.UserInfo;

import cn.bmob.v3.BmobQuery;

public class ChangePassword extends Activity implements View.OnClickListener {

    private EditText edt_preview_password;
    private EditText edt_new_password;
    private EditText edt_confirm_password;
    private Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        edt_preview_password = (EditText) findViewById(R.id.edt_preview_password);
        edt_new_password = (EditText) findViewById(R.id.edt_new_password);
        edt_confirm_password = (EditText) findViewById(R.id.edt_confirm_password);
        btn_register = (Button) findViewById(R.id.btn_register);

        btn_register.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        String preview_password = edt_preview_password.getText().toString();
        String new_password = edt_new_password.getText().toString();
        String confirm_password = edt_confirm_password.getText().toString();

        if (TextUtils.isEmpty(preview_password)){
            Toast.makeText(ChangePassword.this,"请输入原密码",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(new_password)){
            Toast.makeText(ChangePassword.this,"请输入新密码",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(confirm_password)){
            Toast.makeText(ChangePassword.this,"请再次输入新密码",Toast.LENGTH_SHORT).show();
        }else if(!new_password.equals(confirm_password)){
            Toast.makeText(ChangePassword.this,"两次密码不一致请重新输入",Toast.LENGTH_SHORT).show();
        }else{
            BmobQuery<UserInfo> query = new BmobQuery<UserInfo>("UserInfo");
//            query.addWhereEqualTo("phoneNumber",phone)
            UserInfo userInfo = new UserInfo();
            userInfo.setPassword(new_password);
//            userInfo.
        }
    }
}
