package com.example.nzse.util;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForwardOnLongClickListener implements View.OnLongClickListener {
    private Object mTarget;
    private Method mMethod;

    ForwardOnLongClickListener(Object target, String methodName) {
        mTarget = target;
        try {
            mMethod = target.getClass().getMethod(methodName, View.class);
            if(!boolean.class.isAssignableFrom(mMethod.getReturnType())){
                throw new NoSuchMethodException("Method must return boolean");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            return (boolean) mMethod.invoke(mTarget, v);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
