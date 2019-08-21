package com.rbkmoney.wb.list.manager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;

public class WbListServiceHandlerTest {

    public static final String VALUE = "value";
    public static final String PARTY_ID = "partyId";
    public static final String SHOP_ID = "shopId";
    public static final String LIST_NAME = "listName";
    WbListServiceHandler wbListServiceHandler;

    @Mock
    ListRepository listRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        wbListServiceHandler = new WbListServiceHandler(listRepository, new ObjectMapper());
    }

    @Test
    public void isExist() throws TException {
        com.rbkmoney.damsel.wb_list.Row row = createRow();
        boolean exist = wbListServiceHandler.isExist(row);
        Assert.assertTrue(exist);

        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.empty());
        exist = wbListServiceHandler.isExist(row);
        Assert.assertFalse(exist);
    }

    @Test
    public void isAllExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isAllExist(list);
        Assert.assertTrue(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(new Row()));
        Assert.assertTrue(wbListServiceHandler.isAllExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.of(new Row()))
                .thenReturn(Optional.empty());
        Assert.assertFalse(wbListServiceHandler.isAllExist(list));
    }

    @Test
    public void isAnyExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isAnyExist(list);
        Assert.assertFalse(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.of(new Row()))
                .thenReturn(Optional.empty());
        Assert.assertTrue(wbListServiceHandler.isAnyExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        Assert.assertFalse(wbListServiceHandler.isAnyExist(list));
    }

    @Test
    public void isNoOneExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isNotOneExist(list);
        Assert.assertTrue(exist);

        list.add(createRow());
        list.add(createRow());
        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        Assert.assertTrue(wbListServiceHandler.isNotOneExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Row()));
        Assert.assertFalse(wbListServiceHandler.isNotOneExist(list));
    }

    @NotNull
    private com.rbkmoney.damsel.wb_list.Row createRow() {
        Row value = new Row();
        value.setValue(VALUE);
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(value));
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setPartyId(PARTY_ID);
        row.setShopId(SHOP_ID);
        row.setListType(ListType.black);
        row.setListName(LIST_NAME);
        row.setValue(VALUE);
        return row;
    }
}