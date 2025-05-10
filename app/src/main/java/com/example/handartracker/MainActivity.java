package com.example.handartracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private HandTrackingView handTrackingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vérifier la permission caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            setupHandTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void setupHandTracking() {
        FrameLayout container = findViewById(R.id.tracking_container);

        // Créer et ajouter la vue de suivi
        handTrackingView = new HandTrackingView(this);
        container.addView(handTrackingView);

        Toast.makeText(this, "Pointez la caméra vers votre main", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupHandTracking();
            } else {
                Toast.makeText(this, "La permission caméra est requise", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handTrackingView != null) {
            handTrackingView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handTrackingView != null) {
            handTrackingView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handTrackingView != null) {
            handTrackingView.onDestroy();
        }
    }
}