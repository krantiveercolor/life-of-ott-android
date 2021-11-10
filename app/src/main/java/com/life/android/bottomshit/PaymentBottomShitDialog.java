package com.life.android.bottomshit;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.life.android.R;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PaymentBottomShitDialog extends BottomSheetDialogFragment {

    public static final String PAYPAL = "paypal";
    public static final String STRIP = "strip";
    public static final String RAZOR_PAY = "razorpay";
    public static final String OFFLINE_PAY = "offline_pay";
    private DatabaseHelper databaseHelper;
    public boolean enableWallet = false;
    public String paymentType = "";
    private OnBottomShitClickListener bottomShitClickListener;

    public void setOnBottomShitClickListener(OnBottomShitClickListener bottomShitClickListener) {
        this.bottomShitClickListener = bottomShitClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_payment_bottom_shit, container,
                false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        databaseHelper = new DatabaseHelper(getContext());
        PaymentConfig config = databaseHelper.getConfigurationData().getPaymentConfig();
        CardView paypalBt, stripBt, razorpayBt, offlineBtn, walletBtn;
        TextView walletAmountView;
        paypalBt = view.findViewById(R.id.paypal_btn);
        stripBt = view.findViewById(R.id.stripe_btn);
        razorpayBt = view.findViewById(R.id.razorpay_btn);
        offlineBtn = view.findViewById(R.id.offline_btn);
        walletBtn = view.findViewById(R.id.wallet_btn);
        walletAmountView = view.findViewById(R.id.wallet_text_view);
        Space space = view.findViewById(R.id.space2);
        Space space4 = view.findViewById(R.id.space4);
        ImageView walletImgView = view.findViewById(R.id.select_wallet_img_view);
        ImageView onlineImgView = view.findViewById(R.id.select_pg_img_view);
        RelativeLayout selectWalletLayout = view.findViewById(R.id.select_wallet_layout);
        RelativeLayout selectOnlineLayout = view.findViewById(R.id.select_online_layout);

        if (!config.getPaypalEnable()) {
            paypalBt.setVisibility(View.GONE);
            space.setVisibility(View.GONE);
        }

        if (!config.getStripeEnable()) {
            stripBt.setVisibility(View.GONE);
            space.setVisibility(View.GONE);
        }

        if (!config.getRazorpayEnable()) {
            razorpayBt.setVisibility(View.GONE);
        }
        if (!config.isOfflinePaymentEnable()) {
            offlineBtn.setVisibility(View.GONE);
            space4.setVisibility(View.GONE);
        }

        if (enableWallet) {
            walletBtn.setVisibility(View.GONE);
            selectWalletLayout.setVisibility(View.GONE);
        }

        paypalBt.setOnClickListener(view1 -> bottomShitClickListener.onBottomShitClick(PAYPAL));

        stripBt.setOnClickListener(view12 -> bottomShitClickListener.onBottomShitClick(STRIP));

        razorpayBt.setOnClickListener(v -> bottomShitClickListener.onBottomShitClick(RAZOR_PAY));

        offlineBtn.setOnClickListener(v -> bottomShitClickListener.onBottomShitClick(OFFLINE_PAY));

        walletBtn.setOnClickListener(v -> bottomShitClickListener.onBottomShitClick(Constants.WALLET));

        view.findViewById(R.id.process_payment_btn).setOnClickListener(v -> {
            if (paymentType.isEmpty()) {
                new ToastMsg(requireContext()).toastIconError("Please select payment type");
            } else {
                bottomShitClickListener.onBottomShitClick(paymentType);
            }
        });

        selectOnlineLayout.setOnClickListener(v -> {
            selectOnlineLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_stroke_bg_selected));
            selectWalletLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_stroke_bg_un_selected));
            onlineImgView.setImageResource(R.drawable.ic_tick);
            walletImgView.setImageResource(R.drawable.success_circle);
            paymentType = RAZOR_PAY;
        });

        selectWalletLayout.setOnClickListener(v -> {
            selectWalletLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_stroke_bg_selected));
            selectOnlineLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_stroke_bg_un_selected));
            walletImgView.setImageResource(R.drawable.ic_tick);
            onlineImgView.setImageResource(R.drawable.success_circle);
            paymentType = Constants.WALLET;
        });

        walletAmountView.setText(String.format("Wallet (%s%s)", config.getCurrencySymbol(), PreferenceUtils.getWalletAmount(requireContext())));
        return view;
    }

    public interface OnBottomShitClickListener {
        void onBottomShitClick(String paymentMethodName);
    }

}

