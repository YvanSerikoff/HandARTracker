package com.example.handartracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.handartracker.models.HandLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandTrackingView extends FrameLayout {
    private static final String TAG = "HandTrackingView";
    
    private final TextureView cameraPreview;
    private final HandPointsView pointsView;
    
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    
    // Pour MediaPipe
    private HandLandmarkerHelper handLandmarkerHelper;
    private List<HandLandmark> detectedLandmarks = new ArrayList<>();
    
    public HandTrackingView(Context context) {
        super(context);
        
        // Configurer la prévisualisation de la caméra
        cameraPreview = new TextureView(context);
        cameraPreview.setSurfaceTextureListener(surfaceTextureListener);
        addView(cameraPreview);
        
        // Configurer la vue pour les points
        pointsView = new HandPointsView(context);
        addView(pointsView);
        
        // Initialiser MediaPipe Hand Landmarker
        handLandmarkerHelper = new HandLandmarkerHelper(context, 
                landmarks -> {
                    detectedLandmarks = landmarks;
                    pointsView.updateLandmarks(landmarks);
                    pointsView.invalidate();
                });
    }
    
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        
        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            // Gérer le changement de taille
        }
        
        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return true;
        }
        
        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // Traiter chaque frame
            if (handLandmarkerHelper != null && cameraPreview.getBitmap() != null) {
                handLandmarkerHelper.detectLandmarks(cameraPreview.getBitmap());
            }
        }
    };
    
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0]; // Caméra arrière
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession();
                }
                
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }
                
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                }
            }, backgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
        }
    }
    
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            Surface surface = new Surface(texture);
            
            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    // Gérer l'échec
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    public void onResume() {
        startBackgroundThread();
        if (cameraPreview.isAvailable()) {
            openCamera();
        }
    }
    
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }
    
    public void onDestroy() {
        if (handLandmarkerHelper != null) {
            handLandmarkerHelper.close();
        }
    }
    
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    
    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    
    // Vue pour dessiner les points
    private static class HandPointsView extends View {
        private final Paint paint = new Paint();
        private List<HandLandmark> landmarks = new ArrayList<>();
        
        public HandPointsView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            setWillNotDraw(false);
        }
        
        public void updateLandmarks(List<HandLandmark> landmarks) {
            this.landmarks = landmarks;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            if (landmarks.isEmpty()) {
                return;
            }
            
            int width = getWidth();
            int height = getHeight();
            
            for (HandLandmark landmark : landmarks) {
                // Convertir les coordonnées normalisées
                float x = landmark.getX() * width;
                float y = landmark.getY() * height;
                
                // Choisir la couleur selon l'index
                int index = landmark.getIndex();
                if (index >= 0 && index <= 4) {
                    paint.setColor(Color.RED); // Pouce
                } else if (index >= 5 && index <= 8) {
                    paint.setColor(Color.GREEN); // Index
                } else if (index >= 9 && index <= 12) {
                    paint.setColor(Color.BLUE); // Majeur
                } else if (index >= 13 && index <= 16) {
                    paint.setColor(Color.YELLOW); // Annulaire
                } else if (index >= 17 && index <= 20) {
                    paint.setColor(Color.MAGENTA); // Auriculaire
                } else {
                    paint.setColor(Color.WHITE); // Paume
                }
                
                canvas.drawCircle(x, y, 15, paint);
            }
        }
    }
}