package com.example.nzse.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.nzse.R;

public class SimpleToolbar extends Toolbar {
    private TextView mTextView;

    public SimpleToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_simple_toolbar, this);
        mTextView = findViewById(R.id.textView);

        applyAttributes(attrs);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SimpleToolbar, 0, 0);

        String title = a.getString(R.styleable.SimpleToolbar_title);
        setTitle(title);

        a.recycle();
    }

    public void setTitle(String title) {
        mTextView.setText(title);
    }
}
