/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

import com.google.common.io.Files;

/**
 * Default implementation of {@link ZooKeeperRuleConfig} which uses a
 * {@link NoJMXZooKeeperServer} and uses temp directories for the {@link FileTxnSnapLog}.
 *
 * @author juang
 */
public final class DefaultZooKeeperRuleConfig implements ZooKeeperRuleConfig {

    @Override
    public ServerCnxnFactory getServer(int port) {
        ZooKeeperServer zkServer = new NoJMXZooKeeperServer();

        FileTxnSnapLog ftxn;
        try {
            ftxn = new FileTxnSnapLog(Files.createTempDir(), Files.createTempDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        zkServer.setTxnLogFactory(ftxn);

        try {
            ServerCnxnFactory cnxnFactory = ServerCnxnFactory.createFactory();
            cnxnFactory.configure(new InetSocketAddress(port), cnxnFactory.getMaxClientCnxnsPerHost());
            cnxnFactory.startup(zkServer);

            return cnxnFactory;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
