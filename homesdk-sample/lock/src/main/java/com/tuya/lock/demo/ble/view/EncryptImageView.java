package com.tuya.lock.demo.ble.view;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.thingclips.drawee.view.DecryptImageView;
import com.thingclips.imagepipeline.okhttp3.DecryptImageRequest;
import com.thingclips.smart.android.common.utils.L;

public class EncryptImageView extends DecryptImageView {

    private int mImageRotation;
    private int mScaleType;
    private EncryptImageViewLoadListener mEncryptImageViewLoadListener;

    public void setEncryptImageViewLoadListener(EncryptImageViewLoadListener encryptImageViewLoadListener) {
        this.mEncryptImageViewLoadListener = encryptImageViewLoadListener;
    }

    public EncryptImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public EncryptImageView(Context context) {
        super(context);
    }

    public EncryptImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EncryptImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EncryptImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageRotation(int rotation) {
        mImageRotation = rotation;
    }

    public void setImageScaleType(int scaleType) {
        mScaleType = scaleType;
    }

    @Override
    public void setImageURI(String uriString, byte[] key) {
        L.e("setImageURI", hashCode() + " " + uriString);
        try {
            ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uriString))
                    .setRotationOptions(mImageRotation == -1 ? RotationOptions.autoRotateAtRenderTime() : RotationOptions.forceRotation(mImageRotation))
                    .disableDiskCache();
            DecryptImageRequest imageRequest = new DecryptImageRequest(builder, key);

            PipelineDraweeController controller = (PipelineDraweeController)
                    Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imageRequest)
                            .setOldController(getController())
                            .setControllerListener(new ControllerListener(uriString, mEncryptImageViewLoadListener))
                            .build();
            setController(controller);
            if (mScaleType != -1) {
                GenericDraweeHierarchy hierarchy = getHierarchy();
                if (hierarchy != null) {
                    getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class ControllerListener extends BaseControllerListener<ImageInfo> {
        EncryptImageViewLoadListener mEncryptImageViewLoadListener;
        String mUrl;

        public ControllerListener(String url, EncryptImageViewLoadListener encryptImageViewLoadListener) {
            mEncryptImageViewLoadListener = encryptImageViewLoadListener;
            mUrl = url;
        }

        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);
            if (mEncryptImageViewLoadListener != null && imageInfo != null) {
                mEncryptImageViewLoadListener.success(mUrl, imageInfo.getWidth(), imageInfo.getHeight());
            }
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            super.onFailure(id, throwable);
            if (mEncryptImageViewLoadListener != null) {
                mEncryptImageViewLoadListener.failure(mUrl, throwable.getMessage());
            }
        }
    }

    public interface EncryptImageViewLoadListener {
        void success(String url, int width, int height);

        void failure(String url, String error);
    }
}
