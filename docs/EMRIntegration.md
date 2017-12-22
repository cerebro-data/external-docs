# Cerebro Data Access Service EMR Integration

This documents describes how to use Cerebro Data Access Service (CDAS) from EMR. It
assumes that the CDAS cluster is already running. This describes how to configure each
of the supported EMR services.

In general, we require:

1. Specifying a bootstrap action which will download our client library on the cluster
nodes.
2. Specifying a configuration which configures the client library to use a CDAS install.
This is optional for some components.

An EMR cluster that is using multiple components should apply each configuration.

Note that EMR versions 5.3.0 through 5.9.0 are supported through the latest bootstrap
scripts.

## Bootstrap action

The bootstrap action will always place the client jars in the the `/usr/lib/cerebro`
directory, and linked into component-specific library path.

To bootstrap the cluster, run our script specifying the version and components you have
installed. The bootstrap script is located at:

```shell
s3://cerebrodata-release-useast/utils/emr/cdas-emr-bootstrap.sh
# Usage:
cdas-emr-bootstrap.sh <cdas version> [options] <list of components>

# Options:
# --planner-hostports <HOSTPORT> link to the cerebro_planner:planner endpoint
# --token <TOKEN> the token that identifies the user
```

For example, to bootstrap a spark-2.x cluster from the 0.7.1 client release, provide
the arguments `0.7.1 spark-2.x`. If running EMR with spark-2 and hive, provide
`0.7.1 spark-2.x hive`.

The complete list of supported components are:

- spark-2.x
- hive
- presto

Non-compute components can also be used and do not require any CDAS related steps.
These include:

- Zeppelin
- Ganglia
- ZooKeeper
- Hue

## End to End example

As an end to end example, we will start up a multi-tenant EMR cluster running
Spark 2.x, Hive, and Presto configured to run against CDAS planner running
at 10.1.10.104:12050.

