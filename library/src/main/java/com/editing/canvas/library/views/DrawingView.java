package com.editing.canvas.library.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.editing.canvas.library.R;
import com.editing.canvas.library.ShapeDrawingActivity;
import com.editing.canvas.library.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

/**
 * Created by bansi.bhalodiya on 4/21/2017.
 */

public class DrawingView extends View {

    public static final int LINE = 1;
    public static final int RECTANGLE = 3;
    public static final int CIRCLE = 5;
    public static final int SMOOTHLINE = 2;
    public static final int ROUND_RECT = 7;
    public static final int CIRCLE_FILL = 8;
    public static final float TOUCH_TOLERANCE = 4;
    public static int STROCK_WIDTH_RECT = 5;
    public static int STROCK_COLOR_RECT = -1;//white
    public static int FILL_COLOR_RECT = 0;//transparent
    public static int CURVED_EDGES_RECT = 0;
    public static int STROCK_WIDTH_CIRCLE = 4;
    public static int STROCK_COLOR_CIRCLE = -1;
    public static int FILL_COLOR_CIRCLE = 0;
    public static int STROCK_WIDTH_LINE = 7;
    public static int STROCK_COLOR_LINE = -1;
    public static int STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_20;
    public static int STROCK_COLOR_SMOOTHLINE = -11030226;//green
    private final Paint emptyPaint = new Paint();
    public int mCurrentShape;
    public Path mPath;
    public Canvas mCanvas;
    protected Paint mPaintFinal;
    protected Bitmap mBitmap;
    /**
     * Indicates if you are drawing
     */
    protected boolean isDrawing = false;
    protected float mStartX;
    protected float mStartY;
    protected float mStartXForLineAngle;
    protected float mStartYForLineAngle;
    protected float mStartXFromPrev = 0;
    protected float mStartYFromPrev = 0;
    protected float mx;
    protected float my;
    RelativeLayout layoutAllShapesLinear;
    LinearLayout layoutShapeSelection;
    PopupWindow popupWindow;
    private List<Path> pathLists = new ArrayList<Path>();
    private List<Paint> paintLists = new ArrayList<Paint>();
    public List<Integer> shapeList = new ArrayList<Integer>();
    private List<Integer> fillColorList = new ArrayList<>();
    private List<Integer> strokeColorList = new ArrayList<>();
    private List<Integer> strokeWidthList = new ArrayList<>();
    private List<Integer> fillColorListCircle = new ArrayList<>();
    private List<Integer> strokeColorListCircle = new ArrayList<>();
    private List<Integer> strokeWidthListCircle = new ArrayList<>();
    // for Eraser
    private int baseColor = Color.WHITE;

    // for Undo, Redo
    public int historyPointer = 0;
    private boolean isDown = false;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //  mCanvas = new Canvas(mBitmap);
    }

    protected void init() {
        mPath = new Path();
        mPaintFinal = new Paint(Paint.DITHER_FLAG);
        mPaintFinal.setAntiAlias(true);
        mPaintFinal.setDither(true);
        mPaintFinal.setColor(getContext().getResources().getColor(R.color.colorgreen));
        mPaintFinal.setStyle(Paint.Style.STROKE);
        mPaintFinal.setStrokeJoin(Paint.Join.ROUND);
        mPaintFinal.setStrokeCap(Paint.Cap.ROUND);
        mPaintFinal.setStrokeWidth(STROCK_WIDTH_LINE);

        this.pathLists.add(new Path());
        this.paintLists.add(mPaintFinal);
        this.shapeList.add(SMOOTHLINE);

        this.fillColorList.add(FILL_COLOR_RECT);
        this.strokeWidthList.add(STROCK_WIDTH_RECT);
        this.strokeColorList.add(STROCK_COLOR_RECT);

        this.fillColorListCircle.add(FILL_COLOR_CIRCLE);
        this.strokeWidthListCircle.add(STROCK_WIDTH_CIRCLE);
        this.strokeColorListCircle.add(STROCK_COLOR_CIRCLE);
        this.historyPointer++;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0F, 0F, emptyPaint);
        }

