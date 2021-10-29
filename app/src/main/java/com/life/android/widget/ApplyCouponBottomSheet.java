package com.life.android.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.life.android.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ApplyCouponBottomSheet extends BottomSheetDialogFragment {

    public static ApplyCouponBottomSheet newInstance() {
        Bundle args = new Bundle();
        ApplyCouponBottomSheet fragment = new ApplyCouponBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_apply_coupon_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.apply_coupon_btn).setOnClickListener(view1 -> dismiss());

        view.findViewById(R.id.apply_coupon_close_btn).setOnClickListener(view1 -> dismiss());
    }
}
