package com.example.simpleui.ringstudy1;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by wudeyan on 07/12/2016.
 */
public class DrawCanvas extends View {

    Paint paint;
    Path path;

    float mX, mY;


    public DrawCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);
//        path.addCircle(30, 30, 100, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX(), y = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            path.moveTo(x,y);

        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= 4 || dy >= 4) {
                path.lineTo(x,y);
                path.moveTo(x,y);

            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            path.lineTo(x, y);
        }
        mX = x;
        mY = y;
        invalidate();
        return  true;
    }
}

