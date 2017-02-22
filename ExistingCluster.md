# Connecting to an existing cluster
It is possible to connect the Deployment Manager to a running kubernetes cluster. In
this case, the Deployment Manager will provision Cerebro services on that cluster but
does not try to manage the cluster.

### Prerequisites
The cluster should already be running. The kube config file needs to be copied to
the machine running the Deployment Manager. This file is typically generated as
part of installing and starting up a kubernetes cluster. It contains cluster names
and various other credentials.

Copy the file to the Deployment Manager's external clusters directory. This is by default
'/etc/cerebro/clusters'.

### Registering the cluster
We can register the cluster by creating it from the CLI.
```shell
$ cerebro_cli clusters create --name=ExistingTest --type=CANARY_CLUSTER --numNodes=1 --existingCluster=<conf file name>
# For example:
$ cerebro_cli clusters create --name=ExistingTest --type=CANARY_CLUSTER --numNodes=1 --existingCluster=test_cluster.conf
# List the cluster until it is ready. This should take < 30 seconds
$ cerebro_cli clusters list
# Once it is ready, get the endpoints. The cluster id is returned by the 'clusters create' command.
$ cerebro_cli clusters endpoints --clusterid=<id>
```

