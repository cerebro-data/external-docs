# Cerebro Data Access Service CDH Integration

This documents describes how to use Cerebro Data Access Service (CDAS) from a cluster
admin perspective. It describes how to configure an existing CDH cluster to an existing
Cerebro deployment.

Prerequisites:
- CDAS running. We need the endpoints for the Cerebro Catalog components referred to as
CDAS HMS and CDAS Sentry. We also need the Cerebro planner endpoint referred to as
CDAS Planner.
- If kerberized, the principal for CDAS (referred to as CDAS Principal)
- CDH (5.7+) running and managed by Cloudera Manager (CM). This cluster should be fully
functional with kerberos enabled (if desired) and Sentry enabled. This can included any
subset of the CDH components.

The result of these configuration changes will have CDH use the Cerebro Catalog,
replacing the HiveMetastore and Sentry Store components. Note that even if these
components are still running, when properly configured, they will not be used. No
clients should be interacting with them.

A summary of what we will do is:
1. Configure HMS clients to talk to the Cerebro catalog. This includes other services
such as Impala and HiveServer2 as well as gateway client configs for Spark, Pig, etc.
2. Configure Sentry clients to talk to the Cerebro catalog. Clients typically do not
contact this service directly, we will only need to update HiveServer2 and Impala.
3. Configure the gateway client configs to use Cerebro's data access service. This
provides the functionality that the RecordService service provided.

These steps are repeated across multiple CDH clusters allowing them to share the
same metadata.

## HMS Configs

We need to make these configuration changes in multiple places for the different HMS
clients. The configs are:

```xml
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

The configs need to be set in Hive -> Service Wide -> Hive Service Advanced Configuration
Snippet (Safety Value) for `hive-site.xml`

This will require restarting the hive service. You can verify this is set properly by
going on the machine (requires root) running HiveServer2 and looking in
`/var/run/cloudera-scm-agent/process/<latest folder for hive server2>/hive-site.xml`.

The CM generated config should make it very clear that these two values have been
overridden.

**Note** This will result in the Hive Metastore Server as unhealthy. This is expected
and can be safely ignored. **HiveServer2** health should be healthy.

#### Hive client configs

The configs need to be set in Hive -> Gateway -> Hive Client Advanced Configuration
Snippet (Safety Value) for `hive-site.xml`

This will require deploying the client configs and restarting dependent services.
You can verify this is set properly by going to any gateway machine and looking in
`/etc/hive/conf/hive-site.xml`.

#### Impala configs

Impala will also need to be configured to the Cerebro catalog. This involves updating
the above two configs in Impala -> Impala Catalog Server -> Catalog Server Hive Advanced
Configuration Snippet (Safety Valve)

## Sentry store configs

For Sentry use, we require that these configs are set. Again, the kerberos principal is
only required for kerberized clusters.

```xml
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

This will need to be set in: Hive -> Service Wide -> Hive Service Advanced Configuration
Snippet (Safety Valve) for `sentry-site.xml`

This will require restarting the dependent services. These can be verified by looking in
the generated config for the HiveServer2 service (see above for the HMS config on
details).

#### Impala

This will need to be set in: Impala -> Service Wide -> Impala Service Advanced
Configuration Snippet (Safety Valve) for `sentry-site.xml`

This will require restarting Impala.

**NOTE**: For Impala integration, the Impala principal's primary (typically 'impala')
must also be in the list of Cerebro catalog admins (env: CEREBRO\_CATALOG\_ADMINS).

## RecordService Configs

RecordService configs can be set in either mapred-site.xml or yarn-site.xml depending on
which one you are using. The configuration is:

```xml
<property>
  <name>recordservice.planner.hostports</name>
  <value><CDAS Planner Host:Port></value>
</property>
```

This has to be set in the safety valves in Yarn -> Gateway -> MapReduce Client Advanced
Configuration Snippet (Safety Valve) for `mapred-site.xml` and Yarn -> Gateway -> YARN
Client Advanced Configuration Snippet (Safety Valve) for `yarn-site.xml`

This requires redeploying the client configs. You can verify it is set by going on any
gateway machine and looking in `/etc/hadoop/conf/[mapred|yarn]-site.xml`.

## Client jars

Cerebro publishes jars that are API compatible with the RecordService jars.

### POM configuration

To use these jars from maven, you can configure the pom to use our repo and version.
This can be added to the pom.

```xml
  <properties>
    <recordservice.version>1.0.0-beta-9</recordservice.version>
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

```shell
s3://cerebrodata-release-useast/<version>/client
# For example:
s3://cerebrodata-release-useast/0.7.1/client
```
