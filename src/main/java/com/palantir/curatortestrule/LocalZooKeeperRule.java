/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import javax.annotation.CheckForNull;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link ZooKeeperRule} that gets a {@link ServerCnxnFactory} from
 * {@link ZooKeeperRuleConfig} on the port before running the statement, and shutdowns the
 * {@link ServerCnxnFactory} after the statement.
 * <p>
 * If port 0 is specified, then the port provided by the OS will be used. Therefore, multiple tests
 * can bind to port 0 and a different server will be used for each.
 * <p>
 * WARNING: behavior is unspecified if two {@link LocalZooKeeperRule}s use the same non-0 port in parallel.
 *
 * @author juang
 */
public final class LocalZooKeeperRule extends ZooKeeperRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalZooKeeperRule.class);

    @CheckForNull
    private ServerCnxnFactory cnxnFactory;

    public LocalZooKeeperRule() {
        super();
    }

    public LocalZooKeeperRule(String namespace, int port, ZooKeeperRuleConfig serverWrapper) {
        super(namespace, port, serverWrapper);
    }

    @Override
    protected void before() {
        super.before();

        LOGGER.debug("Starting new ZooKeeper server at port {}", port);

        this.cnxnFactory = this.ruleConfig.getServer(port);

        if (port == 0) {
            LOGGER.debug("ZooKeeper server bound to 0 actually started at port {}", this.cnxnFactory.getLocalPort());
        } else {
            LOGGER.debug("ZooKeeper server started at port {}", this.cnxnFactory.getLocalPort());
        }
    }

    @Override
    protected void after() {
        if (this.cnxnFactory != null) {
            LOGGER.debug("Closing ZooKeeper server at port {}", this.cnxnFactory.getLocalPort());

            this.cnxnFactory.shutdown();
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

        return this.cnxnFactory;
    }
}
