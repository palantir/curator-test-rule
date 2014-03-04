package com.palantir.curatortest;

/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

import org.apache.zookeeper.server.ZooKeeperServer;

/**
 * A subclass of {@link ZooKeeperServer} that skips JMX related operations.
 * 
 * @author juang
 */
public final class NoJMXZooKeeperServer extends ZooKeeperServer {

    @Override
    protected void registerJMX() {
        // don't do jmx-related things in testing classes
    }

    @Override
    protected void unregisterJMX() {
        // don't do jmx-related things in testing classes
    }
}
