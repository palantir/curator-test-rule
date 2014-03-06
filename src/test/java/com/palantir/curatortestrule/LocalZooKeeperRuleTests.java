/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.curatortestrule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;

/**
 * Tests for {@link LocalZooKeeperRule}.
 *
 * @author juang
 */
public final class LocalZooKeeperRuleTests {

    @Test
    public void testConnectToServer() throws Exception {
        LocalZooKeeperRule rule1 = new LocalZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            CuratorFramework client = rule1.getClient();

            String path = "testpath";
            byte[] data = new byte[] { 1 };
            try {
                client.create().forPath(path, data);
                assertArrayEquals(data, client.getData().forPath(path));
                client.delete().forPath(path);
                assertNull(client.checkExists().forPath(path));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } finally {
            rule1.after();
        }
    }

    @Test
    public void testDoubleBindToSamePort() throws Exception {
        final LocalZooKeeperRule rule1 = new LocalZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());
        final LocalZooKeeperRule rule2 = new LocalZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            try {
                rule2.before();
                fail();
            } catch (RuntimeException e) {
                // expected
            } finally {
                rule2.after();
            }

        } finally {
            rule1.after();
        }
    }

    @Test
    public void testBindToPortZero() throws Exception {
        final LocalZooKeeperRule rule1 = new LocalZooKeeperRule("namespace1", 0, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();
            assertTrue(rule1.getCnxnFactory().getLocalPort() != 0);
        } finally {
            rule1.after();
        }
    }

    @Test
    public void testDoubleBindToPortZero() throws Exception {
        final LocalZooKeeperRule rule1 = new LocalZooKeeperRule("namespace1", 0, new DefaultZooKeeperRuleConfig());
        final LocalZooKeeperRule rule2 = new LocalZooKeeperRule("namespace1", 0, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            try {
                rule2.before();

                assertNotEquals(0, rule1.getCnxnFactory().getLocalPort());
                assertNotEquals(0, rule2.getCnxnFactory().getLocalPort());

                assertNotEquals(rule1.getCnxnFactory().getLocalPort(), rule2.getCnxnFactory().getLocalPort());
            } finally {
                rule2.after();
            }
        } finally {
            rule1.after();
        }
    }
}
