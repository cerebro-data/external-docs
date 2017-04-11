# Cerebro Data Access Service CDH Integration
This documents describes how to use Cerebro Data Access Service (CDAS) from a cluster
admin perspective. It describes how to configure an existing CDH cluster to an existing
Cerebro deployment.

Prerequisites:
  - CDAS running. We need the endpoints for the Cerebro Catalog components referred
    to as CDAS HMS and CDAS Sentry. We also need the Cerebro planner endpoint referred
    to as CDAS Planner.
  - If kerberized, the principal for CDAS (refereed to as CDAS Principal)
  - CDH (5.7+) running and managed by Cloudera Manager (CM). This cluster should be fully
    functional with kerberos enabled (if desired) and Sentry enabled. This can included
    any subset of the CDH components.

The result of these configuration changes will have CDH use the Cerebro Catalog, replacing
the HiveMetastore and Sentry Store components. Note that even if these components are still
running, when properly configured, they will not be used. No clients should be interacting
with them.

A summary of what we will do is:
1. Configure HMS clients to talk to the Cerebro catalog. This includes other services
such as Impala and HiveServer2 as well as gateway client configs for Spark, Pig, etc.
2. Configure Sentry clients to talk to the Cerebro catalog. Clients typically do not
contact this service directly, we will only need to update HiveServer2 and Impala.
3. Configure the gateway client configs to use Cerebro's data access service. This
provides the functionality that the RecordService service provided.

These steps are repeated across multiple CDH clusters allowing them to share
the same metadata.

## HMS Configs
We need to make these configuration changes in multiple places for the different HMS
clients. The configs are:
```
<property>
  <name>hive.metastore.uris</name>
  <value>thrift://<CDAS HMS Host:Port></value>
</property>
<property>
  <name>hive.metastore.kerberos.principal</name>
  <value><CDAS Principal></value>
</property>
```
If the cluster is not kerberized, then the kerberos principal is not necessary.

#### Hive service configs
The configs need to be set in Hive -> Service Wide ->
Hive Service Advanced Configuration Snippet (Safety Value) for hive-site.xml

This will require restarting the hive service. You can verify this is set properly
by going on the machine (requires root) running HiveServer2 and looking in
/var/run/cloudera-scm-agent/process/<latest folder for hive server2>/hive-site.xml.

The CM generatd config should make it very clear that these two values have been
overridden.

**Note** This will result in the Hive Metastore Server as unhealthy. This is expected
and can be safely ignored. **HiveServer2** health should be healthy.

#### Hive client configs
The configs need to be set in Hive -> Gateway ->
Hive Client Advanced Configuration Snippet (Safety Value) for hive-site.xml

This will require deploying the client configs and restarting dependent services. You
can verify this is set properly by going to any gateway machine and looking in
'/etc/hive/conf/hive-site.xml'

#### Impala configs
Impala will also need to configured to the Cerebro catalog. This involves updating
the above two configs in Impala -> Impala Catalog Server ->
Catalog Server Hive Advanced Configuration Snippet (Safety Valve)

## Sentry Store Configs
For sentry we these configs set. Again, kerberos principal is only required for
kerberized clusters.
```
<property>
  <name>sentry.service.client.server.rpc-address</name>
  <value><CDAS Sentry Host></value>
</property>
<property>
  <name>sentry.service.client.server.rpc-port</name>
  <value><CDAS Sentry Port></value>
</property>
<property>
  <name>sentry.service.principal</name>
  <value><CDAS Principal></value>
</property>
```

#### Hive Server 2
This will need to be set in:
Hive -> Service Wide -> Hive Service Advanced Configuration Snippet (Safety Valve) for
sentry-site.xml

