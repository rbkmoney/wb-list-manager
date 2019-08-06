package com.rbkmoney.wb.list.manager.utils;

import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.damsel.wb_list.Row;
import org.springframework.util.StringUtils;

public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(String partyId, ListType listType, String listName, String value) {
        return generateKey(partyId, null, listType, listName, value);
    }

    public static String generateKey(ListType listType, String listName, String value) {
        return generateKey(null, null, listType, listName, value);
    }

    public static String generateKey(Row row) {
        return generateKey(row.getPartyId(), row.getShopId(), row.getListType(), row.getListName(), row.getValue());
    }

    private static String generateKey(String partyId, String shopId, ListType listType, String listName, String value) {
        StringBuilder stringBuilder = new StringBuilder();
        addIfExist(partyId, stringBuilder);
        return addIfExist(shopId, stringBuilder)
                .append(listType)
                .append(DELIMITER)
                .append(listName)
                .append(DELIMITER)
                .append(value)
                .toString();
    }

    private static StringBuilder addIfExist(String id, StringBuilder stringBuilder) {
        if (!StringUtils.isEmpty(id)) {
            stringBuilder.append(id)
                    .append(DELIMITER);
        }
        return stringBuilder;
    }
}
