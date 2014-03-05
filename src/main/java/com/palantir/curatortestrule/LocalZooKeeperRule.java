/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link ZooKeeperRule} that starts the {@link ZooKeeperServerWrapper} on the port before
 * running the statement, and shutdowns the {@link ZooKeeperServerWrapper} after the statement. TODO
 * Figure out what happens when an exception is thrown in {@link #before()}.
 *
 * @author juang
 */
public final class LocalZooKeeperRule extends ZooKeeperRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalZooKeeperRule.class);

    public LocalZooKeeperRule() {
        super();
    }

    public LocalZooKeeperRule(String namespace, int port, ZooKeeperServerWrapper serverWrapper) {
        super(namespace, port, serverWrapper);
    }

    @Override
    protected void before() {
        super.before();

        LOGGER.debug("Starting new ZooKeeper server at port {}", port);

        this.serverWrapper.startServer(port);
    }

    @Override
    protected void after() {
        super.after();

        LOGGER.debug("Closing ZooKeeper server at port {}", port);

        this.serverWrapper.shutdownServer();
    }
}
