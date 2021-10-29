package com.life.android.helper

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.life.android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar


/**
 * Created by Pushpendra Kumar on 22/08/21 @ 1:19 pm
 * Organization - Team Leader @ Colour Moon Technologies PVT LTD INDIA
 * Contact - pushpendra@thecolourmoon.com ► +91-9719325299
 */

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun ImageView.setImageInGlide(imageUrl: String, cornerRadius: Int) {
    Glide.with(this.context).load(imageUrl)
        .placeholder(R.drawable.poster_placeholder)
        .apply(RequestOptions().transform(RoundedCorners(cornerRadius)))
        .error(R.drawable.poster_placeholder).into(this)
}

/**
 * Use for show the messages
 * @param message -> Message in details
 * @param status -> 0 for regular, 1 for Success message and 2 for failure
 */
fun View.setSnackBar(message: String?, status: Int) {
    if (message != null) {
        val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        val view = snackBar.view
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.typeface = ResourcesCompat.getFont(this.context, R.font.amazon)
        textView.maxLines = 5
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setTextColor(ContextCompat.getColor(this.context, android.R.color.white))
        if (status == 1) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    this.context,
                    android.R.color.holo_green_dark
                )
            )
            textView.setTextColor(ContextCompat.getColor(this.context, android.R.color.white))
        } else if (status == 2) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    this.context,
                    android.R.color.holo_red_dark
                )
            )
            textView.setTextColor(ContextCompat.getColor(this.context, android.R.color.white))
        }
        snackBar.show()
    }
}