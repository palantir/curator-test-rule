package com.palantir.curatortestrule;

/*
 * Copyright 2013 Palantir Technologies, Inc. All rights reserved.
 */

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

/**
 * A {@link ZooKeeperRule} that shares servers based on port numbers.
 * <p>
 * If the server on that port has not been created by a
 * {@link SharedZooKeeperRule}, then a new server will be started. If another
 * {@link SharedZooKeeperRule} is using that same port at the same time (during
 * concurrent execution), then that same server will be connected to. A
 * {@link ZooKeeperServerWrapper} started by this class will be closed after
 * execution of the JUnit {@link Statement} only if it is the last
 * {@link SharedZooKeeperRule} that references it.
 * <p>
 * WARNING: Since {@link ZooKeeperServerWrapper} instances are shared, behavior
 * is undefined if two different {@link SharedZooKeeperRule}s share a server on
 * the same port while expecting different {@link ZooKeeperServerWrapper} implementations.
 *
 * @author juang
 */
public final class SharedZooKeeperRule extends ZooKeeperRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedZooKeeperRule.class);

    private static final SharedServerManager SHARED_SERVER_MANAGER = new SharedServerManager();

    @Override
    protected void before() {
        super.before();

        SHARED_SERVER_MANAGER.startServer(this.serverWrapper, port);
    }

    @Override
    protected void after() {
        super.after();

        SHARED_SERVER_MANAGER.shutdownServer(port);
    }

    private static final class SharedServerManager {
        private final Multiset<Integer> portReferenceCounts = HashMultiset.create();
        private final Map<Integer, ZooKeeperServerWrapper> servers = Maps.newHashMap();

        private synchronized void startServer(ZooKeeperServerWrapper serverWrapper, int port) {
            int prevCount = portReferenceCounts.count(port);

            if (prevCount == 0) {
                LOGGER.debug("Starting new ZooKeeper server at port {}", port);

                serverWrapper.startServer(port);
                servers.put(port, serverWrapper);
            } else {
                LOGGER.debug("Using existing ZooKeeper server at port {}", port);
            }

            portReferenceCounts.add(port);
        }

        private synchronized void shutdownServer(int port) {
            portReferenceCounts.remove(port);

            if (portReferenceCounts.count(port) == 0) {
                ZooKeeperServerWrapper serverWrapper = servers.remove(port);

                LOGGER.debug("Closing ZooKeeper server at port {}", port);

                serverWrapper.shutdownServer();
            }
        }
    }
}
