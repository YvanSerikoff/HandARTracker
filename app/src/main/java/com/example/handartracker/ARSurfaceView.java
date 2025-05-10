package com.example.handartracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.handartracker.models.HandLandmark;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.List;

public class ARSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "ARSurfaceView";
    
    private final Session arSession;
    private final Paint pointPaint;
    private final SurfaceHolder surfaceHolder;
    private boolean isRunning = false;
    private FrameCallback frameCallback;
    private List<HandLandmark> landmarks = new ArrayList<>();
    
    public interface FrameCallback {
        void onFrame(Frame frame);
    }
    
    public ARSurfaceView(Context context, Session session) {
        super(context);
        this.arSession = session;
        
        // Initialiser la peinture pour les points
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
        
        // Configurer le SurfaceHolder
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
        // Rendre cette surface transparente
        setZOrderOnTop(true);
        surfaceHolder.setFormat(android.graphics.PixelFormat.TRANSLUCENT);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        
        // Démarrer le thread de rendu
        new Thread(() -> {
            while (isRunning) {
                if (!holder.getSurface().isValid()) {
                    continue;
                }
                
                try {
                    // Capturer la frame AR
                    Frame frame = arSession.update();
                    if (frameCallback != null) {
                        frameCallback.onFrame(frame);
                    }
                    
                    // Dessiner les landmarks
                    Canvas canvas = holder.lockCanvas();
                    if (canvas != null) {
                        try {
                            // Effacer le canvas
                            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                            
                            // Dessiner les landmarks
                            drawHandLandmarks(canvas);
                        } finally {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                    
                    // Limiter le taux de rafraîchissement
                    Thread.sleep(16); // ~60 FPS
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Gérer les changements de taille de surface
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }
    
    public void setFrameCallback(FrameCallback callback) {
        this.frameCallback = callback;
    }
    
    public void updateHandLandmarks(List<HandLandmark> landmarks) {
        this.landmarks = new ArrayList<>(landmarks);
    }
    
    private void drawHandLandmarks(Canvas canvas) {
        if (landmarks == null || landmarks.isEmpty()) {
            return;
        }
        
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        
        for (HandLandmark landmark : landmarks) {
            // Convertir les coordonnées normalisées en coordonnées d'écran
            float x = landmark.getX() * width;
            float y = landmark.getY() * height;
            
            // Choisir la couleur en fonction de l'index
            int index = landmark.getIndex();
            if (index >= 0 && index <= 4) {
                pointPaint.setColor(Color.RED); // Pouce
            } else if (index >= 5 && index <= 8) {
                pointPaint.setColor(Color.GREEN); // Index
            } else if (index >= 9 && index <= 12) {
                pointPaint.setColor(Color.BLUE); // Majeur
            } else if (index >= 13 && index <= 16) {
                pointPaint.setColor(Color.YELLOW); // Annulaire
            } else if (index >= 17 && index <= 20) {
                pointPaint.setColor(Color.MAGENTA); // Auriculaire
            } else {
                pointPaint.setColor(Color.WHITE); // Paume
            }
            
            // Dessiner un cercle pour représenter le landmark
            canvas.drawCircle(x, y, 15, pointPaint);
        }
    }
    
    public void onResume() {
        isRunning = true;
    }
    
    public void onPause() {
        isRunning = false;
    }
}