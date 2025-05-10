package com.example.handartracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import com.example.handartracker.models.HandLandmark;
import com.google.ar.core.Frame;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HandTrackingProcessor {
    private static final String TAG = "HandTrackingProcessor";

    private final Context context;
    private HandLandmarker handLandmarker;
    private OnHandLandmarksListener listener;
    private boolean isProcessing = false;

    public interface OnHandLandmarksListener {
        void onHandLandmarksDetected(List<HandLandmark> landmarks);
    }

    public HandTrackingProcessor(Context context) {
        this.context = context;
        setupMediaPipe();
    }

    private void setupMediaPipe() {
        try {
            // Configuration du détecteur de main MediaPipe
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .build();

            HandLandmarker.HandLandmarkerOptions options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.IMAGE)
                    .setNumHands(1)
                    .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);
            Log.d(TAG, "MediaPipe Hand Landmarker initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up MediaPipe: " + e.getMessage());
        }
    }

    public void processARFrame(Frame frame) {
        if (handLandmarker == null || isProcessing) {
            return;
        }

        isProcessing = true;

        try {
            Image image = frame.acquireCameraImage();
            Bitmap bitmap = imageToBitmap(image);
            image.close();

            if (bitmap != null) {
                MPImage mpImage = new BitmapImageBuilder(bitmap).build();

                // Exécuter la détection des landmarks
                HandLandmarkerResult result = handLandmarker.detect(mpImage);

                // Convertir le résultat en liste de points
                List<HandLandmark> handLandmarks = new ArrayList<>();
                if (!result.landmarks().isEmpty()) {
                    for (int i = 0; i < result.landmarks().get(0).size(); i++) {
                        NormalizedLandmark landmark =
                                result.landmarks().get(0).get(i);
                        handLandmarks.add(new HandLandmark(
                                landmark.x(),
                                landmark.y(),
                                landmark.z(),
                                i
                        ));
                    }

                    // Notifier le listener des landmarks détectés
                    if (listener != null) {
                        listener.onHandLandmarksDetected(handLandmarks);
                    }
                }
            }
        } catch (NotYetAvailableException e) {
            Log.d(TAG, "Camera image not available yet");
        } catch (Exception e) {
            Log.e(TAG, "Error processing AR frame: " + e.getMessage());
        } finally {
            isProcessing = false;
        }
    }

    private Bitmap imageToBitmap(Image image) {
        if (image == null) {
            return null;
        }

        try {
            // Implémentation pour convertir YUV_420_888 à RGBA
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();
            out.close();

            return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to bitmap: " + e.getMessage(), e);
            return null;
        }
    }

    public void setHandLandmarksListener(OnHandLandmarksListener listener) {
        this.listener = listener;
    }

    public void close() {
        if (handLandmarker != null) {
            handLandmarker.close();
            handLandmarker = null;
        }
    }
}