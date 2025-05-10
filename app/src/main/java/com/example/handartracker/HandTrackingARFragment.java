package com.example.handartracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.handartracker.models.HandLandmark;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.ArrayList;
import java.util.List;

public class HandTrackingARFragment extends Fragment {
    private static final String TAG = "HandTrackingARFragment";

    private HandTrackingProcessor handTrackingProcessor;
    private Session arSession;
    private ARSurfaceView arSurfaceView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Créer un FrameLayout pour contenir la vue AR
        FrameLayout frameLayout = new FrameLayout(requireContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Initialiser le processeur de suivi de la main
        handTrackingProcessor = new HandTrackingProcessor(requireContext());

        // Configurer ARCore
        setupARCore();

        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (arSession != null) {
            arSurfaceView = new ARSurfaceView(requireContext(), arSession);
            ((FrameLayout) view).addView(arSurfaceView);

            // Configurer le callback pour les frames
            arSurfaceView.setFrameCallback(frame -> {
                if (frame != null) {
                    handTrackingProcessor.processARFrame(frame);
                }
            });

            // Configurer le callback pour les landmarks
            handTrackingProcessor.setHandLandmarksListener(landmarks -> {
                if (arSurfaceView != null) {
                    arSurfaceView.updateHandLandmarks(landmarks);
                }
            });
        }
    }

    private void setupARCore() {
        // Vérifier la disponibilité d'ARCore
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(requireContext());
        if (availability.isSupported()) {
            try {
                // Créer la session AR
                arSession = new Session(requireContext());

                // Configurer la session
                Config config = new Config(arSession);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                config.setFocusMode(Config.FocusMode.AUTO);
                config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                arSession.configure(config);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (arSession != null) {
            try {
                arSession.resume();
                if (arSurfaceView != null) {
                    arSurfaceView.onResume();
                }
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSession != null) {
            arSession.pause();
            if (arSurfaceView != null) {
                arSurfaceView.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSession != null) {
            arSession.close();
            arSession = null;
        }
        if (handTrackingProcessor != null) {
            handTrackingProcessor.close();
        }
    }
}