package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.org_QR.QRGenerator;

import org.junit.Test;

/**
 * Unit tests for the QRGenerator utility.
 * Focuses on input validation and handling.
 */
public class QRGeneratorTest {

    @Test
    public void testGenerateWithNullData() {
        assertNull("Generator should return null if input data is null",
                QRGenerator.generateQRCode(null));
    }

    @Test
    public void testGenerateWithEmptyData() {
        // Now we expect null because we added the check in the code!
        assertNull("Generator should return null if input data is empty",
                QRGenerator.generateQRCode(""));
    }
}