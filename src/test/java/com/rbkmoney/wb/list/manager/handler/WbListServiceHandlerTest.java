package com.rbkmoney.wb.list.manager.handler;

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
        boolean exist = wbListServiceHandler.isExist("partyId", "shopId", "listName", "value");
        Assert.assertTrue(exist);

        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.empty());
        exist = wbListServiceHandler.isExist("partyId", "shopId", "listName", "value");
        Assert.assertFalse(exist);
    }
}