//        canvas.drawBitmap(mBitmap, 0, 0, mPaintFinal);
        for (int i = 0; i < this.historyPointer; i++) {
            Path path = this.pathLists.get(i);
            Paint paint = this.paintLists.get(i);
            int shape = this.shapeList.get(i);
            if (shape == ROUND_RECT) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(fillColorList.get(i));
                paint.setAntiAlias(true);//necessary whenever you apply rounded to any view..
                canvas.drawPath(path, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidthList.get(i));
                paint.setColor(strokeColorList.get(i));
                paint.setAntiAlias(true);
                paint.setStrokeCap(Paint.Cap.SQUARE);//for start and end of strock
                paint.setStrokeJoin(Paint.Join.MITER);
                canvas.drawPath(path, paint);
            } else if (shape == CIRCLE_FILL) {

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(fillColorListCircle.get(i));
                canvas.drawPath(path, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidthListCircle.get(i));
                paint.setColor(strokeColorListCircle.get(i));
                canvas.drawPath(path, paint);
            } else
                canvas.drawPath(path, paint);
        }
        //   mCanvas = canvas;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //  ShapeDrawingActivity.hideTopBottomLayout();
        mx = event.getX();
        my = event.getY();
        switch (mCurrentShape) {
            case LINE:
                //  shapeList.add(LINE);
                onTouchEventLine(event);
                break;
            case SMOOTHLINE:
                //  shapeList.add(SMOOTHLINE);
                onTouchEventSmoothLine(event);
                break;
            case RECTANGLE:
                //  shapeList.add(RECTANGLE);
                onTouchEventRectangle(event);
                break;
            case CIRCLE:
                // shapeList.add(CIRCLE);
                onTouchEventCircle(event);
                break;
        }
        invalidate();
        return true;
    }

    //------------------------------------------------------------------
    // Line
    //------------------------------------------------------------------
    private Paint drawLinePaint() {
        Paint paint1 = new Paint();
        paint1 = new Paint(Paint.DITHER_FLAG);
        paint1.setAntiAlias(true);
        paint1.setDither(true);
        paint1.setStrokeJoin(Paint.Join.ROUND);
        paint1.setStrokeCap(Paint.Cap.ROUND);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(STROCK_WIDTH_LINE);
        paint1.setColor(STROCK_COLOR_LINE);

        return paint1;
    }

    private void updateHistoryLine(Path path) {
        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(drawLinePaint());
            this.shapeList.add(mCurrentShape);
            this.fillColorList.add(FILL_COLOR_RECT);
            this.strokeWidthList.add(STROCK_WIDTH_RECT);
            this.strokeColorList.add(STROCK_COLOR_RECT);
            this.fillColorListCircle.add(FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.add(STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.add(STROCK_COLOR_CIRCLE);
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, drawLinePaint());
            this.shapeList.set(this.historyPointer, mCurrentShape);
            this.fillColorList.set(this.historyPointer, FILL_COLOR_RECT);
            this.strokeWidthList.set(historyPointer, STROCK_WIDTH_RECT);
            this.strokeColorList.set(historyPointer, STROCK_COLOR_RECT);
            this.fillColorListCircle.set(this.historyPointer, FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.set(historyPointer, STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.set(historyPointer, STROCK_COLOR_CIRCLE);
            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
                this.shapeList.remove(this.historyPointer);
                this.fillColorList.remove(this.historyPointer);
                this.strokeWidthList.remove(this.historyPointer);
                this.strokeColorList.remove(this.historyPointer);

                this.fillColorListCircle.remove(this.historyPointer);
                this.strokeWidthListCircle.remove(this.historyPointer);
                this.strokeColorListCircle.remove(this.historyPointer);
            }
        }
    }

    private void onTouchEventLine(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                if (ShapeDrawingActivity.isLineConnectorOn && mStartXFromPrev != 0 && mStartYFromPrev != 0) {
                    mStartX = mStartXFromPrev;
                    mStartY = mStartYFromPrev;
                } else {
                    mStartX = mx;
                    mStartY = my;
                }
                mStartXForLineAngle = mStartX;
                mStartYForLineAngle = mStartY;
                //    invalidate();
                this.updateHistoryLine(this.createPath(event));
                this.isDown = true;
                hideLayouts();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isDown) {
                    return;
                }

                Path path = this.getCurrentPath();
                path.reset();
                path.moveTo(mStartX, mStartY);
                path.lineTo(mx, my);
                hideLayouts();
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                if (ShapeDrawingActivity.isSpecificDirectionLine) {

                    double angle = Math.atan2(my - mStartYForLineAngle, mx - mStartXForLineAngle) * 180 / PI;
                    Path path1 = this.getCurrentPath();
                    path1.reset();
                    if (angle >= 0 && angle < 23) //draw horizontal right line(keep y as it is)
                    {
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, mStartYForLineAngle);
                        // uncomment this line when want to draw connected line with angles
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = mStartYForLineAngle;
                    } else if (angle >= 67 && angle < 123) {//draw vertical bottom line(x as it is)
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mStartXForLineAngle, my);
//                        mStartXFromPrev = mStartXForLineAngle;
//                        mStartYFromPrev = my;
                    } else if (angle >= 157 && angle <= 180) {//draw horizontal left line
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, mStartYForLineAngle);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = mStartYForLineAngle;
                    } else if (angle <= 0 && angle > -23)//draw horizontal right
                    {
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, mStartYForLineAngle);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = mStartYForLineAngle;
                    } else if (angle <= -67 && angle > -123)//draw vertical up
                    {
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mStartXForLineAngle, my);
//                        mStartXFromPrev = mStartXForLineAngle;
//                        mStartYFromPrev = my;
                    } else if (angle <= -157 && angle > -180)//draw horizontal left line
                    {
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, mStartYForLineAngle);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = mStartYForLineAngle;
                    } else if (angle <= -23 && angle > -67)// draaw 45 degree top right cordinate
                    {
                        float xdiff = mx - mStartXForLineAngle;
                        float ydiff = my - mStartYForLineAngle;

                        float d = (float) Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
                        mx = (float) (mStartXForLineAngle + (d) * Math.sin(315));
                        my = (float) (mStartYForLineAngle - (d) * Math.cos(315));
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, my);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = my;
                    } else if (angle >= 23 && angle < 67) {//draw 45 degree below right cordinate..
                        float xdiff = mx - mStartXForLineAngle;
                        float ydiff = my - mStartYForLineAngle;
                        float d = (float) Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
                        mx = (float) (mStartXForLineAngle + (d) * Math.sin(45));
                        my = (float) (mStartYForLineAngle + (d) * Math.cos(45));
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, my);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = my;
                    } else if (angle >= 123 && angle < 157) {//draw 45 degree left bottom cordinate
                        float xdiff = mx - mStartXForLineAngle;
                        float ydiff = my - mStartYForLineAngle;

                        float d = (float) Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
                        mx = (float) (mStartXForLineAngle + (d) * Math.sin(180));
                        my = (float) (mStartYForLineAngle - (d) * Math.cos(180));
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, my);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = my;
                    } else if (angle <= -123 && angle > -157)//draw 45 degree left top cordinate
                    {
                        float xdiff = mx - mStartXForLineAngle;
                        float ydiff = my - mStartYForLineAngle;

                        float d = (float) Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
                        mx = (float) (mStartXForLineAngle + (d) * Math.sin(180));
                        my = (float) (mStartYForLineAngle + (d) * Math.cos(180));
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, my);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = my;
                    } else {
                        path1.moveTo(mStartXForLineAngle, mStartYForLineAngle);
                        path1.lineTo(mx, my);
//                        mStartXFromPrev = mx;
//                        mStartYFromPrev = my;
                    }
                } else {
//                    mStartXFromPrev = mx;
//                    mStartYFromPrev = my;
                }

                if (isDown) {
                    mStartX = 0F;
                    mStartY = 0F;
                    this.isDown = false;
                }
                mStartXFromPrev = mx;
                mStartYFromPrev = my;
                //  invalidate();
                showLayouts();
                break;
        }
    }

    //------------------------------------------------------------------
    // Smooth Line
    //------------------------------------------------------------------
    private Paint drawSmoothlinePaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(STROCK_WIDTH_SMOOTHLINE);
        paint.setColor(STROCK_COLOR_SMOOTHLINE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private void updateHistorySmoothLine(Path path) {
        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(drawSmoothlinePaint());
            this.shapeList.add(mCurrentShape);
            this.fillColorList.add(FILL_COLOR_RECT);
            this.strokeWidthList.add(STROCK_WIDTH_RECT);
            this.strokeColorList.add(STROCK_COLOR_RECT);

            this.fillColorListCircle.add(FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.add(STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.add(STROCK_COLOR_CIRCLE);
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, drawSmoothlinePaint());
            this.shapeList.set(this.historyPointer, mCurrentShape);
            this.fillColorList.set(this.historyPointer, FILL_COLOR_RECT);
            this.strokeWidthList.set(historyPointer, STROCK_WIDTH_RECT);
            this.strokeColorList.set(historyPointer, STROCK_COLOR_RECT);

            this.fillColorListCircle.set(this.historyPointer, FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.set(historyPointer, STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.set(historyPointer, STROCK_COLOR_CIRCLE);

            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
                this.shapeList.remove(this.historyPointer);
                this.fillColorList.remove(this.historyPointer);
                this.strokeWidthList.remove(this.historyPointer);
                this.strokeColorList.remove(this.historyPointer);

                this.fillColorListCircle.remove(this.historyPointer);
                this.strokeWidthListCircle.remove(this.historyPointer);
                this.strokeColorListCircle.remove(this.historyPointer);
            }
        }
    }

    private void onTouchEventSmoothLine(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;

                this.updateHistorySmoothLine(this.createPath(event));
                this.isDown = true;
                hideLayouts();
                break;
            case MotionEvent.ACTION_MOVE:

                if (!isDown) {
                    return;
                }
                float dx = Math.abs(mx - mStartX);
                float dy = Math.abs(my - mStartY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mStartX, mStartY, (mx + mStartX) / 2, (my + mStartY) / 2);
                    mStartX = mx;
                    mStartY = my;
                }
                Path path = this.getCurrentPath();
                path.lineTo(mx, my);
                invalidate();
                hideLayouts();
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                if (isDown) {
                    mStartX = 0F;
                    mStartY = 0F;
                    this.isDown = false;
                }
                invalidate();
                showLayouts();
                break;
        }
    }

    //------------------------------------------------------------------
    // Circle
    //------------------------------------------------------------------
    public Paint drawCirclePaint() {
        Paint paint1;
        paint1 = new Paint(Paint.DITHER_FLAG);
        paint1.setAntiAlias(true);
        paint1.setDither(true);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(STROCK_WIDTH_CIRCLE);
        paint1.setColor(STROCK_COLOR_CIRCLE);
        return paint1;
    }

    private void updateHistoryCircle(Path path) {
        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(drawCirclePaint());
            if (FILL_COLOR_CIRCLE != 0)
                this.shapeList.add(CIRCLE_FILL);
            else
                this.shapeList.add(mCurrentShape);
            this.fillColorList.add(FILL_COLOR_RECT);
            this.strokeWidthList.add(STROCK_WIDTH_RECT);
            this.strokeColorList.add(STROCK_COLOR_RECT);

            this.fillColorListCircle.add(FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.add(STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.add(STROCK_COLOR_CIRCLE);
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, drawCirclePaint());
            if (FILL_COLOR_CIRCLE != 0)
                this.shapeList.set(this.historyPointer, CIRCLE_FILL);
            else
                this.shapeList.set(this.historyPointer, mCurrentShape);
            this.fillColorList.set(this.historyPointer, FILL_COLOR_RECT);
            this.strokeWidthList.set(historyPointer, STROCK_WIDTH_RECT);
            this.strokeColorList.set(historyPointer, STROCK_COLOR_RECT);

            this.fillColorListCircle.set(this.historyPointer, FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.set(historyPointer, STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.set(historyPointer, STROCK_COLOR_CIRCLE);
            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
                this.shapeList.remove(this.historyPointer);

                this.fillColorList.remove(this.historyPointer);
                this.strokeWidthList.remove(this.historyPointer);
                this.strokeColorList.remove(this.historyPointer);

                this.fillColorListCircle.remove(this.historyPointer);
                this.strokeWidthListCircle.remove(this.historyPointer);
                this.strokeColorListCircle.remove(this.historyPointer);
            }
        }
    }

    private void onTouchEventCircle(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //   isDrawing = true;
                mStartX = mx;
                mStartY = my;
                this.updateHistoryCircle(this.createPath(event));
                this.isDown = true;
                // invalidate();
                hideLayouts();
                break;
            case MotionEvent.ACTION_MOVE:
                double distanceX = Math.abs((double) (mStartX - mx));
                double distanceY = Math.abs((double) (mStartY - my));
                double radius = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0));
                Path path = this.getCurrentPath();
                path.reset();
                if (ShapeDrawingActivity.isCircleCenterAlign)
                    path.addCircle(getWidth() / 2, getHeight() / 2, (float) radius, Path.Direction.CCW);
                else
                    path.addCircle(mStartX, mStartY, (float) radius, Path.Direction.CCW);
                //   invalidate();
                hideLayouts();
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                if (isDown) {
                    mStartX = 0F;
                    mStartY = 0F;
                    this.isDown = false;
                }
                showLayouts();
                break;
        }
    }

    //------------------------------------------------------------------
    // Rectangle
    //------------------------------------------------------------------
    public Paint drawRectanglePaint() {
        Paint paint1 = new Paint();
        paint1 = new Paint(Paint.DITHER_FLAG);
        paint1.setDither(true);
      /*  paint1.setStyle(Paint.Style.FILL);
        paint1.setColor(FILL_COLOR_RECT);
        paint1.setAntiAlias(true);//necessary whenever you apply rounded to any view..
        // drawRectangle(canvas, mPaintFinal);*/
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(STROCK_WIDTH_RECT);
        paint1.setColor(STROCK_COLOR_RECT);
        paint1.setAntiAlias(true);
        paint1.setStrokeCap(Paint.Cap.SQUARE);//for start and end of strock
        paint1.setStrokeJoin(Paint.Join.MITER);
        return paint1;
    }

    private void updateHistoryRectangle(Path path) {
        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(drawRectanglePaint());
            if (FILL_COLOR_RECT != 0)
                this.shapeList.add(ROUND_RECT);
            else
                this.shapeList.add(mCurrentShape);
            this.fillColorList.add(FILL_COLOR_RECT);
            this.strokeWidthList.add(STROCK_WIDTH_RECT);
            this.strokeColorList.add(STROCK_COLOR_RECT);
            this.fillColorListCircle.add(FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.add(STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.add(STROCK_COLOR_CIRCLE);

            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, drawRectanglePaint());
            if (FILL_COLOR_RECT != 0)
                this.shapeList.set(this.historyPointer, ROUND_RECT);
            else
                this.shapeList.set(this.historyPointer, mCurrentShape);
            this.fillColorList.set(this.historyPointer, FILL_COLOR_RECT);
            this.strokeWidthList.set(historyPointer, STROCK_WIDTH_RECT);
            this.strokeColorList.set(historyPointer, STROCK_COLOR_RECT);

            this.fillColorListCircle.set(this.historyPointer, FILL_COLOR_CIRCLE);
            this.strokeWidthListCircle.set(historyPointer, STROCK_WIDTH_CIRCLE);
            this.strokeColorListCircle.set(historyPointer, STROCK_COLOR_CIRCLE);

            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
                this.shapeList.remove(this.historyPointer);
                this.fillColorList.remove(this.historyPointer);
                this.strokeWidthList.remove(historyPointer);
                this.strokeColorList.remove(historyPointer);

                this.fillColorListCircle.remove(this.historyPointer);
                this.strokeWidthListCircle.remove(historyPointer);
                this.strokeColorListCircle.remove(historyPointer);
            }
        }
    }

    private void onTouchEventRectangle(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                this.updateHistoryRectangle(this.createPath(event));
                this.isDown = true;
                // invalidate();
                hideLayouts();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isDown) {
                    return;
                }

                Path path = this.getCurrentPath();
                path.reset();

                if (ShapeDrawingActivity.isRectCenterAlign) {
                    int height = getHeight();
                    int width = getWidth();
                    int diameter = width;
                    if (height < width) {
                        diameter = height;
                    }
                    double rectwidth = Math.abs((double) (mStartX - mx));
                    double rectheight = Math.abs((double) (mStartY - my));
                    float left = (float) ((width / 2) - (rectwidth / 2));
                    float right = (float) (width / 2 + (rectwidth / 2));
                    float top = (float) (height / 2 - (rectheight / 2));
                    float bottom = (float) (height / 2 + (rectheight / 2));


                    RectF rectF = new RectF(left, top, right, bottom);
                    path.addRoundRect(rectF, CURVED_EDGES_RECT, CURVED_EDGES_RECT, Path.Direction.CCW);
                } else {
                    float left = Math.min(mStartX, mx);
                    float right = Math.max(mStartX, mx);
                    float top = Math.min(mStartY, my);
                    float bottom = Math.max(mStartY, my);
                    RectF rectF = new RectF(left, top, right, bottom);
                    // Log.e("rectangle point", "startx , starty {" + mStartX + "," + mStartY + "} -- mx,my {" + mx + "," + my + "}");

                    path.addRoundRect(rectF, CURVED_EDGES_RECT, CURVED_EDGES_RECT, Path.Direction.CCW);
                }
                //  invalidate();
                hideLayouts();

                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;

                //   onDrawRectangle(mCanvas);
                //  drawRectangle(mCanvas, mPaintFinal);
                if (isDown) {
                    mStartX = 0F;
                    mStartY = 0F;
                    this.isDown = false;
                }
                //  invalidate();
                //   mPaintFinal.setColor(ContextCompat.getColor(getContext(),STROCK_COLOR));
                showLayouts();
                break;
        }
    }

    //------------------------------------------------------------------

    public void reset(RelativeLayout layoutAllShapesLinear1, LinearLayout layoutShapeSelection1, PopupWindow popupWindow1) {
        this.layoutAllShapesLinear = layoutAllShapesLinear1;
        this.layoutShapeSelection = layoutShapeSelection1;
        this.popupWindow = popupWindow1;
        mPath = new Path();

    }

    private Path createPath(MotionEvent event) {
        Path path = new Path();
        path.moveTo(mStartX, mStartY);
        return path;
    }

    private Path getCurrentPath() {
        return this.pathLists.get(this.historyPointer - 1);
    }

    public void clear() {
        historyPointer = 0;
        pathLists.clear();
        paintLists.clear();
        shapeList.clear();
        fillColorList.clear();
        strokeColorList.clear();
        strokeWidthList.clear();
        fillColorListCircle.clear();
        strokeColorListCircle.clear();
        strokeWidthListCircle.clear();
        this.invalidate();
    }

    /**
     * This method checks if Undo is available
     *
     * @return If Undo is available, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean canUndo() {
        return this.historyPointer >= 1;
    }

    /**
     * This method checks if Redo is available
     *
     * @return If Redo is available, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean canRedo() {
        return this.historyPointer < this.pathLists.size();
    }

    /**
     * This method draws canvas again for Undo.
     *
     * @return If Undo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean undo() {
        if (canUndo()) {
            this.historyPointer--;
            this.invalidate();

            return true;
        } else {
            this.invalidate();
            return false;
        }
    }

    private void hideLayouts() {
        layoutAllShapesLinear.setVisibility(GONE);
        layoutShapeSelection.setVisibility(GONE);
        if (popupWindow != null && popupWindow.isShowing())
            popupWindow.dismiss();
    }

    private void showLayouts() {
        layoutAllShapesLinear.setVisibility(VISIBLE);
        layoutShapeSelection.setVisibility(VISIBLE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
