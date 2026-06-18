package edu.icet.util;

public final class BarcodeUtil {

    private BarcodeUtil() {
    }

    public static String generateEan13(int variantId) {
        String base = String.format("890%09d", variantId);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return base + check;
    }
}
