# Cluster Types

When starting a Cerebro CDAS cluster, you can specify the type of the cluster as part
of the `cerebro_cli clusters create` command, which is commonly used as such:

```shell
cerebro_cli clusters create --name=<Name> --numNodes=<Number of nodes> \
  --type=<Cluster type> --environmentid=<ID from environments create>
```

For example:
```shell
./cerebro_cli clusters create --name=fintech_prod --numNodes=1 \
  --type=STANDALONE_CLUSTER --environmentid=1
```

Each cluster type starts a specific set of services, which differ between the various
types. While the purpose of each service is less of a concern (since they are managed
by the Cerebro DeploymentManager and the Kubernetes Master), it is useful to understand
which service may be in trouble when there are operational problems. For example, the
following shows the output of the `cerebro_cli clusters list` command, with one
cluster having an issue with health checking the third service of a standalone cluster:

```shell
[ec2-user@ip-10-1-10-201 cerebro]$ ./cerebro_cli clusters list
description      id  name               numNodes  numRunningServices    owner    statusCode    statusMessage                                                                                                                    type
-------------  ----  ---------------  ----------  --------------------  -------  ------------  -------------------------------------------------------------------------------------------------------------------------------  ------------------
                  1  dev-env-0377              2  7/7                   admin    READY         All services running.                                                                                                            STANDALONE_CLUSTER
                  3  staging-env-4290          2  2/7                   admin    CONCERNING    Health check unsuccessful. Unable to reach 10.1.10.164:7182: java.net.ConnectException: Connection refused (Connection refused)  STANDALONE_CLUSTER
```

The following lists the services started for each available cluster type, starting with
the most common type, the *standalone* cluster. It contains everything needed to run a
fully functional CDAS cluster.

## Standalone Cluster

Type: `STANDALONE_CLUSTER`

| No.   | Service     |
| :---: | :---------- |
| 1     | Canary      |
| 2     | ZooKeeper   |
| 3     | Catalog     |
| 4     | Planner     |
| 5     | Worker      |
| 6     | REST Server |
| 7     | Web UI      |

> **Note:** The number of services for a standalone cluster changed in 0.7.0. Before
that version a cluster had eight (8) services. With 0.7.x there are only seven (7)
services.

---

There are more specific cluster types, which are used less often though. These are
needed when, for example, a single CDAS Catalog should be shared by multiple
*data access* cluster instances.

## Catalog-Only Cluster

Type: `CATALOG_CLUSTER`

| No.   | Service     |
| :---: | :---------- |
| 1     | Canary      |
| 2     | ZooKeeper   |
| 3     | Catalog     |
| 4     | Planner     |
| 5     | REST Server |
| 6     | Web UI      |

## Data-Access-Only Cluster

Type: `DATA_ACCESS_CLUSTER`

| No.   | Service     |
| :---: | :---------- |
| 1     | Canary      |
| 2     | ZooKeeper   |
| 3     | Planner     |
| 4     | Worker      |

