package com.life.android.network.apis;


import com.life.android.models.TransactionIdModel;
import com.life.android.network.model.APIResponse;
import com.life.android.network.model.CreatePaymentModel;
import com.life.android.network.model.WalletHistoryResponseModel;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PaymentApi {

    @FormUrlEncoded
    @POST("store_payment_info")
    Call<ResponseBody> savePayment(@Header("API-KEY") String apiKey,
                                   @FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("recharge_wallet_payment_info")
    Call<ResponseBody> addMoneyToWallet(@Header("API-KEY") String apiKey,
                                        @FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("store_event_purchase_info")
    Call<ResponseBody> saveEventPayment(@Header("API-KEY") String apiKey,
                                        @Field("event_id") String planId,
                                        @Field("user_id") String userId,
                                        @Field("paid_amount") String paidAmount,
                                        @Field("payment_info") String paymentInfo,
                                        @Field("payment_method") String paymentMethod);

    @GET("wallet_history")
    Call<WalletHistoryResponseModel> walletHistory(@Header("API-KEY") String apiKey,
                                                   @Query("user_id") String userId);

    @FormUrlEncoded
    @POST("create_payment_request")
    Call<APIResponse<CreatePaymentModel>> createPaymentRequest(@Header("API-KEY") String apiKey,
                                                               @FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("initiate_ssl_commerz")
    Call<TransactionIdModel> getTransId(@Header("API-KEY") String apiKey,
                                        @FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("payment_success")
    Call<ResponseBody> paySuccess(@Header("API-KEY") String apiKey,
                                  @FieldMap HashMap<String, String> map);
}
