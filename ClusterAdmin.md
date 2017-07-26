# Cluster administration
This document describes how to use the CLI to administer running clusters. Refer
to the Install document for details on how to create new clusters.

As a reminder, a single DeploymentManager can administer multiple CDAS clusters.

## Prerequisites
cerebro_cli must be installed and configured to the DeploymentManager. You can ensure
connectivity with
```
cerebro_cli status
```

## Scaling an existing cluster
An existing cluster can be scaled to a new size (must be >= 1). This can be done
from the CLI using the update command:
```
cerebro_cli clusters update --numNodes=<desired size> <CLUSTER_ID>
# For example to scale cluster 1 to 20 nodes,
cerebro_cli clusters update --numNodes=20 1
```

This can be used to scale a cluster up or down. Cerebro will manage the life cycle
of the underlying machines, launching new ones and terminating scaled down ones
as required.

Available in: 0.4.0+. The cluster must have been created using the --launchScript
option.

## Termination protection
Clusters can be marked to have termination protection enabled. If enabled, this
will prevent the cluster from being scaled or terminated without first explicitly
disabling it (and then probably enabling it again). This is to prevent accidentally
misconfiguring a running cluster. To do so:

```
cerebro_cli clusters update --terminationProtectionEnabled=<true/false> <CLUSTER_ID>
# For example, to enable it for cluster 5
cerebro_cli clusters update --terminationProtectionEnabled=true 5
TODO: need to implement CLI commands
```

## Upgrading an existing cluster to a new version of CDAS
An existing cluster can be upgraded with new version of CDAS components.
The CLI command can be used to upgrade one or more components. After upgrade
the cluster is restarted with the newer components and features. 
Both version and components options cannot be specified at the same time.
  
To upgrade all components in an existing cluster to a new version, specify version only.  
To upgrade specific components to a different version, specify components only.
```
cerebro_cli clusters upgrade --components=<comp:version list> <CLUSTER_ID>

# For example to upgrade cluster 2 components cdas to version 0.4.0 and catalog-ui to 0.4.5,
cerebro_cli clusters upgrade --components=cdas:0.4.0,catalog-ui:0.4.5 2 

# To upgrade all CDAS components in cluster 3 to version 0.4.5.
cerebro_cli clusters upgrade --version=0.4.5 3 
```


Available in: 0.4.0+.

Note that this feature is supported going forward.
Upgrading from version 0.3.0 to 0.4.0 is not supported.
