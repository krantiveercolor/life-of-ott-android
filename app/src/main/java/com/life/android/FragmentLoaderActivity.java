package com.life.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import com.life.android.fragments.ChangePasswordFragment;
import com.life.android.fragments.LanguagesFragment;
import com.life.android.fragments.TicketFragment;
import com.life.android.utils.Constants;

public class FragmentLoaderActivity extends AppCompatActivity {

    public String from;
    public static final String TAG = "FRG_LOADER_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_loader);

        if (getIntent().hasExtra("fragmentLoaderIntent")) {
            from = getIntent().getStringExtra("fragmentLoaderIntent");
        }

        if (from != null && !from.isEmpty()) {
            if (from.equalsIgnoreCase(Constants.LANGUAGES_FRAGMENT)) {
                loadFragment(LanguagesFragment.newInstance(), Constants.LANGUAGES_FRAGMENT);
            } else if (from.equalsIgnoreCase(Constants.TICKET_FRAGMENT)) {
                loadFragment(TicketFragment.newInstance(getIntent().getParcelableExtra("single_details")), Constants.TICKET_FRAGMENT);
            } else if (from.equalsIgnoreCase(Constants.CHANGE_PASS_FRAGMENT)) {
                loadFragment(new ChangePasswordFragment(), Constants.CHANGE_PASS_FRAGMENT);
            }
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.loader_fragment_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed data: ");
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
        } else
            super.onBackPressed();
    }
}