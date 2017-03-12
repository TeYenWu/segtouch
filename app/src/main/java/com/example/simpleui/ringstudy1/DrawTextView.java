package com.example.simpleui.ringstudy1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Debug;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudeyan on 07/12/2016.
 */
public class DrawTextView extends TextView {

    Paint paint;
    Path path;
    Object selectionStyle = new UnderlineSpan();
    float mX, mY;

    private int mode = -1;
    int startPosition = 0;
    int endPostion = 0;




    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {

        this.mode = mode;
        if(paint != null)
        {
            if(mode == 1)
                paint.setColor(Color.RED);
            else if(mode == 2)
                paint.setColor(Color.BLUE);
        }



    }

    public DrawTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                Log.e("onCreateActionMode", "onCreateActionMode");
//                menu.clear();
//                menu.close();
//                if(getMode() != -1 && getMode() != 2 && getMode() != 1) {
////                    menu.clear();
////                    mode.finish();
//
//                    startSelection();
////                    return false;
//                }

                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Log.e("onPrepareActionMode", "onPrepareActionMode");

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Log.e("onActionItemClicked", "onActionItemClicked");
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.e("onDestroyActionMode", "onDestroyActionMode");

            }
        });

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);

        path = new Path();
//        int start  = this.getSelectionStart()
    }

    void startSelection()
    {
        startPosition = this.getSelectionStart();
        endPostion = this.getSelectionEnd();

        if(startPosition == 0 && endPostion == 0) {
//            Selection.setSelection((Spannable) this.getText(), ss.getSpanStart(this.getText()), ss.getSpanEnd(this.getText()));
//            Selection.removeSelection((Spannable) this.getText());
//            this.set
//            this.
            return;
        }

        if(mode == 5)
            selectionStyle = new UnderlineSpan();
        else if (mode == 6)
            selectionStyle = new StrikethroughSpan();
        else if (mode == 4)
            selectionStyle = new BackgroundColorSpan(Color.YELLOW);

        int start = Math.min(startPosition, endPostion);
        int end = start == startPosition ? endPostion : startPosition;
        SpannableString spannableString =  (SpannableString)(this.getText());

        spannableString.setSpan(selectionStyle, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//        this.setText(spannableString);
//        Selection.setSelection(spannableString, startPosition, endPostion);
//        this.setMovementMethod(new LinkMovementMethod());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mode == 1 || mode == 2) {
            float x = event.getX(), y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                path.moveTo(x, y);

            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= 4 || dy >= 4) {
                    path.lineTo(x, y);
                    path.moveTo(x, y);

                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                path.lineTo(x, y);

            }
            mX = x;
            mY = y;
            invalidate();
            return true;
        }
        else
        {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                if(getMode() != -1 && getMode() != 2 && getMode() != 1) {
//                    menu.clear();
//                    mode.finish();

                    startSelection();
//                    return false;
                }

            }
            return super.onTouchEvent(event);
        }

    }

    class SpanData{
        int startIndex=0;
        int endIndex = 0;
        Object style;

        public SpanData(int startIndex, int endIndex, Object style)
        {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.style = style;
        }
    }

}
