package com.rbkmoney.wb.list.manager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Row {

    private String bucketName;
    private String key;
    private String value;

}
