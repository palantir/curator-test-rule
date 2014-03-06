/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule.examples;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Rule;
import org.junit.Test;

import com.palantir.curatortestrule.LocalZooKeeperRule;
import com.palantir.curatortestrule.ZooKeeperRule;

/**
 * Example usage of {@link LocalZooKeeperRule}.
 *
 * @author juang
 */
public final class ZooKeeperRuleExample {

    @Rule                                                  // or @ClassRule
    public ZooKeeperRule rule1 = new LocalZooKeeperRule(); // or SharedZooKeeperRule

    @Test
    public void testCase() {
        CuratorFramework client = rule1.getClient();
        client.getState();

        // do something
    }
}
