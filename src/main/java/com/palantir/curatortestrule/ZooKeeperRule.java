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

import java.util.List;
import java.util.UUID;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.junit.rules.ExternalResource;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * JUnit rule to create a zookeeper server and to create curator instances to connect to the
 * zookeeper server. The zookeeper server will be started before the {@link Statement} that the rule
 * wraps, and closed after the {@link Statement} runs (read the javadoc for the concrete subclasses
 * for caveats when running multiple tests concurrently).
 * <p>
 * An open port must be specified when creating the {@link ZooKeeperRule}.
 * <p>
 * A namespace is used so that in the case that multiple tests are using the same zookeeper server,
 * their operations on the server won't collide. If a namespace is not provided, then a random
 * namespace will be used, NOT the root namespace.
 *
 * @author juang
 */
public abstract class ZooKeeperRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperRule.class);

    /**
     * The port to be used in the zero-argument constructor if the system property is not set.
     */
    private static final int DEFAULT_PORT = 9500;
    private static final String PORT_SYSTEM_PROPERTY_NAME = "zookeeper.test.port";

    private final List<CuratorFramework> curatorClients = Lists.newCopyOnWriteArrayList();

    protected final int port;
    protected final String namespace;
    protected final ZooKeeperRuleConfig ruleConfig;

    public ZooKeeperRule() {
        this(generateRandomNamespace(), getDefaultPort(), new DefaultZooKeeperRuleConfig());
    }

    /**
     * Creates a zookeeper rule that sets up a server.
     */
    public ZooKeeperRule(String namespace, int port, ZooKeeperRuleConfig ruleConfig) {
        Preconditions.checkArgument(namespace != null);
        Preconditions.checkArgument(ruleConfig != null);

        if (port < 0) {
            throw new RuntimeException("Port number must be positive");
        }

        String format = "Creating ZooKeeperRule with namespace: {},  port: {}, and ruleConfig: {}";
        LOGGER.debug(format, namespace, port, ruleConfig.getClass().getName());

        this.port = port;
        this.namespace = namespace;
        this.ruleConfig = ruleConfig;
    }

    public CuratorFramework getClient() {
        return getClient(new ExponentialBackoffRetry(1000, 3));
    }

    /**
     * Returns a {@link CuratorFramework} with the {@link Builder#connectString(String)} and
     * {@link Builder#namespace(String)} already set. The {@link CuratorFramework} will already be
     * started, and will be closed automatically.
     */
    public CuratorFramework getClient(RetryPolicy retryPolicy) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder = builder.connectString("127.0.0.1:" + getCnxnFactory().getLocalPort());
        builder = builder.retryPolicy(retryPolicy);
        builder = builder.namespace(this.namespace);

        CuratorFramework client = builder.build();

        client.start();

        curatorClients.add(client);

        return client;
    }

    protected abstract ServerCnxnFactory getCnxnFactory();

    @Override
    protected void before() {
        // do nothing
    }

    @Override
    protected void after() {
        closeClients();
        closeServer();
        ruleConfig.cleanup();
    }

    protected void closeClients() {
        LOGGER.debug("Closing {} curator clients", curatorClients.size());
        for (CuratorFramework client : curatorClients) {
            if (client.getState() == CuratorFrameworkState.STARTED) {
                client.close();
            }
        }
    }

    protected abstract void closeServer();

    public static String generateRandomNamespace() {
        return UUID.randomUUID().toString();
    }

    public static int getDefaultPort() {
        return Integer.getInteger(PORT_SYSTEM_PROPERTY_NAME, DEFAULT_PORT);
    }
}
