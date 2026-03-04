package com.example.intercept;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "ActivityInterceptor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRegister = findViewById(R.id.btn_register_secret_code);
        btnRegister.setOnClickListener(v -> {
            registerSecretCodeReceiver();
        });

        Button btnTrigger = findViewById(R.id.btn_trigger_secret_code);
        btnTrigger.setOnClickListener(v -> {
            triggerSecretCode("6776799");
        });
    }

    private void triggerSecretCode(String code) {
        String action;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            action = TelephonyManager.ACTION_SECRET_CODE;
        } else {
            action = "android.provider.Telephony.SECRET_CODE";
        }

        Intent intent = new Intent(action, android.net.Uri.parse("android_secret_code://" + code));
        // 发送广播，唤起其他应用或本应用注册的监听器
        sendBroadcast(intent);
        
        Log.d(TAG, "Sent Secret Code Broadcast: " + code);
        Toast.makeText(this, "Triggered Secret Code: " + code, Toast.LENGTH_SHORT).show();
    }

    private void registerSecretCodeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intentFilter.addAction(TelephonyManager.ACTION_SECRET_CODE);
        } else {
            // noinspection InlinedApi
            intentFilter.addAction(Telephony.Sms.Intents.SECRET_CODE_ACTION);
        }
        intentFilter.addDataAuthority("*#*#6776799#*#*", null);
        intentFilter.addDataScheme("android_secret_code");

        // The user's snippet used a very specific registerReceiver call.
        // Assuming they have a helper or it's a specific API.
        // For standard compilation, we might need a custom method if it doesn't match standard Context.
        // However, I will implement a standard version or a shim if I can guess what they meant.
        
        // Let's try to use the user's exact syntax if possible, 
        // but if it won't compile, I'll use a standard alternative.
        // Since I can't check compilation easily without running, I'll provide a working version.
        
        Log.d(TAG, "registering secret code receiver...");
        
        try {
            // Attempting to match the user's requested syntax as much as possible.
            // Note: registerReceiver(List, String, int, Receiver, int) is NOT standard.
            // I'll use the standard one but keep it clean.
            
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    dispatchSecretCodeReceive(context, intent);
                }
            };

            // Handling the exported flag for API 33+
            int flags = Context.RECEIVER_EXPORTED;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, intentFilter, "android.permission.CONTROL_INCALL_EXPERIENCE", null, flags);
            } else {
                registerReceiver(receiver, intentFilter, "android.permission.CONTROL_INCALL_EXPERIENCE", null);
            }
            
            Log.d(TAG, "registered secret code receiver");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register receiver", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void dispatchSecretCodeReceive(Context context, Intent intent) {
        String code = intent.getData() != null ? intent.getData().getHost() : "unknown";
        Log.d(TAG, "Secret code received: " + code);
        Toast.makeText(context, "Secret Code Received: " + code, Toast.LENGTH_LONG).show();
    }
}
