package com.editing.canvas.library;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.editing.canvas.library.util.Constants;
import com.editing.canvas.library.util.FileUtils;
import com.editing.canvas.library.util.Helper;
import com.editing.canvas.library.views.DrawingView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static android.view.View.GONE;

/**
 * Created by hemangi.vekaria on 17-04-2017.
 */

public class ShapeDrawingActivity extends AppCompatActivity {

    ImageView imgSmoothLineDraw20, imgSmoothLineDraw15, imgSmoothLineDraw10, imgSmoothLineDraw5, imgSmoothLineDraw2;
    //CircleImageView imgShape6ColorPicPensil;

    LinearLayout linearLayoutCircles, layoutShapeClick, linearLayoutLines, linearLayoutSquare, linearLayoutPensil;
    ImageView imgSquareBorderWidth, imgSquareRoundedBorderWidth, imgSquareDrawAlignment,
            imgCircleBorderWidth, imgCircleDrawAlignment,
            imgSelectedShape, imgSelectionPensil, imgSelectionCircle, imgSelectionLine, imgSelectionSquare,
            imgLineWidth, imgLineDrawAtAngle, imgLineColor, imgLineConnector, imgDownArrow;
    TextView imgUndo, imgClearAllDrawing, imgsave, imgCloseScreen;
    TextView txtCircleBorderWidth, txtSquareBorderWidth, txtSquareRoundedBorderWidth, txtLineWidth;
    GradientDrawable shapeDrawableLine, shapeDrawableLineWidth;
    FrameLayout mainFrameLayout;
    RelativeLayout layoutAllShapeInclude, relativeLayoutMain;
    DrawingView mDrawingView;
    PopupWindow popupWindow;
    View popupView;

    public static int TAG_LINE_COLOR = 2, TAG_FREE_DRAW_COLOR = 1, TAG_RECTANGLE_COLOR = 3, TAG_RECTANGLE_FILLCOLOR = 4, TAGCIRCLE_COLOR = 5, TAGCIRCLE_FILLCOLOR = 6;
    public static boolean isSpecificDirectionLine, isLineConnectorOn, isCircleCenterAlign = true, isRectCenterAlign = true;
    NumberPicker npDialog;
    int int_strock_width;

