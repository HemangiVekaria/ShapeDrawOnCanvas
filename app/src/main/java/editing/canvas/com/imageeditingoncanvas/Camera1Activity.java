package editing.canvas.com.imageeditingoncanvas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.editing.canvas.library.util.Constants;
import com.editing.canvas.library.util.FileUtils;
import com.editing.canvas.library.views.AutoFitTextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;


public class Camera1Activity extends AppCompatActivity {

    AutoFitTextureView mTextureView;
    ImageView imgFrontBackCamera, imgFlash, imgCapture;
    RelativeLayout relBottomCameraOptionView;  // view which contain option like flash , front  back camera , capture button
    private Camera mCamera;
    private Camera.CameraInfo mBackCameraInfo;
    private Camera.ShutterCallback myShutterCallback;
    private Camera.PictureCallback myPictureCallback_RAW;
    private Camera.PictureCallback myPictureCallback_JPG;
    private Camera.CameraInfo mCameraInfo;
    private SurfaceTexture surfaceTemp;
    private OrientationEventListener mOrientationEventListener;

    Camera.CameraInfo currentCamInfo;
    private int mCameraId;
    private int mFacing;
    private int camBackId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int camFrontId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public static final int FACING_BACK = 0;
    public static final int FACING_FRONT = 1;
    private boolean isFlashOn = false;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        init();

        mTextureView.setSurfaceTextureListener(texturelistener);

