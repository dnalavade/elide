/*
 * Copyright 2021, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package com.yahoo.elide.graphql.subscriptions;

import com.yahoo.elide.core.PersistentResource;
import com.yahoo.elide.core.request.EntityProjection;
import com.yahoo.elide.graphql.Environment;
import com.yahoo.elide.graphql.NonEntityDictionary;
import com.yahoo.elide.graphql.containers.NodeContainer;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * Data Fetcher which fetches Elide subscription models.
 */
@Slf4j
public class SubscriptionDataFetcher implements DataFetcher<Object> {

    private final NonEntityDictionary nonEntityDictionary;
    private final Integer bufferSize;

    /**
     * Constructor
     * @param nonEntityDictionary Entity dictionary for types that are not Elide models.
     */
    public SubscriptionDataFetcher(NonEntityDictionary nonEntityDictionary) {
        this(nonEntityDictionary, 100);
    }

    /**
     * Constructor
     * @param nonEntityDictionary Entity dictionary for types that are not Elide models.
     * @param bufferSize Internal buffer for reactive streams.
     */
    public SubscriptionDataFetcher(NonEntityDictionary nonEntityDictionary, int bufferSize) {
        this.nonEntityDictionary = nonEntityDictionary;
        this.bufferSize = bufferSize;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        /* build environment object, extracts required fields */
        Environment context = new Environment(environment, nonEntityDictionary);

        /* safe enable debugging */
        if (log.isDebugEnabled()) {
            //TODO refactor logging for common logging.
        }

        if (context.isRoot()) {
            String entityName = context.field.getName();
            String aliasName = context.field.getAlias();
            EntityProjection projection = context.requestScope
                    .getProjectionInfo()
                    .getProjection(aliasName, entityName);

            Flowable<PersistentResource> recordPublisher =
                    PersistentResource.loadRecords(projection, new ArrayList<>(), context.requestScope)
                            .toFlowable(BackpressureStrategy.BUFFER)
                            .onBackpressureBuffer(bufferSize, true, false);

            return recordPublisher.map(NodeContainer::new);
        }

        //If this is not the root, instead of retuning a reactive publisher, we process same
        //as the PersistentResourceFetcher.
        return context.container.processFetch(context);
    }
}