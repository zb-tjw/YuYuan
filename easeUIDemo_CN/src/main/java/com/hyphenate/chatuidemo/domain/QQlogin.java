package com.hyphenate.chatuidemo.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by Lenovo_PC on 2017/4/26.
 */

public class QQlogin extends BmobObject {

    private String uid;//用户唯一标识-uid
    private String nickname;//昵称-name
    private String province;//-province
    private String city;//-city
    private String gender;//-gender
    private String icon_url;//-iconurl

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
