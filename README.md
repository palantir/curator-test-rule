curator-test-rule [![Build Status](https://travis-ci.org/palantir/curator-test-rule.png?branch=master)](https://travis-ci.org/palantir/curator-test-rule)
=================

Summary
-----------------
This project provides a JUnit `TestRule` that manages the startup and shutdown of the underlying `ZooKeeperServer`. The `TestRule` also includes a way of getting `CuratorFramework` instances that automatically connect and disconnect from the underlying `ZooKeeperServer` based on the scope of rule.

Why didn't we use [`curator-test`](https://curator.apache.org/curator-test/)
-----------------
* `curator-test` provides a `TestingServer` base class, which presents a problem when a testing class needs to extend two different base classes. Using a JUnit `TestRule` gives us multiple advantages, including the option to start and stop a test server once per test class (`@ClassRule`) or test method (`@Rule`).
* There was a race condition in `curator-test` where if we didn't specify a port, it would bind 0 to get a free port, then unbind from that port, then try to bind to that port later in the code.
* The underlying `ZooKeeperServer` timeout cannot be configured since we are locked into the configurations in `InstanceSpec`
* Because of a blocking `cnxnFactory.join()` call in `ZooKeeperServerMain#runFromConfig`, `curator-test` had to do several hacks like spawning a new thread and synchronizing on the status of the `ZooKeeperServer`

How to use it
-----------------

```java
public final class LocalZooKeeperRuleExample {

    @Rule                                                  // or @ClassRule
    public ZooKeeperRule rule1 = new LocalZooKeeperRule(); // or SharedZooKeeperRule()

    @Test
    public void testCase() {
        CuratorFramework client = rule1.getClient();
        client.getState();

        // do something
    }
}
```


There are two subclasses of `ZooKeeperRule` as of now: `LocalZooKeeperRule` and `SharedZooKeeperRule`. `LocalZooKeeperRule` starts and closes the underlying server based on the scope of the `TestRule`. `SharedZooKeeperRule` also does this for serial execution. For concurrent execution (such as by using a `ParallelSuite`), all `SharedZooKeeperRule`s using the same port will share the same underlying server.

Please read the Javadocs for `LocalZooKeeperRule` and `SharedZooKeeperRule` for some caveats regarding these classes.

License
-----------------
See LICENSE.txt
