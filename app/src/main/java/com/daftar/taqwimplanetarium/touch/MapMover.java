// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.daftar.taqwimplanetarium.touch;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.daftar.taqwimplanetarium.util.MathUtil;
import com.daftar.taqwimplanetarium.util.MiscUtil;
import com.daftar.taqwimplanetarium.views.MyGLRenderer;
import com.daftar.taqwimplanetarium.views.MyGLSurfaceView;

/**
 * Applies drags, zooms and rotations to the model.
 * Listens for events from the DragRotateZoomGestureDetector.
 *
 * @author John Taylor
 */
public class MapMover implements
        DragRotateZoomGestureDetector.DragRotateZoomGestureDetectorListener {

    // Convert Degrees to Radians
    public static final float DEGREES_TO_RADIANS = MathUtil.PI / 180.0f;

    // Convert Radians to Degrees
    public static final float RADIANS_TO_DEGREES = 180.0f / MathUtil.PI;


    private static final String TAG = MiscUtil.getTag(MapMover.class);
    private MyGLRenderer model;
    private MyGLSurfaceView controllerGroup;
    private float sizeTimesRadiansToDegrees;

    public MapMover(MyGLRenderer model, MyGLSurfaceView controllerGroup, Context context) {
        this.model = model;
        this.controllerGroup = controllerGroup;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenLongSize = metrics.heightPixels;
        Log.i(TAG, "Screen height is " + screenLongSize + " pixels.");
        sizeTimesRadiansToDegrees = screenLongSize * RADIANS_TO_DEGREES;
    }

    @Override
    public boolean onDrag(float xPixels, float yPixels) {
        // Log.d(TAG, "Dragging by " + xPixels + ", " + yPixels);
        final float pixelsToRadians = model.getZoom() / sizeTimesRadiansToDegrees;
        model.setPanAzimuth(model.getPanAzimuth() - xPixels * pixelsToRadians);
        model.setPanAltitude(model.getPanAltitude() + yPixels * pixelsToRadians);
        controllerGroup.requestRender();
        return true;
    }

    @Override
    public boolean onRotate(float degrees) {
//    controllerGroup.rotate(-degrees);
        return true;
    }

    @Override
    public boolean onStretch(float ratio) {
        controllerGroup.setZoom(controllerGroup.getZoom() * 1.0f / ratio);
        return true;
    }
}