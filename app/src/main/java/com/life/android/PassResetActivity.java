package com.life.android;

import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PassResetApi;
import com.life.android.network.model.PasswordReset;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PassResetActivity extends AppCompatActivity {
    private EditText etEmail;
    private TextView btnReset;
    private ProgressDialog dialog;
    private View backgroundView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_reset);
        Toolbar toolbar = findViewById(R.id.toolbar);
        backgroundView = findViewById(R.id.background_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "pass_reset_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        etEmail = findViewById(R.id.email);
        btnReset = findViewById(R.id.reset_pass);

        /*if (isDark) {
            backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            btnReset.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }*/

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidEmailAddress(etEmail.getText().toString())) {
                    new ToastMsg(PassResetActivity.this).toastIconError("please enter valid email");
                    return;
                } else {
                    resetPass(etEmail.getText().toString());
                }

            }
        });
    }

    private void resetPass(String email) {
        dialog.show();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PassResetApi passResetApi = retrofit.create(PassResetApi.class);
        Call<PasswordReset> call = passResetApi.resetPassword(AppConfig.API_KEY, email);
        call.enqueue(new Callback<PasswordReset>() {
            @Override
            public void onResponse(Call<PasswordReset> call, Response<PasswordReset> response) {
                if (response.code() == 200) {
                    PasswordReset pr = response.body();
                    if (pr.getStatus().equals("success")) {
                        new ToastMsg(PassResetActivity.this).toastIconSuccess(pr.getMessage());
                        dialog.cancel();
                    } else {
                        new ToastMsg(PassResetActivity.this).toastIconError(pr.getData() != null ? pr.getData() : pr.getMessage());
                        dialog.cancel();
                    }
                }
            }

            @Override
            public void onFailure(Call<PasswordReset> call, Throwable t) {
                new ToastMsg(PassResetActivity.this).toastIconError("Something went wrong." + t.getMessage());
                dialog.cancel();
                t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}
