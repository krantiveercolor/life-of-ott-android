package com.life.android.network.apis;

import com.life.android.models.home_content.Video;
import com.life.android.network.model.FavoriteModel;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FavouriteApi {

    @GET("favorite")
    Call<List<Video>> getFavoriteList(@Header("API-KEY") String apiKey,
                                      @Query("user_id") String userId,
                                      @Query("page") int page,
                                      @Query("wish_list_type") String wish_list_type);

    @GET("add_favorite")
    Call<FavoriteModel> addToFavorite(@Header("API-KEY") String apiKey,
                                      @Query("user_id") String userId,
                                      @Query("videos_id") String videoId,
                                      @Query("wish_list_type") String wish_list_type);

    @GET("remove_favorite")
    Call<FavoriteModel> removeFromFavorite(@Header("API-KEY") String apiKey,
                                           @Query("user_id") String userId,
                                           @Query("videos_id") String videoId,
                                           @Query("wish_list_type") String wish_list_type);

    @GET("verify_favorite_list")
    Call<FavoriteModel> verifyFavoriteList(@Header("API-KEY") String apiKey,
                                           @Query("user_id") String userId,
                                           @Query("videos_id") String videoId,
                                           @Query("wish_list_type") String wish_list_type);
}
