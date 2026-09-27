package com.example.barapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;

public class SimpleLineChart extends View {

    private List<Double> dataPoints = new ArrayList<>();
    private Paint linePaint;
    private Paint dotPaint;
    private Paint axisPaint;
    private float animationProgress = 0f;

    public SimpleLineChart(Context context) {
        super(context);
        init();
    }

    public SimpleLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        // Neon Green Theme for Line
        linePaint.setColor(Color.parseColor("#39FF14"));
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        // Shadow for "Neon" glow effect
        linePaint.setShadowLayer(12, 0, 0, Color.GREEN);

        dotPaint = new Paint();
        dotPaint.setColor(Color.parseColor("#39FF14"));
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        axisPaint = new Paint();
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(2f);
    }

    public void setData(List<Double> data) {
        this.dataPoints = data;
        // Reset and animate
        this.animationProgress = 0f;
        startAnimation();
    }

    private void startAnimation() {
        ChartAnimation anim = new ChartAnimation();
        anim.setDuration(2000); // 2 seconds for full draw
        startAnimation(anim);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Important for hardware acceleration with ShadowLayer
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        if (dataPoints == null || dataPoints.isEmpty())
            return;

        float width = getWidth();
        float height = getHeight();
        float padding = 50f;

        // Draw Axis
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint); // X
        canvas.drawLine(padding, height - padding, padding, padding, axisPaint); // Y

        double maxVal = 0;
        for (double d : dataPoints) {
            if (d > maxVal)
                maxVal = d;
        }
        if (maxVal == 0) maxVal = 100;

        float graphHeight = height - 2 * padding;
        float graphWidth = width - 2 * padding;
        float xStep = graphWidth / (dataPoints.size() > 1 ? dataPoints.size() - 1 : 1);

        // Build full path first
        Path fullPath = new Path();
        for (int i = 0; i < dataPoints.size(); i++) {
             float x = padding + i * xStep;
             float y = (float) (height - padding - (dataPoints.get(i) / maxVal) * graphHeight);
             if (i == 0) fullPath.moveTo(x, y);
             else fullPath.lineTo(x, y);
             
             // Draw dots only if "reached" by animation roughly? 
             // Or simpler: Draw dots at the end or always? 
             // Let's draw dots if their 'x' position is covered by progress.
             float progressX = padding + graphWidth * animationProgress;
             if (x <= progressX + 0.1f) { // Tolerance
                 canvas.drawCircle(x, y, 6f, dotPaint);
             }
        }

        // Use PathMeasure to segment the path
        PathMeasure pm = new PathMeasure(fullPath, false);
        float length = pm.getLength();
        Path partialPath = new Path();
        pm.getSegment(0f, length * animationProgress, partialPath, true);
        
        canvas.drawPath(partialPath, linePaint);
    }

    private class ChartAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            animationProgress = interpolatedTime;
            invalidate();
        }
    }
}
