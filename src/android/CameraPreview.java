package org.schoolsfirstfcu.mobile.plugin.checkcapture;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("ClickableViewAccessibility")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private final CameraActivity cameraActivity;
    
    public CameraPreview(Context context) {
        super(context);
        this.cameraActivity = (CameraActivity) context;
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        getHolder().addCallback(this);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            cameraActivity.getCamera().setPreviewDisplay(holder);
            cameraActivity.getCamera().setDisplayOrientation(90);
            cameraActivity.getCamera().startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (getHolder().getSurface() == null) {
            return;
        }
        Camera camera = cameraActivity.getCamera();
        try {
            camera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "Tried to stop a non-existent preview: " + e.getMessage());
        }
        
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && !cameraActivity.isInprogress()) {
            cameraActivity.takePictureWithAutoFocus();
        }
        return true;
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        
    }
    
}
