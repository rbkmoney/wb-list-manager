package com.rbkmoney.wb.list.manager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.IdInfo;
import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.damsel.wb_list.PaymentId;
import com.rbkmoney.damsel.wb_list.Result;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class WbListServiceHandlerTest {

    public static final String VALUE = "value";
    public static final String PARTY_ID = "partyId";
    public static final String SHOP_ID = "shopId";
    public static final String LIST_NAME = "listName";
    private WbListServiceHandler wbListServiceHandler;

    @Mock
    private ListRepository listRepository;

    @BeforeEach
    void setUp() {
        wbListServiceHandler = new WbListServiceHandler(listRepository, new ObjectMapper());
    }

    @Test
    void isExist() throws TException {
        com.rbkmoney.damsel.wb_list.Row row = createRow();
        boolean exist = wbListServiceHandler.isExist(row);
        assertTrue(exist);

        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.empty());
        exist = wbListServiceHandler.isExist(row);
        assertFalse(exist);
    }

    @Test
    void getRowInfo() {
        com.rbkmoney.damsel.wb_list.Row row = createRow();
        Result result = wbListServiceHandler.getRowInfo(row);
        assertFalse(result == null);
    }

    @Test
    void isAllExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isAllExist(list);
        assertTrue(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(new Row()));
        assertTrue(wbListServiceHandler.isAllExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.of(new Row()))
                .thenReturn(Optional.empty());
        assertFalse(wbListServiceHandler.isAllExist(list));
    }

    @Test
    void isAnyExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isAnyExist(list);
        assertFalse(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.of(new Row()))
                .thenReturn(Optional.empty());
        assertTrue(wbListServiceHandler.isAnyExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        assertFalse(wbListServiceHandler.isAnyExist(list));
    }

    @Test
    void isNoOneExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isNotOneExist(list);
        assertTrue(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        assertTrue(wbListServiceHandler.isNotOneExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Row()));
        assertFalse(wbListServiceHandler.isNotOneExist(list));
    }

    private com.rbkmoney.damsel.wb_list.Row createRow() {
        Row value = new Row();
        value.setValue(VALUE);
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(value));
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID)
        ));
        row.setListType(ListType.black);
        row.setListName(LIST_NAME);
        row.setValue(VALUE);
        return row;
    }
}