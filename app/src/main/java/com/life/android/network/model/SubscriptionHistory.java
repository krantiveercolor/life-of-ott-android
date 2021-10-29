package com.life.android.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionHistory {

    @SerializedName("active_subscription")
    @Expose
    private List<ActiveSubscription> activeSubscription = null;
    @SerializedName("inactive_subscription")
    @Expose
    private List<ActiveSubscription> inactiveSubscription = null;
    @SerializedName("pay_watch_active_subscription")
    @Expose
    private List<ActiveSubscription> payWatchActiveSubscription = null;
    @SerializedName("pay_watch_inactive_subscription")
    @Expose
    private List<ActiveSubscription> payWatchInActiveSubscription = null;

    public List<ActiveSubscription> getActiveSubscription() {
        return activeSubscription;
    }

    public List<ActiveSubscription> getInactiveSubscription() {
        return inactiveSubscription;
    }

    public List<ActiveSubscription> getPayWatchActiveSubscription() {
        return payWatchActiveSubscription;
    }

    public List<ActiveSubscription> getPayWatchInActiveSubscription() {
        return payWatchInActiveSubscription;
    }
}
