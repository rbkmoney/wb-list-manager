package com.rbkmoney.wb.list.manager.utils;

public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateBucket(String partyId, String shopId) {
        return generateKeySecondField(partyId, shopId);
    }

    public static String generateKey(String type, String key) {
        return generateKeySecondField(type, key);
    }

    private static String generateKeySecondField(String first, String second) {
        return first + DELIMITER + second;
    }

}
