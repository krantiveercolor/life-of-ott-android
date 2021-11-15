package com.life.android;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.life.android.databinding.ActivityPlainBinding;
import com.life.android.fragments.AddCashFragment;
import com.life.android.fragments.CMSFragment;
import com.life.android.fragments.ChangePasswordFragment;
import com.life.android.fragments.ContactUsFragment;
import com.life.android.fragments.GenreFragment;
import com.life.android.fragments.MoviesFragment;
import com.life.android.fragments.OtpFragment;
import com.life.android.fragments.SettingsFragment;
import com.life.android.fragments.TicketFragment;
import com.life.android.fragments.TvSeriesFragment;
import com.life.android.fragments.WalletFragment;
import com.life.android.helper.IConstants;
import com.life.android.network.model.User;
import com.bumptech.glide.Glide;

public class PlainActivity extends AppCompatActivity {
    private ActivityPlainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarPlainAct);

        Glide.with(this).load(R.drawable.logo_anim).into(binding.indicatorImageView);

        binding.backBtn.setOnClickListener(v -> onBackPressed());

        if (getIntent().hasExtra(IConstants.IntentString.type)) {
            String type = getIntent().getStringExtra(IConstants.IntentString.type);
            switch (type) {
                case IConstants.Fragments.movie: {
                    binding.tvPlainActTitle.setText(getString(R.string.movie));
                    MoviesFragment moviesFragment = new MoviesFragment();
                    moviesFragment.movieType = IConstants.VideoTypeMovieAPI.movie;
                    loadFragment(moviesFragment);
                    break;
                }
                case IConstants.Fragments.payAndWatch: {
                    binding.tvPlainActTitle.setText(getString(R.string.pay_watch));
                    MoviesFragment moviesFragment = new MoviesFragment();
                    moviesFragment.movieType = IConstants.VideoTypeMovieAPI.payAndWatch;
                    loadFragment(moviesFragment);
                    break;
                }
                case IConstants.Fragments.favorite: {
                    binding.tvPlainActTitle.setText(getString(R.string.favorite));
                    MoviesFragment moviesFragment = new MoviesFragment();
                    moviesFragment.movieType = IConstants.VideoTypeMovieAPI.favorite;
                    loadFragment(moviesFragment);
                    break;
                }
                case IConstants.Fragments.watchLater: {
                    binding.tvPlainActTitle.setText(getString(R.string.hint_watch_later));
                    MoviesFragment moviesFragment = new MoviesFragment();
                    moviesFragment.movieType = IConstants.VideoTypeMovieAPI.watchLater;
                    loadFragment(moviesFragment);
                    break;
                }
                case IConstants.Fragments.tvSeries: {
                    binding.tvPlainActTitle.setText(getString(R.string.tv_series));
                    loadFragment(new TvSeriesFragment());
                    break;
                }
                case IConstants.Fragments.genres: {
                    binding.tvPlainActTitle.setText(getString(R.string.all_genres));
                    loadFragment(new GenreFragment());
                    break;
                }
                case IConstants.Fragments.otp: {
                    binding.tvPlainActTitle.setText(getString(R.string.otp_verification));
                    OtpFragment fragment = new OtpFragment();
                    fragment.loggedInUser = ((User) getIntent().getSerializableExtra(IConstants.IntentString.payload));
                    loadFragment(fragment);
                    break;
                }
                case IConstants.Fragments.setting: {
                    binding.tvPlainActTitle.setText(getString(R.string.hint_settings));
                    loadFragment(new SettingsFragment());
                    break;
                }
                case IConstants.Fragments.change_password: {
                    binding.tvPlainActTitle.setText(getString(R.string.change_password));
                    loadFragment(new ChangePasswordFragment());
                    break;
                }
                case IConstants.Fragments.support: {
                    binding.tvPlainActTitle.setText(getString(R.string.support));
                    CMSFragment fragment = new CMSFragment();
                    fragment.from = getString(R.string.support);
                    loadFragment(fragment);
                    break;
                }
                case IConstants.Fragments.contact_us: {
                    binding.tvPlainActTitle.setText(getString(R.string.contact_us));
                    loadFragment(new ContactUsFragment());
                    break;
                }
                case IConstants.Fragments.terms_conditions: {
                    binding.tvPlainActTitle.setText(getString(R.string.terms_conditions));
                    CMSFragment fragment = new CMSFragment();
                    fragment.from = getString(R.string.terms_conditions);
                    loadFragment(fragment);
                    break;
                }
                case IConstants.Fragments.privacy_policy: {
                    binding.tvPlainActTitle.setText(getString(R.string.privacy_policy));
                    CMSFragment fragment = new CMSFragment();
                    fragment.from = getString(R.string.privacy_policy);
                    loadFragment(fragment);
                    break;
                }
                case IConstants.Fragments.wallet: {
                    binding.tvPlainActTitle.setText(getString(R.string.wallet));
                    loadFragment(new WalletFragment());
                    break;
                }
                case IConstants.Fragments.add_cash_wallet: {
                    binding.tvPlainActTitle.setText(getString(R.string.add_cash));
                    loadFragment(new AddCashFragment());
                    break;
                }
                case IConstants.Fragments.ticket: {
                    binding.toolbarPlainAct.setVisibility(View.GONE);
                    loadFragment(TicketFragment.newInstance(getIntent().getParcelableExtra(IConstants.IntentString.payload)));
                    break;
                }
            }
        }
    }


    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(binding.container.getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }

    public void activityIndicator(boolean show) {
        if (show) {
            binding.indicatorImageView.setVisibility(View.VISIBLE);
            binding.container.animate().alpha(0.2f);
        } else {
            binding.indicatorImageView.setVisibility(View.GONE);
            binding.container.animate().alpha(1f);
        }
    }
}