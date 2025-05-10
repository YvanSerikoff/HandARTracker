package com.example.handartracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.handartracker.models.HandLandmark;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HandLandmarkerHelper {
    private static final String TAG = "HandLandmarkerHelper";

    private final Context context;
    private HandLandmarker handLandmarker;
    private final LandmarkListener listener;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface LandmarkListener {
        void onLandmarksDetected(List<HandLandmark> landmarks);
    }

    public HandLandmarkerHelper(Context context, LandmarkListener listener) {
        this.context = context;
        this.listener = listener;
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

    public void detectLandmarks(final Bitmap bitmap) {
        if (handLandmarker == null) {
            return;
        }

        executor.execute(() -> {
            try {
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
                        listener.onLandmarksDetected(handLandmarks);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error detecting landmarks: " + e.getMessage());
            }
        });
    }

    public void close() {
        if (handLandmarker != null) {
            handLandmarker.close();
            handLandmarker = null;
        }
    }
}