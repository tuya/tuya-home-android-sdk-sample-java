package com.tuya.smart.android.demo.camera.utils;

import android.content.Context;
import android.os.StatFs;

import androidx.annotation.Nullable;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.tuya.imagepipeline.okhttp3.OkHttpImagePipelineConfigFactory;
import com.tuya.smart.android.common.task.TuyaExecutor;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * huangdaju
 * 2020-02-20
 **/

public class FrescoManager {
    public static void initFresco(Context context){
        ImagePipelineConfig defaultConfig = getDefaultConfig(context, null, null);
        initFresco(context, defaultConfig);
    }

    public static void initFresco(Context context, ImagePipelineConfig config){
        Fresco.initialize(context, config);
    }

    private static ImagePipelineConfig getDefaultConfig(Context context, @Nullable RequestListener listener, @Nullable DiskCacheConfig diskCacheConfig) {
        HashSet requestListeners = new HashSet();
        File cacheDir = new File(context.getCacheDir(), "okhttp3");
        long size = calculateDiskCacheSize(cacheDir);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(new Cache(cacheDir, size))
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS).build();

        ImagePipelineConfig.Builder builder = OkHttpImagePipelineConfigFactory.newBuilder(
                context.getApplicationContext(), okHttpClient);
        builder.setDownsampleEnabled(false).setRequestListeners(requestListeners);
        if (diskCacheConfig != null) {
            builder.setMainDiskCacheConfig(diskCacheConfig);
        }
        builder.setExecutorSupplier(new ExecutorSupplier() {
            @Override
            public Executor forLocalStorageRead() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }

            @Override
            public Executor forLocalStorageWrite() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }

            @Override
            public Executor forDecode() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }

            @Override
            public Executor forBackgroundTasks() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }

            @Override
            public Executor forLightweightBackgroundTasks() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }

            @Override
            public Executor forThumbnailProducer() {
                return TuyaExecutor.getInstance().getTuyaExecutorService();
            }
        });

        return builder.build();
    }

    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 10 * 1024 * 1024; // 50MB

    private static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }
}
