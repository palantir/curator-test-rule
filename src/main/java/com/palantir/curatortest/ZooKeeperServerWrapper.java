package com.palantir.curatortest;

/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

import org.apache.zookeeper.server.ZooKeeperServer;

/**
 * Wrapper around {@link ZooKeeperServer} that supports starting the server on a
 * port, and shutting down the server.
 *
 * @author juang
 */
public interface ZooKeeperServerWrapper {
    void startServer(int port);

    void shutdownServer();
}
