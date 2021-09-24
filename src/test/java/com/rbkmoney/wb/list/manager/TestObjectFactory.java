package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.Command;
import com.rbkmoney.damsel.wb_list.IdInfo;
import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.damsel.wb_list.PaymentId;
import com.rbkmoney.wb.list.manager.utils.ChangeCommandWrapper;

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
}
