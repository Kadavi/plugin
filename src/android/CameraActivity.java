package org.schoolsfirstfcu.mobile.plugin.checkcapture;

import java.io.InputStream;
import java.util.List;

import org.apache.cordova.LOG;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    
    private static final String FOCUS_MODE_AUTO = android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
    private static final String FLASH_MODE_AUTO = android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
    
    private Camera camera;
    private RelativeLayout layout;
    private FrameLayout cameraPreviewView;
    private TextView headerText;
    private TextView titleText;
    private TextView cancelText;
    private ImageButton captureButton;
    private ProgressDialog progressDlg;
    private Bitmap lightButton, darkButton;
    private MediaPlayer shootMP;
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            openCamera();
            configureCamera();
            displayCameraPreview();
        } catch (Exception ex) {
            finishWithError("Camera is not accessible", ex);
        }
    }
    
    private void configureCamera() {
        
        Camera.Parameters cameraSettings = camera.getParameters();
        cameraSettings.setJpegQuality(100);
        cameraSettings.setFocusMode(FOCUS_MODE_AUTO);
        cameraSettings.setFlashMode(FLASH_MODE_AUTO);
        
        camera.setParameters(cameraSettings);
    }
    
    private void displayCameraPreview() {
        cameraPreviewView.removeAllViews();
        cameraPreviewView.addView(new CameraPreview(this, camera));
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                                   LayoutParams.MATCH_PARENT);
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
        cameraPreviewView.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent e){
                camera.autoFocus(null);
                return true;
            }
        });
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
        RelativeLayout.LayoutParams headerLayoutParams = new RelativeLayout.LayoutParams(pixelsToDp(HEADER_HEIGHT),
                                                                                         LayoutParams.MATCH_PARENT);
        headerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        headerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        View headerView = new View(this);
        headerView.setBackgroundColor(0xFF2D4452);
        headerView.setLayoutParams(headerLayoutParams);
        layout.addView(headerView);
        
        // Header Message
        RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                                       LayoutParams.MATCH_PARENT);
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
        RelativeLayout.LayoutParams bottomPaneLayoutParams = new RelativeLayout.LayoutParams(
                                                                                             pixelsToDp(FRAME_BORDER_SIZE), getScreenHeightInPixels());
        View bottomPaneView = new View(this);
        bottomPaneView.setBackgroundColor(0xFFB9C7D4);
        bottomPaneView.setLayoutParams(bottomPaneLayoutParams);
        layout.addView(bottomPaneView);
        
        // Top Pane
        RelativeLayout.LayoutParams topPaneLayoutParams = new RelativeLayout.LayoutParams(
                                                                                          pixelsToDp(FRAME_BORDER_SIZE), getScreenHeightInPixels());
        topPaneLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        topPaneLayoutParams.rightMargin = pixelsToDp(HEADER_HEIGHT);
        View topPaneView = new View(this);
        topPaneView.setBackgroundColor(0xFFB9C7D4);
        topPaneView.setLayoutParams(topPaneLayoutParams);
        layout.addView(topPaneView);
        
        // Front/Back Title
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                                        LayoutParams.MATCH_PARENT);
        titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleLayoutParams.rightMargin = pixelsToDp(HEADER_HEIGHT + 6);
        titleLayoutParams.topMargin = pixelsToDp(FRAME_BORDER_SIZE);
        titleText = new VerticalTextView(this);
        titleText.setTextColor(Color.parseColor("#000000"));
        titleText.setText(getIntent().getStringExtra(TITLE));
        titleText.setLayoutParams(titleLayoutParams);
        layout.addView(titleText);
        
        // Cancel Button
        RelativeLayout.LayoutParams cancelLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                                         LayoutParams.MATCH_PARENT);
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
                finishWithError("no image selected", new Exception());
            }
        });
        layout.addView(cancelText);
        
        CropMarks cropMarks = new CropMarks(this);
        cropMarks.draw(getScreenWidthInPixels(), getScreenHeightInPixels(), FRAME_BORDER_SIZE, HEADER_HEIGHT);
        layout.addView(cropMarks);
    }
    
    public void shootSound()
    {
        AudioManager meng = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = meng.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        if (currentVolume != 0)
        {
            if (shootMP == null) {
                shootMP = MediaPlayer.create(getBaseContext(), Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                int maxVolume = meng.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float percent = 0.7f;
                int seventyVolume = (int) (maxVolume * percent);
                meng.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
            }
            
            if (shootMP != null){
                
                shootMP.start();
            }
            
        }
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
        
        lightButton = Bitmap.createScaledBitmap(lightButton, pixelsToDp(FRAME_BORDER_SIZE) * 2,
                                                pixelsToDp(FRAME_BORDER_SIZE) * 2, false);
        darkButton = Bitmap.createScaledBitmap(darkButton, pixelsToDp(FRAME_BORDER_SIZE) * 2,
                                               pixelsToDp(FRAME_BORDER_SIZE) * 2, false);
        
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
    
    private void takePictureWithAutoFocus() {
        try{
            Camera.Parameters cameraSettings = camera.getParameters();
            List<String> supportedFocusModes = cameraSettings.getSupportedFocusModes();
            if (supportedFocusModes.contains(FOCUS_MODE_AUTO)) {
                camera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            shootSound();
                            captureButton.setEnabled(false);
                            Log.d(TAG, "calling takePicture()");
                            takePicture();
                            camera.cancelAutoFocus();
                        }
                    }
                });
            }
        } catch (Exception ex) {
            finishWithError("Failed to take image.", ex);
        }
    }
    
    private void takePicture() {
        try {
            camera.takePicture(null, null, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] jpegData, Camera camera) {
                    int targetWidth = getIntent().getIntExtra(TARGET_WIDTH, 1600);
                    int targetHeight = getIntent().getIntExtra(TARGET_HEIGHT, 1200);
                    int picQuality = getIntent().getIntExtra(QUALITY, 30);
                    ProcessImageTask processImageTask = new ProcessImageTask(targetWidth, targetHeight, picQuality, new ProcessImageListener(){
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
