# Cluster Administration

This document describes how to use the CLI to administer running clusters. Refer to
the [Installation](Install.md) document for details on how to create new clusters.

As a reminder, a single DeploymentManager can administer multiple CDAS clusters.

## Prerequisites

`cerebro_cli` must be installed and configured to the DeploymentManager. You can ensure
connectivity with

```shell
cerebro_cli status
```

## Scaling an Existing Cluster

An existing cluster can be scaled to a new size (must be >= 1). This can be done from
the CLI using the update command:

```shell
cerebro_cli clusters update --numNodes=<desired size> <CLUSTER_ID>
# For example to scale cluster 1 to 20 nodes,
cerebro_cli clusters update --numNodes=20 1
```

This can be used to scale a cluster up or down. Cerebro will manage the life cycle of
the underlying machines, launching new ones and terminating scaled down ones as required.

Available in: 0.4.0+. The cluster must have been created using the `--launchScript` option.

## Setting the Number of Planners

Cerebro will by default pick the number of planners to run but the optimal number
depends on the environment and workload. For users that want to fine tune the
cluster, the number can be controlled using the `clusters update` command.

```shell
cerebro_cli clusters update --numPlanners=<desired number> <CLUSTER_ID>
```

This number cannot exceed the cluster size and will require restarting the
cluster to take effect.

## Enable Termination Protection

Clusters can be marked to have termination protection enabled. If enabled, this will
prevent the cluster from being scaled or terminated without first explicitly disabling
it (and then probably enabling it again). This is to prevent accidentally
misconfiguring a running cluster. To do so:

```shell
cerebro_cli clusters update --terminationProtectionEnabled=<true/false> <CLUSTER_ID>
# For example, to enable it for cluster 5
cerebro_cli clusters update --terminationProtectionEnabled=true 5
```

## Upgrading an Existing Cluster

An existing cluster can be upgraded with new version of CDAS components. The CLI command
can be used to upgrade one or more components. After upgrade the cluster is restarted
with the newer components and features. Both version and components options cannot be
specified at the same time.

To upgrade all components in an existing cluster to a new version, specify version only.
To upgrade specific components to a different version, specify components only.

```shell
cerebro_cli clusters upgrade --components=<comp:version list> <CLUSTER_ID>

# For example to upgrade cluster 2's CDAS components to version 0.6.0 and web UI
# to 0.6.1:
cerebro_cli clusters upgrade --components=cdas:0.6.0,web-ui:0.6.1 2

# To upgrade all CDAS components in cluster 3 to version 0.4.5.
cerebro_cli clusters upgrade --version=0.4.5 3
```

### Set New Cluster Version

To configure the DeploymentManager so that new clusters use upgraded version of CDAS
components, use `cerebro_cli clusters set_default_version`.

Example: If your current version is 0.5.0, and would like to upgrade any new cluster to
be running version 0.5.0 of the product, but with the planner and workers updated to 0.5.1

```shell
cerebro_cli clusters set_default_version --version=0.5.0 --components=cdas:0.5.1
```

Note that `--components` option is required. It may be specified as an empty string("").

Available in: 0.4.0+.

Note that this feature is supported going forward. Upgrading from version 0.3.0 to 0.4.0
is not supported.


## Cluster status

To query the status of all of the clusters that a deployment manager controls, run
```shell
cerebro_cli clusters list
```

The output will look something like this:
```shell
cerebro_cli clusters list
description      id  name       numNodes  numRunningServices    owner    statusCode    statusMessage                                                                           type
-------------  ----  -------  ----------  --------------------  -------  ------------  --------------------------------------------------------------------------------------  ------------------
                137  cluster1          1  7/7                   admin    READY         All services running.                                                                   STANDALONE_CLUSTER
```

The value under "numRunningServices" is an indicator of which services are currently
passing their health checks. The services are always enumerated in a fixed order, enabling
an administrator to understand which services are up healthy at any given time.
The specific set of services running on a given cluster depends on the cluster's
configuration. Refer to the [Cluster Types](ClusterTypes.md) document for the
listing that applies to your given setup.

As an example, let's consider the situation where a standalone cluster
had an issue with the planner configuration such that the planner could not start up.
In that scenario, the number of running services would be listed as 3/7 as the first
three services (canary, zookeeper and catalog) would all be health checking successfully
while the fourth service (planner) would not. The state of services 5, 6 & 7 are not
known in that situation. Generally speaking, services with higher numbers depend
on a subset of the services with lower numbers, so a lower-numbered service having
issues likely precludes a higher numbered service from correctly providing its
full range of functionality. 

A given service successfully passing its health check does not
preclude it from returning an error on a given request. Rather, it indicates that
the service was able to startup successfully, including passing all of our initial
validations of said service's configurations. and that the service is responded
to the most recent deployment manager request to its health check endpoint.


## Synchronizing Roles and Permissions between Catalog/Sentry and Planner nodes

The Planner refreshes its roles from the Catalog every 60 seconds, and for this reason,
any changes made directly to the Sentry database in the Catalog, might take up to a minute to
reflect in the Planner nodes.

Planner nodes cache the role and permissions information from the Catalog. In cases where multiple
Planner nodes are present in a CDAS cluster, we follow a policy of eventual consistency for
maintaining cache coherence. This may be fine tuned in the future if needed.

