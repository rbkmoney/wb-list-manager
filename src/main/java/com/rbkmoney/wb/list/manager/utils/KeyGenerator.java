package com.rbkmoney.wb.list.manager.utils;

import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.damsel.wb_list.P2pId;
import com.rbkmoney.damsel.wb_list.PaymentId;
import com.rbkmoney.wb.list.manager.constant.RowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(com.rbkmoney.damsel.wb_list.Row row) {
        if (row.isSetId() && row.getId().isSetPaymentId()) {
            PaymentId paymentId = row.getId().getPaymentId();
            return generateKey(row.getListType(), row.getListName(), row.getValue(), paymentId.getPartyId(),
                    paymentId.getShopId());
        } else if (row.isSetId() && row.getId().isSetP2pId()) {
            P2pId p2pId = row.getId().getP2pId();
            return generateKey(row.getListType(), row.getListName(), row.getValue(), RowType.P_2_P,
                    p2pId.getIdentityId());
        }
        return generateKey(row.getListType(), row.getListName(), row.getValue(), row.getPartyId(), row.getShopId());
    }

    public static String generateKey(ListType listType, String listName, String value, String... params) {
        StringBuilder stringBuilder = new StringBuilder();
        if (params != null) {
            for (String param : params) {
                addIfExist(param, stringBuilder);
            }
        }
        return stringBuilder
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
