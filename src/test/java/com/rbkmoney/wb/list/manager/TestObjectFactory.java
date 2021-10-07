package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.*;
import com.rbkmoney.wb.list.manager.utils.ChangeCommandWrapper;

import java.time.Instant;
import java.util.UUID;

public abstract class TestObjectFactory {

    public static com.rbkmoney.damsel.wb_list.Row testRow() {
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(randomString())
                .setPartyId(randomString())
        ));
        row.setListType(ListType.black);
        row.setListName(randomString());
        row.setValue(randomString());
        row.setPartyId(row.getId().getPaymentId().getPartyId());
        row.setShopId(row.getId().getPaymentId().getShopId());
        return row;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static ChangeCommandWrapper testCommand() {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        com.rbkmoney.damsel.wb_list.Row row = testRow();
        changeCommand.setRow(row);
        return changeCommand;
    }

    public static Row testRowWithCountInfo(String startTimeCount) {
        Row row = testRow();
        row.setRowInfo(RowInfo.count_info(
                new CountInfo()
                        .setCount(5L)
                        .setStartCountTime(startTimeCount)
                        .setTimeToLive(Instant.now().plusSeconds(6000L).toString()))
        );
        return row;
    }
}
