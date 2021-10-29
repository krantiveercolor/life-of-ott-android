package com.life.android.network.apis;

import com.life.android.network.model.CmsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CmsApi {
    @GET("privacy_policy")
    Call<CmsModel> privacyPolicy(@Header("API-KEY") String apiKey);

    @GET("terms_and_conditions")
    Call<CmsModel> termsAndConditions(@Header("API-KEY") String apiKey);

    @GET("help_and_support")
    Call<CmsModel> helpAndSupport(@Header("API-KEY") String apiKey);

    @GET("pay_and_watch_terms_and_conditions")
    Call<CmsModel> payAndWatchTermsAndConditions(@Header("API-KEY") String apiKey);
}