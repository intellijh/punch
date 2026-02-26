package com.punch.shop.common.util;

public class PhoneUtils {

    private static final String MOBILE_PHONE_REGEX = "^010\\d{8}$";

    private PhoneUtils() {}

    public static String normalize(String phone) {
        return phone != null ? phone.replaceAll("[^0-9]", "") : null;
    }

    public static String format(String phone) {
        if (phone == null) return null;
        String digits = normalize(phone);
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        return phone;
    }

    public static String validateAndNormalize(String phone) {
        String digits = normalize(phone);
        if (digits == null || !digits.matches(MOBILE_PHONE_REGEX)) {
            throw new IllegalArgumentException("유효하지 않은 휴대폰 번호입니다: " + phone);
        }
        return digits;
    }
}
