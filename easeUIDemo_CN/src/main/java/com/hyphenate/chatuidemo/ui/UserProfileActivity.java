package com.hyphenate.chatuidemo.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoApplication;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.db.UserDao;
import com.hyphenate.chatuidemo.utils.ChangeAddressPopwindow;
import com.hyphenate.chatuidemo.utils.UserInfo;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.exceptions.HyphenateException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class UserProfileActivity extends BaseActivity implements OnClickListener{
	
	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private ImageView headAvatar;
	private ImageView headPhotoUpdate;
	private ImageView iconRightArrow;
	private TextView tvNickName;
	private TextView tvUsername;
	private ProgressDialog dialog;
	private RelativeLayout rlNickName;

	private TextView tv_send_message;
	private TextView tv_delete_firend;
	private TextView tv_add_blacklist;

	private String username;//传过来的用户ID

	//对应每一条数据的布局
	private RelativeLayout rl_age;
	private RelativeLayout rl_sex;
	private RelativeLayout rl_address;
	private RelativeLayout rl_id;

	//对应的文本信息，需要从后台读取设置上
	private TextView tv_age;
	private TextView tv_sex;
	private TextView tv_address;
	private TextView tv_id;

	//页面最右侧的三个箭头
	private ImageView img_user_age;
	private ImageView img_user_sex;
	private ImageView img_user_address;

	//Bmob后台查询时的依据
	private String objectId ="";
	private int id;
	private int age;
	private String Bmob_nickname = "";
	private String sex  = "";
	private String address  = "";

	//修改数据时的中间变量
	private int age_new;
	private String sex_new = "";
	private String address_new = "";
	private String Bmob_nickname_new = "";


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.em_activity_user_profile);
		initView();
		initListener();
		setDate(username);
	}

	private void setDate(String username) {
		BmobQuery<UserInfo> query = new BmobQuery<UserInfo>("userInfo");
		query.addWhereEqualTo("user_id",Integer.parseInt(username));
		query.findObjects(new FindListener<UserInfo>() {
			@Override
			public void done(List<UserInfo> list, BmobException e) {
				if(e == null){
					for (UserInfo data : list){
						objectId = data.getObjectId();
						id = data.getUser_id();
						Bmob_nickname = data.getUsername();
						age = data.getAge();
						sex = data.getSex();
						address = data.getLocation();

						tv_age.setText(String.valueOf(age));
						tv_id.setText(String.valueOf(id));
						tv_sex.setText(sex);
						tv_address.setText(address);
					}
				}else {
					Toast.makeText(UserProfileActivity.this, R.string.read_userdetail_failed,Toast.LENGTH_SHORT).show();
					Log.i("tag",R.string.read_userdetail_failed+e.toString());
				}
			}
		});
	}

	private void initView() {
		headAvatar = (ImageView) findViewById(R.id.user_head_avatar);
		headPhotoUpdate = (ImageView) findViewById(R.id.user_head_headphoto_update);
		tvUsername = (TextView) findViewById(R.id.user_username);
		tvNickName = (TextView) findViewById(R.id.user_nickname);
		rlNickName = (RelativeLayout) findViewById(R.id.rl_nickname);
		iconRightArrow = (ImageView) findViewById(R.id.ic_right_arrow);

		tv_send_message = (TextView) findViewById(R.id.send_message);
		tv_delete_firend = (TextView) findViewById(R.id.delete_firend);
		tv_add_blacklist = (TextView) findViewById(R.id.add_blacklist);

		rl_id = (RelativeLayout) findViewById(R.id.rl_name);
		rl_sex = (RelativeLayout) findViewById(R.id.rl_sex);
		rl_age = (RelativeLayout) findViewById(R.id.rl_age);
		rl_address = (RelativeLayout) findViewById(R.id.rl_address);

		tv_age = (TextView) findViewById(R.id.user_age);
		tv_sex = (TextView) findViewById(R.id.user_sex);
		tv_address = (TextView) findViewById(R.id.user_address);
		tv_id = (TextView) findViewById(R.id.user_name);

		img_user_age = (ImageView) findViewById(R.id.img_user_age);
		img_user_sex = (ImageView) findViewById(R.id.img_user_sex);
		img_user_address = (ImageView) findViewById(R.id.img_user_address);
	}
	
	private void initListener() {
		Intent intent = getIntent();
		username = intent.getStringExtra("username");
		boolean enableUpdate = intent.getBooleanExtra("setting", false);
		if (enableUpdate) {
			headPhotoUpdate.setVisibility(View.VISIBLE);
			iconRightArrow.setVisibility(View.VISIBLE);
			rlNickName.setOnClickListener(this);
			headAvatar.setOnClickListener(this);

			tv_send_message.setVisibility(View.GONE);
			tv_delete_firend.setVisibility(View.GONE);
			tv_add_blacklist.setVisibility(View.GONE);

			rl_age.setOnClickListener(this);
			rl_sex.setOnClickListener(this);
			rl_address.setOnClickListener(this);

		} else {
			headPhotoUpdate.setVisibility(View.GONE);
			iconRightArrow.setVisibility(View.INVISIBLE);

			img_user_age.setVisibility(View.GONE);
			img_user_sex.setVisibility(View.GONE);
			img_user_address.setVisibility(View.GONE);

			tv_send_message.setOnClickListener(this);
			tv_delete_firend.setOnClickListener(this);
			tv_add_blacklist.setOnClickListener(this);
		}
		if(username != null){
    		if (username.equals(EMClient.getInstance().getCurrentUser())) {
    			tvUsername.setText(EMClient.getInstance().getCurrentUser());
    			EaseUserUtils.setUserNick(username, tvNickName);
                EaseUserUtils.setUserAvatar(this, username, headAvatar);
    		} else {
    			tvUsername.setText(username);
    			EaseUserUtils.setUserNick(username, tvNickName);
    			EaseUserUtils.setUserAvatar(this, username, headAvatar);
    			asyncFetchUserInfo(username);
    		}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_head_avatar://修改头像
			uploadHeadPhoto();
			break;
		case R.id.rl_nickname://修改昵称
			final EditText editText = new EditText(this);
			new AlertDialog.Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
					.setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String nickString = editText.getText().toString();
							if (TextUtils.isEmpty(nickString)) {
								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
								return;
							}
							updateRemoteNick(nickString,true);
						}
					}).setNegativeButton(R.string.dl_cancel, null).show();
			break;
			case R.id.rl_age:
				updateUserDialog(true,false);
				break;
			case R.id.rl_sex:
				updateUserDialog(false,true);
				break;
			case R.id.rl_address:
				ChangeAddressPopwindow mChangeAddressPopwindow = new ChangeAddressPopwindow(UserProfileActivity.this);
				mChangeAddressPopwindow.setAddress("山东","淄博","临淄区");
				mChangeAddressPopwindow.showAtLocation(tv_address, Gravity.BOTTOM, 0, 0);
				mChangeAddressPopwindow
						.setAddresskListener(new ChangeAddressPopwindow.OnAddressCListener() {

							@Override
							public void onClick(String province, String city, String area) {
								// TODO Auto-generated method stub
								if ("".equals(area)){
									address_new = province + "-" + city;
									tv_address.setText(address_new);
								}else {
									address_new = province + "-" + city + "-" + area;
									tv_address.setText(address_new);
									updateBmobUserDate(false,false,true);
								}
							}
						});
				break;
			case R.id.send_message://发送消息
				startActivity(new Intent(UserProfileActivity.this, ChatActivity.class).putExtra("userId", username));
				break;
			case R.id.delete_firend://删除好友
				String st1 = getResources().getString(R.string.deleting);
				final String st2 = getResources().getString(R.string.Delete_failed);
				final ProgressDialog pd = new ProgressDialog(UserProfileActivity.this);
				pd.setMessage(st1);
				pd.setCanceledOnTouchOutside(false);
				pd.show();
				try {
					EMClient.getInstance().contactManager().deleteContact(username);
					// remove user from memory and database
					UserDao dao = new UserDao(UserProfileActivity.this);
					dao.deleteContact(username);
					DemoHelper.getInstance().getContactList().remove(username);
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							finish();
						}
					});
				} catch (HyphenateException e) {
					e.printStackTrace();
				}
				break;
			case R.id.add_blacklist://加入黑名单
				EaseContactListFragment easecontactlistfragment = new EaseContactListFragment();
				boolean isFinishmoveToBlacklist = easecontactlistfragment.moveToBlacklist(username,UserProfileActivity.this,false);
				if (isFinishmoveToBlacklist){
					finish();
				}
				break;
		default:
			break;
		}

	}

	private void updateUserDialog(final boolean updateAge, final boolean updateSex) {
		String title = "";
		if (updateAge){
			title = getString(R.string.set_age);
		}
		if (updateSex){
			title = getString(R.string.set_Sex);
		}
		final EditText editText = new EditText(this);
		if (updateAge){
			editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		new Builder(this).setTitle(title).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String editText_text = editText.getText().toString();
                        if (TextUtils.isEmpty(editText_text)) {
                            Toast.makeText(UserProfileActivity.this, R.string.content_not_block, Toast.LENGTH_SHORT).show();
                            return;
                        }
						if (updateAge){
							age_new = Integer.parseInt(editText_text);
							if (age_new<=0 || age_new >200){
								Toast.makeText(UserProfileActivity.this, R.string.one_to_hunderd,Toast.LENGTH_SHORT).show();
							}else{
								updateBmobUserDate(updateAge,updateSex,false);
							}
						}
						if (updateSex){
							sex_new = editText_text;
							if (getString(R.string.man).equals(editText_text) || getString(R.string.woman).equals(editText_text)){
								updateBmobUserDate(updateAge,updateSex,false);
							}else{
								Toast.makeText(UserProfileActivity.this,R.string.man_or_woman,Toast.LENGTH_SHORT).show();
							}
						}
                    }
                }).setNegativeButton(R.string.dl_cancel, null).show();
	}

	private void updateBmobUserDate(final boolean updateAge, final boolean updateSex, final boolean updateAddress) {
		BmobQuery<UserInfo> query = new BmobQuery<UserInfo>("userInfo");
		query.addWhereEqualTo("user_id",id);
		query.findObjects(new FindListener<UserInfo>() {
            @Override
            public void done(List<UserInfo> list, BmobException e) {
                if(e == null) {
                    for (UserInfo data : list){
                        UserInfo userInfo = new UserInfo();
						userInfo.setUser_id(id);
						if (updateAge){
							userInfo.setAge(age_new);
						}else {
							userInfo.setAge(age);
						}
                        if (updateSex){
							userInfo.setSex(sex_new);
						}else{
							userInfo.setSex(sex);
						}
						if (updateAddress){
							userInfo.setLocation(address_new);
						}else {
							userInfo.setLocation(address);
						}
						userInfo.setUsername(Bmob_nickname);
                        userInfo.update(objectId, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e!=null){
                                    Log.i("bmob","update success");
                                    Toast.makeText(UserProfileActivity.this,"update success",Toast.LENGTH_LONG).show();
									if (updateAge){
										age = age_new;
										tv_age.setText(String.valueOf(age_new));
									}
									if (updateSex){
										sex = sex_new;
										tv_sex.setText(sex_new);
									}
									if (updateAddress){
										address = address_new;
										tv_address.setText(address_new);
									}
                                }else{
                                    Log.i("bmob","update fail："+e.getMessage()+","+e.getErrorCode());
                                    Toast.makeText(UserProfileActivity.this,"update fail："+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
	}

	public void asyncFetchUserInfo(String username){
		DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {
			
			@Override
			public void onSuccess(EaseUser user) {
				if (user != null) {
				    DemoHelper.getInstance().saveContact(user);
				    if(isFinishing()){
				        return;
				    }
					tvNickName.setText(user.getNick());
					if(!TextUtils.isEmpty(user.getAvatar())){
						 Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(headAvatar);
					}else{
					    Glide.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).into(headAvatar);
					}
				}
			}
			
			@Override
			public void onError(int error, String errorMsg) {
			}
		});
	}
	
	
	
	private void uploadHeadPhoto() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.dl_title_upload_photo);
		builder.setItems(new String[] { getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload) },
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
									Toast.LENGTH_SHORT).show();
							break;
						case 1:
							Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
							pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(pickIntent, REQUESTCODE_PICK);
							break;
						default:
							break;
						}
					}
				});
		builder.create().show();
	}
	
	

	public boolean updateRemoteNick(final String nickName, final boolean isShown) {
		if (isShown){
			dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean updatenick = DemoHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
				if (isShown){
					if (UserProfileActivity.this.isFinishing()) {
						return;
					}
				}
				if (!updatenick) {
					runOnUiThread(new Runnable() {
						public void run() {
							if (isShown){
								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
										.show();
								dialog.dismiss();
							}
							Log.i("tag","updatenick failed");
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (isShown){
								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
										.show();
								dialog.dismiss();
								tvNickName.setText(nickName);
								Bmob_nickname_new = nickName;
								Bmob_nickname = Bmob_nickname_new;
								updateBmobUserDate(false,false,false);
							}
							DemoApplication.currentUserNick = nickName;
							Log.i("tag","updatenick successed");
						}
					});
				}
			}
		}).start();
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_PICK:
			if (data == null || data.getData() == null) {
				return;
			}
			startPhotoZoom(data.getData());
			break;
		case REQUESTCODE_CUTTING:
			if (data != null) {
				setPicToView(data);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}
	
	/**
	 * save the picture data
	 * 
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(getResources(), photo);
			headAvatar.setImageDrawable(drawable);
			uploadUserAvatar(Bitmap2Bytes(photo));
		}

	}
	
	public void uploadUserAvatar(final byte[] data) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String avatarUrl = DemoHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						if (avatarUrl != null) {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
									Toast.LENGTH_SHORT).show();
							try {
								//new一个文件对象用来保存图片，默认保存当前工程根目录
								File file = new File("/sdcard/tjw_YuYuan/");
								file.mkdirs();
								File imageFile = new File("/sdcard/tjw_YuYuan/"+username+".jpg");
								//创建输出流
								FileOutputStream outStream = new FileOutputStream(imageFile);
								//写入数据
								outStream.write(data);
								//关闭输出流
								outStream.close();
								final BmobFile icon = new BmobFile(new File("/sdcard/tjw_YuYuan/"+username+".jpg"));
								icon.upload(new UploadFileListener() {
									@Override
									public void done(BmobException e) {
										UserInfo userInfo = new UserInfo();
										userInfo.setIcon(icon);
										userInfo.setAge(age);
										userInfo.setUser_id(Integer.parseInt(username));
										userInfo.update(objectId,new UpdateListener() {
											@Override
											public void done(BmobException e) {
												if(e==null){
													Toast.makeText(UserProfileActivity.this, R.string.bmob_icon_success,Toast.LENGTH_SHORT).show();
												}else{
													Log.i("tag", "BmobException===="+e.toString());
													Toast.makeText(UserProfileActivity.this, R.string.bmob_icon_fail,Toast.LENGTH_SHORT).show();
												}
											}
										});
									}
								});
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
									Toast.LENGTH_SHORT).show();
						}

					}
				});

			}
		}).start();

		dialog.show();
	}
	
	
	public byte[] Bitmap2Bytes(Bitmap bm){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
}