        myPictureCallback_RAW = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
            }
        };
        myPictureCallback_JPG = new Camera.PictureCallback() {

            public void onPictureTaken(final byte[] data1, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String date = Constants.DATE_FORMAT.format(new Date());
                        String photoFile = "Picture_" + date + ".jpg";
                        File pictureFile = FileUtils.Instance(getApplicationContext()).getCameraImageSavePath(photoFile);

                        Bitmap realImage = null;
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            realImage = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                            ExifInterface exif = new ExifInterface(pictureFile.toString());
                            Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                                realImage = rotate(realImage, 90);
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                                realImage = rotate(realImage, 270);
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                                realImage = rotate(realImage, 180);
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                                if (mFacing == FACING_FRONT)
                                    realImage = rotate(realImage, 0);
                                else if (mFacing == FACING_BACK)
                                    realImage = rotate(realImage, 180);
                            }
                            realImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("Info", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("TAG", "Error accessing file: " + e.getMessage());
                        }
                        if (mCamera != null)
                            mCamera.stopPreview();
                        Intent intent = new Intent();
                        intent.setData(Uri.fromFile(pictureFile));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }).start();
            }

            public Bitmap rotate(Bitmap bitmap, int degree) {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Matrix mtx = new Matrix();
                mtx.setRotate(degree);
                return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
            }
        };

    }

    TextureView.SurfaceTextureListener texturelistener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Pair<Camera.CameraInfo, Integer> backCamera = getBackCamera();
            if (backCamera != null) {
                mBackCameraInfo = backCamera.first;
                surfaceTemp = surface;
                openCamera();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void init() {
        mTextureView = findViewById(R.id.textureview);
        imgFrontBackCamera = findViewById(R.id.imgFrontBackCamera);
        imgFlash = findViewById(R.id.imgflash);
        imgCapture = findViewById(R.id.captureCircle);
        relBottomCameraOptionView = findViewById(R.id.relativeCameraOptionsView);

        if (checkCameraRear())
            imgFrontBackCamera.setVisibility(View.VISIBLE);
        else imgFrontBackCamera.setVisibility(View.INVISIBLE);

        setFacing(FACING_BACK);

        if (isFlashAvailable())
            imgFlash.setVisibility(View.VISIBLE);
        else imgFlash.setVisibility(View.INVISIBLE);
    }


    /**
     * All click events
     *
     * @param v
     */

    public void onFlashClick(View v) {
        if (isFlashOn) {
            imgFlash.setColorFilter(ContextCompat.getColor(getApplicationContext(), com.editing.canvas.library.R.color.white), PorterDuff.Mode.SRC_IN);
            turnOffFlash();
        } else {
            imgFlash.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            turnOnFlash();
        }
    }

    public void onFrontBackCameraClick(View v) {
        if (mFacing == FACING_BACK) {
            setFacing(FACING_FRONT);
            openCamera();
        } else {
            openCamera();
            setFacing(FACING_BACK);
        }
    }

    public void onCaptureClick(View v) {
        if (mCamera != null) {
            mCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, myPictureCallback_RAW, myPictureCallback_JPG);
        }
    }

    /**
     * Camera open Close
     *
     * @return
     */

    // check for rear camera available or not
    public static boolean checkCameraRear() {
        int numCamera = Camera.getNumberOfCameras();
        if (numCamera > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void openCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        currentCamInfo = new Camera.CameraInfo();
        try {
            if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFacing = FACING_FRONT;
                mCamera = Camera.open(camFrontId);
            } else {
                mFacing = FACING_BACK;
                mCamera = Camera.open(camBackId);
            }
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, currentCamInfo);
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; //Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; //Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;//Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;//Landscape right
            }
            int rotate = (currentCamInfo.orientation - degrees + 360) % 360;

            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();

            int MaxWidth = 0;
            int maxHeight = 0;
            Camera.Size mSize = null;
            for (Camera.Size size : sizes) {
                mSize = size;
                if (mSize.width >= MaxWidth)
                    MaxWidth = mSize.width;
                if (mSize.height >= maxHeight)
                    maxHeight = mSize.height;
            }

            params.setPreviewSize(MaxWidth, maxHeight);
            //   params.setPictureSize(MaxWidth, maxHeight);//for getting full resolution image
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

            params.setRotation(rotate);
            mCamera.setPreviewTexture(surfaceTemp);
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();

            cameraDisplayRotation();
        } catch (Exception e) {
            Log.e(getString(com.editing.canvas.library.R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void cameraDisplayRotation() {
        final int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        final int displayOrientation = (mBackCameraInfo.orientation - degrees + 360) % 360;
        mCamera.setDisplayOrientation(displayOrientation);
    }

    private Pair<Camera.CameraInfo, Integer> getBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return new Pair<Camera.CameraInfo, Integer>(cameraInfo,
                        Integer.valueOf(i));
            }
        }
        return null;
    }

    boolean isCameraOpened() {
        return mCamera != null;
    }

    /**
     * Turn ON OFF Flash stuff here
     */
    private void turnOnFlash() {
        isFlashOn = true;
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        mCamera.setParameters(p);
        mCamera.startPreview();
    }

    private void turnOffFlash() {
        isFlashOn = false;
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(p);
        mCamera.startPreview();
    }

    private boolean isFlashAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    // set front or back face camera
    void setFacing(int facing) {
        int internalFacing = new Facing(facing).map();
        if (internalFacing == -1) {
            return;
        }
        mCameraInfo = new Camera.CameraInfo();
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == internalFacing) {
                mCameraId = i;
                mFacing = facing;
                break;
            }
        }
        if (mFacing == facing && isCameraOpened()) {
            openCamera();
        }
    }

    private abstract static class BaseMapper<T> {

        protected int mCameraKitConstant;

        protected BaseMapper(int cameraKitConstant) {
            this.mCameraKitConstant = cameraKitConstant;
        }

        abstract T map();

    }

    static class Facing extends BaseMapper<Integer> {

        private static final SparseArrayCompat<Integer> FACING_MODES = new SparseArrayCompat<>();

        static {
            FACING_MODES.put(FACING_BACK, Camera.CameraInfo.CAMERA_FACING_BACK);
            FACING_MODES.put(FACING_FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        protected Facing(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        Integer map() {
            return FACING_MODES.get(mCameraKitConstant, FACING_MODES.get(FACING_BACK));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null)
            mCamera.startPreview();
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(this,
                    SensorManager.SENSOR_DELAY_NORMAL) {
                private int mOrientation;

                @Override
                public void onOrientationChanged(int orientation) {
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != Surface.ROTATION_0) {
                            mOrientation = Surface.ROTATION_0;
                        }
                    } else if (orientation >= 45 && orientation < 135) {
                        if (mOrientation != Surface.ROTATION_90) {
                            mOrientation = Surface.ROTATION_90;
                        }
                    } else if (orientation >= 135 && orientation < 225) {
                        if (mOrientation != Surface.ROTATION_180) {
                            mOrientation = Surface.ROTATION_180;
                        }
                    } else if (mOrientation != Surface.ROTATION_270) {
                        mOrientation = Surface.ROTATION_270;
                    }

                    if (lastOrientation != mOrientation) {
                        Log.d("!!!!", "rotation!!! lastOrientation:"
                                + lastOrientation + " mOrientation:"
                                + mOrientation + " orientaion:"
                                + orientation);
                    }
                }
            };
        }

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationEventListener.disable();
    }

    public void hideShowViewOfPicture() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
        relBottomCameraOptionView.setVisibility(View.VISIBLE);
        if (isFlashAvailable())
            imgFlash.setVisibility(View.VISIBLE);
        else imgFlash.setVisibility(View.INVISIBLE);
        if (checkCameraRear())
            imgFrontBackCamera.setVisibility(View.VISIBLE);
        else imgFrontBackCamera.setVisibility(View.INVISIBLE);
    }

}



