package com.chart.chartline.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.chart.chartline.chartlinedemo.R;
import com.chart.chartline.model.LowPriceResp;
import com.chart.chartline.utils.Utils;

import java.util.ArrayList;

/**
 * 趋势图
 */
public class DrawBezierView extends View {
    private static final String TAG = "DrawBezierView";

    private int splitCount;
    private int mScreenHeight;

    //第一个点(可能不可见)
    private PointF mFirstPointF;
    //最后一个点(可能不可见)
    private PointF mLastPointF;

    //曲线
    private Path mMovePath;
    private Paint mPaintBezier;
    //第一个可见的点
    private PointF mFistVisiblePoint;

    //圆点
    private Paint mPaintBall;
    private float mBallRadius = getResources().getDimension(R.dimen.low_price_point_radius);

    //移动线
    private Paint mPaintLine;
    //移动线x坐标，初始值必须小于第一个点的x坐标
    private float mMoveLineX = -20;

    //底线
    private Paint mPaintLineBottom;
    //分割线(竖线)
    private Paint mPaintSplitLineV;
    private int mSplitLineVHeight = (int) getResources().getDimension(R.dimen.low_price_split_v_h);
    //分割线(横线)
    private Paint mPaintSplitLineH;

    private PathMeasure mPathMeasure;
    private float drawScale = 1f;

    private OnTouchSelectListener mOnTouchSelectListener;
    private ArrayList<LowPriceResp> mPointList;

    public DrawBezierView(Context context) {
        this(context, null);
    }

