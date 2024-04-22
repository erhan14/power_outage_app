package com.yigitbasi.power;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;

import android.util.Rational;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.graphics.PixelFormat;

import com.google.common.util.concurrent.ListenableFuture;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MyService extends LifecycleService {

    private final IBinder binder = new MyBinder();
    private WindowManager windowManager;
    private FrameLayout layout;
    private LayoutInflater inflater;
    private WindowManager.LayoutParams params;
    private View myview;

    private PreviewView preview;

    private ImageCapture imageCapture = null;

    private TelegramBot bot = null;

    public static String TAG = "YGTBS";

    ProcessCameraProvider cameraProvider = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String NOTIFICATION_CHANNEL_ID = "mypowerapp";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("PowerControl")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Güç Kontrol")
                .setContentText("Monitör ediliyor")
                .setContentInfo("Devam ediyor");
        notificationManager.notify(1, notificationBuilder.build());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        myview = inflater.inflate(R.layout.service_layout, null);
        layout = (FrameLayout) myview.findViewById(R.id.preview);

        params = new WindowManager.LayoutParams(500, 300,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 150;


        windowManager.addView(myview, params);

        //pengrad api
        try {
            bot = new TelegramBot(TelegramMessageSenderService.TELEGRAM_TOKEN);
            // Register for updates
            bot.setUpdatesListener(updates -> {
                // ... process updates
                // return id of last processed update or confirm them all
                for (Update u: updates
                ) {
                    try {
                        if (u.message() != null) {
                            Message m = u.message();
                            Log.d(TAG, "Message: " + u.message());
                            if (m.chat() != null && m.chat().id().toString().equals(TelegramMessageSenderService.TELEGRAM_CHAT_ID)) {
                                Log.i(TAG, "Received command from channel " + m.text());
                                switch (m.text().toUpperCase(Locale.ENGLISH)) {
                                    case "YARDIM":
                                    case "HELP":
                                        bot.execute(new SendMessage(TelegramMessageSenderService.TELEGRAM_CHAT_ID, "Desteklenen kelimeler: YARDIM\n" +
                                                "FOTO\n" +
                                                "VIDEO\n" +
                                                "DURUM..."));
                                        break;
                                    case "DURUM":
                                        bot.execute(new SendMessage(TelegramMessageSenderService.TELEGRAM_CHAT_ID, "" +
                                                (MainActivity.isPowerConnected(this) ? this.getString(R.string.power_connected) :
                                                        this.getString(R.string.power_disconnected))));
                                        break;
                                    case "FOTO":
                                        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(bos).build();
                                        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                                            @Override
                                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults img) {
                                                Log.i(TAG, "Image ready " + bos.size());
                                                sendPhoto(bos);
                                                try {
                                                    bos.close();
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Error bos closee ", e);
                                                }
                                            }

                                            @Override
                                            public void onError(@NonNull ImageCaptureException error) {
                                                Log.e(TAG, "Error taking photo ", error);
                                            }
                                        });
                                        break;
                                }
                            }
                        }
                    }catch (Exception t) {
                        Log.e(TAG, "Error message parse ", t);
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
                // Create Exception Handler
            }, e -> {
                if (e.response() != null) {
                    Log.e(TAG,"Error listening telegram bot " + e.response().errorCode() +
                            " - " + e.response().description());
                } else {
                    Log.e(TAG,"Error listening telegram bot ", e);
                }
            });

            startCamera();
        } catch (Throwable e) {
            Log.e(TAG,"Error creating telegram bot ", e);
        }

        Log.i(MainActivity.TAG, "Service created");
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        PreviewView mSurfaceView = myview.findViewById(R.id.previewView);
        /* start preview */
        int aspRatioW = mSurfaceView.getWidth(); // get width of screen
        int aspRatioH = mSurfaceView.getHeight(); // get height
        Rational asp = new Rational(aspRatioW, aspRatioH); // aspect ratio
        Size screen = new Size(aspRatioW, aspRatioH); // size of the screen

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        int rot = windowManager.getDefaultDisplay().getRotation();
        int width = 800;
        int height = 600;
        if(rot== Surface.ROTATION_0 || rot==Surface.ROTATION_180) {
            width = 600;
            height = 800;
        }
        ResolutionSelector selector = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(
                        new Size(width, height),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)).build();

        imageCapture = builder
                .setTargetRotation(rot)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setResolutionSelector(selector)
                .build();

        preview.setSurfaceProvider(mSurfaceView.getSurfaceProvider());

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);

    }

    private void sendPhoto(ByteArrayOutputStream bos ) {
        ContextCompat.getMainExecutor(this).execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Sending image " + bos.size());
                bot.execute(new SendPhoto(TelegramMessageSenderService.TELEGRAM_CHAT_ID, bos.toByteArray()));
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.e(TAG,"Error bos ", e);
                }
            }
        });
    }
}
