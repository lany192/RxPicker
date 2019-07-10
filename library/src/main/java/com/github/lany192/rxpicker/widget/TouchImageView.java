package com.github.lany192.rxpicker.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Scroller;


public class TouchImageView extends AppCompatImageView {


    public static final float AUTOMATIC_MIN_ZOOM = -1.0f;
    private static final String DEBUG = "DEBUG";
    //
    // SuperMin and SuperMax multipliers. Determine how much the image can be
    // zoomed below or above the zoom boundaries, before animating back to the
    // min/max zoom boundary.
    //
    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;
    //
    // Scale of image ranges from minScale to maxScale, where minScale == 1
    // when the image is stretched to fit view.
    //
    private float normalizedScale;
    //
    // Matrix applied to image. MSCALE_X and MSCALE_Y should always be equal.
    // MTRANS_X and MTRANS_Y are the other values used. prevMatrix is the matrix
    // saved prior to the screen rotating.
    //
    private Matrix matrix, prevMatrix;
    private boolean zoomEnabled = true;
    private FixedPixel orientationChangeFixedPixel = FixedPixel.CENTER;
    private FixedPixel viewSizeChangeFixedPixel = FixedPixel.CENTER;
    private boolean orientationJustChanged = false;
    private State state;
    private float userSpecifiedMinScale;
    private float minScale;
    private boolean maxScaleIsSetByMultiplier = false;
    private float maxScaleMultiplier;
    private float maxScale;
    private float superMinScale;
    private float superMaxScale;
    private float[] m;
    private Context context;
    private Fling fling;
    private int orientation;
    private ScaleType mScaleType;
    private boolean imageRenderedAtLeastOnce;
    private boolean onDrawReady;
    private ZoomVariables delayedZoomVariables;
    //
    // Size of view and previous view size (ie before rotation)
    //
    private int viewWidth, viewHeight, prevViewWidth, prevViewHeight;
    //
    // Size of image when it is stretched to fit view. Before and After rotation.
    //
    private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener doubleTapListener = null;
    private OnTouchListener userTouchListener = null;
    private OnTouchImageViewListener touchImageViewListener = null;
    public TouchImageView(Context context) {
        this(context, null);
    }
    public TouchImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configureImageView(context, attrs, defStyle);
    }

    private void configureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        super.setClickable(true);

        orientation = getResources().getConfiguration().orientation;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());

        matrix = new Matrix();
        prevMatrix = new Matrix();

        m = new float[9];
        normalizedScale = 1;
        if (mScaleType == null) {
            mScaleType = ScaleType.FIT_CENTER;
        }

        minScale = 1;
        maxScale = 3;

        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;

        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setState(State.NONE);

        onDrawReady = false;

        super.setOnTouchListener(new PrivateOnTouchListener());
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener l) {
        userTouchListener = l;
    }

    public void setOnTouchImageViewListener(OnTouchImageViewListener l) {
        touchImageViewListener = l;
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener l) {
        doubleTapListener = l;
    }

    public boolean isZoomEnabled() {
        return zoomEnabled;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }

    @Override
    public void setImageResource(int resId) {
        imageRenderedAtLeastOnce = false;
        super.setImageResource(resId);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        imageRenderedAtLeastOnce = false;
        super.setImageBitmap(bm);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        imageRenderedAtLeastOnce = false;
        super.setImageDrawable(drawable);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageURI(Uri uri) {
        imageRenderedAtLeastOnce = false;
        super.setImageURI(uri);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setScaleType(ScaleType type) {
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX);

        } else {
            mScaleType = type;
            if (onDrawReady) {
                //
                // If the image is already rendered, scaleType has been called programmatically
                // and the TouchImageView should be updated with the new scaleType.
                //
                setZoom(this);
            }
        }
    }

    public FixedPixel getOrientationChangeFixedPixel() {
        return orientationChangeFixedPixel;
    }

    public void setOrientationChangeFixedPixel(FixedPixel fixedPixel) {
        this.orientationChangeFixedPixel = fixedPixel;
    }

    public FixedPixel getViewSizeChangeFixedPixel() {
        return viewSizeChangeFixedPixel;
    }

    public void setViewSizeChangeFixedPixel(FixedPixel viewSizeChangeFixedPixel) {
        this.viewSizeChangeFixedPixel = viewSizeChangeFixedPixel;
    }


    public boolean isZoomed() {
        return normalizedScale != 1;
    }


    public RectF getZoomedRect() {
        if (mScaleType == ScaleType.FIT_XY) {
            throw new UnsupportedOperationException("getZoomedRect() not supported with FIT_XY");
        }
        PointF topLeft = transformCoordTouchToBitmap(0, 0, true);
        PointF bottomRight = transformCoordTouchToBitmap(viewWidth, viewHeight, true);

        float w = getDrawable().getIntrinsicWidth();
        float h = getDrawable().getIntrinsicHeight();
        return new RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h);
    }


    public void savePreviousImageValues() {
        if (matrix != null && viewHeight != 0 && viewWidth != 0) {
            matrix.getValues(m);
            prevMatrix.setValues(m);
            prevMatchViewHeight = matchViewHeight;
            prevMatchViewWidth = matchViewWidth;
            prevViewHeight = viewHeight;
            prevViewWidth = viewWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("orientation", orientation);
        bundle.putFloat("saveScale", normalizedScale);
        bundle.putFloat("matchViewHeight", matchViewHeight);
        bundle.putFloat("matchViewWidth", matchViewWidth);
        bundle.putInt("viewWidth", viewWidth);
        bundle.putInt("viewHeight", viewHeight);
        matrix.getValues(m);
        bundle.putFloatArray("matrix", m);
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce);
        bundle.putSerializable("viewSizeChangeFixedPixel", viewSizeChangeFixedPixel);
        bundle.putSerializable("orientationChangeFixedPixel", orientationChangeFixedPixel);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            normalizedScale = bundle.getFloat("saveScale");
            m = bundle.getFloatArray("matrix");
            prevMatrix.setValues(m);
            prevMatchViewHeight = bundle.getFloat("matchViewHeight");
            prevMatchViewWidth = bundle.getFloat("matchViewWidth");
            prevViewHeight = bundle.getInt("viewHeight");
            prevViewWidth = bundle.getInt("viewWidth");
            imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered");
            viewSizeChangeFixedPixel = (FixedPixel) bundle.getSerializable("viewSizeChangeFixedPixel");
            orientationChangeFixedPixel = (FixedPixel) bundle.getSerializable("orientationChangeFixedPixel");
            int oldOrientation = bundle.getInt("orientation");
            if (orientation != oldOrientation) {
                orientationJustChanged = true;
            }
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawReady = true;
        imageRenderedAtLeastOnce = true;
        if (delayedZoomVariables != null) {
            setZoom(delayedZoomVariables.scale, delayedZoomVariables.focusX, delayedZoomVariables.focusY, delayedZoomVariables.scaleType);
            delayedZoomVariables = null;
        }
        super.onDraw(canvas);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int newOrientation = getResources().getConfiguration().orientation;
        if (newOrientation != orientation) {
            orientationJustChanged = true;
            orientation = newOrientation;
        }
        savePreviousImageValues();
    }


    public float getMaxZoom() {
        return maxScale;
    }


    public void setMaxZoom(float max) {
        maxScale = max;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
        maxScaleIsSetByMultiplier = false;
    }


    public void setMaxZoomRatio(float max) {
        maxScaleMultiplier = max;
        maxScale = minScale * maxScaleMultiplier;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
        maxScaleIsSetByMultiplier = true;
    }


    public float getMinZoom() {
        return minScale;
    }


    public void setMinZoom(float min) {
        userSpecifiedMinScale = min;
        if (min == AUTOMATIC_MIN_ZOOM) {
            if (mScaleType == ScaleType.CENTER || mScaleType == ScaleType.CENTER_CROP) {
                Drawable drawable = getDrawable();
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();
                if (drawable != null && drawableWidth > 0 && drawableHeight > 0) {
                    float widthRatio = (float) viewWidth / drawableWidth;
                    float heightRatio = (float) viewHeight / drawableHeight;
                    if (mScaleType == ScaleType.CENTER) {
                        minScale = Math.min(widthRatio, heightRatio);
                    } else {  // CENTER_CROP
                        minScale = Math.min(widthRatio, heightRatio) / Math.max(widthRatio, heightRatio);
                    }
                }
            } else {
                minScale = 1.0f;
            }
        } else {
            minScale = userSpecifiedMinScale;
        }
        if (maxScaleIsSetByMultiplier) {
            setMaxZoomRatio(maxScaleMultiplier);
        }
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
    }


    public float getCurrentZoom() {
        return normalizedScale;
    }


    public void resetZoom() {
        normalizedScale = 1;
        fitImageToView();
    }


    public void setZoom(float scale) {
        setZoom(scale, 0.5f, 0.5f);
    }


    public void setZoom(float scale, float focusX, float focusY) {
        setZoom(scale, focusX, focusY, mScaleType);
    }


    public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType) {
        //
        // setZoom can be called before the image is on the screen, but at this point,
        // image and view sizes have not yet been calculated in onMeasure. Thus, we should
        // delay calling setZoom until the view has been measured.
        //
        if (!onDrawReady) {
            delayedZoomVariables = new ZoomVariables(scale, focusX, focusY, scaleType);
            return;
        }
        if (userSpecifiedMinScale == AUTOMATIC_MIN_ZOOM) {
            setMinZoom(AUTOMATIC_MIN_ZOOM);
            if (normalizedScale < minScale) {
                normalizedScale = minScale;
            }
        }

        if (scaleType != mScaleType) {
            setScaleType(scaleType);
        }
        resetZoom();
        scaleImage(scale, viewWidth / 2, viewHeight / 2, true);
        matrix.getValues(m);
        m[Matrix.MTRANS_X] = -((focusX * getImageWidth()) - (viewWidth * 0.5f));
        m[Matrix.MTRANS_Y] = -((focusY * getImageHeight()) - (viewHeight * 0.5f));
        matrix.setValues(m);
        fixTrans();
        setImageMatrix(matrix);
    }


    public void setZoom(TouchImageView img) {
        PointF center = img.getScrollPosition();
        setZoom(img.getCurrentZoom(), center.x, center.y, img.getScaleType());
    }


    public PointF getScrollPosition() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        PointF point = transformCoordTouchToBitmap(viewWidth / 2, viewHeight / 2, true);
        point.x /= drawableWidth;
        point.y /= drawableHeight;
        return point;
    }


    public void setScrollPosition(float focusX, float focusY) {
        setZoom(normalizedScale, focusX, focusY);
    }


    private void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, getImageWidth());
        float fixTransY = getFixTrans(transY, viewHeight, getImageHeight());

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }


    private void fixScaleTrans() {
        fixTrans();
        matrix.getValues(m);
        if (getImageWidth() < viewWidth) {
            m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2;
        }

        if (getImageHeight() < viewHeight) {
            m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2;
        }
        matrix.setValues(m);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;

        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    private float getImageWidth() {
        return matchViewWidth * normalizedScale;
    }

    private float getImageHeight() {
        return matchViewHeight * normalizedScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        int drawableWidth = (drawable == null ? 0 : drawable.getIntrinsicWidth());
        int drawableHeight = (drawable == null ? 0 : drawable.getIntrinsicHeight());
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int totalViewWidth = setViewSize(widthMode, widthSize, drawableWidth);
        int totalViewHeight = setViewSize(heightMode, heightSize, drawableHeight);

        if (!orientationJustChanged) {
            savePreviousImageValues();
        }

        // Image view width, height must consider padding
        int width = totalViewWidth - getPaddingLeft() - getPaddingRight();
        int height = totalViewHeight - getPaddingTop() - getPaddingBottom();

        //
        // Set view dimensions
        //
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //
        // Fit content within view.
        //
        // onMeasure may be called multiple times for each layout change, including orientation
        // changes. For example, if the TouchImageView is inside a ConstraintLayout, onMeasure may
        // be called with:
        // widthMeasureSpec == "AT_MOST 2556" and then immediately with
        // widthMeasureSpec == "EXACTLY 1404", then back and forth multiple times in quick
        // succession, as the ConstraintLayout tries to solve its constraints.
        //
        // onSizeChanged is called once after the final onMeasure is called. So we make all changes
        // to class members, such as fitting the image into the new shape of the TouchImageView,
        // here, after the final size has been determined. This helps us avoid both
        // repeated computations, and making irreversible changes (e.g. making the View temporarily too
        // big or too small, thus making the current zoom fall outside of an automatically-changing
        // minZoom and maxZoom).
        //
        viewWidth = w;
        viewHeight = h;
        fitImageToView();
    }


    private void fitImageToView() {
        FixedPixel fixedPixel = orientationJustChanged ?
                orientationChangeFixedPixel : viewSizeChangeFixedPixel;
        orientationJustChanged = false;

        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }
        if (matrix == null || prevMatrix == null) {
            return;
        }

        if (userSpecifiedMinScale == AUTOMATIC_MIN_ZOOM) {
            setMinZoom(AUTOMATIC_MIN_ZOOM);
            if (normalizedScale < minScale) {
                normalizedScale = minScale;
            }
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        //
        // Scale image for view
        //
        float scaleX = (float) viewWidth / drawableWidth;
        float scaleY = (float) viewHeight / drawableHeight;

        switch (mScaleType) {
            case CENTER:
                scaleX = scaleY = 1;
                break;

            case CENTER_CROP:
                scaleX = scaleY = Math.max(scaleX, scaleY);
                break;

            case CENTER_INSIDE:
                scaleX = scaleY = Math.min(1, Math.min(scaleX, scaleY));

            case FIT_CENTER:
            case FIT_START:
            case FIT_END:
                scaleX = scaleY = Math.min(scaleX, scaleY);
                break;

            case FIT_XY:
                break;

            default:
        }

        //
        // Put the image's center in the right place.
        //
        float redundantXSpace = viewWidth - (scaleX * drawableWidth);
        float redundantYSpace = viewHeight - (scaleY * drawableHeight);
        matchViewWidth = viewWidth - redundantXSpace;
        matchViewHeight = viewHeight - redundantYSpace;
        if (!isZoomed() && !imageRenderedAtLeastOnce) {
            //
            // Stretch and center image to fit view
            //
            matrix.setScale(scaleX, scaleY);
            switch (mScaleType) {
                case FIT_START:
                    matrix.postTranslate(0, 0);
                    break;
                case FIT_END:
                    matrix.postTranslate(redundantXSpace, redundantYSpace);
                    break;
                default:
                    matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
            }
            normalizedScale = 1;
        } else {
            //
            // These values should never be 0 or we will set viewWidth and viewHeight
            // to NaN in newTranslationAfterChange. To avoid this, call savePreviousImageValues
            // to set them equal to the current values.
            //
            if (prevMatchViewWidth == 0 || prevMatchViewHeight == 0) {
                savePreviousImageValues();
            }

            //
            // Use the previous matrix as our starting point for the new matrix.
            //
            prevMatrix.getValues(m);

            //
            // Rescale Matrix if appropriate
            //
            m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale;
            m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale;

            //
            // TransX and TransY from previous matrix
            //
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];

            //
            // X position
            //
            float prevActualWidth = prevMatchViewWidth * normalizedScale;
            float actualWidth = getImageWidth();
            m[Matrix.MTRANS_X] = newTranslationAfterChange(transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth, fixedPixel);

            //
            // Y position
            //
            float prevActualHeight = prevMatchViewHeight * normalizedScale;
            float actualHeight = getImageHeight();
            m[Matrix.MTRANS_Y] = newTranslationAfterChange(transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight, fixedPixel);

            //
            // Set the matrix to the adjusted scale and translation values.
            //
            matrix.setValues(m);
        }
        fixTrans();
        setImageMatrix(matrix);
    }


    private int setViewSize(int mode, int size, int drawableWidth) {
        int viewSize;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                viewSize = size;
                break;

            case MeasureSpec.AT_MOST:
                viewSize = Math.min(drawableWidth, size);
                break;

            case MeasureSpec.UNSPECIFIED:
                viewSize = drawableWidth;
                break;

            default:
                viewSize = size;
                break;
        }
        return viewSize;
    }


    private float newTranslationAfterChange(float trans, float prevImageSize, float imageSize, int prevViewSize, int viewSize, int drawableSize, FixedPixel sizeChangeFixedPixel) {
        if (imageSize < viewSize) {
            //
            // The width/height of image is less than the view's width/height. Center it.
            //
            return (viewSize - (drawableSize * m[Matrix.MSCALE_X])) * 0.5f;

        } else if (trans > 0) {
            //
            // The image is larger than the view, but was not before the view changed. Center it.
            //
            return -((imageSize - viewSize) * 0.5f);

        } else {
            //
            // Where is the pixel in the View that we are keeping stable, as a fraction of the
            // width/height of the View?
            //
            float fixedPixelPositionInView = 0.5f;  // CENTER
            if (sizeChangeFixedPixel == FixedPixel.BOTTOM_RIGHT) {
                fixedPixelPositionInView = 1.0f;
            } else if (sizeChangeFixedPixel == FixedPixel.TOP_LEFT) {
                fixedPixelPositionInView = 0.0f;
            }
            //
            // Where is the pixel in the Image that we are keeping stable, as a fraction of the
            // width/height of the Image?
            //
            float fixedPixelPositionInImage = (-trans + (fixedPixelPositionInView * prevViewSize)) / prevImageSize;
            //
            // Here's what the new translation should be so that, after whatever change triggered
            // this function to be called, the pixel at fixedPixelPositionInView of the View is
            // still the pixel at fixedPixelPositionInImage of the image.
            //
            return -((fixedPixelPositionInImage * imageSize) - (viewSize * fixedPixelPositionInView));
        }
    }

    private void setState(State state) {
        this.state = state;
    }

    public boolean canScrollHorizontallyFroyo(int direction) {
        return canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        matrix.getValues(m);
        float x = m[Matrix.MTRANS_X];

        if (getImageWidth() < viewWidth) {
            return false;

        } else if (x >= -1 && direction < 0) {
            return false;

        } else if (Math.abs(x) + viewWidth + 1 >= getImageWidth() && direction > 0) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        matrix.getValues(m);
        float y = m[Matrix.MTRANS_Y];

        if (getImageHeight() < viewHeight) {
            return false;

        } else if (y >= -1 && direction < 0) {
            return false;

        } else if (Math.abs(y) + viewHeight + 1 >= getImageHeight() && direction > 0) {
            return false;
        }

        return true;
    }

    private void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {
        float lowerScale, upperScale;
        if (stretchImageToSuper) {
            lowerScale = superMinScale;
            upperScale = superMaxScale;

        } else {
            lowerScale = minScale;
            upperScale = maxScale;
        }

        float origScale = normalizedScale;
        normalizedScale *= deltaScale;
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale;
            deltaScale = upperScale / origScale;
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
        }

        matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
    }


    private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float finalX = ((x - transX) * origW) / getImageWidth();
        float finalY = ((y - transY) * origH) / getImageHeight();

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0), origW);
            finalY = Math.min(Math.max(finalY, 0), origH);
        }

        return new PointF(finalX, finalY);
    }


    private PointF transformCoordBitmapToTouch(float bx, float by) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float px = bx / origW;
        float py = by / origH;
        float finalX = m[Matrix.MTRANS_X] + getImageWidth() * px;
        float finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py;
        return new PointF(finalX, finalY);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void compatPostOnAnimation(Runnable runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable);

        } else {
            postDelayed(runnable, 1000 / 60);
        }
    }

    private void printMatrixInfo() {
        float[] n = new float[9];
        matrix.getValues(n);
        Log.d(DEBUG, "Scale: " + n[Matrix.MSCALE_X] + " TransX: " + n[Matrix.MTRANS_X] + " TransY: " + n[Matrix.MTRANS_Y]);
    }

    public enum FixedPixel {CENTER, TOP_LEFT, BOTTOM_RIGHT}

    private enum State {NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM}

    public interface OnTouchImageViewListener {
        void onMove();
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (doubleTapListener != null) {
                return doubleTapListener.onSingleTapConfirmed(e);
            }
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (fling != null) {
                //
                // If a previous fling is still active, it should be cancelled so that two flings
                // are not run simultaenously.
                //
                fling.cancelFling();
            }
            fling = new Fling((int) velocityX, (int) velocityY);
            compatPostOnAnimation(fling);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if (isZoomEnabled()) {
                if (doubleTapListener != null) {
                    consumed = doubleTapListener.onDoubleTap(e);
                }
                if (state == State.NONE) {
                    float targetZoom = (normalizedScale == minScale) ? maxScale : minScale;
                    DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                    compatPostOnAnimation(doubleTap);
                    consumed = true;
                }
            }
            return consumed;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (doubleTapListener != null) {
                return doubleTapListener.onDoubleTapEvent(e);
            }
            return false;
        }
    }


    private class PrivateOnTouchListener implements OnTouchListener {

        //
        // Remember last point position for dragging
        //
        private PointF last = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (getDrawable() == null) {
                setState(State.NONE);
                return false;
            }
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        if (fling != null)
                            fling.cancelFling();
                        setState(State.DRAG);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (state == State.DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, getImageWidth());
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, getImageHeight());
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        setState(State.NONE);
                        break;
                }
            }

            setImageMatrix(matrix);

            //
            // User-defined OnTouchListener
            //
            if (userTouchListener != null) {
                userTouchListener.onTouch(v, event);
            }

            //
            // OnTouchImageViewListener is set: TouchImageView dragged by user.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            //
            // indicate event was handled
            //
            return true;
        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setState(State.ZOOM);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);

            //
            // OnTouchImageViewListener is set: TouchImageView pinch zoomed by user.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            setState(State.NONE);
            boolean animateToZoomBoundary = false;
            float targetZoom = normalizedScale;
            if (normalizedScale > maxScale) {
                targetZoom = maxScale;
                animateToZoomBoundary = true;

            } else if (normalizedScale < minScale) {
                targetZoom = minScale;
                animateToZoomBoundary = true;
            }

            if (animateToZoomBoundary) {
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, viewWidth / 2, viewHeight / 2, true);
                compatPostOnAnimation(doubleTap);
            }
        }
    }


    private class DoubleTapZoom implements Runnable {

        private static final float ZOOM_TIME = 500;
        private long startTime;
        private float startZoom, targetZoom;
        private float bitmapX, bitmapY;
        private boolean stretchImageToSuper;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        private PointF startTouch;
        private PointF endTouch;

        DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
            setState(State.ANIMATE_ZOOM);
            startTime = System.currentTimeMillis();
            this.startZoom = normalizedScale;
            this.targetZoom = targetZoom;
            this.stretchImageToSuper = stretchImageToSuper;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
            this.bitmapX = bitmapPoint.x;
            this.bitmapY = bitmapPoint.y;

            //
            // Used for translating image during scaling
            //
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(viewWidth / 2, viewHeight / 2);
        }

        @Override
        public void run() {
            if (getDrawable() == null) {
                setState(State.NONE);
                return;
            }
            float t = interpolate();
            double deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
            translateImageToCenterTouchPosition(t);
            fixScaleTrans();
            setImageMatrix(matrix);

            //
            // OnTouchImageViewListener is set: double tap runnable updates listener
            // with every frame.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            if (t < 1f) {
                //
                // We haven't finished zooming
                //
                compatPostOnAnimation(this);

            } else {
                //
                // Finished zooming
                //
                setState(State.NONE);
            }
        }


        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            matrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }


        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / ZOOM_TIME;
            elapsed = Math.min(1f, elapsed);
            return interpolator.getInterpolation(elapsed);
        }


        private double calculateDeltaScale(float t) {
            double zoom = startZoom + t * (targetZoom - startZoom);
            return zoom / normalizedScale;
        }
    }


    private class Fling implements Runnable {

        CompatScroller scroller;
        int currX, currY;

        Fling(int velocityX, int velocityY) {
            setState(State.FLING);
            scroller = new CompatScroller(context);
            matrix.getValues(m);

            int startX = (int) m[Matrix.MTRANS_X];
            int startY = (int) m[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (getImageWidth() > viewWidth) {
                minX = viewWidth - (int) getImageWidth();
                maxX = 0;

            } else {
                minX = maxX = startX;
            }

            if (getImageHeight() > viewHeight) {
                minY = viewHeight - (int) getImageHeight();
                maxY = 0;

            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
            currX = startX;
            currY = startY;
        }

        public void cancelFling() {
            if (scroller != null) {
                setState(State.NONE);
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {

            //
            // OnTouchImageViewListener is set: TouchImageView listener has been flung by user.
            // Listener runnable updated with each frame of fling animation.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int transX = newX - currX;
                int transY = newY - currY;
                currX = newX;
                currY = newY;
                matrix.postTranslate(transX, transY);
                fixTrans();
                setImageMatrix(matrix);
                compatPostOnAnimation(this);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private class CompatScroller {
        Scroller scroller;
        OverScroller overScroller;

        CompatScroller(Context context) {
            overScroller = new OverScroller(context);
        }

        void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        }

        void forceFinished(boolean finished) {
            overScroller.forceFinished(finished);
        }

        public boolean isFinished() {
            return overScroller.isFinished();
        }

        boolean computeScrollOffset() {
            overScroller.computeScrollOffset();
            return overScroller.computeScrollOffset();
        }

        int getCurrX() {
            return overScroller.getCurrX();
        }

        int getCurrY() {
            return overScroller.getCurrY();
        }
    }

    private class ZoomVariables {
        float scale;
        float focusX;
        float focusY;
        ScaleType scaleType;

        ZoomVariables(float scale, float focusX, float focusY, ScaleType scaleType) {
            this.scale = scale;
            this.focusX = focusX;
            this.focusY = focusY;
            this.scaleType = scaleType;
        }
    }

}