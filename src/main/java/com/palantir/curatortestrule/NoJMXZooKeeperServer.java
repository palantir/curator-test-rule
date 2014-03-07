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
