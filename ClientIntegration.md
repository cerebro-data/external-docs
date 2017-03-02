# Cerebro Data Access Service Integration
This documents describes how to use Cerero Data Access Service (CDAS) from a user's
perspective. It describes how Cerebro can be used from various existing tools to:
  - Explore and create new data sets and views.
  - Define fine-grained access policies over datasets.
  - Read datasets.

Note that currently CDAS does not support data writes but many use cases can be
covered by writing using the existing tools and then creating a dataset in CDAS.

This document will through various tools we are integrated with. It might not
make sense to do all of the above with each tool.

### HiveServer2/Beeline
HiveServer2 provides a service to run SQL. Typically clients connect to it through
beeline, which gives a traditional SQL client shell. HS2/Beeline is suitable to
do all tasks above. The original hive shell ('hive') is not supported.

From the user's point of view, they simply connect to HS2 as always. HS2 in fact
is not provided by Cerebro and clients talk to the same HS2 without directly
interacting with Cerebro (HS2 is configured and integrated with Cerebro). Authentication
works exactly as always.

Here is an example that registers and external dataset, creates a role, creates
a view of the data and grants access to that role. In all of these steps, there
should be no difference between how these commands work with or without Cerebro.

##### Setting up the admin role quick start
These are quick start steps to set up the admin role which has full access to
the server. The user running these commands needs to have admin to the Cerebro
Catalog.

```
beeline> !connect jdbc:hive2://<host:port of hs2>/default;principal=<hs2_principal>
beeline> CREATE ROLE admin_role;
beeline> GRANT ALL ON SERVER server1 TO ROLE admin_role;
beeline> GRANT ROLE admin_role TO GROUP <YOUR ADMIN USER/GROUP>;
```

**Note**: These steps assume a few things about your set up that are no different than 
typical HS2 requirements. The admin user or group that is granted must exist on the unix
system in both Cerebro and HS2.

##### Creating a dataset
In the next step, we will create an external dataset for data in S3.
```
beeline> create external table sample (s STRING) LOCATION 's3n://cerebrodata/sample'
beeline> show tables;
beeline> select * from sample;
```

At this point we have added a dataset to Cerebro By default only the admin user/group
has access to the dataset, which is now accessible to all the Cerebro integrated clients.
Other users accessing this dataset should see the request fail.

**Note:** These steps also assumes that the beeline client has access to this location. 
This, for example,  involves IAM roles or AWS access keys to be set up if the data is in 
S3.

**Note:** Creating non-external table is currently considered undefined behavior and should
not be done.

##### Creating a view and granting access to another role
Finally, we will create a view and grant access to the view to a different set of users.
In this case we will create a view that only returns records which contain 'test'.
```
beeline> CREATE ROLE test_role;
beeline> GRANT ROLE test_role TO GROUP <YOUR TEST USER/GROUP>;

beeline> CREATE VIEW sample_view as SELECT * FROM sample WHERE s LIKE '%test%';
beeline> SHOW TABLES;
beeline> SELECT * FROM sample_view;

beeline> GRANT SELECT ON TABLE sample_view TO ROLE test_role;
```

At this point the admin group should see the full dataset and the test group
should only see a subset of the records.

The remaining GRANT/REVOKE/DROP are supported and work identically to HS2.

**Note**: Updating permissions can take a few minutes to be reflected everywhere
in the system as policies are cached.

