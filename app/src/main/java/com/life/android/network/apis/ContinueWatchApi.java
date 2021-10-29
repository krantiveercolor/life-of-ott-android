package com.life.android.network.apis;

import com.life.android.network.model.APIResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ContinueWatchApi {
    @FormUrlEncoded
    @POST("continue_watch/save")
    Call<APIResponse<Object>> saveContinueWatch(@Header("API-KEY") String apiKey,
                                                @Field("user_id") String user_id,
                                                @Field("videos_id") String videos_id,
                                                @Field("last_watched_at") String last_watched_at,
                                                @Field("video_type") String video_type,
                                                @Field("from_source") String from_source
    );

    @FormUrlEncoded
    @POST("continue_watch/clear_all")
    Call<APIResponse<Object>> clearContinueWatch(@Header("API-KEY") String apiKey,
                                                 @FieldMap HashMap<String, String> params);

}
