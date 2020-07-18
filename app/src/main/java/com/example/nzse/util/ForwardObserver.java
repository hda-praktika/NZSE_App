package com.example.nzse.util;

import androidx.lifecycle.Observer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForwardObserver<T> implements Observer<T> {
    private Object mTarget;
    private Method mMethod;

    ForwardObserver(Object target, String methodName) {
        mTarget = target;

        Method[] methods = target.getClass().getMethods();
        for(Method method : methods) {
            if(method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
                mMethod = method;
                break;
            }
        }

        if(mMethod == null) {
            throw new RuntimeException(new NoSuchMethodException(methodName));
        }
    }

    @Override
    public void onChanged(T t) {
        try {
            mMethod.invoke(mTarget, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
