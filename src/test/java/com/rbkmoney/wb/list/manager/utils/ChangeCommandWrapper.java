package com.rbkmoney.wb.list.manager.utils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.rbkmoney.damsel.wb_list.ChangeCommand;

@JsonFilter("myFilter")
public class ChangeCommandWrapper extends ChangeCommand {
}
