package com.pushkin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private MaskFilter mEmboss;
    private MaskFilter  mBlur;

    Context context;

    public Bitmap getmBitmap() {return mBitmap;}
    public Canvas getmCanvas() {return mCanvas;}
    public Path getmPath() {return mPath;}
    public Paint getmBitmapPaint() {return mBitmapPaint;}
    public Paint getmPaint() {return mPaint;}
    public MaskFilter getmEmboss() {return mEmboss;}
    public MaskFilter getmBlur() {return mBlur;}

    public void prep() {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);
    }
    public void setEmboss() {mPaint.setMaskFilter(mEmboss);}
    public void setEmbossNull() {mPaint.setMaskFilter(null);}

    public void setBlur() {mPaint.setMaskFilter(mBlur);}
    public void setBlurNull() {mPaint.setMaskFilter(null);}

    public void erase() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setAlpha(0x80);
    }

    public void srcATop() {
        mPaint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.SRC_ATOP));
        mPaint.setAlpha(0x80);
    }

    public void setColor(int c) {mPaint.setColor(c);}

    public DrawingView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        setDrawingCacheEnabled(true);
    }

    public DrawingView(Context c, AttributeSet attrs, Paint paint) {
        this(c, attrs);
        context=c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = paint;
//        if (!isInEditMode())
//            hostActivity = (TrajectoryActivity) this.getContext();

    }

    public DrawingView(Context c, Paint paint) {
        super(c);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = paint;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0)
            mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        else
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        //showDialog();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        //mPaint.setMaskFilter(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:

                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
}