/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import java.util.Map;

import javax.annotation.CheckForNull;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

/**
 * A {@link ZooKeeperRule} that shares servers based on the port number specified in the
 * constructor.
 * <p>
 * If the server on the specified port has not been created by a {@link SharedZooKeeperRule}, then a
 * new server will be started. If another {@link SharedZooKeeperRule} is using that same port at the
 * same time (during concurrent execution), then that same server will be connected to. A
 * {@link ZooKeeperRuleConfig} started by this class will be closed after execution of the JUnit
 * {@link Statement} only if it is the last {@link SharedZooKeeperRule} that references it.
 * <p>
 * If port 0 is specified, then the port provided by the OS will be used. Unlike
 * {@link LocalZooKeeperRule}, the same server will be shared among all tests specifying port 0.
 * <p>
 * WARNING: Since {@link ZooKeeperRuleConfig} instances are shared, behavior is undefined if two
 * different {@link SharedZooKeeperRule}s share a server on the same port while expecting different
 * {@link ZooKeeperRuleConfig} implementations.
 *
 * @author juang
 */
public final class SharedZooKeeperRule extends ZooKeeperRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedZooKeeperRule.class);

    private static final SharedServerManager SHARED_SERVER_MANAGER = new SharedServerManager();

    @CheckForNull
    private ServerCnxnFactory cnxnFactory;

    public SharedZooKeeperRule() {
        super();
    }

    public SharedZooKeeperRule(String namespace, int port, ZooKeeperRuleConfig serverWrapper) {
        super(namespace, port, serverWrapper);
    }

    @Override
    protected void before() {
        super.before();

        this.cnxnFactory = SHARED_SERVER_MANAGER.acquireServer(this.ruleConfig, port);
    }

    @Override
    protected void after() {
        if (this.cnxnFactory != null) {
            LOGGER.debug("Closing ZooKeeper server at port {}", this.cnxnFactory.getLocalPort());

            SHARED_SERVER_MANAGER.releaseServer(port);
        } else {
            LOGGER.debug("Cannot close ZooKeeper server. It is likely that it had trouble starting.");
        }

        super.after();
    }

    @Override
    protected ServerCnxnFactory getCnxnFactory() {
        if (this.cnxnFactory == null) {
            throw new IllegalStateException();
        }

        return cnxnFactory;
    }

    private static final class SharedServerManager {
        private final Multiset<Integer> portReferenceCounts = HashMultiset.create();
        private final Map<Integer, ServerCnxnFactory> servers = Maps.newHashMap();

        private synchronized ServerCnxnFactory acquireServer(ZooKeeperRuleConfig ruleConfig, int port) {
            int prevCount = portReferenceCounts.count(port);

            if (prevCount == 0) {
                LOGGER.debug("Starting new ZooKeeper server at port {}", port);

                ServerCnxnFactory cnxnFactory = ruleConfig.getServer(port);

                if (port == 0) {
                    LOGGER.debug("ZooKeeper server bound to 0 actually started at port {}", cnxnFactory.getLocalPort());
                } else {
                    LOGGER.debug("ZooKeeper server started at port {}", cnxnFactory.getLocalPort());
                }

                servers.put(port, cnxnFactory);
            } else {
                LOGGER.debug("Using existing ZooKeeper server at port {}", port);
            }

            portReferenceCounts.add(port);

            return servers.get(port);
        }

        private synchronized void releaseServer(int port) {
            portReferenceCounts.remove(port);

            if (portReferenceCounts.count(port) == 0) {
                ServerCnxnFactory cnxnFactory = servers.remove(port);

                LOGGER.debug("Closing ZooKeeper server at port {}", cnxnFactory.getLocalPort());

                cnxnFactory.shutdown();
            }
        }
    }
}