### MapReduce Integration
The MapReduce integratio is API compatible with the Cloudera open source
[RecordServiceClient](http://recordservice.io/examples/). For details on
those APIs, refer to those docs.

Running the MapReduce application is done as normal running;
```
hadoop jar <application.jar> [arguments]
```

##### Configuration
The only required configuration is the location of the RecordService planner port. This
can be configured either in the standard Hadoop config files (mapred-site.xml or
yarn-site.xml) or from the environment ('RECORD_SERVICE_PLANNER_HOST'). In the config file,
the name of the config is 'recordservice.planner.hostports'. In either case, the value
should be the host port of the planner. If both are set, the file based config takes
precedence.

In a typical end user use case, the config should have been populated by the cluster admin,
typically in /etc/hadoop/conf. In this case, there is no configuration required by the end
user. If the config is not set, the client will by default connect to localhost, likely
resulting in connetion errors.

##### Dependency management
Libraries (jars) that your application depend on need to be available on all
the nodes in the compute cluster. There are multiple resources on how to do this
for Hadoop and we will summarize here. There are two ways to do this:
  1. Create a fat jar/shade the dependencies in your application. This means that
     when building the MapReduce application, you bundle all the dependencies and
     the application is self contained. An example is how the RecordService examples
     do this with [maven](https://github.com/cloudera/RecordServiceClient/blob/master/java/examples/pom.xml#L92).
  2. Provide all the dependent jars when submitting the job with 'hadoop jar'. This
     requires setting HADOOP_CLASSPATH to the jars (either folder or individial paths)
     and specifying '--libjars' and passing all the dependencies. Note that
     HADOOP_CLASSPATH is colon separated and libjars is comma separated.

As a complete example, assuming we are using the RecordServiceAvro client library. For
option 1, we can simply just run the application as the dependncies have been handled
as part of the build.
```
hadoop jar AvroApplication.jar <arguments>
```

For option 2, we need to:
```
# All jars are paths to the jar on the local (submitting machine) filesystem.
export HADOOP_CLASSPATH=<recordservice-avro.jar>:<recordservice-avro-mr.jar>:<recordservice-core.jar>:<recordservice-mr.jar>
hadoop jar --libjars <recordservice-avro.jar>,<recordservice-avro-mr.jar>,<recordservice-core.jar>,<recordservice-mr.jar> AvroApplication.jar <arguments>
```

### Pig Integration
The Pig Integration is very similar to the MapReduce integration. The API is also
compatible with the open source client. Pig handles some of the dependency management
automatically and the only required dependency jars is the
recordservice-hcatalog-pid-adapter jar.

##### Configuration
Pig configuration is identical to MapReduce. Refer to that section.

### Hue Integration
Hue does not require any additional steps to work with Cerebro. Hue also connects to HiveServer2 and
is integrated very similarly as beeline.

##### Troubleshooting
If requests are failing with users not having privileges, ensure that the user exists. The user must
exist on (as a unix user):
  - Machine running HS2.
  - Cerebro catalog.

### Impala Integration
Impala does not require any end user steps. After Impala is configured by the cluster admin, users
can connect to Impala via the shell as normal.

### Spark Integration
Spark provides a few ways to integrate with spark. Refer to the open source client
documentation for [details](https://github.com/cloudera/RecordServiceClient/tree/master/java/examples-spark)

##### HiveContext
Spark can be configured to use an existing HiveMetastore to retrieve catalog
metadata. If this is set up, you can retrieve metadata as with a typical Hive
client (e.g. similar to beeline). If configured, you can issues queries such as
```
sqlContext.sql("SHOW TABLES").show()
```

**Note**: The sql supported is not necessarily compatible with beeline. This is
more intended to retrieve catalog metadata and not recommended as a way to adminstor
access control policies (grant/revoke).

##### Configuration
Spark requires a single configuration that is the host port of the planner:
'spark.recordservice.planner.hostports'. This should be set to a list of comma separated
host:port where the Cerebro planners are running. This configuration can either be
set system wide (typically /etc/spark/conf/spark-defaults.conf) or can be specified
when launching spark-shell or spark submit. For example, you can connect to a particular
planner with:
```
spark-shell --conf spark.recordservice.planner.hostports=IP:PORT
# or
spark-submit --conf spark.recordservice.hostports=IP:PORT <rest of args>
```

If using the HiveContext to directly interact with HiveMetastore, the Cerebro catalog
configs will need to be set. This requires a configured hive client which is by default
located in '/etc/hive/conf/hive-site.xml'. The configuration should be configured by
the cluster admin as it can require various settings, particularly if kerberos is
enabled. To spot check, the config 'hive.metastore.uris' should be set to the Cerebro
catalog.

### REST Scan API
CDAS exposes a REST API that returns data as JSON. This API is only intended to
read data, not to register datasets or update their policies.

The REST API simply exposes a HTTP endpoint. This endpoint is referenced in other
documents as the 'cdas REST server' endpoint. To read data, you can simply reach:
```
http://<hostport>/api/scan/<dataset name>
# You can optionally specify how many records with:
http://<hostport>/api/scan/<dataset>?records=N
```

Continuing the above example with data registered via HiveServer2, we should see:
```
# Read the entire dataset
curl <hostport>/api/scan/sample

# If running on a kerberized cluster, you will need to authenticate the curl
# request. This assumes you have local kerberos credentials (i.e. already ran
# kinit)
curl --negotiate -u : hostport/api/scan/sample
```

### Python Pandas Integration
Reading the data into a panda data frame is very simple with the REST API.

```
import pandas as pd
df = pd.read_json('http://<hostport>/api/scan/<dataset>')
```

