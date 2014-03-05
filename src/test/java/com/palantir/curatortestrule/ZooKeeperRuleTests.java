/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link ZooKeeperRule}.
 *
 * @author juang
 */
public final class ZooKeeperRuleTests {

    @Rule
    public final ZooKeeperRule zooKeeperRule = new SharedZooKeeperRule();

    @Rule
    public final ZooKeeperRule zooKeeperRule2 = new SharedZooKeeperRule();

    @Test
    public void testCreateData() throws Exception {
        CuratorFramework client = zooKeeperRule.getClient();

        String path = "testpath";

        byte[] data = new byte[] { 1 };
        client.create().forPath(path, data);
        assertArrayEquals(data, client.getData().forPath(path));
        client.delete().forPath(path);
        assertNull(client.checkExists().forPath(path));
    }

}
