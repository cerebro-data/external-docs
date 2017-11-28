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
