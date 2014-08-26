package org.schoolsfirstfcu.mobile.plugin.checkcapture;

import java.io.InputStream;
import java.util.List;

import org.apache.cordova.LOG;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CameraActivity extends Activity {
    
    private static final String TAG = CameraActivity.class.getSimpleName();
    
    public static String TITLE = "Title";
    public static String QUALITY = "Quality";
    public static String TARGET_WIDTH = "TargetWidth";
    public static String TARGET_HEIGHT = "TargetHeight";
    public static String LOGO_FILENAME = "LogoFilename";
    public static String DESCRIPTION = "Description";
    public static String IMAGE_DATA = "ImageData";
    public static String ERROR_MESSAGE = "ErrorMessage";
    public static int RESULT_ERROR = 2;
    
    private static final int HEADER_HEIGHT = 54;
    private static final int FRAME_BORDER_SIZE = 34;
    
    private Camera camera;
    private RelativeLayout layout;
    private FrameLayout cameraPreviewView;
    private TextView headerText;
    private TextView titleText;
    private TextView cancelText;
    private ImageButton captureButton;
    private ProgressDialog progressDlg;
    private Bitmap lightButton, darkButton;
    private int autoFocusErrCounter;
    
    public CameraActivity() {
        autoFocusErrCounter = 0;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            openCamera();
            displayCameraPreview();
        } catch (Exception ex) {
            finishWithError("Camera is not accessible.", ex);
        }
    }
    
    private void configureCamera() {
        Camera.Parameters cameraSettings = getCamera().getParameters();
        cameraSettings.setJpegQuality(100);
        cameraSettings.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        cameraSettings.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        setZoom(cameraSettings);
        getCamera().setParameters(cameraSettings);
    }
    
    private void setZoom(Camera.Parameters cameraSettings) {
        // zoom in if supported
        if (cameraSettings.isZoomSupported()) {
            List<Integer> zoomRatios = cameraSettings.getZoomRatios();
            int zoomTotIdx = zoomRatios.size();
            if (zoomTotIdx / 5 < zoomTotIdx) {
                int newZoomIndex = (int) (zoomTotIdx * 0.05);
                cameraSettings.setZoom(newZoomIndex);
            }
        }
    }
    
    private void displayCameraPreview() {
        cameraPreviewView.removeAllViews();
        cameraPreviewView.addView(new CameraPreview(this));
    }
    
    @Override
    protected void onPause() {
        try {
            super.onPause();
            releaseCamera();
        } catch (Exception ex) {
            Log.e(TAG, "Error:", ex);
        }
    }
    
    private void openCamera() {
        if (camera == null) {
            camera = Camera.open();
            configureCamera();
        }
    }
    
    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        
        createCameraPreview();
        createFrame();
        createCaptureButton();
        createProgressDialog();
        setContentView(layout);
    }
    
    private void createCameraPreview() {
        cameraPreviewView = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getScreenWidthInPixels()
                                                                             - pixelsToDp(HEADER_HEIGHT) - (pixelsToDp(FRAME_BORDER_SIZE) * 2), getScreenHeightInPixels()
                                                                             - (pixelsToDp(FRAME_BORDER_SIZE) * 3));
        cameraPreviewView.setLayoutParams(layoutParams);
        cameraPreviewView.setX(pixelsToDp(FRAME_BORDER_SIZE));
        cameraPreviewView.setY(pixelsToDp(FRAME_BORDER_SIZE));
        layout.addView(cameraPreviewView);
    }
    
    private void createProgressDialog() {
        progressDlg = new ProgressDialog(this);
        progressDlg.setTitle("Loading");
        progressDlg.setMessage("Please wait...");
        progressDlg.setIndeterminate(true);
        progressDlg.setCancelable(false);
    }
    
    private void createFrame() {
        // Header
        RelativeLayout.LayoutParams headerLayoutParams = new RelativeLayout.LayoutParams(pixelsToDp(HEADER_HEIGHT), LayoutParams.MATCH_PARENT);
        headerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        headerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        View headerView = new View(this);
        headerView.setBackgroundColor(0xFF2D4452);
        headerView.setLayoutParams(headerLayoutParams);
        layout.addView(headerView);
        
        // Header Message
        RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        logoLayoutParams.rightMargin = pixelsToDp(6);
        headerText = new VerticalTextView(this);
        headerText.setGravity(Gravity.CENTER);
        headerText.setText(getIntent().getStringExtra(DESCRIPTION));
        headerText.setLayoutParams(logoLayoutParams);
        layout.addView(headerText);
        
        // Left Pane
        RelativeLayout.LayoutParams leftPaneLayoutParams = new RelativeLayout.LayoutParams(getScreenWidthInPixels()
                                                                                           - pixelsToDp(HEADER_HEIGHT), pixelsToDp(FRAME_BORDER_SIZE));
        View leftPaneView = new View(this);
        leftPaneView.setBackgroundColor(0xFFB9C7D4);
        leftPaneView.setLayoutParams(leftPaneLayoutParams);
        layout.addView(leftPaneView);
        
        // Right Pane
        RelativeLayout.LayoutParams rightPaneLayoutParams = new RelativeLayout.LayoutParams(getScreenWidthInPixels()
                                                                                            - pixelsToDp(HEADER_HEIGHT), pixelsToDp(FRAME_BORDER_SIZE) * 2);
        rightPaneLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        View rightPaneView = new View(this);
        rightPaneView.setBackgroundColor(0xFFB9C7D4);
        rightPaneView.setLayoutParams(rightPaneLayoutParams);
        layout.addView(rightPaneView);
        
        // Bottom Pane
        RelativeLayout.LayoutParams bottomPaneLayoutParams = new RelativeLayout.LayoutParams(pixelsToDp(FRAME_BORDER_SIZE), getScreenHeightInPixels());
        View bottomPaneView = new View(this);
        bottomPaneView.setBackgroundColor(0xFFB9C7D4);
        bottomPaneView.setLayoutParams(bottomPaneLayoutParams);
        layout.addView(bottomPaneView);
        
        // Top Pane
        RelativeLayout.LayoutParams topPaneLayoutParams = new RelativeLayout.LayoutParams(pixelsToDp(FRAME_BORDER_SIZE), getScreenHeightInPixels());
        topPaneLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        topPaneLayoutParams.rightMargin = pixelsToDp(HEADER_HEIGHT);
        View topPaneView = new View(this);
        topPaneView.setBackgroundColor(0xFFB9C7D4);
        topPaneView.setLayoutParams(topPaneLayoutParams);
        layout.addView(topPaneView);
        
        // Front/Back Title
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleLayoutParams.rightMargin = pixelsToDp(HEADER_HEIGHT + 6);
        titleLayoutParams.topMargin = pixelsToDp(FRAME_BORDER_SIZE);
        titleText = new VerticalTextView(this);
        titleText.setTextColor(Color.parseColor("#000000"));
        titleText.setText(getIntent().getStringExtra(TITLE));
        titleText.setLayoutParams(titleLayoutParams);
        layout.addView(titleText);
        
        // Cancel Button
        RelativeLayout.LayoutParams cancelLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cancelLayoutParams.leftMargin = pixelsToDp(FRAME_BORDER_SIZE);
        cancelText = new TextView(this);
        cancelText.setY(getScreenHeightInPixels() - (pixelsToDp(FRAME_BORDER_SIZE) * 2) + pixelsToDp(20));
        cancelText.setTextColor(Color.parseColor("#FFFFFF"));
        cancelText.setTextSize(18);
        cancelText.setText("Cancel");
        cancelText.setLayoutParams(cancelLayoutParams);
        cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        layout.addView(cancelText);
        
        CropMarks cropMarks = new CropMarks(this);
        cropMarks.draw(getScreenWidthInPixels(), getScreenHeightInPixels(), FRAME_BORDER_SIZE, HEADER_HEIGHT);
        layout.addView(cropMarks);
    }
    
    private void createCaptureButton() {
        try {
            InputStream inputStream = getAssets().open("www/img/buttonup.png");
            lightButton = BitmapFactory.decodeStream(inputStream);
            inputStream = getAssets().open("www/img/buttondown.png");
            darkButton = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            LOG.e(ERROR_MESSAGE, "Button image(s) not found.");
        }
        
        lightButton = Bitmap.createScaledBitmap(lightButton, pixelsToDp(FRAME_BORDER_SIZE) * 2, pixelsToDp(FRAME_BORDER_SIZE) * 2, false);
        darkButton = Bitmap.createScaledBitmap(darkButton, pixelsToDp(FRAME_BORDER_SIZE) * 2, pixelsToDp(FRAME_BORDER_SIZE) * 2, false);
        
        captureButton = new ImageButton(this);
        captureButton.setImageBitmap(lightButton);
        captureButton.setBackgroundColor(Color.TRANSPARENT);
        captureButton.setX((getScreenWidthInPixels() - lightButton.getWidth() - pixelsToDp(HEADER_HEIGHT)) / 2);
        captureButton.setY(getScreenHeightInPixels() - lightButton.getHeight() - pixelsToDp(6));
        
        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setCaptureButtonImageForEvent(event);
                return false;
            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureWithAutoFocus();
            }
        });
        captureButton.setSoundEffectsEnabled(false);
        
        layout.addView(captureButton);
    }
    
    private void setCaptureButtonImageForEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            captureButton.setImageBitmap(darkButton);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            captureButton.setImageBitmap(lightButton);
        }
    }
    
    public Camera getCamera() {
        return camera;
    }
    private int getScreenWidthInPixels() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }
    
    private int getScreenHeightInPixels() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size.y;
    }
    
    private void adjustFocus() {
        Camera.Parameters settings = getCamera().getParameters();
        LOG.d(TAG, "Focus Error Counter:" + autoFocusErrCounter);
        if(autoFocusErrCounter == 0) {
            if (settings.isAutoWhiteBalanceLockSupported()) {
                settings.setAutoWhiteBalanceLock(true);
            }
            if (settings.isAutoExposureLockSupported()) {
                settings.setAutoExposureLock(true);
            }
            int maxExposure = settings.getMaxExposureCompensation();
            if (maxExposure > 0) {
                settings.setExposureCompensation((int)(maxExposure * 0.50));
            }
            getCamera().setParameters(settings);
            takePictureWithAutoFocus();
        } else if (autoFocusErrCounter == 1) {
            settings.setExposureCompensation(0);
            settings.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            getCamera().setParameters(settings);
            takePictureWithAutoFocus();
        } else {
            takePicture();
        }
        autoFocusErrCounter++;
    }
    public void takePictureWithAutoFocus() {
        try {
            Camera.Parameters cameraSettings = getCamera().getParameters();
            setInProgress(true);
            if (cameraSettings.getMaxNumFocusAreas() > 0) {
                getCamera().autoFocus(
                                      new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            Log.d(TAG, "Camera has autofocus and successful focus on the object");
                            takePicture();
                            autoFocusErrCounter = 0;
                        } else {
                            Log.d(TAG, "Camera has autofocus and failed focus on the object");
                            getCamera().cancelAutoFocus();
                            adjustFocus();
                        }
                    }
                });
            } else {
                takePicture();
            }
        } catch (Exception ex) {
            finishWithError("Failed to take image.", ex);
        }
    }
    
    public void setInProgress(boolean value) {
        // use captureButton state for now.
        captureButton.setEnabled(!value);
    }
    public boolean isInprogress() {
        return !captureButton.isEnabled();
    }
    private void takePicture() {
        try {
            getCamera().takePicture(
                                    new ShutterCallback(){
                @Override
                public void onShutter() {
                    setInProgress(true);
                }
            },
                                    null,
                                    new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] jpegData, Camera camera) {
                    Log.d(TAG, "Picture taken");
                    int targetWidth = getIntent().getIntExtra(TARGET_WIDTH, 1600);
                    int targetHeight = getIntent().getIntExtra(TARGET_HEIGHT, 1200);
                    int picQuality = getIntent().getIntExtra(QUALITY, 30);
                    ProcessImageTask processImageTask = new ProcessImageTask(targetWidth, targetHeight, picQuality, new ProcessImageListener() {
                        public void onStarted() {
                            progressDlg.show();
                        }
                        
                        public void onCompleted(String imageData) {
                            Intent data = new Intent();
                            data.putExtra(IMAGE_DATA, imageData);
                            
                            setResult(RESULT_OK, data);
                            progressDlg.dismiss();
                            finish();
                        }
                    });
                    processImageTask.execute(jpegData);
                }
            });
        } catch (Exception ex) {
            finishWithError("Failed to take image.", ex);
        }
    }
    
    private void dismiss() {
        Intent data = new Intent().putExtra(ERROR_MESSAGE, (String) null);
        setResult(RESULT_ERROR, data);
        finish();
    }
    
    private void finishWithError(String message, Exception ex) {
        if (ex != null) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        Intent data = new Intent().putExtra(ERROR_MESSAGE, message);
        setResult(RESULT_ERROR, data);
        finish();
    }
    
    private int pixelsToDp(int pixels) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(pixels * density);
    }
}