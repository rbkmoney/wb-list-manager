package com.rbkmoney.wb.list.manager;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.rbkmoney.wb.list.manager.config.RiakConfig;
import com.rbkmoney.wb.list.manager.extension.RiakContainerExtension;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class, RiakContainerExtension.class})
@ContextConfiguration(classes = {ListRepository.class, RiakConfig.class})
@TestPropertySource(properties = {"riak.bucket=wblist", "riak.address=localhost"})
public class RiakTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Value("${riak.bucket}")
    private String bucketName;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private RiakClient client;

    @Test
    void riakTest() throws ExecutionException, InterruptedException {
        sleep(10000);

        Row row = new Row();
        row.setKey(KEY);
        row.setValue(VALUE);
        listRepository.create(row);

        Namespace ns = new Namespace(bucketName);
        Location location = new Location(ns, KEY);
        FetchValue fv = new FetchValue.Builder(location).build();
        FetchValue.Response response = client.execute(fv);
        RiakObject obj = response.getValue(RiakObject.class);

        String result = obj.getValue().toString();
        assertEquals(VALUE, result);

        Optional<Row> resultGet = listRepository.get(KEY);
        assertFalse(resultGet.isEmpty());
        assertEquals(VALUE, resultGet.get().getValue());

        listRepository.remove(row);
        response = client.execute(fv);
        obj = response.getValue(RiakObject.class);
        assertNull(obj);
    }

}