    Uri imageURI;
    ImageView imgMainPlaceholder;
    Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_draw);
        initView();
        getDeviceSize();

        if (savedInstanceState != null) {
            imgMainPlaceholder.setVisibility(View.VISIBLE);
            imageURI = savedInstanceState.getParcelable("imageUri");
        } else {
            imgMainPlaceholder.setVisibility(View.VISIBLE);
            imageURI = getIntent().getData();
        }
        imgCircleDrawAlignment.setImageResource(R.drawable.align_center_icon);
        imgSquareDrawAlignment.setImageResource(R.drawable.align_center_icon);

        imgSmoothLineDraw20.setImageResource(R.drawable.select_icon_01_icon);

        imgUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoOperation();
            }
        });
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
            imgMainPlaceholder.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            imgMainPlaceholder.setImageURI(imageURI);
        }
    }

    private void initView() {
        imgMainPlaceholder = findViewById(R.id.imgMainPlaceholder);
        imgSelectedShape = findViewById(R.id.imgSelectionShape);
        imgDownArrow = findViewById(R.id.imgDownArrow);
        mDrawingView = (DrawingView) findViewById(R.id.drawingViewAll);

        relativeLayoutMain = (RelativeLayout) findViewById(R.id.relativeMain);
        layoutAllShapeInclude = findViewById(R.id.bottomLayoutAllSizeShapes);
        mainFrameLayout = (FrameLayout) findViewById(R.id.wholeFrameLayout);
        imgClearAllDrawing = findViewById(R.id.txtClear);
        imgUndo = findViewById(R.id.txtUndo);
        layoutShapeClick = (LinearLayout) findViewById(R.id.layoutShapeClick);
        linearLayoutCircles = (LinearLayout) findViewById(R.id.linearLayoutCircles);
        imgCircleBorderWidth = findViewById(R.id.imgShape1CircleWidth);
        imgCircleDrawAlignment = findViewById(R.id.imgShape2CircleAlignment);
        txtCircleBorderWidth = (TextView) findViewById(R.id.txtShape1SCircleWidth);
        linearLayoutSquare = (LinearLayout) findViewById(R.id.linearLayoutSquares);
        imgSquareBorderWidth = findViewById(R.id.imgShape1SquareWidth);
        imgSquareRoundedBorderWidth = findViewById(R.id.imgShape2SquareCornerRad);
        imgSquareDrawAlignment = findViewById(R.id.imgShape3SquareAlign);
        txtSquareBorderWidth = (TextView) findViewById(R.id.txtShape1SquareWidth);
        txtSquareRoundedBorderWidth = (TextView) findViewById(R.id.txtShape2SquareCornerRad);
        linearLayoutLines = (LinearLayout) findViewById(R.id.linearLayoutLines);
        imgLineWidth = findViewById(R.id.imgShape1LineWidth);
        imgLineDrawAtAngle = findViewById(R.id.imgShape2Line);
        imgLineColor = findViewById(R.id.imgShape3LineColor);
        imgLineConnector = findViewById(R.id.imgShape4LineConnector);
        txtLineWidth = (TextView) findViewById(R.id.txtShape1LineRadius);
        imgCloseScreen = findViewById(R.id.txtClose);
        imgsave = findViewById(R.id.txtSave);
        linearLayoutPensil = (LinearLayout) findViewById(R.id.linearLayoutpensils);
        imgSmoothLineDraw20 = findViewById(R.id.imgShape1Pensil);
        imgSmoothLineDraw15 = findViewById(R.id.imgShape2Pensil);
        imgSmoothLineDraw10 = findViewById(R.id.imgShape3Pensil);
        imgSmoothLineDraw5 = findViewById(R.id.imgShape4Pensil);
        imgSmoothLineDraw2 = findViewById(R.id.imgShape5Pensil);
        //  imgShape6ColorPicPensil = (CircleImageView) findViewById(R.id.imgShape6ColorPicPensil);

        imgsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
    }

    private void initializeAllViews() {
        mDrawingView.mCurrentShape = DrawingView.SMOOTHLINE;
        mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);

        //initiate popup menu
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.popup_shapes, (ViewGroup) findViewById(R.id.popup_element));
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imgSelectionPensil = popupView.findViewById(R.id.imgSelectionPensil1);
        imgSelectionCircle = popupView.findViewById(R.id.imgSelectionCircle1);
        imgSelectionLine = popupView.findViewById(R.id.imgSelectionLine1);
        imgSelectionSquare = popupView.findViewById(R.id.imgSelectionSquare1);

        //set bottom layout lines..
        shapeDrawableLineWidth = (GradientDrawable) imgLineWidth.getDrawable();
        shapeDrawableLineWidth.setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        shapeDrawableLineWidth.setStroke(2, ContextCompat.getColor(getApplicationContext(), R.color.colorgreen));//constant green always in below layout..
        shapeDrawableLine = (GradientDrawable) imgLineColor.getDrawable();
        shapeDrawableLine.setColor(DrawingView.STROCK_COLOR_LINE);
        shapeDrawableLine.setStroke(2, ContextCompat.getColor(getApplicationContext(), R.color.colorgreen));//constant green always in below layout..

        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_20;

        txtLineWidth.setText(DrawingView.STROCK_WIDTH_LINE + "");
        isLineConnectorOn = false;
        isSpecificDirectionLine = false;
        txtSquareBorderWidth.setText(DrawingView.STROCK_WIDTH_RECT + "");
        txtSquareRoundedBorderWidth.setText(DrawingView.CURVED_EDGES_RECT + "");
        txtCircleBorderWidth.setText(DrawingView.STROCK_WIDTH_CIRCLE + "");
    }

    private void allClickListners() {
        layoutShapeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    if (popupWindow == null) {
                        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        imgSelectionPensil = popupView.findViewById(R.id.imgSelectionPensil1);
                        imgSelectionCircle = popupView.findViewById(R.id.imgSelectionCircle1);
                        imgSelectionLine = popupView.findViewById(R.id.imgSelectionLine1);
                        imgSelectionSquare = popupView.findViewById(R.id.imgSelectionSquare1);
                    }
                    if (!Helper.isLandscapeScreen(getApplicationContext()))
                        popupWindow.showAsDropDown(layoutShapeClick, 0, -360);
                    else
                        popupWindow.showAsDropDown(layoutShapeClick, 70, -70);
                }
                clickListnerOfPopUp();
            }
        });

    }


    int intWidthScreen, intHeightScreen;


    private void hideTopBottomEditingOption() {
        layoutAllShapeInclude.setVisibility(GONE);
        layoutShapeClick.setVisibility(GONE);
        mDrawingView.setVisibility(GONE);
        if (popupWindow != null && popupWindow.isShowing())
            popupWindow.dismiss();
    }

    private void getDeviceSize() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        intWidthScreen = displayMetrics.widthPixels;
        intHeightScreen = displayMetrics.heightPixels;
    }

    public void setSquareCornerRadius(View v) {
        final Dialog dialog = new Dialog(ShapeDrawingActivity.this);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_line_width);
        dialog.show();
        npDialog = dialog.findViewById(R.id.numberpickerLineWidth);
        TextView tvDialogOk = dialog.findViewById(R.id.txtOkLineWidth);
        TextView tvDialogSetLineWidth = dialog.findViewById(R.id.txtSetLineWidth);
        npDialog.setMinValue(0);
        npDialog.setMaxValue(25);

        tvDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int_strock_width = npDialog.getValue();
                dialog.dismiss();
                DrawingView.CURVED_EDGES_RECT = int_strock_width;
                txtSquareRoundedBorderWidth.setText(int_strock_width + "");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeAllViews();
                allClickListners();
            }
        }, 100);
    }

    public void onSaveImageClick(View v) {
//        if (!isVideo) {
//            Observable<Uri> saveImageObserv = Observable.just(saveImage());
//            saveImageObserv.subscribe(new Subscriber<Uri>() {
//                @Override
//                public void onCompleted() {
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onNext(Uri uri) {
//                    Intent intent = new Intent();
//                    intent.setData(uri);
//                    setResult(RESULT_OK, intent);
//                    Log.e("Sendfile ShapeFormat", uri.toString());
//                    overridePendingTransition(0, 0);
//                    finish();
//                }
//            });
//
//        } else {
//            mergeVideoWithImageFile();
//        }
    }

    public Uri saveImage() {
        Uri imageuri = null;
        mainFrameLayout.setDrawingCacheEnabled(true);
        mainFrameLayout.buildDrawingCache(true);
        Bitmap bitmap = mainFrameLayout.getDrawingCache();

        FileOutputStream out = null;
        try {
            String filename = Constants.DATE_FORMAT.format(new Date()) + "_shape.jpg";
            File file = FileUtils.Instance(getApplicationContext()).getSavedImagePath(filename);
            out = new FileOutputStream(file.getAbsoluteFile());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            mainFrameLayout.setDrawingCacheEnabled(false);
            imageuri = Uri.fromFile(file);
            setResult(RESULT_OK, new Intent().setData(imageuri));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageuri;
    }

    public void onCloseDrawingClick(View v) {
        mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);
        mDrawingView.invalidate();
        mDrawingView.clear();
    }

    public void onCloseScreenClick(View v) {
        setResult(RESULT_CANCELED);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        overridePendingTransition(0, 0);
        finish();
    }

    public void setFreeShape1Pensil(View v) {
        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_20;
        imgSmoothLineDraw20.setImageResource(R.drawable.select_icon_01_icon);
        imgSmoothLineDraw15.setImageResource(R.drawable.unselect_icon_02_icon);
        imgSmoothLineDraw10.setImageResource(R.drawable.unselect_icon_03_icon);
        imgSmoothLineDraw5.setImageResource(R.drawable.unselect_icon_04_icon);
        imgSmoothLineDraw2.setImageResource(R.drawable.unselect_icon_05_icon);
    }

    public void setFreeShape2Pensil(View v) {
        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_15;
        imgSmoothLineDraw15.setImageResource(R.drawable.select_icon_02_icon);
        imgSmoothLineDraw20.setImageResource(R.drawable.unselect_icon_01_icon);
        imgSmoothLineDraw10.setImageResource(R.drawable.unselect_icon_03_icon);
        imgSmoothLineDraw5.setImageResource(R.drawable.unselect_icon_04_icon);
        imgSmoothLineDraw2.setImageResource(R.drawable.unselect_icon_05_icon);
    }

    public void setFreeShape3Pensil(View v) {
        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_10;
        imgSmoothLineDraw10.setImageResource(R.drawable.select_icon_03_icon);
        imgSmoothLineDraw15.setImageResource(R.drawable.unselect_icon_02_icon);
        imgSmoothLineDraw20.setImageResource(R.drawable.unselect_icon_01_icon);
        imgSmoothLineDraw5.setImageResource(R.drawable.unselect_icon_04_icon);
        imgSmoothLineDraw2.setImageResource(R.drawable.unselect_icon_05_icon);
    }

    public void setFreeShape4Pensil(View v) {
        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_5;
        imgSmoothLineDraw5.setImageResource(R.drawable.select_icon_04_icon);
        imgSmoothLineDraw15.setImageResource(R.drawable.unselect_icon_02_icon);
        imgSmoothLineDraw10.setImageResource(R.drawable.unselect_icon_03_icon);
        imgSmoothLineDraw20.setImageResource(R.drawable.unselect_icon_01_icon);
        imgSmoothLineDraw2.setImageResource(R.drawable.unselect_icon_05_icon);
    }

    public void setFreeShape5Pensil(View v) {
        DrawingView.STROCK_WIDTH_SMOOTHLINE = Constants.SMOOTHLINE_2;
        imgSmoothLineDraw2.setImageResource(R.drawable.select_icon_05_icon);
        imgSmoothLineDraw15.setImageResource(R.drawable.unselect_icon_02_icon);
        imgSmoothLineDraw10.setImageResource(R.drawable.unselect_icon_03_icon);
        imgSmoothLineDraw5.setImageResource(R.drawable.unselect_icon_04_icon);
        imgSmoothLineDraw20.setImageResource(R.drawable.unselect_icon_01_icon);
    }

    public void setOnlySpecificAngleLine(View v) {
        //set angle of line here
        if (!isSpecificDirectionLine) {
            isSpecificDirectionLine = true;
            imgLineDrawAtAngle.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            if (isLineConnectorOn) {
                isLineConnectorOn = false;
                imgLineConnector.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            }
        } else {
            isSpecificDirectionLine = false;
            imgLineDrawAtAngle.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        }
    }

    public void setLineConnector(View v) {
        if (isLineConnectorOn) {
            isLineConnectorOn = false;
            imgLineConnector.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            isLineConnectorOn = true;
            imgLineConnector.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            if (isSpecificDirectionLine) {
                isSpecificDirectionLine = false;
                imgLineDrawAtAngle.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public void setRectangleAlignment(View v) {
        if (isRectCenterAlign) {
            imgSquareDrawAlignment.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            imgSquareDrawAlignment.setImageResource(R.drawable.not_align_icon);
            isRectCenterAlign = false;
        } else {
            imgSquareDrawAlignment.setImageResource(R.drawable.align_center_icon);
            imgSquareDrawAlignment.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            isRectCenterAlign = true;
        }
    }

    public void setCircleAlignment(View v) {
       /* if (popupWindow != null && popupWindow.isShowing())
            popupWindow.dismiss();*/
        //showAlignMentPopup(ConstHelper.IS_CIRCLE_ALIGNMENT_POPUP);
        if (isCircleCenterAlign) {
            imgCircleDrawAlignment.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            imgCircleDrawAlignment.setImageResource(R.drawable.not_align_icon);
            isCircleCenterAlign = false;
        } else {
            imgCircleDrawAlignment.setImageResource(R.drawable.align_center_icon);
            imgCircleDrawAlignment.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            isCircleCenterAlign = true;
        }
    }

    private void clickListnerOfPopUp() {
        imgSelectionSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutSquare.setVisibility(View.VISIBLE);
                linearLayoutLines.setVisibility(GONE);
                linearLayoutCircles.setVisibility(GONE);
                linearLayoutPensil.setVisibility(GONE);
                imgSelectedShape.setImageResource(R.drawable.plain_square);
                popupWindow.dismiss();
                imgSelectionSquare.setVisibility(GONE);
                imgSelectionLine.setVisibility(View.VISIBLE);
                imgSelectionCircle.setVisibility(View.VISIBLE);
                imgSelectionPensil.setVisibility(View.VISIBLE);

                mDrawingView.mCurrentShape = DrawingView.RECTANGLE;
                mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);

            }
        });

        imgSelectionLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutLines.setVisibility(View.VISIBLE);
                linearLayoutSquare.setVisibility(GONE);
                linearLayoutCircles.setVisibility(GONE);
                linearLayoutPensil.setVisibility(GONE);
                imgSelectedShape.setImageResource(R.drawable.line_img);
                popupWindow.dismiss();
                imgSelectionLine.setVisibility(GONE);
                imgSelectionSquare.setVisibility(View.VISIBLE);
                imgSelectionCircle.setVisibility(View.VISIBLE);
                imgSelectionPensil.setVisibility(View.VISIBLE);

                mDrawingView.mCurrentShape = DrawingView.LINE;
                mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);


            }
        });
        imgSelectionCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutCircles.setVisibility(View.VISIBLE);
                linearLayoutSquare.setVisibility(GONE);
                linearLayoutLines.setVisibility(GONE);
                linearLayoutPensil.setVisibility(GONE);
                imgSelectedShape.setImageResource(R.drawable.unselect_icon_01_icon);
                popupWindow.dismiss();
                imgSelectionCircle.setVisibility(GONE);
                imgSelectionLine.setVisibility(View.VISIBLE);
                imgSelectionSquare.setVisibility(View.VISIBLE);
                imgSelectionPensil.setVisibility(View.VISIBLE);

                mDrawingView.mCurrentShape = DrawingView.CIRCLE;
                mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);


            }
        });
        imgSelectionPensil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutPensil.setVisibility(View.VISIBLE);
                linearLayoutSquare.setVisibility(GONE);
                linearLayoutLines.setVisibility(GONE);
                linearLayoutCircles.setVisibility(GONE);
                imgSelectedShape.setImageResource(R.drawable.pencil_large);
                popupWindow.dismiss();
                imgSelectionPensil.setVisibility(GONE);
                imgSelectionLine.setVisibility(View.VISIBLE);
                imgSelectionCircle.setVisibility(View.VISIBLE);
                imgSelectionSquare.setVisibility(View.VISIBLE);
                mDrawingView.mCurrentShape = DrawingView.SMOOTHLINE;
                mDrawingView.reset(layoutAllShapeInclude, layoutShapeClick, popupWindow);
            }
        });
    }

    private void undoOperation() {
        mDrawingView.undo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageUri", imageURI);
    }


}
