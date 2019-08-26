package com.rbkmoney.wb.list.manager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.*;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.model.CountInfoModel;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class WbListServiceHandler implements WbListServiceSrv.Iface {

    private final ListRepository listRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isExist(Row row) throws TException {
        return checkExist(KeyGenerator.generateKey(row.list_type, row.list_name, row.value))
                || checkExist(KeyGenerator.generateKey(row.party_id, row.list_type, row.list_name, row.value))
                || checkExist(KeyGenerator.generateKey(row));
    }

    private boolean checkExist(String key) throws TException {
        try {
            boolean present = listRepository.get(key).isPresent();
            log.info("WbListServiceHandler isExist key: {} result: {}", key, present);
            return present;
        } catch (RiakExecutionException e) {
            log.info("WbListServiceHandler error when isExist key: {} e: ", key, e);
            throw new TException(e);
        }
    }

    @Override
    public boolean isAllExist(List<Row> list) throws TException {
        if (list != null && !list.isEmpty()) {
            for (Row row : list) {
                if (!isExist(row)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isAnyExist(List<Row> list) throws TException {
        if (list != null && !list.isEmpty()) {
            for (Row row : list) {
                if (isExist(row)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isNotOneExist(List<Row> list) throws TException {
        return !isAnyExist(list);
    }

    @Override
    public Result getRowInfo(Row row) throws ListNotFound, TException {
        log.info("WbListServiceHandler getRowInfo row: {}", row);
        String key = KeyGenerator.generateKey(row);
        Optional<com.rbkmoney.wb.list.manager.model.Row> result = listRepository.get(key);
        if (result.isPresent()) {
            log.info("WbListServiceHandler getRowInfo key: {} result: {} isPresent=true!", key, result);
            try {
                CountInfoModel countInfoModel = objectMapper.readValue(result.get().getValue(), CountInfoModel.class);
                return new Result().setRowInfo(RowInfo.count_info(new CountInfo()
                        .setCount(countInfoModel.getCount())
                        .setTimeToLive(countInfoModel.getTtl())
                        .setStartCountTime(countInfoModel.getStartCountTime())
                ));
            } catch (IOException e) {
                log.error("Error when parse count info for key: {} e: ", key, e);
            }
        }
        log.info("WbListServiceHandler getRowInfo key: {} result: {} not present!", key, result);
        return new Result();
    }

}
