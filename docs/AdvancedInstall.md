# Advanced installations

This document describes advanced Cerebro installs. This document assumes the user is
familiar with the base install and will describe changes relative to that process.

## External Hive Metastore

In a typical CDAS install, CDAS will start up and manage a Hive Metastore (HMS)
compatible service. If instead, you'd like to have CDAS use an existing, externally
managed HMS, you can do so. In this configuration, CDAS will simply read and write
from the external HMS but all other behavior is unchanged.

CDAS is compatible with Hive 1.1.x and 1.2.x (CDH5.7+).

To use an external HMS, add this config to your DeploymentManager configs,
typically `/etc/cerebro/env.sh`.

```shell
export CEREBRO_EXTERNAL_HMS_CONFIG=<path to hive-site.xml>
```

The value should be a full path to the hive-site.xml client config for the external HMS.
The path must be accessible from the DeploymentManager process but can be either local
or remote (e.g. S3). After setting the config and restarting the DeploymentManager, newly
created clusters will use the external HMS and restarting existing clusters will update
to use the external HMS config.

It is possible to have multiple CDAS clusters use the same external HMS, subject to the
concurrency settings of your HMS.

## Sharing Existing Hive Metastore RDBMS

It is also possible to start a Cerebro catalog which shares the same RDBMS database as
an existing Hive Metastore. This is useful, for example, during migration to have the
Cerebro catalog use the same underlying database as the existing Hive Metastore. This
allows the existing catalog information to be automatically visible through Cerebro.

To do so, configure `CEREBRO_DB_URL` (when starting up the DeploymentManager) to be
the same database instance (i.e. same MySQL server) as the one used by the existing
Hive Metastore, and then when creating the cluster, specify `--hmsDbName` when creating
the cluster from the CLI.

For example, if an existing HMS was running ontop of a MySQL instance at
`hms.db.mycompany.com:3306` with metadata in `hive_db`, you can:

In env.sh:
```shell
export CEREBRO_DB_URL=hms.db.mycompany.com:3306
```

And when creating the cluster:
```shell
./cerebro_cli clusters create --hmsDbName=hive_db --name=fintech_prod --numNodes=1 --type=STANDALONE_CLUSTER --environmentid=1
```


## Advanced Networking

### Configuring the IP range that a CDAS cluster should use for internal routing

Each Cerebro cluster configures a private network for communication within the cluster.
By default, Cerebro will use the 172.30.0.0/16 range for internal communication.
The environment variable CEREBRO_CDAS_INTERNAL_NETWORK_IP_RANGE on the DM can be
used to configure this setting.

NOTE: AWS attaches an HTTP server to every instance at IP address 169.254.169.254.
You should never configure an internal network range that would overlap with that
IP address. If that occurs, then AWS libraries will not be able to query the HTTP
server for the instance's IAM credentials, precluding access to AWS resources.

### Cerebro Port Allocation ###

Cerebro utilizes network ports for external (user facing) and internal system
communication. This section outlines the default minimum port requirements for
successful cluster functionality.

Please note that for optimal cluster performance, it is assumed that all internal
ports between cluster node instances are open. If environment policy requires that
only essential ports remain open, be aware that it is possible that the cluster
may perform sub-optimally as the underlying Kubernetes and Docker layers are
optimized over time.

External ports to the Deployment Manager and other CDAS services are used for CDAS
cluster and data administration. It is assumed that these ports are available to
administrative users.

#### Static Ports ####

Cerebro reserves the following ports for Cerebro service functionality.
These ports are not user configurable and are required for cluster creation,
management, and operation.

| Port | Description |
|:---:|:---:|
|443|Kubernetes Master Communication|
|6443|Kubernetes API Server|
|6783|Kubernetes Weave|
|8085|Cerebro Master Host Port|

#### Customizable Ports ####

Cerebro provides the ability to change the following port assignments in the
`/etc/cerebro/env.sh` configuration file for your environment's Deployment
Manager instance.

The table below lists the suggested port assignments as defined by Cerbero. All port
assignments listed below are optional and any that are not explicitly assigned will
be assigned by Kubernetes during cluster creation.

In the case that no ports are assigned, ports will be assigned in the 30000-32000 port
range. In cases where ports are partially assigned, Kubernetes will assign ports to the
unassigned services in the port range between the lowest numbered port assignment and
the largest assignment or 2000 ports from the lowest assigned port; whichever range is
larger.

**Example 1:**
* Lowest configured port: 5000 (CDAS REST Server)
* Highest configured port: 10020 (Planner)
* Port range for possible assignment of non-assigned ports: 5000-10020

**Example 2:**
* Lowest configured port: 5000 (CDAS REST Server)
* Highest configured port: 5010 (Planner)
* Port range for possible assignment of non-assigned ports: 5000-7000

External ports are required for user administration and are necessary for cluster
creation, maintenance, and usage.

| Port | Description | Usage |
|:---:|:---:|:---:|
|5000|CDAS REST Server|External|
|8083|Cerebro WebUI|External|
|11050|Cerebro Planner WebUI|External|
|11060|Sentry|Internal|
|11061|Cerebro HMS|Internal|
|11062|Kubernetes WebUI|External|
|12050|Planner|External|
|13050|Cerebro Worker|External|
|5000-13050|Kubernetes Dynamic Range|Internal|