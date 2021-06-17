package com.rbkmoney.wb.list.manager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.*;
import com.rbkmoney.wb.list.manager.constant.RowType;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.exception.UnknownRowTypeException;
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
        try {
            return getCascadeRow(row).isPresent();
        } catch (RiakExecutionException | UnknownRowTypeException e) {
            log.error("WbListServiceHandler error when isExist row: {} e: ", row, e);
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
        Optional<com.rbkmoney.wb.list.manager.model.Row> result = getCascadeRow(row);
        if (result.isPresent() && row.getListType() == ListType.grey) {
            log.info("WbListServiceHandler getRowInfo result: {} isPresent=true!", result);
            try {
                CountInfoModel countInfoModel = objectMapper.readValue(result.get().getValue(), CountInfoModel.class);
                return new Result().setRowInfo(RowInfo.count_info(new CountInfo()
                        .setCount(countInfoModel.getCount())
                        .setTimeToLive(countInfoModel.getTtl())
                        .setStartCountTime(countInfoModel.getStartCountTime())
                ));
            } catch (IOException e) {
                log.error("Error when parse count info for row: {} e: ", row, e);
            }
        }
        log.info("WbListServiceHandler getRowInfo row: {} result: {} not present!", row, result);
        return new Result();
    }

    private Optional<com.rbkmoney.wb.list.manager.model.Row> getCascadeRow(Row row) {
        if (row.isSetId() && row.getId().isSetPaymentId()) {
            PaymentId paymentId = row.getId().getPaymentId();
            return cascadeGetRow(row.getListType(), row.getListName(), row.getValue(), paymentId.getPartyId(),
                    paymentId.getShopId());
        } else if (row.isSetId() && row.getId().isSetP2pId()) {
            P2pId p2pId = row.getId().getP2pId();
            return cascadeGetRow(row.getListType(), row.getListName(), row.getValue(), RowType.P_2_P,
                    p2pId.getIdentityId());
        }
        return cascadeGetRow(row.list_type, row.list_name, row.value, row.getPartyId(), row.getShopId());
    }

    private Optional<com.rbkmoney.wb.list.manager.model.Row> cascadeGetRow(ListType listType, String listName,
                                                                           String value, String partyId,
                                                                           String shopId) {
        return Optional.ofNullable(
                listRepository.get(KeyGenerator.generateKey(listType, listName, value))
                        .orElse(listRepository.get(KeyGenerator.generateKey(listType, listName, value, partyId))
                                .orElse(listRepository
                                        .get(KeyGenerator.generateKey(listType, listName, value, partyId, shopId))
                                        .orElse(null))));
    }

}
