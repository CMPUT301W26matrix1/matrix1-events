/**
 * Utility for generating QR Codes.
 * Converts event-related data into scannable Bitmaps.
 * Used for event sharing and check-ins.
 */
package com.example.eventflow.org_QR;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class responsible for converting text-based data into
 * scannable QR Code images (Bitmaps).
 *
 * This class uses the ZXing (Zebra Crossing) library for the encoding process.
 */
public class QRGenerator {

    /**
     * Takes a string of data and converts it into a 512x512 QR Code bitmap.
     *
     * @param data The text or URI to be encoded into the QR code.
     * @return A Bitmap object of the QR code, or null if an encoding error occurs.
     */
    public static Bitmap generateQRCode(String data) {
        // UPDATED: Check for null OR empty strings to prevent library crashes
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
