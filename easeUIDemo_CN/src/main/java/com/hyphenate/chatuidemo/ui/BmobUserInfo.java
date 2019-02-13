package com.hyphenate.chatuidemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Lenovo_PC on 2017/4/25.
 */

public class BmobUserInfo extends Activity {


    private RelativeLayout title;
    private ImageView userHeadAvatar;
    private ImageView userHeadHeadphotoUpdate;
    private TextView userUsername;
    private RelativeLayout rlName;
    private TextView userName;
    private RelativeLayout rlNickname;
    private TextView userNickname;
    private ImageView icRightArrow;
    private RelativeLayout rlAge;
    private TextView userAge;
    private ImageView imgUserAge;
    private RelativeLayout rlSex;
    private TextView userSex;
    private ImageView imgUserSex;
    private RelativeLayout rlAddress;
    private TextView userAddress;
    private ImageView imgUserAddress;
    private String user_id = "";
    private String nickname = "";
    private int age;
    private String sex = "";
    private String address = "";
    private String avatar_url = "";
    private Drawable drawable = null;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    userHeadAvatar.setImageDrawable(drawable);
                    break;
                case 2:
                    userName.setText(user_id);
                    userNickname.setText(nickname);
                    userAge.setText(String.valueOf(age));
                    userSex.setText(sex);
                    userAddress.setText(address);
                    break;
            }
        }
    };

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-04-25 15:38:25 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        title = (RelativeLayout)findViewById( R.id.title );
        userHeadAvatar = (ImageView)findViewById( R.id.user_head_avatar );
        userHeadHeadphotoUpdate = (ImageView)findViewById( R.id.user_head_headphoto_update );
        userUsername = (TextView)findViewById( R.id.user_username );
        rlName = (RelativeLayout)findViewById( R.id.rl_name );
        userName = (TextView)findViewById( R.id.user_name );
        rlNickname = (RelativeLayout)findViewById( R.id.rl_nickname );
        userNickname = (TextView)findViewById( R.id.user_nickname );
        icRightArrow = (ImageView)findViewById( R.id.ic_right_arrow );
        rlAge = (RelativeLayout)findViewById( R.id.rl_age );
        userAge = (TextView)findViewById( R.id.user_age );
        imgUserAge = (ImageView)findViewById( R.id.img_user_age );
        rlSex = (RelativeLayout)findViewById( R.id.rl_sex );
        userSex = (TextView)findViewById( R.id.user_sex );
        imgUserSex = (ImageView)findViewById( R.id.img_user_sex );
        rlAddress = (RelativeLayout)findViewById( R.id.rl_address );
        userAddress = (TextView)findViewById( R.id.user_address );
        imgUserAddress = (ImageView)findViewById( R.id.img_user_address );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmob_userinfo);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        nickname = intent.getStringExtra("nickname");
        age = intent.getIntExtra("age",0);
        sex = intent.getStringExtra("sex");
        address = intent.getStringExtra("address");
        avatar_url = intent.getStringExtra("avator_url");
        findViews();

        initData();
    }

    private void initData2() {
        userName.setText(user_id);
        userNickname.setText(nickname);
        userAge.setText(String.valueOf(age));
        userSex.setText(sex);
        userAddress.setText(address);
    }

    private void initData() {
        Log.i("tag","user_id==="+user_id);
        if (user_id != null){
            setAvatorPic();
        }

    }

    private void setAvatorPic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(2);
                drawable = UrlToDrawable();
                if (drawable != null){
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }


    private Drawable UrlToDrawable() {
        Log.i("tag","UrlToDrawable");
        URL url = null;
        try {
            url = new URL(avatar_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            int max = conn.getContentLength();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;

            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] result = baos.toByteArray();
            BitmapDrawable bd = new BitmapDrawable(BitmapFactory.decodeByteArray(result, 0, result.length));
            return bd;
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void back(View view){
        finish();
    }

}
