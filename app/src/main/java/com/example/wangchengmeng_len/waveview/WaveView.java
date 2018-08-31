package com.example.wangchengmeng_len.waveview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class WaveView extends View {

    private int     mWaveViewBoatBitmap; //水波纹上的图片
    private long    mWaveViewDuration;//水波纹的时长
    private int     mWaveViewOriginY;//水波纹的起始高度
    private boolean mWaveViewRise;//水波纹是否上升
    private int     mWaveViewHeight;//水波纹的波纹高度
    private int     mWaveViewLength;//水波纹的波长

    private Bitmap        mBitmap;
    private Paint         mPaint;
    private Path          mPath;
    private int           mWidth;
    private int           mHeight;
    private ValueAnimator mAnimator;


    private int    dx;//移动曲线的起始点 让波纹动起来
    private int    dy;//移动曲线起始点的Y坐标，可以实现涨水的效果
    private Region mRegion;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //获取自定义属性

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

        mWaveViewBoatBitmap = array.getResourceId(R.styleable.WaveView_boatBitmap, 0);
        mWaveViewDuration = (long) array.getDimension(R.styleable.WaveView_duration, 2000);
        mWaveViewOriginY = (int) array.getDimension(R.styleable.WaveView_originY, 500);
        mWaveViewRise = array.getBoolean(R.styleable.WaveView_rise, false);
        mWaveViewHeight = (int) array.getDimension(R.styleable.WaveView_waveHeight, 200);
        mWaveViewLength = (int) array.getDimension(R.styleable.WaveView_waveLength, 400);

        array.recycle();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        if (mWaveViewBoatBitmap > 0) {
            mBitmap = BitmapFactory.decodeResource(getResources(), mWaveViewBoatBitmap, options);
        } else {
            //没有设置图片 加载个默认图片
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_default_avatar_round, options);
        }

        mPaint = new Paint();
        mPaint.setColor(context.getColor(R.color.wave_view));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);//设置画笔填充

        mPath = new Path();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量的时候获取到宽高
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (mWaveViewOriginY == 0) {
            mWaveViewOriginY = mHeight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画曲线
        setPathData();

        canvas.drawPath(mPath, mPaint);

        //获得一个矩形区域 ***
        Rect bounds = mRegion.getBounds();
        //绘制头像

        if (bounds.top > 0 || bounds.left > 0) {
            if (bounds.top < mWaveViewOriginY) {//从波峰到基准线
                canvas.drawBitmap(mBitmap, bounds.left - mBitmap.getWidth() / 2, bounds.top - mBitmap.getHeight(), mPaint);
            } else {
                //从基准线到波谷
                canvas.drawBitmap(mBitmap, bounds.left - mBitmap.getWidth() / 2, bounds.bottom - mBitmap.getHeight(), mPaint);
            }
        } else {
            //bounds坐标都为0的时候直接将图片画在居中的位置
            float x = mWidth / 2 - mBitmap.getWidth() / 2;
            canvas.drawBitmap(mBitmap, x, mWaveViewOriginY - mBitmap.getHeight(), mPaint);
        }

    }

    private void setPathData() {
        mPath.reset();
        int halfWaveLength = mWaveViewLength / 2;//半个波长

        //以一个波长为单位开始画，每循环一次就画了一个波长（i += mWaveViewLength）
        //为了轮训效果 从屏幕的左边开始（ i = -mWaveViewLength）
        //一直画到出屏幕外，且还要多一个波长（i < mWidth + mWaveViewLength）


        //通过 动画的监听去计算dx的值 改变dx的值，从而修改起始点的位置 实现动画
        mPath.moveTo(-mWaveViewLength + dx, mWaveViewOriginY/*-dy*/);//移动到起始点位置 ****

        for (int i = -mWaveViewLength; i < mWidth + mWaveViewLength; i += mWaveViewLength) {
            //            mPath.quadTo(); 画二阶曲线

            mPath.rQuadTo(halfWaveLength / 2, -mWaveViewHeight, halfWaveLength, 0);//画二阶曲线，使用的是相对坐标

            mPath.rQuadTo(halfWaveLength / 2, mWaveViewHeight, halfWaveLength, 0);
        }

        mRegion = new Region();  //*****
        //q切割举行 左右同一个点的时候就是一根线
        float x = mWidth / 2;//不能直接 使用 mwidth/2 不然会丢失精度 ****

        Region clip = new Region((int) (x - 0.1), 0, (int) x, mHeight);
        mRegion.setPath(mPath, clip);

        //曲线封闭
        mPath.lineTo(mWidth, mHeight);
        mPath.lineTo(0, mHeight);
        mPath.close();//关闭前面两条线
    }

    public void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(mWaveViewDuration);
        //循环动画
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //线性动画
        mAnimator.setInterpolator(new LinearInterpolator());

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                //一个波长 * 动画执行长
                dx = (int) (mWaveViewLength * fraction);
                //                dy += 2;
                postInvalidate();//重新绘制
            }
        });

        mAnimator.start();
    }
}
