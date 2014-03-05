package com.palantir.curatortestrule;

/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

import com.google.common.io.Files;

/**
 * Default implementation of {@link ZooKeeperServerWrapper} which uses a
 * {@link NoJMXZooKeeperServer} and uses temp directories for the
 * {@link FileTxnSnapLog}.
 *
 * @author juang
 */
public final class DefaultZooKeeperServerWrapper implements ZooKeeperServerWrapper {

    private ServerCnxnFactory cnxnFactory;

    @Override
    public void startServer(int port) {
        ZooKeeperServer zkServer = new NoJMXZooKeeperServer();

        FileTxnSnapLog ftxn;
        try {
            ftxn = new FileTxnSnapLog(Files.createTempDir(), Files.createTempDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        zkServer.setTxnLogFactory(ftxn);

        try {
            this.cnxnFactory = ServerCnxnFactory.createFactory();
            this.cnxnFactory.configure(new InetSocketAddress(port), cnxnFactory.getMaxClientCnxnsPerHost());
            this.cnxnFactory.startup(zkServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdownServer() {
        if (this.cnxnFactory == null) {
            throw new IllegalStateException();
        }

        this.cnxnFactory.shutdown();
    }

}
