package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class GestureRecyclerView extends RecyclerView {

    private ScaleGestureDetector scaleGestureDetector;

    public GestureRecyclerView(@NonNull Context context) {
        super(context);
    }

    public GestureRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Set the ScaleGestureDetector
    public void setScaleGestureDetector(ScaleGestureDetector detector) {
        this.scaleGestureDetector = detector;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass touch events to ScaleGestureDetector if it's set
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Allow ScaleGestureDetector to handle gestures
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
        }
        return super.onInterceptTouchEvent(event);
    }
}
