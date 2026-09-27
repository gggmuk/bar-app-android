package com.example.barapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.HashMap;
import java.util.Map;

public class SimplePieChart extends View {

    private Map<String, Float> data = new HashMap<>();
    private Paint paint;
    private RectF rectF;
    // Progress 0.0 to 1.0
    private float animationProgress = 0f;

    // Green Theme Colors
    private int[] colors = {
            Color.parseColor("#1B5E20"), // Dark Green
            Color.parseColor("#39FF14"), // Neon Green
            Color.parseColor("#00C853"), // Green A700
            Color.parseColor("#69F0AE"), // Green A200
            Color.parseColor("#00E676"), // Green A400
            Color.parseColor("#2E7D32")  // Green 800
    };

    public SimplePieChart(Context context) {
        super(context);
        init();
    }

    public SimplePieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();
    }

    public void setData(Map<String, Float> data) {
        this.data = data;
        this.animationProgress = 0f;
        startAnimation();
    }

    private void startAnimation() {
        PieAnimation anim = new PieAnimation();
        anim.setDuration(1500);
        startAnimation(anim);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty())
            return;

        float width = getWidth();
        float height = getHeight();
        float minDim = Math.min(width, height);
        float padding = 40f;

        float left = (width - minDim) / 2 + padding;
        float top = (height - minDim) / 2 + padding;
        float right = left + minDim - 2 * padding;
        float bottom = top + minDim - 2 * padding;

        rectF.set(left, top, right, bottom);

        float total = 0;
        for (float val : data.values()) {
            total += val;
        }

        float startAngle = -90; // Top
        // The total angle we are allowed to draw based on progress
        float maxDrawAngle = 360f * animationProgress;

        int colorIndex = 0;

        for (Map.Entry<String, Float> entry : data.entrySet()) {
            float sweepAngle = (entry.getValue() / total) * 360f;
            
            // Determine how much of this slice we can draw
            float remainingAngle = maxDrawAngle - (startAngle + 90); 
            // Normalized accumulated angle check:
            // Current Abs Start = startAngle + 90
            // If we are iterating, we can just track accumulated vs maxDrawAngle.
            // Let's re-logic slightly for simplicity:
            
            // We just draw slices one by one.
            // If accumulatedAngle + sweep > maxDrawAngle, clip sweep.
            // If accumulatedAngle > maxDrawAngle, break.
        }
        
        // Re-loop with simple accumulator
        float currentAngle = 0f; // 0 to 360 relative to start
        float absoluteStart = -90f;
        
        int idx = 0;
        for (float val : data.values()) {
            float sweep = (val / total) * 360f;
            
            if (currentAngle >= maxDrawAngle) break; // Optimization
            
            float drawSweep = sweep;
            if (currentAngle + sweep > maxDrawAngle) {
                drawSweep = maxDrawAngle - currentAngle;
            }
            
            paint.setColor(colors[idx % colors.length]);
            canvas.drawArc(rectF, absoluteStart + currentAngle, drawSweep, true, paint);
            
            currentAngle += sweep;
            idx++;
        }
    }

    private class PieAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            animationProgress = interpolatedTime;
            invalidate();
        }
    }
}
