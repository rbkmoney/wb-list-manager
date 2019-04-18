package com.rbkmoney.wb.list.manager.handler;

import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
        wbListServiceHandler = new WbListServiceHandler(listRepository);
    }

    @Test
    public void isExist() throws TException {
        Row value = new Row();
        value.setValue(VALUE);
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(value));
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setPartyId(PARTY_ID);
        row.setShopId(SHOP_ID);
        row.setListType(ListType.black);
        row.setListName(LIST_NAME);
        row.setValue(VALUE);
        boolean exist = wbListServiceHandler.isExist(row);
        Assert.assertTrue(exist);

        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.empty());
        exist = wbListServiceHandler.isExist(row);
        Assert.assertFalse(exist);
    }
}