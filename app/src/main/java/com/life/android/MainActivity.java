package com.life.android;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.adapters.NavigationAdapter;
import com.life.android.database.DatabaseHelper;
import com.life.android.fragments.HomeFragment;
import com.life.android.helper.IConstants;
import com.life.android.models.NavigationModel;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.CmsApi;
import com.life.android.network.model.CmsModel;
import com.life.android.utils.Constants;
import com.life.android.utils.HelperUtils;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.SpacingItemDecoration;
import com.life.android.utils.Tools;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.volcaniccoder.bottomify.BottomifyNavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private LinearLayout navHeaderLayout;
    private RecyclerView recyclerView;
    private TextView versionView, contactUsView, supportView, privacyPolicyView, termsAndCondView;
    private NavigationAdapter mAdapter;
    private List<NavigationModel> list = new ArrayList<>();
    private NavigationView navigationView;
    private String[] navItemImage;
    private String[] navItemName2;
    private String[] navItemImage2;
    private boolean status = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    public boolean isDark;
    private String navMenuStyle;

    private final int PERMISSION_REQUEST_CODE = 100;
    private String searchType;
    private boolean[] selectedtype = new boolean[3]; // 0 for movie, 1 for series, 2 for live tv....
    private DatabaseHelper db;
    private boolean vpnStatus;
    private HelperUtils helperUtils;

    public BottomifyNavigationView bottomifyNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        db = new DatabaseHelper(MainActivity.this);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //check vpn connection
        helperUtils = new HelperUtils(MainActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus) {
            helperUtils.showWarningDialog(MainActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
        }
        // To resolve cast button visibility problem. Check Cast State when app is open.
        CastContext castContext = CastContext.getSharedInstance(this);
        castContext.getCastState();

        navMenuStyle = db.getConfigurationData().getAppConfig().getMenu();

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "main_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (sharedPreferences.getBoolean("firstTime", true)) {
            showTermServicesDialog();
        }

        //update subscription
        if (PreferenceUtils.isLoggedIn(MainActivity.this)) {
            PreferenceUtils.updateSubscriptionStatus(MainActivity.this);
        }

        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                createDownloadDir();
            } else {
                requestPermission();
            }
        } else {
            createDownloadDir();
        }

        //----init---------------------------
        navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navHeaderLayout = findViewById(R.id.nav_head_layout);
        bottomifyNavigationView = findViewById(R.id.bottom_navigation_view);

        SwitchCompat themeSwitch = findViewById(R.id.theme_switch);
        themeSwitch.setChecked(isDark);


        navigationView.setNavigationItemSelectedListener(this);

        //----fetch array------------
        String[] navItemName = getResources().getStringArray(R.array.nav_item_name);
        navItemImage = getResources().getStringArray(R.array.nav_item_image);

        navItemImage2 = getResources().getStringArray(R.array.nav_item_image_2);
        navItemName2 = getResources().getStringArray(R.array.nav_item_name_2);

        //----navigation view items---------------------
        recyclerView = findViewById(R.id.recyclerView);
        versionView = findViewById(R.id.version_view);
        contactUsView = findViewById(R.id.contact_us_screen);
        supportView = findViewById(R.id.support);
        privacyPolicyView = findViewById(R.id.privacy_policy);
        termsAndCondView = findViewById(R.id.terms_and_conditions);

        if (navMenuStyle == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        } else if (navMenuStyle.equals("grid")) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 15), true));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        }
        recyclerView.setHasFixedSize(true);

        versionView.setText("Version " + BuildConfig.VERSION_NAME);

        contactUsView.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.contact_us);
            startActivity(intent);
            mDrawerLayout.closeDrawers();
        });

        supportView.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.support);
            startActivity(intent);
            mDrawerLayout.closeDrawers();
        });

        privacyPolicyView.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.privacy_policy);
            startActivity(intent);
            mDrawerLayout.closeDrawers();
        });

        termsAndCondView.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.terms_conditions);
            startActivity(intent);
            mDrawerLayout.closeDrawers();
        });

        status = PreferenceUtils.isLoggedIn(this);
        if (status) {
            PreferenceUtils.updateSubscriptionStatus(MainActivity.this);
            for (int i = 0; i < navItemName.length; i++) {
                NavigationModel models = new NavigationModel(navItemImage[i], navItemName[i]);
                list.add(models);
            }
        } else {
            for (int i = 0; i < navItemName2.length; i++) {
                NavigationModel models = new NavigationModel(navItemImage2[i], navItemName2[i]);
                list.add(models);
            }
        }


        //set data and list adapter
        list.get(0).is_selected = true;
        mAdapter = new NavigationAdapter(this, list, navMenuStyle);
        recyclerView.setAdapter(mAdapter);

        final NavigationAdapter.OriginalViewHolder[] viewHolder = {null};

        mAdapter.setOnItemClickListener((view, obj, position, holder) -> {
            String title = holder.name.getText().toString();
            switch (title) {
                case "Home":
                    break;
                case "My Subscriptions": {
                    Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit);
                    break;
                }
                case "Watch Later": {
                    Intent intent = new Intent(MainActivity.this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.watchLater);
                    startActivity(intent);
                    break;
                }
                case "App Settings": {

                    Intent intent = new Intent(MainActivity.this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.setting);
                    startActivity(intent);
                    break;
                }
                case "Login": {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit);
                    break;
                }

                case "Movies": {
                    Intent plainActivity = new Intent(this, PlainActivity.class);
                    plainActivity.putExtra(IConstants.IntentString.type, IConstants.Fragments.movie);
                    startActivity(plainActivity);
                    break;
                }
                case "Wallet": {
                    Intent intent = new Intent(this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.wallet);
                    startActivity(intent);
                    break;
                }
                case "Genre": {
                    Intent payAndWatch = new Intent(this, PlainActivity.class);
                    payAndWatch.putExtra(IConstants.IntentString.type, IConstants.Fragments.genres);
                    startActivity(payAndWatch);
                    break;
                }
                case "Pay & Watch": {
                    Intent payAndWatch = new Intent(this, PlainActivity.class);
                    payAndWatch.putExtra(IConstants.IntentString.type, IConstants.Fragments.payAndWatch);
                    startActivity(payAndWatch);
                    break;
                }
                case "Search":
                    goToSearchActivity();
                    break;
                case "Favourites": {
                    Intent plainActivity = new Intent(this, PlainActivity.class);
                    plainActivity.putExtra(IConstants.IntentString.type, IConstants.Fragments.favorite);
                    startActivity(plainActivity);
                    break;
                }
                case "Profile": {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit);
                    break;
                }
                case "Sign Out": {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Are you sure to logout from " + getString(R.string.app_name) + "?")
                            .setPositiveButton("YES", (dialog, which) -> {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    FirebaseAuth.getInstance().signOut();

                                }

                                SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                                editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                                editor.apply();

                                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                                databaseHelper.deleteUserData();

                                PreferenceUtils.clearSubscriptionSavedData(MainActivity.this);

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()).create().show();
                    break;
                }
            }

            if (!obj.getTitle().equals("Settings") && !obj.getTitle().equals("Login") && !obj.getTitle().equals("Sign Out")) {
                if (navMenuStyle.equals("grid")) {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.rowLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.round_grey_transparent));
                }
                viewHolder[0] = holder;
            }
            mDrawerLayout.closeDrawers();
        });

        loadFragment(new HomeFragment());

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
            editor.putBoolean("dark", isChecked);
            editor.apply();
            mDrawerLayout.closeDrawers();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        });

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.open,
                R.string.close
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                FrameLayout frameLayout = findViewById(R.id.container);
                frameLayout.setTranslationX(slideX);
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);


        bottomifyNavigationView.setOnNavigationItemChangedListener(navigationItem -> {
            switch (navigationItem.getPosition()) {
                case 0: {
                    Intent intent = new Intent(this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.movie);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.payAndWatch);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    Intent intent = new Intent(this, PlainActivity.class);
                    intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.tvSeries);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
                }
                case 4:
                    openDrawer();
                    break;
            }
        });
        bottomifyNavigationView.setActiveNavigationIndex(2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action, menu);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {

                    Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                    intent.putExtra("q", s);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment instanceof HomeFragment) {
                new AlertDialog.Builder(this, R.style.DialogeTheme).setMessage("Do you want to exit from " + getString(R.string.app_name) + "?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            dialog.dismiss();
                            finishAffinity();
                            overridePendingTransition(0, R.anim.slide_out_bottom);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()).create().show();
            } else {
                super.onBackPressed();
            }
        }
    }

    //----nav menu item click---------------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void showTermServicesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_term_of_services);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        WebView webView = dialog.findViewById(R.id.webView);
        Button declineBt = dialog.findViewById(R.id.bt_decline);
        Button acceptBt = dialog.findViewById(R.id.bt_accept);
        TextView termsView = dialog.findViewById(R.id.terms_text_view);

        getCMSContent(termsView);


        dialog.findViewById(R.id.bt_close).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        acceptBt.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
            dialog.dismiss();
        });

        declineBt.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void getCMSContent(TextView privacy_policy_text_view) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        CmsApi api = retrofit.create(CmsApi.class);

        Call<CmsModel> call = api.privacyPolicy(AppConfig.API_KEY);

        call.enqueue(new Callback<CmsModel>() {
            @Override
            public void onResponse(@NotNull Call<CmsModel> call, @NotNull Response<CmsModel> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        privacy_policy_text_view.setText(HtmlCompat.fromHtml(response.body().content, HtmlCompat.FROM_HTML_MODE_COMPACT));
                    } else {
                        privacy_policy_text_view.setText(R.string.no_data_found);
                    }
                } else {
                    privacy_policy_text_view.setText(R.string.no_data_found);
                }

            }

            @Override
            public void onFailure(@NotNull Call<CmsModel> call, @NotNull Throwable t) {
                privacy_policy_text_view.setText(R.string.no_data_found);
            }
        });
    }


    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");

                // creating the download directory named oxoo
                createDownloadDir();

            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }

    // creating download folder
    public void createDownloadDir() {
        File file = new File(Constants.getDownloadDir(MainActivity.this), getResources().getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void goToSearchActivity() {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomifyNavigationView != null) {
            bottomifyNavigationView.setActiveNavigationIndex(2);
        }
        helperUtils = new HelperUtils(MainActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus) {
            helperUtils.showWarningDialog(MainActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
        }
    }
}
