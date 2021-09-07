package cc.brainbook.android.beziercurvebottomnavigationview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;

public class BezierCurveBottomNavigationView  extends BottomNavigationView {
    public BezierCurveBottomNavigationView(@NonNull Context context) {
        super(context);

        init(context, null);
    }

    public BezierCurveBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public BezierCurveBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }


    /* -----------------------///[BezierCurve]--------------------- */
    ///https://mlog.club/article/740643
    ///https://proandroiddev.com/how-i-drew-custom-shapes-in-bottom-bar-c4539d86afd7

    /** the CURVE_CIRCLE_RADIUS represent the radius of the fab button */
    private int mBezierCurveCircleRadius;   ///0:no Curve, 20 - 45
    public void setBezierCurveCircleRadius(int bezierCurveCircleRadius) {
        mBezierCurveCircleRadius = bezierCurveCircleRadius;
    }
    private View mAnchorView;
    public void setAnchorView(View anchorView) {
        mAnchorView = anchorView;
    }

    private Path mPath;
    private Paint mPaint;

    // the coordinates of the first curve
    private final Point mFirstCurveStartPoint = new Point();
    private final Point mFirstCurveEndPoint = new Point();
    private final Point mFirstCurveControlPoint1 = new Point();
    private final Point mFirstCurveControlPoint2 = new Point();

    //the coordinates of the second curve
    @SuppressWarnings("FieldCanBeLocal")
    private Point mSecondCurveStartPoint = new Point();
    private final Point mSecondCurveEndPoint = new Point();
    private final Point mSecondCurveControlPoint1 = new Point();
    private final Point mSecondCurveControlPoint2 = new Point();
    private int mNavigationBarHeight;

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BezierCurveBottomNavigationView, 0, 0);
            try {
                ///dp2px
                ///https://blog.csdn.net/zhangphil/article/details/80613879
                mBezierCurveCircleRadius = Math.round(getResources().getDisplayMetrics().density
                        * ta.getFloat(R.styleable.BezierCurveBottomNavigationView_bezierCurveCircleRadius, 0F));
            } finally {
                ta.recycle();
            }

            if (mBezierCurveCircleRadius != 0) {
                initBezierCurve();
            }
        }
    }

    private void initBezierCurve() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(getBackgroundColor());
        setBackgroundColor(Color.TRANSPARENT);
    }

    @ColorInt
    private int getBackgroundColor() {
        if (getBackground() instanceof MaterialShapeDrawable) {
            ColorStateList colorStateList = ((MaterialShapeDrawable) getBackground()).getFillColor();
            if (colorStateList != null) {
                return colorStateList.getDefaultColor();
            }
        } else if (getBackground() instanceof ColorDrawable) {
            return ((ColorDrawable) getBackground()).getColor();
        }

        return Color.TRANSPARENT;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed) {
            return;
        }

        ///[BezierCurve#AnchorView]
        if (mAnchorView != null && mAnchorView.getHeight() > 0 && mAnchorView.getY() > 0) {
            mBezierCurveCircleRadius = (int) (mAnchorView.getHeight() + mAnchorView.getY() - getY());
        }

        if (mBezierCurveCircleRadius != 0) {
            // get width and height of navigation bar
            // Navigation bar bounds (width & height)
            int navigationBarWidth = getWidth();
            mNavigationBarHeight = getHeight();

            // the coordinates (x,y) of the start point before curve
            mFirstCurveStartPoint.set((navigationBarWidth / 2) - (mBezierCurveCircleRadius * 2) - (mBezierCurveCircleRadius / 3), 0);
            // the coordinates (x,y) of the end point after curve
            mFirstCurveEndPoint.set(navigationBarWidth / 2, mBezierCurveCircleRadius + (mBezierCurveCircleRadius / 4));
            // same thing for the second curve
            mSecondCurveStartPoint = mFirstCurveEndPoint;
            mSecondCurveEndPoint.set((navigationBarWidth / 2) + (mBezierCurveCircleRadius * 2) + (mBezierCurveCircleRadius / 3), 0);

            // the coordinates (x,y)  of the 1st control point on a cubic curve
            mFirstCurveControlPoint1.set(mFirstCurveStartPoint.x + mBezierCurveCircleRadius + (mBezierCurveCircleRadius / 4), mFirstCurveStartPoint.y);
            // the coordinates (x,y)  of the 2nd control point on a cubic curve
            mFirstCurveControlPoint2.set(mFirstCurveEndPoint.x - (mBezierCurveCircleRadius * 2) + mBezierCurveCircleRadius, mFirstCurveEndPoint.y);

            mSecondCurveControlPoint1.set(mSecondCurveStartPoint.x + (mBezierCurveCircleRadius * 2) - mBezierCurveCircleRadius, mSecondCurveStartPoint.y);
            mSecondCurveControlPoint2.set(mSecondCurveEndPoint.x - (mBezierCurveCircleRadius + (mBezierCurveCircleRadius / 4)), mSecondCurveEndPoint.y);

            mPath.reset();
            mPath.moveTo(0, 0);
            mPath.lineTo(mFirstCurveStartPoint.x, mFirstCurveStartPoint.y);

            mPath.cubicTo(mFirstCurveControlPoint1.x, mFirstCurveControlPoint1.y,
                    mFirstCurveControlPoint2.x, mFirstCurveControlPoint2.y,
                    mFirstCurveEndPoint.x, mFirstCurveEndPoint.y);

            mPath.cubicTo(mSecondCurveControlPoint1.x, mSecondCurveControlPoint1.y,
                    mSecondCurveControlPoint2.x, mSecondCurveControlPoint2.y,
                    mSecondCurveEndPoint.x, mSecondCurveEndPoint.y);

            mPath.lineTo(navigationBarWidth, 0);
            mPath.lineTo(navigationBarWidth, mNavigationBarHeight);
            mPath.lineTo(0, mNavigationBarHeight);
            mPath.close();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBezierCurveCircleRadius != 0) {
            canvas.drawPath(mPath, mPaint);
        }
    }

}
