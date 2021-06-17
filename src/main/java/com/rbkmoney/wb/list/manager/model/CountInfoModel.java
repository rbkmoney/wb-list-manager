package com.rbkmoney.wb.list.manager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountInfoModel {

    private Long count;
    private String ttl;
    private String startCountTime;

}
