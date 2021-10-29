package com.life.android.helper

interface IConstants {
    enum class ObserverEvents {
    }

    object IntentString {
        const val type = "type"
        const val payload = "payload"
    }

    object Fragments {
        const val movie = "movie"
        const val payAndWatch = "pay_and_watch"
        const val favorite = "favorite"
        const val watchLater = "watch_later"
        const val tvSeries = "tv_series"
        const val genres = "genres"
        const val otp = "otp"
        const val setting = "setting"
        const val languages = "languages"
        const val ticket = "ticket"


        const val privacy_policy = "privacy_policy"


        const val pay_watch_terms_conditions = "pay_watch_terms_conditions"

        const val video_ticket = "video_ticket"

        const val change_password = "change_password"
        const val support = "support"
        const val contact_us = "contact_us"
        const val terms_conditions = "terms_conditions"
        const val wallet = "wallet"
        const val add_cash_wallet = "add_cash_wallet"
    }

    object VideoTypeMovieAPI {
        const val movie = "movie"
        const val payAndWatch = "Pay and Watch"
        const val favorite = "fav"
        const val watchLater = "watch_later"
    }
}