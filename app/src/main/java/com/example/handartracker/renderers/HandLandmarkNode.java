package com.example.handartracker.renderers;

import android.content.Context;
import android.util.Log;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

public class HandLandmarkNode extends Node {
    private static final String TAG = "HandLandmarkNode";
    private static final float LANDMARK_RADIUS = 0.008f;  // 8mm de rayon pour les points

    private final Context context;
    private Renderable sphereRenderable;
    private final Vector3 position;
    private final int landmarkIndex;

    public HandLandmarkNode(Context context, Vector3 position, int landmarkIndex) {
        this.context = context;
        this.position = position;
        this.landmarkIndex = landmarkIndex;
        createSphereRenderable();
    }

    private void createSphereRenderable() {
        // Créer différentes couleurs selon l'index du landmark
        // Par exemple: pouces en rouge, index en vert, etc.
        Color color;

        if (landmarkIndex >= 0 && landmarkIndex <= 4) {
            // Pouce
            color = new Color(1.0f, 0.0f, 0.0f); // Rouge
        } else if (landmarkIndex >= 5 && landmarkIndex <= 8) {
            // Index
            color = new Color(0.0f, 1.0f, 0.0f); // Vert
        } else if (landmarkIndex >= 9 && landmarkIndex <= 12) {
            // Majeur
            color = new Color(0.0f, 0.0f, 1.0f); // Bleu
        } else if (landmarkIndex >= 13 && landmarkIndex <= 16) {
            // Annulaire
            color = new Color(1.0f, 1.0f, 0.0f); // Jaune
        } else if (landmarkIndex >= 17 && landmarkIndex <= 20) {
            // Auriculaire
            color = new Color(1.0f, 0.0f, 1.0f); // Magenta
        } else {
            // Points de la paume
            color = new Color(1.0f, 1.0f, 1.0f); // Blanc
        }

        MaterialFactory.makeOpaqueWithColor(context, color)
                .thenAccept(material -> {
                    sphereRenderable = ShapeFactory.makeSphere(
                            LANDMARK_RADIUS,
                            Vector3.zero(),
                            material);
                    setRenderable(sphereRenderable);
                    setLocalPosition(position);
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Could not create landmark sphere", throwable);
                    return null;
                });
    }

    public void updatePosition(Vector3 newPosition) {
        setLocalPosition(newPosition);
    }
}