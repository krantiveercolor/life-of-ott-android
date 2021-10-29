package com.life.android.network.model;

import com.life.android.models.WalletHistoryModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletHistoryResponseModel {

    @SerializedName("wallet_amount")
    private String walletAmount;

    @SerializedName("wallet_transactions")
    private List<WalletHistoryModel> walletTransactions;

    public String getWalletAmount() {
        return walletAmount;
    }

    public List<WalletHistoryModel> getWalletTransactions() {
        return walletTransactions;
    }
}
