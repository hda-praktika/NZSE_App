package com.example.nzse.util;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForwardOnClickListener implements View.OnClickListener {
    private Object mTarget;
    private Method mMethod;

    ForwardOnClickListener(Object target,  String methodName) {
        mTarget = target;
        try {
            mMethod = target.getClass().getMethod(methodName, View.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View v) {
        try {
            mMethod.invoke(mTarget, v);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
