package com.tuya.lock.demo.zigbee.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;


import com.tuya.lock.demo.R;

import java.util.Timer;
import java.util.TimerTask;

public class LockButtonProgressView extends View {

    private static final String TAG = "HealthSportButtonProgressView";
    private Paint mBackPaint;   // 背景
    private Paint mProgPaint;   // 绘制画笔
    private RectF mRectF;       // 绘制区域
    private RectF mProgRectF;       // 绘制区域
    private int mProgress;      // 圆环进度(0-100)
    private String title;         // 中心点文案

    private Matrix matrix;
    private Paint paint;

    private Timer timer;
    private TimerTask timerTask;
    private boolean isClick = false;
    private static final int LONELIEST_TIME = 300;//长按超过0.3秒，触发长按事件

    private ClickCallback clickCallback;

    private boolean isEnabled = false;

    public LockButtonProgressView(Context context) {
        this(context, null);
    }

    public LockButtonProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockButtonProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (null == context) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HealthSportButtonProgressView);

        // 初始化背景圆环画笔
        mBackPaint = new Paint();
        mBackPaint.setStyle(Paint.Style.FILL);      // 填充
        mBackPaint.setStrokeCap(Paint.Cap.ROUND);   // 设置圆角
        mBackPaint.setAntiAlias(true);              // 设置抗锯齿
        mBackPaint.setDither(true);                 // 设置抖动
        mBackPaint.setColor(typedArray.getColor(R.styleable.HealthSportButtonProgressView_buttonBgColor, Color.RED));

        // 初始化进度圆环画笔
        mProgPaint = new Paint();
        mProgPaint.setStyle(Paint.Style.STROKE);    // 只描边，不填充
        mProgPaint.setStrokeCap(Paint.Cap.ROUND);   // 设置圆角
        mProgPaint.setAntiAlias(true);              // 设置抗锯齿
        mProgPaint.setDither(true);                 // 设置抖动
        mProgPaint.setStrokeWidth(typedArray.getDimension(R.styleable.HealthSportButtonProgressView_progressWidth, 10));
        mProgPaint.setColor(typedArray.getColor(R.styleable.HealthSportButtonProgressView_progressColor, Color.WHITE));

        // 中心点文案
        title = typedArray.getString(R.styleable.HealthSportButtonProgressView_buttonTitle);


        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!TextUtils.isEmpty(title)) {
//            matrix = new Matrix();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(40f);
            paint.setColor(getResources().getColor(R.color.ty_theme_color_m1_n1));
//            bitmap = BitmapFactory.decodeResource(getResources(), iconBg);
//            int bbw = bitmap.getWidth();
//            int bbh = bitmap.getHeight();
//            Point center = new Point(w / 2, h / 2);
//            Point bmpCenter = new Point(bbw / 2, bbh / 2);
//            matrix.postTranslate(center.x - bmpCenter.x, center.y - bmpCenter.y); // 移动到当前view 的中心
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWide = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int viewHigh = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int mRectLength = (int) ((Math.min(viewWide, viewHigh)) - (Math.max(mBackPaint.getStrokeWidth(), mProgPaint.getStrokeWidth())));
        int mRectL = getPaddingLeft() + (viewWide - mRectLength) / 2;
        int mRectT = getPaddingTop() + (viewHigh - mRectLength) / 2;
        int padding = 20;
        mRectF = getRectBg(mRectL, mRectT, mRectL + mRectLength, mRectT + mRectLength);
        mProgRectF = getProgRectBg(mRectL + padding, mRectT + padding, mRectL + mRectLength - padding, mRectT + mRectLength - padding);
    }

    private static RectF getRectBg(float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom);
    }

    private static RectF getProgRectBg(float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(mRectF, mBackPaint);
        if (!TextUtils.isEmpty(title)) {
            int viewWide = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            drawTextCenterInVertical(canvas, viewWide / 2, viewHeight / 2, title, paint);
        }
        canvas.drawArc(mProgRectF, 275, 360 * mProgress / 100, false, mProgPaint);
    }

    public void resetProgress(long animTime) {
        if (mProgress > 0) {
            ValueAnimator animator = ValueAnimator.ofInt(mProgress, 0);
            animator.addUpdateListener(animation -> {
                mProgress = (int) animation.getAnimatedValue();
                invalidate();
            });
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(animTime);
            animator.start();
        }
    }

    /**
     * df
     * 设置当前进度
     *
     * @param progress 当前进度（0-100）
     */
    public void setProgress(int progress) {
        this.mProgress = progress;
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void setTitle(String title) {
        this.title = title;
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled) return isClick;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            L.d(TAG, "ACTION_DOWN");
            //长按逻辑触发，isClick置为false，手指移开后，不触发点击事件
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //长按逻辑触发，isClick置为false，手指移开后，不触发点击事件
                    isClick = false;
                    mProgress = mProgress + 2;
//                    L.d(TAG, "mProgress:" + mProgress);
                    if (mProgress <= 100) {
                        doLongPress(mProgress);
                    } else {
                        timerTask.cancel();
                        timer.cancel();
                    }
                }
            };
            isClick = true;
            timer = new Timer();
            timer.schedule(timerTask, LONELIEST_TIME, 20);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
//            L.d(TAG, "ACTION_UP isClick:" + isClick);
            //没有触发长按逻辑，进行点击事件
            if (mProgress < 100) {
                resetProgress(500);
            }
            if (null != timerTask) {
                timerTask.cancel();
            }
            if (null != timer) {
                timer.cancel();
            }
        }
        return isClick;
    }

    //回归主线程回调
    private void doLongPress(int progress) {
        setProgress(progress);
        if (null != clickCallback && progress == 100) {
//            L.i(TAG, "doLongPress");
            clickCallback.doLongPress();
        }
    }

    public void addClickCallback(ClickCallback callback) {
        clickCallback = callback;
    }

    public interface ClickCallback {

        /**
         * 等于100的时候执行
         */
        void doLongPress();
    }

    /**
     * 竖直居中绘制文字
     *
     * @param canvas
     * @param centerX
     * @param centerY
     * @param text
     * @param paint
     */
    private void drawTextCenterInVertical(Canvas canvas, int centerX, int centerY, String text, Paint paint) {
        //获取文本的宽度，但是是一个比较粗略的结果
        float textWidth = paint.measureText(text);
        //文字度量
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        //得到基线的位置
        float baselineY = centerY + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        //绘制
        canvas.drawText(text, centerX - textWidth / 2, baselineY, paint);
    }
}
