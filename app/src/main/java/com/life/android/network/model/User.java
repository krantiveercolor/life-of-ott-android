package com.life.android.network.model;

import java.io.Serializable;

public class User implements Serializable {
    private String status;

    private String user_id;

    private String name;

    private String email;

    private String phone;

    private String gender;

    private String join_date;

    private String last_login;

    private String image_url;

    private boolean password_available;

    private String mobile_verified;

    private String wallet_amount;

    private String country_code;

    private String data;

    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getJoin_date() {
        return join_date;
    }

    public String getLast_login() {
        return last_login;
    }

    public String getImage_url() {
        return image_url;
    }

    public boolean isPassword_available() {
        return password_available;
    }

    public String getMobile_verified() {
        return mobile_verified;
    }

    public String getWallet_amount() {
        return wallet_amount;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setJoin_date(String join_date) {
        this.join_date = join_date;
    }

    public void setLast_login(String last_login) {
        this.last_login = last_login;
    }

    public void setMobile_verified(String mobile_verified) {
        this.mobile_verified = mobile_verified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword_available(boolean password_available) {
        this.password_available = password_available;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWallet_amount(String wallet_amount) {
        this.wallet_amount = wallet_amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public String getGender() {
        return gender;
    }
}
