package com.example.nzse.util;

import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForwardOnTouchListener implements View.OnTouchListener {
    private Object mTarget;
    private Method mMethod;

    ForwardOnTouchListener(Object target, String methodName) {
        mTarget = target;
        try {
            mMethod = target.getClass().getMethod(methodName, View.class, MotionEvent.class);
            if(!boolean.class.isAssignableFrom(mMethod.getReturnType())) {
                throw new RuntimeException("Incompatible return type");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            return (boolean) mMethod.invoke(mTarget, v, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
