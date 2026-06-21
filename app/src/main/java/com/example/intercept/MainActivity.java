package com.example.intercept;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "ActivityInterceptor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        String cmd = "am broadcast -a " + action + " -d android_secret_code://" + code;
        
        try {
            // 使用 su 命令通过 root 权限发送广播，绕过普通应用无法发送受保护广播的限制
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            Log.d(TAG, "Sent Secret Code Broadcast via Root: " + cmd);
            Toast.makeText(this, "Triggering via Root...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute root command, trying standard broadcast", e);
            // 兜底方案（很可能依然报权限错误）
            Intent intent = new Intent(action, android.net.Uri.parse("android_secret_code://" + code));
            sendBroadcast(intent);
        }
    }
}
