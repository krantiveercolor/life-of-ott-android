package com.life.android.models;

import java.io.Serializable;

public class TransactionIdModel implements Serializable {
    private String status;
    private String transaction_id;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public TransactionIdModel(String status, String transaction_id) {
        this.status = status;
        this.transaction_id = transaction_id;
    }
}
