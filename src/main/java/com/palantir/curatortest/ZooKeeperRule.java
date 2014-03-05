package com.palantir.curatortest;

/*
 * Copyright 2013 Palantir Technologies, Inc. All rights reserved.
 */

import java.util.List;
import java.util.UUID;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * JUnit rule to create a zookeeper server and to create curator instances to
 * connect to the zookeeper server. The zookeeper server will be started before
 * the {@link Statement} that the rule wraps, and closed after the
 * {@link Statement} runs (read the javadoc for the concrete subclasses for
 * caveats when running multiple tests concurrently).
 * <p>
 * An open port must be specified when creating the {@link ZooKeeperRule}.
 * <p>
 * A namespace is used so that in the case that multiple tests are using the
 * same {@link NoJMXZooKeeperServer}, their operations on the server won't
 * collide. If a namespace is not provided, then a random namespace will be
 * used, NOT the root namespace.
 *
 * @author juang
 */
public abstract class ZooKeeperRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperRule.class);

    /**
     * The port to be used in the zero-argument constructor if the system
     * property is not set.
     */
    private static final int DEFAULT_PORT = 9500;
    private static final String PORT_SYSTEM_PROPERTY_NAME = "zookeeper.test.port";

    private final List<CuratorFramework> curatorClients = Lists.newCopyOnWriteArrayList();

    protected final int port;
    protected final String namespace;
    protected final ZooKeeperServerWrapper serverWrapper;

    public ZooKeeperRule() {
        this(generateRandomNamespace(), getPort(), new DefaultZooKeeperServerWrapper());
    }

    /**
     * Creates a zookeeper rule that sets up a server.
     */
    public ZooKeeperRule(String namespace, int port, ZooKeeperServerWrapper serverWrapper) {
        Preconditions.checkNotNull(namespace);

        if (port <= 0) {
            throw new RuntimeException("Port number must be positive");
        }

        String format = "Creating ZooKeeperRule with namespace: {},  port: {}, and serverWrapper: {}";
        LOGGER.debug(format, namespace, port, serverWrapper.getClass().getName());

        this.port = port;
        this.namespace = namespace;
        this.serverWrapper = serverWrapper;
    }

    public CuratorFramework getClient() {
        return getClient(new ExponentialBackoffRetry(1000, 3));
    }

    /**
     * Returns a {@link CuratorFramework} with the
     * {@link Builder#connectString(String)} and
     * {@link Builder#namespace(String)} already set. The
     * {@link CuratorFramework} will already be started, and will be closed
     * automatically.
     */
    public CuratorFramework getClient(RetryPolicy retryPolicy) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder = builder.connectString("127.0.0.1:" + port);
        builder = builder.retryPolicy(retryPolicy);
        builder = builder.namespace(this.namespace);

        CuratorFramework client = builder.build();
        client.start();

        curatorClients.add(client);

        return client;
    }

    @Override
    protected void before() {
        // do nothing
    }

    @Override
    protected void after() {
        LOGGER.debug("Closing {} curator clients", curatorClients.size());

        for (CuratorFramework client : curatorClients) {
            if (client.getState() == CuratorFrameworkState.STARTED) {
                client.close();
            }
        }
    }

    public static String generateRandomNamespace() {
        return UUID.randomUUID().toString();
    }

    public static int getPort() {
        return Integer.getInteger(PORT_SYSTEM_PROPERTY_NAME, DEFAULT_PORT);
    }
}
