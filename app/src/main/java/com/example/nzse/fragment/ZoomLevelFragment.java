package com.example.nzse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.nzse.MainActivity;
import com.example.nzse.R;
import com.example.nzse.viewmodel.MainViewModel;
import com.example.nzse.widget.ActionButton;
import com.example.nzse.widget.SimpleToolbar;

import static com.example.nzse.util.Callbacks.forwardOnClick;

public class ZoomLevelFragment extends Fragment {
    private MainViewModel mViewModel;

    private SimpleToolbar mToolbar;
    private ActionButton mActionButtonBack;
    private Button mButtonZoomScaled;
    private Button mButtonZoomNormal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.fragment_zoom_level, container, false);

        // Keine Touch-Events durchlassen.
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mToolbar = root.findViewById(R.id.toolbar);
        mActionButtonBack = mToolbar.getActionButton();
        mButtonZoomScaled = root.findViewById(R.id.btn_zoomed);
        mButtonZoomNormal = root.findViewById(R.id.btn_notzoomed);

        mActionButtonBack.setIcon(R.drawable.ic_back);
        mActionButtonBack.setOnClickListener(forwardOnClick(this, "onClickBack"));
        mButtonZoomScaled.setOnClickListener(forwardOnClick(this, "onClickZoom"));
        mButtonZoomNormal.setOnClickListener(forwardOnClick(this, "onClickZoom"));

        return root;
    }

    @SuppressWarnings("unused")
    public void onClickBack(View v){
        MainActivity activity = (MainActivity) getActivity();
        activity.onBackPressed();
    }

    @SuppressWarnings("unused")
    public void onClickZoom(View v) {
        mViewModel.setZoomLevel(v == mButtonZoomScaled);
    }
}