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

public class SettingsFragment extends Fragment {

    private MainViewModel mViewModel;

    private SimpleToolbar mToolbar;
    private ActionButton mActionButtonBack;
    private Button mButtonZoomLevel;
    private Button mButtonRefreshChannels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Keine Touch-Events durchlassen.
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mToolbar = root.findViewById(R.id.toolbar);
        mActionButtonBack = mToolbar.getActionButton();
        mButtonZoomLevel = root.findViewById(R.id.btn_zoomlevel);
        mButtonRefreshChannels = root.findViewById(R.id.btn_refreshChannels);

        mActionButtonBack.setIcon(R.drawable.ic_back);
        mActionButtonBack.setOnClickListener(forwardOnClick(this, "onClickBack"));
        mButtonZoomLevel.setOnClickListener(forwardOnClick(this, "onClickZoomLevel"));
        mButtonRefreshChannels.setOnClickListener(forwardOnClick(this, "onClickRefreshChannels"));

        return root;
    }

    @SuppressWarnings("unused")
    public void onClickBack(View v){
        MainActivity activity = (MainActivity) getActivity();
        activity.onBackPressed();
    }

    @SuppressWarnings("unused")
    public void onClickZoomLevel(View v){
        MainActivity activity = (MainActivity) getActivity();
        activity.openZoomLevel();
    }

    @SuppressWarnings("unused")
    public void onClickRefreshChannels(View v){
        mViewModel.refreshChannels();
    }
}