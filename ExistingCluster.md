# Connecting to an existing cluster.
It is possible to connect the Deployment Manager to a running kubernetes cluster. In
this case, the Deployment Manager will provision Cerebro services on that cluster but
does not try to manage the cluster.

### Prerequisites
The cluster should already be running. The kube config file needs to be copied to
the machine running the Deployment Manager. This file is typically generated as
part of installing and starting up a kubernetes cluster. It contains cluster names
and various other credentials.

Copy the file to the Deployment Manager's install directory. This is by default
'/var/run/cerebro'. The name of the file should be <provider>_<cluster_handle>.
For the remainder of this document, we will assume the cluster is running in AWS
and shoudl be called 'demo_cluster'. 

After copying, the file should look like:
```
$ ls /var/run/cerebro
aws_demo_cluster.conf
```

### Registering the cluster 
To register the cluster, we will create it in the CLI with differnt arguments. We
do not need to specify an environment in this case, instead we need to specify 
the cluster handle.

```
$ cerebro_cli clusters create --name=ExistingTest --type=CANARY_CLUSTER --numNodes=1 --existingCluster=demo_cluster
# List the cluster until it is ready. This should take < 30 seconds
$ cerebro_cli clusters list
# Once it is ready, get the endpoints. The cluster id is returned by the 'clusters create' command.
$ cerebro_cli clusters endpoints --clusterid=<id>
```

