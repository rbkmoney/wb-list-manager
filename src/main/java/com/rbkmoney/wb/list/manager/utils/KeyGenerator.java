package com.rbkmoney.wb.list.manager.utils;

public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(String partyId, String shopId, String type, String key) {
        return partyId + DELIMITER + shopId + DELIMITER + type + DELIMITER + key;
    }


}
