package com.rbkmoney.wb.list.manager.handler;

import com.rbkmoney.damsel.wb_list.Row;
import com.rbkmoney.damsel.wb_list.WbListServiceSrv;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

@Slf4j
@RequiredArgsConstructor
public class WbListServiceHandler implements WbListServiceSrv.Iface {

    private final ListRepository listRepository;

    @Override
    public boolean isExist(Row row) throws TException {
        String key = KeyGenerator.generateKey(row);
        try {
            return listRepository.get(key).isPresent();
        } catch (RiakExecutionException e) {
            throw new TException(e);
        }
    }

}
