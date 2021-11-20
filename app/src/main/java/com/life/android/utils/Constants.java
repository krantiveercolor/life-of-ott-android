package com.life.android.utils;

import android.content.Context;
import android.os.Environment;

import com.life.android.AppConfig;
import com.life.android.R;
import com.life.android.models.single_details.Country;
import com.life.android.models.single_details.Genre;
import com.life.android.network.model.TvCategory;

import java.io.File;
import java.util.List;

public class Constants {

    public static final String device_type = "Android Application";

    public static String getDownloadDir(Context context) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Download/" + context.getString(R.string.app_name));
        if (!dir.exists()) {
            boolean make = dir.mkdirs();
            System.out.println(make);
        }

        return dir.getPath();
    }

    public static final String USER_LOGIN_STATUS = "login_status";
    public static final String USER_WALLET_AMOUNT = "walletAmount";

    public static List<Genre> genreList = null;
    public static List<Country> countryList = null;
    public static List<TvCategory> tvCategoryList = null;

    //country
    public static final String BANGLA = "bangladesh";
    public static final String OTHER_COUNTRY = "other_country";

    //room related constants

    public static final String CONTENT_ID = "content_id";
    public static final String CONTENT_TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String POSITION = "position";
    public static final String STREAM_URL = "stream_url";
    public static final String CATEGORY_TYPE = "category_type";
    public static final String SERVER_TYPE = "server_type";
    public static final String YOUTUBE = "youtube";
    public static final String YOUTUBE_LIVE = "youtube_live";

    // fragment constants
    public static final String LANGUAGES_FRAGMENT = "languagesFragment";
    public static final String CONTACT_US_FRAGMENT = "contactUsFragment";
    public static final String TERMS_FRAGMENT = "termsFragment";
    public static final String PAY_WATCH_TERMS_FRAGMENT = "payWatchTermsFragment";
    public static final String PRIVACY_FRAGMENT = "privacyFragment";
    public static final String SUPPORT_FRAGMENT = "supportFragment";
    public static final String WALLET_FRAGMENT = "walletFragment";
    public static final String ADD_MONEY_FRAGMENT = "addMoneyFragment";
    public static final String TICKET_FRAGMENT = "ticketFragment";
    public static final String CHANGE_PASS_FRAGMENT = "changePassFragment";

    public static final String EXCLUSIVE_FRAGMENT = "exclusiveFragment";

    public static final String FROM = "from";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String USER_MODEL = "userModel";

    //pay types
    public static final String PAY_WATCH = "payAndWatch";
    public static final String SUBSCRIPTION = "subscription";
    public static final String WALLET = "wallet";
    public static final String pre_booking = "pre_booking";

    public static final String OTHER = "other";
    public static final String HASH_CONTENT_NAME = "#CONTENT_NAME";
    public static final String BASE_URL_SHARE = AppConfig.DOMAIN + "watch/";


    public interface WishListType {
        String fav = "fav";
        String watch_later = "watch_later";
    }

    public interface GenreSizeTypes {
        int VERTICAL_SMALL = 0;
        int VERTICAL_LARGE = 1;
        int HORIZONTAL_SMALL = 2;
        int HORIZONTAL_LARGE = 3;
        int CIRCLE_SMALL = 4;
    }

}
