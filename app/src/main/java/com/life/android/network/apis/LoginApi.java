package com.life.android.network.apis;

import com.life.android.network.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginApi {

    @FormUrlEncoded
    @POST("login")
    Call<User> postLoginStatus(@Header("API-KEY") String apiKey,
                               @Field("email") String email,
                               @Field("password") String password,
                               @Field("device_id") String deviceId,
                               @Field("device_name") String deviceName);

    @FormUrlEncoded
    @POST("send_otp")
    Call<User> sendOtp(
            @Field("phone") String phone);

    @FormUrlEncoded
    @POST("verify_otp")
    Call<User> verifyOtp(
            @Field("phone") String phone,
            @Field("otp_code") String otp);

    @FormUrlEncoded
    @POST("verify_otp_firebase")
    Call<User> verifyOtpFirebase(
            @Field("user_id") String user_id,
            @Field("firebase_auth_token") String firebase_auth_token,
            @Header("API-KEY") String apiKey);
}
