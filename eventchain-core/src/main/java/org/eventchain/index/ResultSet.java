/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.index;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Iterator;

public class ResultSet<O> extends com.googlecode.cqengine.resultset.ResultSet<O> implements AutoCloseable {
    private final com.googlecode.cqengine.resultset.ResultSet resultSet;

    public ResultSet(com.googlecode.cqengine.resultset.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public Iterator<O> iterator() {
        return resultSet.iterator();
    }

    @Override
    public boolean contains(O object) {
        return resultSet.contains(object);
    }

    @Override
    public boolean matches(O object) {
        return resultSet.matches(object);
    }

    @Override
    public Query<O> getQuery() {
        return resultSet.getQuery();
    }

    @Override
    public QueryOptions getQueryOptions() {
        return resultSet.getQueryOptions();
    }

    @Override
    public int getRetrievalCost() {
        return resultSet.getRetrievalCost();
    }

    @Override
    public int getMergeCost() {
        return resultSet.getMergeCost();
    }

    @Override
    public int size() {
        return resultSet.size();
    }

    @Override
    public void close() {
        resultSet.close();
    }
}
