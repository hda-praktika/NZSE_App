package com.example.nzse.util;

import android.view.View;

import androidx.lifecycle.Observer;

public class Callbacks {
    public static View.OnClickListener forwardOnClick(Object target, String method) {
        return new ForwardOnClickListener(target, method);
    }

    public static View.OnLongClickListener forwardOnLongClick(Object target, String method) {
        return new ForwardOnLongClickListener(target, method);
    }

    public static View.OnTouchListener forwardOnTouch(Object target, String method) {
        return new ForwardOnTouchListener(target, method);
    }

    public static <T> Observer<T> forwardObserver(Object target, String method) {
        return new ForwardObserver<>(target, method);
    }
}