    public DrawBezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScreenHeight = Utils.getScreenHeight(getContext()) - Utils.getStatusBarHeight(getContext());
        initBall();
        initLine();
        initPaintLineBottom();
        initPaintSplitLineV();
        initPaintSplitLineH();
        initBezierLine();
    }

    //滑动线
    private void initLine() {
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStrokeWidth(getResources().getDimension(R.dimen.low_price_move_line_h));
        mPaintLine.setDither(true);
        mPaintLine.setColor(Color.parseColor("#67B2FF"));
        mPaintLine.setStyle(Paint.Style.STROKE);
    }

    //底线
    private void initPaintLineBottom() {
        mPaintLineBottom = new Paint();
        mPaintLineBottom.setDither(true);
        mPaintLineBottom.setAntiAlias(true);
        mPaintLineBottom.setStrokeWidth(getResources().getDimension(R.dimen.low_price_split_line_h));
        mPaintLineBottom.setColor(Color.parseColor("#bbbbbb"));
    }

    //分割线 竖线
    private void initPaintSplitLineV() {
        mPaintSplitLineV = new Paint();
        mPaintSplitLineV.setDither(true);
        mPaintSplitLineV.setAntiAlias(true);
        mPaintSplitLineV.setStrokeWidth(getResources().getDimension(R.dimen.low_price_bottom_line_v));
        mPaintSplitLineV.setColor(Color.parseColor("#bbbbbb"));
    }

    //分割线 横线
    private void initPaintSplitLineH() {
        mPaintSplitLineH = new Paint();
        mPaintSplitLineH.setDither(true);
        mPaintSplitLineH.setAntiAlias(true);
        mPaintSplitLineH.setStrokeWidth(getResources().getDimension(R.dimen.low_price_split_line_h));
        mPaintSplitLineH.setColor(Color.parseColor("#e1e1e1"));
    }

    //折线
    private void initBezierLine() {
        mPaintBezier = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBezier.setStrokeWidth(getResources().getDimension(R.dimen.low_price_bezier_line_w));
        mPaintBezier.setStyle(Paint.Style.STROKE);
        mPaintBezier.setColor(Color.parseColor("#3FADF5"));
        mPaintBezier.setAntiAlias(true);
        mPaintBezier.setDither(true);
        mMovePath = new Path();
    }

    //圆点
    private void initBall() {
        mPaintBall = new Paint();
        mPaintBall.setStyle(Paint.Style.STROKE);
        mPaintBall.setStrokeWidth(getResources().getDimension(R.dimen.low_price_point_width));
        mPaintBall.setColor(Color.parseColor("#3FADF5"));
        mPaintBall.setAntiAlias(true);
        mPaintBall.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "w: " + w);
        Log.i(TAG, "h: " + h);
        Log.i(TAG, "oldw: " + oldw);
        Log.i(TAG, "oldh: " + oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPathMeasure == null || mPointList == null
                || mPointList.isEmpty() || mPointList.size() <= 1 || splitCount == 0
                || mFirstPointF == null || mLastPointF == null) {
            return;
        }
        //底线
        canvas.drawLine(mFirstPointF.x, getBottom(), mLastPointF.x, getBottom(), mPaintLineBottom);
        //分割线 横线
        float viewHeight = getBottom() - getTop();
        float topTemp;
        float tempDistanceH = viewHeight / splitCount;
        for (int i = 0; i < splitCount; i++) {
            topTemp = getTop() + tempDistanceH * i;
            canvas.drawLine(mFirstPointF.x, topTemp, mLastPointF.x, topTemp, mPaintSplitLineH);
        }
        //移动线
        canvas.drawLine(mMoveLineX, getTop(), mMoveLineX, getBottom(), mPaintLine);
        //分割线 竖线
        for (int i = 0; i < mPointList.size(); i++) {
            final LowPriceResp lowPriceResp = mPointList.get(i);
            canvas.drawLine(lowPriceResp.pointF.x, getBottom(), lowPriceResp.pointF.x, getBottom() - mSplitLineVHeight, mPaintSplitLineV);
        }
        //折线
        float distance = mPathMeasure.getLength() * drawScale;
        Path path = new Path();
        path.rLineTo(0, 0);
        if (mPathMeasure.getSegment(0, distance, path, true)) {
            canvas.drawPath(path, mPaintBezier);
            float[] shadowDistance = new float[2];
            mPathMeasure.getPosTan(distance, shadowDistance, null);
            drawShadow(canvas, path, shadowDistance);
        }
        //画点
        if (mPointList != null) {
            for (LowPriceResp resp : mPointList) {
                if (resp.pointF.y == 0) {
                    continue;
                }
                canvas.drawCircle(resp.pointF.x,
                        resp.pointF.y, mBallRadius, mPaintBall);

                final Paint paintBall = new Paint();
                paintBall.setStyle(Paint.Style.FILL_AND_STROKE);
                String color = "#E4F9FF";
                paintBall.setColor(Color.parseColor(color));
                paintBall.setAntiAlias(true);
                paintBall.setDither(true);
                canvas.drawCircle(resp.pointF.x, resp.pointF.y, mBallRadius, paintBall);
            }
        }
    }

    //绘制阴影
    private void drawShadow(Canvas canvas, Path path, float[] shadow) {
        if (mPointList == null || mPointList.isEmpty()) {
            return;
        }
        //第二个值只要足够大就行，这里直接简单取屏幕高度
        path.lineTo(shadow[0], mScreenHeight);
        path.lineTo(mFistVisiblePoint == null ? 0 : mFistVisiblePoint.x, mScreenHeight);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        final LinearGradient lg = new LinearGradient(
                mPointList.get(0).pointF.x, mPointList.get(0).pointF.y,
                mPointList.get(0).pointF.x, getMeasuredHeight(),
                Color.parseColor("#69B7FF"),
                Color.parseColor("#E4F9FF"),
                Shader.TileMode.CLAMP
        );
        paint.setAlpha(40);
        paint.setShader(lg);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        LowPriceResp resp = getPointF(x);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (resp != null && resp.pointF != null) {
                    moveLine(resp.pointF.x, resp);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (resp != null && resp.pointF != null) {
                    moveLine(resp.pointF.x, resp);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_UP:
                if (resp != null && resp.pointF != null) {
                    moveLine(resp.pointF.x, resp);
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        Log.i(TAG, "moveX: " + x);
        Log.i(TAG, "moveY: " + y);
        return true;
    }

    private void moveLine(float x, LowPriceResp resp) {
        if (x < 0) {
            x = 0;
        }
        if (x > getRight()) {
            x = getRight();
        }
        if (mOnTouchSelectListener != null && mMoveLineX > 0) {
            mOnTouchSelectListener.clickPoint(resp);
        }
        mMoveLineX = x;
        postInvalidate();
    }

    private LowPriceResp getPointF(float x) {
        LowPriceResp resp = null;
        if (mPointList == null || mPointList.isEmpty()) {
            return null;
        }
        for (int i = 0; i < mPointList.size() - 1; i++) {
            final PointF pf1 = mPointList.get(i).pointF;
            final PointF pf2 = mPointList.get(i + 1).pointF;
            if (x >= pf1.x && x <= pf2.x) {
                float offset1 = Math.abs(x - pf1.x);
                float offset2 = Math.abs(pf2.x - x);
                if (offset1 > offset2) {
                    resp = mPointList.get(i + 1);
                } else {
                    resp = mPointList.get(i);
                }
                break;
            }
        }
        if (resp == null || resp.pointF.y == 0) {
            return null;
        }
        return resp;
    }

    public void startAnimation(long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "drawPath", 0f, 1f);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    public void setDrawPath(float drawScale) {
        this.drawScale = drawScale;
        postInvalidate();
    }

    public void setData(ArrayList<LowPriceResp> lowPriceResps, int minRealPrice, int maxRealPrice, int splitCount) {
        if (lowPriceResps == null || lowPriceResps.isEmpty() || lowPriceResps.size() <= 1) {
            return;
        }
        mPointList = lowPriceResps;
        this.splitCount = splitCount;
        final int size = lowPriceResps.size();
        float viewWidth = getRight() - getLeft();
        float tempDistance = viewWidth / (size - 1f);
        for (int i = 0; i < size; i++) {
            final LowPriceResp lowPriceResp = lowPriceResps.get(i);
            final PointF pointF = new PointF();
            pointF.x = tempDistance * i;

            if (i == 0) {//处理第一个点
                mFirstPointF = pointF;
                pointF.x = tempDistance * i + mBallRadius * 2f;
            }
            if (i == (size - 1)) {//处理最后一个点
                mLastPointF = pointF;
                pointF.x = tempDistance * i - mBallRadius * 2f;
            }
            pointF.y = getPriceY(Utils.parseInt(lowPriceResp.price, 0), minRealPrice, maxRealPrice);
            lowPriceResps.get(i).pointF = pointF;
        }
        measurePath();
        mPathMeasure = new PathMeasure(mMovePath, false);
        postInvalidate();
    }

    private void measurePath() {
        if (mPointList == null || mPointList.isEmpty()) {
            return;
        }
        int[] position = new int[2];
        getLocationOnScreen(position);
        final int lineSize = mPointList.size();
        //查找第一个点
        for (int i = 0; i < lineSize; i++) {
            final PointF pointF = mPointList.get(i).pointF;
            if (pointF.y != 0) {
                mFistVisiblePoint = pointF;
                mMovePath.moveTo(pointF.x, pointF.y);
                break;
            }
        }
        for (int i = 0; i < lineSize; i++) {
            final PointF pointF = mPointList.get(i).pointF;
            if (pointF.y != 0) {
                mMovePath.lineTo(pointF.x, pointF.y);
            }
        }
    }

    private float getPriceY(int price, int minRealPrice, int maxRealPrice) {
        if (price == 0) {
            return 0;
        }
        float viewHeight = getBottom() - getTop();
        if (maxRealPrice == minRealPrice) {
            return mBallRadius * 2f;
        }
        float scale = viewHeight / (maxRealPrice - minRealPrice);
        return scale * (maxRealPrice - price) + mBallRadius * 2f;
    }

    public void setOnTouchSelectListener(OnTouchSelectListener mOnTouchSelectListener) {
        this.mOnTouchSelectListener = mOnTouchSelectListener;
    }

    public interface OnTouchSelectListener {
        void clickPoint(LowPriceResp resp);
    }
}
