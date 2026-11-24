package com.dualaudio.app.routing;

/**
 * Shizuku UserService IPC contract between the app and DualOutputShizukuService.
 */
interface IAudioRoutingService {
    void destroy() = 16777114;
    String enableWithMode(int mode, boolean bluetoothFirst, boolean autoReapply) = 1;
    String enableCustom(int type1, String addr1, int type2, String addr2, boolean autoReapply) = 2;
    String disable() = 3;
    String status() = 4;
    boolean isEnabled() = 5;
    int getActiveMode() = 6;
    boolean isRoutingApplied() = 7;
    String measureLatencyReport() = 8;
    int setDeviceCategoryVolume(int category, int index, int maxIndex) = 9;
    int getDeviceCategoryVolume(int category) = 10;
    String getLastVolumeStatus() = 11;
}
