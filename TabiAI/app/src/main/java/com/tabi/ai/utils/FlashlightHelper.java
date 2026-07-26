package com.tabi.ai.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

/**
 * Controls the device's rear camera flash (torch) via Camera2's CameraManager,
 * which does not require holding the camera open.
 */
public class FlashlightHelper {

    private final CameraManager cameraManager;
    private String cameraId;
    private boolean isOn = false;

    public FlashlightHelper(Context context) {
        this.cameraManager = (CameraManager) context.getApplicationContext()
                .getSystemService(Context.CAMERA_SERVICE);
        resolveCameraId();
    }

    private void resolveCameraId() {
        try {
            if (cameraManager != null) {
                for (String id : cameraManager.getCameraIdList()) {
                    Boolean hasFlash = cameraManager.getCameraCharacteristics(id)
                            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    if (hasFlash != null && hasFlash) {
                        cameraId = id;
                        break;
                    }
                }
            }
        } catch (CameraAccessException e) {
            cameraId = null;
        }
    }

    public boolean hasFlashlight() {
        return cameraManager != null && cameraId != null;
    }

    public boolean toggle() {
        setTorch(!isOn);
        return isOn;
    }

    public void setTorch(boolean enable) {
        if (!hasFlashlight()) {
            return;
        }
        try {
            cameraManager.setTorchMode(cameraId, enable);
            isOn = enable;
        } catch (CameraAccessException e) {
            isOn = false;
        }
    }

    public boolean isOn() {
        return isOn;
    }
}
