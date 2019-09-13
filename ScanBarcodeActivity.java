package com.sims.cleonhotspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScanBarcodeActivity extends AppCompatActivity {
    private FrameLayout pCameraLayout = null;
    SurfaceView cameraPreview;
    Camera camera;
    Button scan;
    Camera.Parameters params;
    TextView txtResult;
    String qrcode;
    String message = "";
    String email = "";
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    private ZoomControls zoomControls;
    public static final int RequestCameraPermissionID = 1001;

    public final static String EXTRA_MESSAGE = "MESSAGE";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        Intent intent = getIntent();
        final String email = intent.getStringExtra(LoginActivity.E_MESSAGE);

        pCameraLayout = (FrameLayout) findViewById(R.id.frcamera);

        enableZoom();

        scan = (Button) findViewById(R.id.btUpdate);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!txtResult.getText().toString().equalsIgnoreCase("Mohon fokus kamera ke QR Code")) {
                Bundle bundle = new Bundle();
                bundle.putString("data1", qrcode);
                bundle.putString("data2", email);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else
            {
                txtResult.setText("Mohon fokus kamera ke QR Code");
                txtResult.setTextColor(getResources().getColor(android.R.color.black));
                txtResult.setTextSize(24);
            }
            }
        });
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.textResult);

        barcodeDetector = new BarcodeDetector.Builder(this)
                //.setBarcodeFormats(Barcode.QR_CODE)
                //.setBarcodeFormats(Barcode.CODABAR)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {

            }

            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                cameraFocus(event, cameraSource, Camera.Parameters.FOCUS_MODE_AUTO);

                return false;
            }
        });
        cameraPreview.performClick();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if (qrcodes.size() != 0) {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            qrcode = qrcodes.valueAt(0).displayValue;
                            txtResult.setText("Sukses!");
                            txtResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            txtResult.setTextSize(24);
                        }
                    });
                }
            }
        });


    }

    private void enableZoom() {
        zoomControls = new ZoomControls(this);
        zoomControls.setIsZoomInEnabled(true);
        zoomControls.setIsZoomOutEnabled(true);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                zoomCamera(true);

            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                zoomCamera(false);
            }
        });
        pCameraLayout.addView(zoomControls);
    }

    /**
     * Enables zoom feature in native camera .  Called from listener of the view
     * used for zoom in  and zoom out.
     *
     *
     * @param zoomInOrOut  "false" for zoom in and "true" for zoom out
     */
    public void zoomCamera(boolean zoomInOrOut) {
        if(camera!=null) {
            Camera.Parameters parameter = camera.getParameters();

            if(parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                if(zoomInOrOut && (currnetZoom <MAX_ZOOM && currnetZoom >=0)) {
                    parameter.setZoom(++currnetZoom);
                }
                else if(!zoomInOrOut && (currnetZoom <=MAX_ZOOM && currnetZoom >0)) {
                    parameter.setZoom(--currnetZoom);
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Zoom Not Avaliable", Toast.LENGTH_SHORT).show();

            camera.setParameters(parameter);
        }
    }


    private void cameraFocus(MotionEvent event, @NonNull CameraSource cameraSource, @NonNull String focusMode) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();


        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);

        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        float touchMajor = event.getTouchMajor();
        float touchMinor = event.getTouchMinor();

        Rect touchRect = new Rect((int)(x - touchMajor / 2), (int)(y - touchMinor / 2), (int)(x + touchMajor / 2), (int)(y + touchMinor / 2));

        Rect focusArea = new Rect();

        focusArea.set(touchRect.left * 2000 / cameraPreview.getWidth() - 1000,
                touchRect.top * 2000 / cameraPreview.getHeight() - 1000,
                touchRect.right * 2000 / cameraPreview.getWidth() - 1000,
                touchRect.bottom * 2000 / cameraPreview.getHeight() - 1000);

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(focusArea, 1000));

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        params = camera.getParameters();
                        params.setFocusMode(focusMode);
                        params.setFocusAreas(focusAreas);
                        camera.setParameters(params);

                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean b, Camera camera) {
                                // currently set to auto-focus on single touch
                                camera.startSmoothZoom(3);
                            }
                        });
                        return;
                    }

                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }
}