This will require restarting the dependent services. You can be verified by looking in
the generated config for the HiveServer2 service (see above for the HMS config on details.

#### Impala
This will need to be set in:
Impala -> Service Wide -> Impala Service Advanced Configuration Snippet (Safety Valve) for
sentry-site.xml

This will require restarting Impala.

## RecordService Configs
RecordService configs can be set in either mapred-site.xml or yarn-site.xml depending
on which one you are using. The configuration is:
```
<property>
  <name>recordservice.planner.hostports</name>
  <value><CDAS Planner Host:Port></value>
</property>
```

This has to be set in the safety valves in
Yarn -> Gateway -> MapReduce Client Advanced Configuration Snippet (Safety Valve) for
mapred-site.xml
and
Yarn -> Gateway -> YARN Client Advanced Configuration Snippet (Safety Valve) for
yarn-site.xml

This requires redeploying the client configs. You can verify it is set by going on any
gateway machine and looking in /etc/hadoop/conf/[mapred|yarn]-site.xml

## Client jars
Cerebro publishes jars that are API compatible with the RecordService jars.

### Pom configuration
To use these jars from maven, you can configure the pom to use our repo and version.
This can be added to the pom.
```
  <properties>
    <recordservice.version>1.0.0-beta-1</recordservice.version>
  </properties>

  <!-- For MapReduce -->
  <dependencies>
    <dependency>
      <groupId>com.cloudera.recordservice</groupId>
      <artifactId>recordservice-mr</artifactId>
      <version>${recordservice.version}</version>
    </dependency>
  </dependencies>

  <!-- For Spark 1.6 -->
  <dependencies>
    <dependency>
      <groupId>com.cloudera.recordservice</groupId>
      <artifactId>recordservice-spark</artifactId>
      <version>${recordservice.version}</version>
    </dependency>
  </dependencies>

  <!-- For Spark 2.0 -->
  <dependencies>
    <dependency>
      <groupId>com.cloudera.recordservice</groupId>
      <artifactId>recordservice-spark-2.0</artifactId>
      <version>${recordservice.version}</version>
    </dependency>
  </dependencies>

  <repositories>
    <repository>
      <id>cerebro.releases.repo</id>
      <name>libs-release</name>
      <url>https://cerebro.jfrog.io/cerebro/libs-release</url>
    </repository>
    <repository>
      <id>cerebro.snapshots.repo</id>
      <name>libs-snapshot</name>
      <url>https://cerebro.jfrog.io/cerebro/libs-snapshot</url>
    </repository>
  </repositories>
```

## Downloading the jars
All of the release jars are also available in S3 in the release location. They are
available at
```
s3://cerebrodata-release-useast/<version>/client
# For example:
s3://cerebrodata-release-useast/0.3.0/client
```

## Configuring CDAS
CDAS supports Hadoop configurations. The configs can be uploaded to the Cerebro S3 bucket
and will be picked up by the services on restart. The configs should have the same names
as their equivalent Hadoop equivalents (e.g. core-site.xml, hive-site.xml, sentry-site.xml).

The configs should just contain the individual properties in xml format. Refer to the examples
below. Multiple configs can be put by having multiple property tags.

### Configuring Sentry admins example
Create the file sentry-site.xml and populate it with:
```xml
<property>
  <name>sentry.service.admin.group</name>
  <value>COMMA SEPARATED LIST OF USERS/GROUPS</value>
</property>
```
For example:
```xml
<property>
  <name>sentry.service.admin.group</name>
  <value>cerebro,impala,hive,admin</value>
</property>
```

NOTE: if the cluster is kerberized, the primary must be in the list of admins. For
example, if the principal for cerebro is 'cerebro/host.com@REALM', cerebro must be
in the list of admins. For Impala integration, the impala principal's primary (typically
'impala') must also be in the list of admins.

Save and upload this file to Cerebro's install bucket, under the /etc/ directory.

For example:
```shell
$ aws s3 cp ./sentry-site.xml s3://<CEREBRO_BUCKET>/etc/
```

### Configuring cross realm kerberos example
Create the file core-site.xml and populate it with the config. The example below
will allow any principals of the form primary/instance@REALM1, primary/instance@REALM2 or
primary@REALM2. By default, Cerebro allows all principals (in either form) from the realm
Cerebro's keytab is in.
```xml
<property>
  <name>hadoop.security.auth_to_local</name>
  <value>
    RULE:[2:$1@$0](.*@REALM1)s/@.*//
    RULE:[2:$1@$0](.*@REALM2)s/@.*//
    RULE:[1:$1@$0](.*@REALM2)s/@.*//
  </value>
</property>
```
Upload this file to S3:
```shell
$ aws s3 cp ./core-site.xml s3://<CEREBRO_BUCKET>/etc/
```


For more details on this config, refer to the Hadoop documentation:
https://hortonworks.com/blog/fine-tune-your-apache-hadoop-security-settings/
