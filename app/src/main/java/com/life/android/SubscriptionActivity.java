package com.life.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.life.android.adapters.ActiveSubscriptionAdapter;
import com.life.android.adapters.InactiveSubscriptionAdapter;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.ActiveSubscription;
import com.life.android.network.model.SubscriptionHistory;
import com.life.android.network.model.User;
import com.life.android.utils.NetworkInst;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.RtlUtils;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubscriptionActivity extends AppCompatActivity implements ActiveSubscriptionAdapter.OnItemClickLiestener {
    private RecyclerView mInactiveRv;
    private CardView activePlanLayout;
    private TextView activeUserName, activeEmail, activeActivePlan, activeExpireDate;
    private LinearLayout mNoActiveLayout, mSubHistoryLayout, mSubRootLayout;
    private Button mUpgradeBt;
    private Toolbar mToolbar;
    private ProgressBar progressBar;
    private TextView mNoHistoryTv;
    private CoordinatorLayout mNoInternetLayout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout historyHeaderLayout;
    private RelativeLayout activeTitleLayout, historyTitleLayout;
    private View activeView;
    private InactiveSubscriptionAdapter inactiveSubscriptionAdapter;
    private String userId;

    private List<ActiveSubscription> activeSubscriptions = new ArrayList<>();
    private boolean isDark;

    private List<ActiveSubscription> activeSubscriptionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeDark);
        setContentView(R.layout.activity_subscription);

        intiView();

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Subscription");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mInactiveRv.setLayoutManager(new LinearLayoutManager(this));
        mInactiveRv.setHasFixedSize(true);
        mInactiveRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        userId = PreferenceUtils.getUserId(SubscriptionActivity.this);

    }

    private void getSubscriptionHistory() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<SubscriptionHistory> call = subscriptionApi.getSubscriptionHistory(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<SubscriptionHistory>() {
            @Override
            public void onResponse(@NotNull Call<SubscriptionHistory> call, @NotNull Response<SubscriptionHistory> response) {
                SubscriptionHistory subscriptionHistory = response.body();
                if (response.code() == 200) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (subscriptionHistory != null) {
                        activeSubscriptionList = subscriptionHistory.getActiveSubscription();
                    }
                    addSubscriptionDataToView();
                    for (int i = 0; i < subscriptionHistory.getPayWatchActiveSubscription().size(); i++) {
                        subscriptionHistory.getPayWatchActiveSubscription().get(i).setStatus("2");
                    }
                    List<ActiveSubscription> allSubscriptions = subscriptionHistory.getPayWatchActiveSubscription();
                    allSubscriptions.addAll(subscriptionHistory.getPayWatchInActiveSubscription());
                    allSubscriptions.addAll(subscriptionHistory.getInactiveSubscription());
                    if (allSubscriptions.size() > 0) {
                        mNoHistoryTv.setVisibility(View.GONE);
                        mSubHistoryLayout.setVisibility(View.VISIBLE);
                        inactiveSubscriptionAdapter = new InactiveSubscriptionAdapter(allSubscriptions,
                                SubscriptionActivity.this);
                        mInactiveRv.setAdapter(inactiveSubscriptionAdapter);
                    } else {
                        mNoHistoryTv.setVisibility(View.VISIBLE);
                        mSubHistoryLayout.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<SubscriptionHistory> call, @NotNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }

    private void addSubscriptionDataToView() {
        DatabaseHelper db = new DatabaseHelper(SubscriptionActivity.this);
        if (db.getActiveStatusCount() > 0 && db.getUserDataCount() > 0) {
            activePlanLayout.setVisibility(View.VISIBLE);
            ActiveStatus activeStatus = db.getActiveStatusData();
            User user = db.getUserData();
            activeUserName.setText(user.getName());
            activeEmail.setText(user.getEmail());
            activeActivePlan.setText(activeStatus.getPackageTitle());
            activeExpireDate.setText(activeStatus.getExpireDate());

        } else {
            activePlanLayout.setVisibility(View.GONE);
        }

    }

    private void intiView() {
        mUpgradeBt = findViewById(R.id.upgrade_bt);
        mToolbar = findViewById(R.id.subscription_toolbar);
        mInactiveRv = findViewById(R.id.inactive_sub_rv);
        mNoActiveLayout = findViewById(R.id.no_current_sub_layout);
        progressBar = findViewById(R.id.progress_bar);
        mSubHistoryLayout = findViewById(R.id.sub_history_layout);
        mNoHistoryTv = findViewById(R.id.no_history_tv);
        mNoInternetLayout = findViewById(R.id.coordinator_lyt);
        mSubRootLayout = findViewById(R.id.sub_root_layout);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        historyHeaderLayout = findViewById(R.id.history_header_layout);
        activeTitleLayout = findViewById(R.id.active_layout_title);
        historyTitleLayout = findViewById(R.id.history_layout_title);
        activeView = findViewById(R.id.active_view);
        activePlanLayout = findViewById(R.id.active_plan_card_view);
        activeUserName = findViewById(R.id.active_user_name);
        activeEmail = findViewById(R.id.active_email);
        activeActivePlan = findViewById(R.id.active_active_plan);
        activeExpireDate = findViewById(R.id.active_expire_date);
    }


    @Override
    protected void onStart() {
        super.onStart();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shimmerFrameLayout.startShimmer();
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                if (new NetworkInst(SubscriptionActivity.this).isNetworkAvailable()) {
                    getSubscriptionHistory();
                    addSubscriptionDataToView();
                } else {
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    swipeRefreshLayout.setRefreshing(false);
                    mNoInternetLayout.setVisibility(View.VISIBLE);
                    mSubRootLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        swipeRefreshLayout.setVisibility(View.GONE);

        if (new NetworkInst(this).isNetworkAvailable()) {
            getSubscriptionHistory();
            addSubscriptionDataToView();
        } else {
            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout.stopShimmer();
            swipeRefreshLayout.setRefreshing(false);
            mNoInternetLayout.setVisibility(View.VISIBLE);
            mSubRootLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        mUpgradeBt.setOnClickListener(v -> {
            startActivityForResult(new Intent(SubscriptionActivity.this, PurchasePlanActivity.class)
                            .putExtra("active_sub", activeSubscriptionList.isEmpty() ? null : activeSubscriptionList.get(activeSubscriptionList.size() - 1))
                    , 1010);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1010 && resultCode == RESULT_OK) {
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            if (new NetworkInst(SubscriptionActivity.this).isNetworkAvailable()) {
                getSubscriptionHistory();
                addSubscriptionDataToView();
            } else {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                swipeRefreshLayout.setRefreshing(false);
                mNoInternetLayout.setVisibility(View.VISIBLE);
                mSubRootLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick() {

    }

    @Override
    public void onCancelBtClick(final String subscriptionId, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Warning");
        builder.setIcon(R.drawable.ic_warning);
        builder.setMessage("Are you want to cancel this subscription?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelSubscription(subscriptionId, position);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void cancelSubscription(String subscriptionId, int pos) {

        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi api = retrofit.create(SubscriptionApi.class);
        Call<ResponseBody> call = api.cancelSubscription(AppConfig.API_KEY, userId, subscriptionId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        List<ActiveSubscription> temp = activeSubscriptions;
                        activeSubscriptions.clear();
                        activeSubscriptions.addAll(temp);
                        // update subscription active status if there are no more subscription have
                        if (activeSubscriptions.size() == 0) {
                            updateActiveStatus();
                        }
                        recreate();

                        Toast.makeText(SubscriptionActivity.this, "Subscription canceled successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SubscriptionActivity.this, "Subscription canceled Failed. code:" + response.code(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SubscriptionActivity.this, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void updateActiveStatus() {
        PreferenceUtils.updateSubscriptionStatus(SubscriptionActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}
