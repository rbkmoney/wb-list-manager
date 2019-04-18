package com.rbkmoney.wb.list.manager.repository;

import com.rbkmoney.wb.list.manager.model.Row;

import java.util.Optional;

public interface CrudRepository<T, K> {

    void create(T row);

    void remove(T row);

    Optional<Row> get(K key);

}