- Select "Go to advanced options" at the top of the "Create Cluster" screen
  ![EMR Config](https://s3.amazonaws.com/cerebrodata-docs/images/CreateCluster.png)
- Pick Spark, Hive, and Presto from the list of EMR components and set the Spark and
Hive specific configs (more details below). Optionally pick Hue and Zeppelin as
components that do not require CDAS related steps:
![EMR Config](https://s3.amazonaws.com/cerebrodata-docs/images/EMRConfig3.png)

  Configuration example:

  ```json
  [
    {
      "Classification":"spark-defaults",
      "Properties": {
         "spark.recordservice.planner.hostports":"10.1.10.104:12050"
       }
    },
    {
      "Classification":"spark-hive-site",
      "Properties":{
        "recordservice.planner.hostports":"10.1.10.104:12050"
      }
    },
    {
      "Classification": "hive-site",
      "Properties": {
        "hive.fetch.task.conversion": "minimal",
        "hive.metastore.rawstore.impl": "com.cerebro.hive.metastore.CerebroObjectStore",
        "recordservice.planner.hostports": "10.1.10.104:12050"
      }
    }
  ]
  ```

  Additional configuration examples can be found in the program-specific sections below.

- Use our bootstrap script and do the following:
  - The script is currently located at ```s3://cerebrodata-release-useast/utils/emr/cdas-emr-bootstrap.sh```
  - Specify the `--planner-hostports` option. Since this is multi-tenant, we will not be
  providing an access token to the bootstrap script. All options need to be specified
  before the list of components.
  - Add the list of supported components to the bootstrap script. Since we're starting
  with Hive, Presto and Spark, specify `hive`, `presto`, and `spark-2.x`:
  ![EMR Bootstrap](https://s3.amazonaws.com/cerebrodata-docs/images/EMRBootstrap2.png)

- When setting up the security options, you will likely need to specify additional
security groups in order for your EMR cluster to be able to communicate with your CDAS
cluster. Add whatever security group(s) you specified for your CDAS hosts to both the
Master as well as Core & Task rows.
![EMR Config](https://s3.amazonaws.com/cerebrodata-docs/images/AdditionalSecurityGroups.png)

Create the EMR cluster and wait for it to be ready at which point the Cerebro components
will already have been installed and configured.

Since this is a multi-tenant cluster, care needs to be taken to manage users that have
access to this cluster. Each user can authenticate to Cerebro with their own token with
access control handled by Cerebro. In the example below, we will assume we are the
default `hadoop` user. Note that this does not need to be the same as the subject in
the user's access token; when we authenticate the user, we use what is specified in the
token.

- A Cerebro service group will have to be created for all services to access each user's
token.

  ```shell
  $ sudo su -
  # groupadd cerebro
  # For Presto
  # usermod -a -G cerebro presto
  # For Hive
  # usermod -a -G cerebro hive
  # For Spark
  # usermod -a -G cerebro spark
  # For Hadoop
  # usermod -a -G cerebro hadoop
  ```

The Hadoop, Hive, Spark and Presto services will need to be restarted to recognize the
groups those services are now part of.

  ```shell
  # Stop all services
  $ sudo stop hadoop-yarn-resourcemanager
  $ sudo stop hadoop-httpfs
  $ sudo stop spark-history-server
  $ sudo stop hive-hcatalog-server
  $ sudo stop presto-server
  $ sudo stop hadoop-hdfs-namenode
  $ sudo stop hive-server2
  $ sudo stop hadoop-yarn-timelineserver
  $ sudo stop hadoop-mapreduce-historyserver

  # Re-start all services
  $ sudo start hadoop-yarn-resourcemanager
  $ sudo start hadoop-httpfs
  $ sudo start spark-history-server
  $ sudo start hive-hcatalog-server
  $ sudo start presto-server
  $ sudo start hadoop-hdfs-namenode
  $ sudo start hive-server2
  $ sudo start hadoop-yarn-timelineserver
  $ sudo start hadoop-mapreduce-historyserver
  ```

- Store the access token in the user's home directory at the path `~/.cerebro/token`.
This file should contain a single line that contains the user's token and should be
accessible to the compute servers (Spark, Hive, Presto).

  ```shell
  # Continuing as root, create the user (if not already created)
  # mkdir -p /home/<user>/.cerebro/
  # Assuming the user's token is "longstringtoken"
  # echo "longstringtoken" > ~/.cerebro/token
  # chmod 750 /home/<user>
  # chown -R <user>:cerebro /home/<user>
  ```

- The same user hierarchy will have to be created in the Hadoop filesystem (HDFS or
EMRFS) as user `hadoop`.

  ```shell
  $ hadoop fs -mkdir /user/<user>
  $ hadoop fs -chown <user>:<user> /user/<user>
  $ hadoop fs -chmod 750 /user/<user>
  ```

At this point, the user can use the EMR components to access data managed by Cerebro:

- An example use of `presto-cli` is as follows:

  ```shell
  presto> show catalogs;
  # This should return 'recordservice' among others.
  presto> SHOW TABLES in recordservice.cerebro_sample;
  presto> select * from recordservice.cerebro_sample.sample;
  ```

- An example use of `hive` is as follows:

  ```shell
  $ hive
  hive> show databases;
  hive> select * from cerebro_sample.sample;
  ```

- An example use of `beeline` is as follows:

  ```shell
  $ beeline -u jdbc:hive2://localhost:10000/default -n hadoop
  beeline> show tables in cerebro_sample;
  beeline> select * from cerebro_sample.users limit 100;
  ```

- An example use of `spark-shell` is as follows:

  ```shell
  $ spark-shell
  scala> spark.conf.set("spark.recordservice.delegation-token.token", "<USER TOKEN>")
  scala> val df = spark.sqlContext.read.format("com.cerebro.recordservice.spark").load("cerebro_sample.sample")
  scala> df.show()
  ```

## Storing Tokens on Multi Tenant clusters

Cerebro supports multi-tenant EMR clusters as each Cerebro request includes the token of
the caller. Different users on the same EMR cluster using different tokens will be
authorized by Cerebro's access controls and potentially see different data.

However, it is the responsibility of the EMR cluster to ensure that it is not easy for
users on the same cluster to access, either by accident or intentionally, other users
tokens (or data). The token is never logged in its entirety by Cerebro but the user
needs to ensure that the token is not accidentally exposed through the OS. For a secure
multi-tenant cluster, we recommend that users logging in do not log in as the same
user e.g. `hadoop` and that the users logging in does not have root permissions.
Otherwise, the local cluster OS is not secure. Similarly, this will also ensure that
users cannot look at other user's intermediate files in HDFS.

We recommend that each user on the cluster persist their token under their home
directory and ensure the directory is not world readable. For example:

```shell
$ mkdir -p ~/.cerebro/
$ echo "<USER TOKEN>" >> ~/.cerebro/token
```

Note that the EMR user does not need to match the token's subject. Cerebro authenticates
the user using the token only.

## Per component configs

In the section below, we will detail the configurations required to configure each
supported EMR component.

With all components, we requires specifying the planner hostport and can optionally
take the access token. For single-tenant clusters, specifying the token removes the
need to specify it after the cluster is up. It should not be used in a multi-tenant
cluster.

### Spark

#### Setting up spark

Multi-tenant cluster:

```json
[
  {
    "Classification":"spark-defaults",
    "Properties": {
       "spark.recordservice.planner.hostports":"10.1.10.104:12050"
     }
  },
  {
    "Classification":"spark-hive-site",
    "Properties":{
      "recordservice.planner.hostports":"10.1.10.104:12050"
    }
  }
]
```

Single-tenant cluster:

```json
[
  {
    "Classification":"spark-defaults",
    "Properties": {
       "spark.recordservice.planner.hostports":"10.1.10.104:12050",
       "spark.recordservice.delegation-token.token":"<TOKEN>"
     }
  },
  {
    "Classification":"spark-hive-site",
    "Properties":{
      "recordservice.planner.hostports":"10.1.10.104:12050"
    }
  }
]
```

#### Using Spark

Once the cluster is set-up, the user can interact with the spark-shell as follows:

```shell
$ spark-shell
# Specify the user's token if this was not specified above.
scala> spark.conf.set("spark.recordservice.delegation-token.token", "<USER TOKEN>")

# For CDAS versions <= 0.4.5, you will aso need to specify the service name. This is
# the first part of the cerebro service principal. For example, if the principal is
# cerebro/hostname@REALM, this would be 'cerebro'.
# This can also be specified as part of the bootstrap configuration.
scala> spark.conf.set("spark.recordservice.delegation-token.service-name", "<PLANNER SERVICE NAME>")

# Load a cerebro table and you're good to go.
scala> val df = spark.sqlContext.read.format("com.cerebro.recordservice.spark").load("<DB.TABLE>")
scala> df.show()
```

### Hive

#### Setting up Hive

In addition to the common flags, hive requires another configuration
`hive.metastore.rawstoreimpl` to integrate with the Cerebro catalog.

Multi-tenant cluster:

```json
# Example connecting to planner at 10.1.10.104:12050
[
  {
    "Classification": "hive-site",
    "Properties": {
      "hive.fetch.task.conversion": "minimal",
      "hive.metastore.rawstore.impl": "com.cerebro.hive.metastore.CerebroObjectStore",
      "recordservice.planner.hostports": "10.1.10.104:12050"
    }
  }
]
```

Single-tenant cluster:

```json
# Example connecting to planner at 10.1.10.104:12050
[
  {
    "Classification": "hive-site",
    "Properties": {
      "hive.fetch.task.conversion": "minimal",
      "hive.metastore.rawstore.impl": "com.cerebro.hive.metastore.CerebroObjectStore",
      "recordservice.planner.hostports": "10.1.10.104:12050"
    }
  },
  {
    "Classification":"core-site",
    "Properties":{
      "recordservice.delegation-token.token":"<TOKEN>"
    }
  }
]
```

#### Using Hive

Users can use hive very similar to before, by using hive

```shell
$ hive
hive> show databases;
hive> select * from cerebro_sample.sample;
```

or beeline. Note that users must specify their local linux user (i.e. hadoop) at
connection time:

```shell
> beeline -u jdbc:hive2://localhost:10000/default -n hadoop
beeline> show tables in cerebro_sample;
beeline> select * from cerebro_sample.users limit 100;
```

#### Cluster local DBs

With the default install, the Hive Metastore (HMS) running on the cluster populates all
of its contents from the Cerebro catalog. There may be cases where it is useful to use
HMS to register cluster local (tmp) tables, for example for intermediate results. This
can be done by configuring (either during bootstrap or updating hive-site.xml and
restarting HMS) the set of databases that should be only local. For example, it can be
configured that any hive operation to the database `localdb` is cluster local. This
includes tables, views, etc. This database is never reflected in the Cerebro and access
to data or metadata in these databases do not use Cerebro in any way.

Spark, by default, uses the `global_temp` exactly this way. If Spark is included in the
EMR cluster, this database will automatically be setup to be cluster local.

In the case where the local database has the same name as a Cerebro database, the local
database takes precedence and the user will not be able see the contents in that Cerebro
database from Hive.

The configuration in hive-site.xml is:

```xml
<property>
  <name>cerebro.local.dbs</name>
  <value>localdb,testdb</value>
  <description>
    Comma-separate list of local database names. These don't need to already exist.
  </description>
</property>
```

The equivalent as a bootstrap action is:

```json
{
  "Classification": "hive-site",
  "Properties": {
    "cerebro.local.dbs": "localdb,testdb"
  }
}
```

### Presto

#### Setting up Presto

Presto requires configurations to be passed as arguments to the bootstrap script instead
of providing them as configurations but otherwise requires similar configs as the other
components. Note that the options must come *before* the list of components.

Multi-tenant:

```shell
--planner-hostports <PLANNER ENDPOINT>
# For example, if the planner is running on "10.1.10.104:12050", then, the bootstrap
# arguments would be:
cdas-emr-bootstrap.sh 0.7.1 --planner-hostports 10.1.10.104:12050 presto
```

Single-tenant:

```shell
--planner-hostports <PLANNER ENDPOINT> --token <TOKEN>
# For example, if the planner is running on "10.1.10.104:12050", then, the bootstrap
# arguments would be:
cdas-emr-bootstrap.sh 0.7.1 --planner-hostports 10.1.10.104:12050 --token <TOKEN> presto
```

#### Using Presto

Once the EMR cluster is launched and token has been stored if necessary, the user can
interact with `presto-cli` as they typically do.

```shell
$ presto-cli
presto> show catalogs;
# This should return 'recordservice' among others.

# Then you can query metadata and select form tables
presto> SHOW TABLES in recordservice.cerebro_sample;
presto> select * from recordservice.cerebro_sample.sample;
```

## Logging

On the EMR machines, the bootstrapping logs will be located in
`/var/log/bootstrap-actions/`. This can be helpful if the cluster is not starting up and
could indicate a misconfiguration of the bootstrap action.

## Configs

Configs are generally written to `/etc/[component]`. These should replicate the
configurations that were specified when the cluster was created.

## Updating CDAS client libraries

CDAS client libraries are located in `/usr/lib/cerebro` directory in each of the EMR
nodes. To upgrade CDAS client, become root user, download the client library and
restart the corresponding service(s). Replace `VERSION` with the version of CDAS
client that you are upgrading to. To restart the services, refer to
https://aws.amazon.com/premiumsupport/knowledge-center/restart-service-emr/

### Presto

ssh to **each** EC2 node in the EMR.

```shell
cd /usr/lib/cerebro
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/<VERSION>/recordservice-presto.jar
```

Restart the presto server on **all** the EC2 nodes

```shell
stop presto-server
start presto-server
```

### Hive

ssh to **each** of the EC2 nodes in the EMR

```shell
cd /usr/lib/cerebro/
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/<VERSION>/recordservice-hive.jar
```

On the **master** EC2 node, restart the hive-server

```shell
stop hive-server2
start hive-server2
```
