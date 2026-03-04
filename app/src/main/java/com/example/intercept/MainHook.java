package com.example.intercept;

import android.app.Activity;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.provider.Telephony;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import java.util.List;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "ActivityInterceptor";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 使用 Log.d 打印，可以在 Logcat 中通过 TAG "ActivityInterceptor" 过滤
        Log.d(TAG, "模块已加载到包: " + lpparam.packageName);
        XposedBridge.log(TAG + ": 模块已加载到包: " + lpparam.packageName);

        // 如果你勾选了多个应用，这里可以用 if 过滤
        // 为了调试，建议初次尝试时先注释掉过滤逻辑，或者确保 packageName 匹配无误
        /*
        if (!lpparam.packageName.equals("com.miui.securitycenter")) {
            return;
        }
        */

        // Hook 所有 Activity 的 onCreate
        XposedHelpers.findAndHookMethod(
            "android.app.Activity", 
            lpparam.classLoader, 
            "onCreate", 
            Bundle.class, 
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String activityName = activity.getClass().getName();
                    
                    Log.d(TAG, "检测到 Activity 启动: " + activityName);
                    XposedBridge.log(TAG + ": 检测到 Activity 启动: " + activityName);

                    // 检查类名是否包含目标字符串
                    if (activityName.contains("com.miui.securityscan.MainActivity") || activityName.contains("com.miui.securitymanager.MainActivity") 
                        activityName.contains("securitycenter")) {
                        
                        Log.w(TAG, "!! 命中拦截规则 !! -> " + activityName);
                        XposedBridge.log(TAG + ": !! 命中拦截规则 !! -> " + activityName);
                        
                        // 拦截
                        activity.finish();
                    }
                }
            }
        );

        // Hook to add a button to activities to trigger the secret code receiver
        XposedHelpers.findAndHookMethod(
            "android.app.Activity", 
            lpparam.classLoader, 
            "onPostCreate", 
            Bundle.class, 
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String activityName = activity.getClass().getName();
                    
                    // Only inject into our desired activities
                    if (activityName.contains("com.miui.securityscan.MainActivity") || 
                        activityName.contains(lpparam.packageName)) {
                        
                        injectButton(activity);
                    }
                }
            }
        );
    }

    private void injectButton(Activity activity) {
        try {
            ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            if (rootView == null) return;

            Button btn = new Button(activity);
            btn.setText("Register Secret Code");
            btn.setOnClickListener(v -> {
                registerSecretCodeReceiver(activity);
                Toast.makeText(activity, "Receiver Registered", Toast.LENGTH_SHORT).show();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, 0, 0, 100);
            
            rootView.addView(btn, params);
            Log.d(TAG, "Button injected into " + activity.getClass().getName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject button", e);
        }
    }

    private void registerSecretCodeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intentFilter.addAction(TelephonyManager.ACTION_SECRET_CODE);
        } else {
            // noinspection InlinedApi
            intentFilter.addAction(Telephony.Sms.Intents.SECRET_CODE_ACTION);
        }
        intentFilter.addDataAuthority("6776799", null);
        intentFilter.addDataScheme("android_secret_code");

        // Using standard registerReceiver call since List.of variant is non-standard for Context
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dispatchSecretCodeReceive(context, intent);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, "android.permission.CONTROL_INCALL_EXPERIENCE", null, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(receiver, intentFilter, "android.permission.CONTROL_INCALL_EXPERIENCE", null);
        }
        
        Log.d(TAG, "registered secret code receiver");
    }

    private void dispatchSecretCodeReceive(Context context, Intent intent) {
        Log.d(TAG, "Secret code received: " + (intent.getData() != null ? intent.getData().toString() : "null"));
    }
}
