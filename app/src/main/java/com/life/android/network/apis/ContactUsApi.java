package com.life.android.network.apis;

import com.life.android.network.model.StatusModel;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ContactUsApi {

    /**
     * @param apiKey : Server authentication key
     * @param map    : name, email, mobile_number, message, from_source = "Android/iOS"
     * @return StatusModel
     */
    @FormUrlEncoded
    @POST("submit_contact_us_form")
    Call<StatusModel> submitContactUsForm(@Header("API-KEY") String apiKey,
                                          @FieldMap HashMap<String, String> map);
}
