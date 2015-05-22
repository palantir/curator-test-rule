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

    void cleanup();
}
