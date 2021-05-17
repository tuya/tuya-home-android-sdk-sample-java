package com.tuya.appsdk.sample.device.config.util.qrcode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

import static android.graphics.Color.BLACK;

/**
 * huangdaju
 * 2019/1/3
 **/

public class QRCodeUtil {

    /**
     * Create a QR code bitmap
     * @param content content(Support Chinese)
     * @param width width, unit px
     * @param height height, unit px
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int width, int height){
        return createQRCodeBitmap(content, width, height, "UTF-8", "H", "2", Color.BLACK, Color.WHITE);
    }

    /**
     * Create QR code bitmap (support custom configuration and custom style)
     *
     * @param content content(Support Chinese)
     * @param width width, unit px
     * @param height height, unit px
     * @param character_set Character set/character transcoding format. When passing null, zxing source code uses "ISO-8859-1" by default
     * @param error_correction Fault tolerance level. When passing null, zxing source code uses "L" by default
     * @param margin Blank margin (can be modified, requirement: integer and >=0), when passing null, zxing source code uses "4" by default
     * @param color_black Custom color value of black color block
     * @param color_white Custom color value of white color block
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            @Nullable String character_set, @Nullable String error_correction, @Nullable String margin,
                                            @ColorInt int color_black, @ColorInt int color_white){

        /** 1.Parameter legality judgment */
        if(TextUtils.isEmpty(content)){ // The string content is blank
            return null;
        }

        if(width < 0 || height < 0){ // Both width and height need to be >=0
            return null;
        }

        try {
            /** 2.Set the QR code related configuration and generate BitMatrix objects */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();

            if(!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set); // Character transcoding format setting
            }

            if(!TextUtils.isEmpty(error_correction)){
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // Fault tolerance level setting
            }

            if(!TextUtils.isEmpty(margin)){
                hints.put(EncodeHintType.MARGIN, margin); // Margin settings
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.Create a pixel array and assign color values to the array elements according to the BitMatrix object */
            int[] pixels = new int[width * height];
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(bitMatrix.get(x, y)){
                        pixels[y * width + x] = color_black; // Black color block pixel settings
                    } else {
                        pixels[y * width + x] = color_white; // White color block pixel setting
                    }
                }
            }

            /** 4.Create a Bitmap object, set the color value of each pixel of the Bitmap according to the pixel array, and then return the Bitmap object */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap createQRCode(String url, int widthAndHeight)
            throws WriterException {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN,0);
        BitMatrix matrix = new MultiFormatWriter().encode(url,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK; //0xff000000
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
