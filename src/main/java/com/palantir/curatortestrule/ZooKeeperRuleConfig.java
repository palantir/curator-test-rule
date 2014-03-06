/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;

/**
 * Defines an interface to get a {@link ServerCnxnFactory}.
 * <p>
 * This is used so that the {@link ZooKeeperServer} and {@link ServerCnxnFactory} can be customized.
 *
 * @author juang
 */
public interface ZooKeeperRuleConfig {

    ServerCnxnFactory getServer(int port);

}
