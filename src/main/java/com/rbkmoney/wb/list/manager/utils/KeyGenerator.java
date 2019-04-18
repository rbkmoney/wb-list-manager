package com.rbkmoney.wb.list.manager.utils;

import com.rbkmoney.damsel.wb_list.Row;

public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(Row row) {
        return row.getPartyId() +
                DELIMITER +
                row.getShopId() +
                DELIMITER +
                row.getListType() +
                DELIMITER +
                row.getListName() +
                DELIMITER +
                row.getValue();
    }


}
