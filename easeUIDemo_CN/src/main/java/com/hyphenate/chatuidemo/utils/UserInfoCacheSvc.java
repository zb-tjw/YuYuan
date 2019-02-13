package com.hyphenate.chatuidemo.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.hyphenate.chatuidemo.db.DbOpenHelper;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * 缓存用户信息（主要用于聊天显示昵称和头像）
 */
public class UserInfoCacheSvc {
	
	
	private static String icon_url = "http://www.3fantizi.com/Article/pic/2013121622416693.jpg";
    private static BmobFile bmobfile = null;
    private static File saveFile = null;
    private static String dir = "";
    private static String username;
	private static String sex;
	private static int age;
	private static String location;
	private static Context appContext;

	
	
	public static List<UserApiModel> getAllList(){
		Dao<UserApiModel, Integer> daoScene = SqliteHelper.getInstance().getUserDao();
		try {
			List<UserApiModel> list = daoScene.queryBuilder().query();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static UserApiModel getByChatUserName(String chatUserName){
		Dao<UserApiModel, Integer> dao = SqliteHelper.getInstance().getUserDao();
		try {
			UserApiModel model = dao.queryBuilder().where().eq("EaseMobUserName", chatUserName).queryForFirst();
			return model;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static UserApiModel getById(long id){
		Dao<UserApiModel, Integer> dao = SqliteHelper.getInstance().getUserDao();
		try {
			UserApiModel model = dao.queryBuilder().where().eq("Id", id).queryForFirst();
			return model;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static boolean createOrUpdate(String chatUserName, String userNickName, String avatarUrl){
		try {
			Dao<UserApiModel, Integer> dao = SqliteHelper.getInstance().getUserDao();

			UserApiModel user = getByChatUserName(chatUserName);

			int changedLines = 0;
			if (user == null){
				user = new UserApiModel();
				user.setUsername(userNickName);
				user.setHeadImg(avatarUrl);
				user.setEaseMobUserName(chatUserName);

				changedLines = dao.create(user);
			}else {
				user.setUsername(userNickName);
				user.setHeadImg(avatarUrl);
				user.setEaseMobUserName(chatUserName);

				changedLines = dao.update(user);
			}

			if(changedLines > 0){
				Log.i("UserInfoCacheSvc", "操作成功~");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("UserInfoCacheSvc", "操作异常~"+e.toString());
		}
		
		return false;
	}

	public static boolean createOrUpdate(UserApiModel model){

		if(model == null) return false;

		try {
			Dao<UserApiModel, Integer> dao = SqliteHelper.getInstance().getUserDao();

//			UserApiModel user = getById(model.Id);
			UserApiModel user = getByChatUserName(model.EaseMobUserName);
//			UserApiModel user = getByChatUserName(model.Username);

			if (!StringUtils.isNullOrEmpty(model.getHeadImg())){
                // 拼接头像的完整路径，或者直接用服务端返回的绝对路径
//				String fullPath = "http://image.baidu.com/" + model.getHeadImg();
				BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
	            query.addWhereEqualTo("username",model.EaseMobUserName);
	            query.findObjects(new FindListener<UserInfo>() {

	                @Override
	                public void done(
	                        List<UserInfo> arg0,
	                        BmobException arg1) {
	                    // TODO Auto-generated method stub
	                    if(arg1==null){
	                        for (UserInfo data : arg0) {
	                            bmobfile = data.getIcon();
	                            icon_url = bmobfile.getFileUrl();
	                            username = data.getUsername();
								sex = data.getSex();
								age = data.getAge();
								location = data.getLocation();
	                            Log.i("tag", "UserInfoCacheSvc_Bmob_username=="+username);
	                            String tjw_nick = query(DbOpenHelper.getInstance(appContext).getReadableDatabase());
	                            UserInfoCacheSvc.createOrUpdate(username, tjw_nick, icon_url);
	                            }
	                    }
	                    if(bmobfile!= null){
	                        dir = Environment.getExternalStorageDirectory()+"/tjw_pic";
	                        File saveFile = new File(dir, bmobfile.getFilename());
	                    }
	                }
	            });
	            Log.i("tag", "icon_url==="+icon_url);
//				model.setHeadImg(fullPath);
				model.setHeadImg(icon_url);
//				UserInfoCacheSvc.createOrUpdate(username, "", icon_url);
			}
			int changedLines = 0;
			if (user == null){
				changedLines = dao.create(model);
			}else {
				model.setRecordId(user.getRecordId());
				changedLines = dao.update(model);
			}

			if(changedLines > 0){
				Log.i("UserInfoCacheSvc", "操作成功~");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("UserInfoCacheSvc", "操作异常~"+e.toString());
		}

		return false;
	}
	
	
	private static String query(SQLiteDatabase db) {   
		//查询获得游标   
		Cursor cursor = db.query ("uers",null,"username=?",new String[]{username},null,null,null); 
		Log.i("tag", "UserInfoCacheSvc_limit_username==="+username);

		//判断游标是否为空   
		if(cursor != null) {
			//遍历游标
			while (cursor.moveToNext()) {
				String nick = cursor.getString(cursor.getColumnIndex("nick"));  
				Log.i("tag", "UserInfoCacheSvc_nick===="+nick);
				//获得头像   
				String avatar=cursor.getString(1);   
				Log.i("tag", "UserInfoCacheSvc_avatar===="+avatar);
				//获得用户名   
				String username=cursor.getString(2); 
				Log.i("tag", "UserInfoCacheSvc_username===="+username);
				return nick;
			}   
		} 
		return username;
	}

}
