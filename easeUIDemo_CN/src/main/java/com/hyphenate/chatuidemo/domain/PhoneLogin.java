package com.hyphenate.chatuidemo.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by Lenovo_PC on 2017/5/4.
 */

public class PhoneLogin extends BmobObject {
    private String phoneNumber;
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
