# Activity-SecretCode

一个 Android 应用，用于通过 Root 权限触发系统 Secret Code 广播。

## 功能

点击按钮即可发送 `SECRET_CODE` 广播（暗码 `6776799`），效果等同于在拨号盘输入 `*#*#6776799#*#*`。

## 原理

Android 系统的 Secret Code 广播是受保护广播（protected broadcast），普通应用无权发送。本应用通过 `su` 执行 `am broadcast` 命令来绕过此限制：

```
am broadcast -a android.telephony.action.SECRET_CODE -d android_secret_code://6776799
```

- **Android 10 (Q) 及以上**：使用 `TelephonyManager.ACTION_SECRET_CODE`
- **Android 10 以下**：使用 `android.provider.Telephony.SECRET_CODE`

如果 Root 执行失败，会回退到标准 `sendBroadcast()`（通常会因权限不足而失败）。

## 前置条件

- **Root 权限**：设备必须已 Root 并安装 Root 管理器（如 Magisk / KernelSU）
- **Android SDK 24+**（minSdk 24，targetSdk 34）

## 项目结构

```
app/src/main/
├── java/com/example/intercept/
│   └── MainActivity.java      # 主界面 & triggerSecretCode 逻辑
├── res/layout/
│   └── activity_main.xml      # 布局（单按钮）
└── AndroidManifest.xml        # 清单
```

## 构建

```bash
# 使用 Gradle Wrapper（如已生成）
./gradlew assembleDebug

# 或使用系统 Gradle
gradle assembleDebug
```

输出 APK 路径：`app/build/outputs/apk/debug/app-debug.apk`

## 使用

1. 安装 APK 到已 Root 的设备
2. 打开应用
3. 点击 **"Trigger Secret Code (6776799)"** 按钮
4. 授予 Root 权限（首次会弹窗）
5. 系统会处理对应的 Secret Code

## 自定义暗码

修改 [MainActivity.java](app/src/main/java/com/example/intercept/MainActivity.java) 中的代码值即可：

```java
triggerSecretCode("你的暗码");
```

## 注意事项

- 不同设备/ROM 注册的 Secret Code 不同，需确认目标暗码在当前设备上有对应的接收方
- 部分 ROM 可能额外限制了 Secret Code 的触发方式
