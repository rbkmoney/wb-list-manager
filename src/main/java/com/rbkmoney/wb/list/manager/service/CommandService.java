package com.rbkmoney.wb.list.manager.service;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.Event;

public interface CommandService {

    Event apply(ChangeCommand command);

}
