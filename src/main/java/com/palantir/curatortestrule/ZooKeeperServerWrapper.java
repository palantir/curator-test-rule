/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import org.apache.zookeeper.server.ZooKeeperServer;

/**
 * Wrapper around {@link ZooKeeperServer} that supports starting the server on a port, and shutting
 * down the server.
 *
 * @author juang
 */
public interface ZooKeeperServerWrapper {
    void startServer(int port);

    void shutdownServer();
}
