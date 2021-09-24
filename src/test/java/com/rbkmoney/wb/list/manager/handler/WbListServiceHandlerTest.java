package com.rbkmoney.wb.list.manager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.Result;
import com.rbkmoney.wb.list.manager.TestObjectFactory;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class WbListServiceHandlerTest {

    private WbListServiceHandler wbListServiceHandler;

    @Mock
    private ListRepository listRepository;
    private com.rbkmoney.damsel.wb_list.Row row;

    @BeforeEach
    void setUp() {
        wbListServiceHandler = new WbListServiceHandler(listRepository, new ObjectMapper());
        Row rowValue = new Row();
        rowValue.setValue(TestObjectFactory.randomString());
        row = TestObjectFactory.testRow();
        row.setValue(rowValue.getValue());
        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.of(rowValue));
    }

    @Test
    void isExist() throws TException {
        boolean exist = wbListServiceHandler.isExist(row);
        assertTrue(exist);

        Mockito.when(listRepository.get(anyString())).thenReturn(Optional.empty());
        exist = wbListServiceHandler.isExist(row);
        assertFalse(exist);
    }

    @Test
    void getRowInfo() {
        Result result = wbListServiceHandler.getRowInfo(row);
        assertNotNull(result);
    }

    @Test
    void isAllExist() throws TException {
        ArrayList<com.rbkmoney.damsel.wb_list.Row> list = new ArrayList<>();
        boolean exist = wbListServiceHandler.isAllExist(list);
        assertTrue(exist);

        list.add(row);
        list.add(row);
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

        list.add(row);
        list.add(row);
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

        list.add(row);
        list.add(row);
        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        assertTrue(wbListServiceHandler.isNotOneExist(list));

        Mockito.when(listRepository.get(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Row()));
        assertFalse(wbListServiceHandler.isNotOneExist(list));
    }
}