package com.rbkmoney.wb.list.manager;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.rbkmoney.wb.list.manager.config.RiakConfig;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@ContextConfiguration(classes = {ListRepository.class, RiakConfig.class})
public class RiakTest extends AbstractRiakIntegrationTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Value("${riak.bucket}")
    private String BUCKET_NAME;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private RiakClient client;

    @Test
    public void riakTest() throws ExecutionException, InterruptedException {
        Row row = new Row();
        row.setKey(KEY);
        row.setValue(VALUE);
        listRepository.create(row);

        Namespace ns = new Namespace(BUCKET_NAME);
        Location location = new Location(ns, KEY);
        FetchValue fv = new FetchValue.Builder(location).build();
        FetchValue.Response response = client.execute(fv);
        RiakObject obj = response.getValue(RiakObject.class);

        String result = obj.getValue().toString();
        Assert.assertEquals(VALUE, result);

        Optional<Row> resultGet = listRepository.get(KEY);
        Assert.assertFalse(resultGet.isEmpty());
        Assert.assertEquals(VALUE, resultGet.get().getValue());

        listRepository.remove(row);
        response = client.execute(fv);
        obj = response.getValue(RiakObject.class);
        Assert.assertNull(obj);

    }

}
