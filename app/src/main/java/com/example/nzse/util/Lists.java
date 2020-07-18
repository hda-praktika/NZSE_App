package com.example.nzse.util;

import java.util.ArrayList;
import java.util.List;

public class Lists {
    public static <T> List<T> difference(List<T> minuend, List<T> subtrahend) {
        List<T> result = new ArrayList<>(minuend);
        result.removeAll(subtrahend);
        return result;
    }
}
