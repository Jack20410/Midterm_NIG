package com.tdtu.edu.vn.mygallery.Utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class GestureRecyclerView extends RecyclerView {

    private ScaleGestureDetector scaleGestureDetector;
    private boolean isScaling = false;

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
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
            isScaling = scaleGestureDetector.isInProgress(); // Check if a scaling gesture is in progress
        }

        // If scaling, don't intercept touch events for scrolling
        if (isScaling) {
            return false;
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
            isScaling = scaleGestureDetector.isInProgress();
        }

        // Allow RecyclerView to handle scrolling if not scaling
        if (!isScaling) {
            return super.onTouchEvent(event);
        }

        return true; // Consume touch event if scaling
    }
}
