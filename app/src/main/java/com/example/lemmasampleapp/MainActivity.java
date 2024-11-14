package com.example.lemmasampleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import lemma.lemmavideosdk.common.LMLogger;
import lemma.lemmavideosdk.common.LemmaWSDK;
import lemma.lemmavideosdk.vast.listeners.AdManagerCallback;
import lemma.lemmavideosdk.vast.manager.LMSharedVideoManagerI;
import lemma.lemmavideosdk.vast.manager.LMSharedVideoManagerPrefetchCallback;
import lemma.lemmavideosdk.vast.manager.LMVideoAdManagerI;
import lemma.lemmavideosdk.vast.manager.LMWAdRequest;
import lemma.lemmavideosdk.vast.manager.LMWConfig;
import lemma.lemmavideosdk.vast.manager.LMWSharedVideoManager;

public class MainActivity extends Activity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    private Button prefetchBtn, showBtn, destroyBtn;
    private TextView textLog;
    LMWSharedVideoManager sharedVideoManagerInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textLog = findViewById(R.id.textLog);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        prefetchBtn = findViewById(R.id.prefetchBtn);
        prefetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Clicked PrefetchBtn", "Clicked PrefetchBtn");
                checkPermissionAndLoadAd();
            }
        });

        showBtn = findViewById(R.id.showBtn);
        showBtn.setVisibility(View.INVISIBLE);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBtn.setVisibility(View.INVISIBLE);
                sharedVideoManagerInstance = LMWSharedVideoManager.getInstance();
                renderAd(sharedVideoManagerInstance);

            }
        });

        destroyBtn = findViewById(R.id.destroyBtn);
        destroyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedVideoManagerInstance != null) {
                    sharedVideoManagerInstance.destroySharedInstance();
                }
            }
        });

    }

    public void checkPermissionAndLoadAd() {
        String listPermissionsNeeded[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, listPermissionsNeeded[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, listPermissionsNeeded[1]) == PackageManager.PERMISSION_GRANTED) {
            loadAd();
        } else {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded, REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    private void loadAd() {
        LemmaWSDK.init(this, false, new LemmaWSDK.SDKInitListener() {
            @Override
            public void onInit() {
                String version = LemmaWSDK.getVersion();
                prefetchAd();
            }
        });

    }

    private void prefetchAd() {
        final LMWSharedVideoManager sharedVideoManager = LMWSharedVideoManager.getInstance();
        LMWAdRequest adRequest = new LMWAdRequest("169", "10910");
        LMWConfig config = new LMWConfig();
        adRequest.setAdServerBaseURL("https://dooh.lemmamedia.com/lemma/servad");
        config.setExecuteImpressionInWebContainer(true);
        config.setSecureConnection(true);
        sharedVideoManager.setRetryCount(2);
        sharedVideoManager.prepare(this, adRequest, config);
        sharedVideoManager.prefetch(new LMSharedVideoManagerPrefetchCallback() {
            @Override
            public void onSuccess() {
                showBtn.setVisibility(View.VISIBLE);
                LMLogger.d("ON-Success", "ON-Success");
                Log.d("ON-Success", "ON-Success");
                Toast.makeText(getApplicationContext(), "ON-Success", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onFailure() {

                LMLogger.d("ON-Failure", "ON-Failure");
                Log.d("ON-Failure", "ON-Failure");
                Toast.makeText(getApplicationContext(), "ON-Failure", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void renderAd(final LMWSharedVideoManager sharedVideoManagerInstance) {
        sharedVideoManagerInstance.renderAdInView((FrameLayout) findViewById(R.id.ad_linear_container), new AdManagerCallback() {
            @Override
            public void onAdError(LMVideoAdManagerI lmVideoAdManagerI, Error error) {

            }

            @Override
            public void onAdEvent(AD_EVENT event) {
                log(event.name());
                switch (event) {
                    case AD_STARTED:
                        Log.d("AD_STARTED", "AD_STARTED");
                        break;
                    case AD_LOOP_COMPLETED:
                        Log.d("AD_LOOP_COMPLETED", "AD_LOOP_COMPLETED");
                        sharedVideoManagerInstance.destroySharedInstance();
                        break;
                    default:
                        break;
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadAd();
    }

    private void log(String log) {
        textLog.append(log + "\n");
    }
}
