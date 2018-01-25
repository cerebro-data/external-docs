# 0.7.2 (Jan 2018)

0.7.2 includes stability fixes. We recommend all 0.7.0/0.7.1 users upgrade.

## Bug Fixes

* Improved REST server scalability in the presence of long running scan requests.
Previously, long running scans could block out other requests, including the service
health check. This causes DeploymentManager to report the service as unhealthy. This has
been fixed so there are separate request handlers for long requests.

* Fix to planner refusing all connections due to race conditions when logging. Specific
request patterns to CDAS can get the planner in a state where it refuses all subsequent
connections. This causes the services to report as 5/7 healthy as well as the REST
server going into a restart loop. This is caused by race conditions resulting in logs
not being drained properly. This has been resolved by upgrading our bundled docker version
(to 1.12.6) as well as log handling improvements.

* Fixed deployment in environments which require HTTP proxy configurations. Some of
the new validations added in 0.7 were not properly using HTTP proxy configurations in all
cases, causing those calls to fail. No configuration changes are required. We now
properly use the proxy configurations if set.

* Fixed deployment when the cluster's private IP range conflicts with what CDAS
requires. Specifically, in 0.7.2, we resolved the conflict on the IP range `10.32.0.0/12`.

* Fixed token expiration display for Json Web Tokens (JWT) in the UI. The value displayed
in the previous version was incorrect and much longer than the actual expiration. Note
that this was a presentation issue only; the tokens would have expired correctly.

* Fixed issue where catalog does not load properly when there are invalid catalog objects.
These catalog objects are unreadable by Cerebro but now this no longer causes other
catalog objects to be skipped.

* Support LDAP default domains and SSL enabled servers. Previously, CDAS only supported
distinguished names. Now it is possible to login with a domain name, for example
`USERS\user`. It is also possible to configure a default domain and just login with
`user`.

# 0.7.1 (Dec 2017)

## New Features

* Support for Json Web Token (JWT) authentication using public key and external server.
In previous releases, CDAS could only be configured to use one or the other. It is not
possible to configure both at the same time. For more information see
[here](Authentication.md).

## Bug Fixes

* Support for non-reneweable keytabs
0.6.2 contains a single fix for kerberized clusters. All CDAS services have been updated
to be more robust to non-renewable keytabs. Previously, services may become unstable
when the kerberos tickets expired when using these kind of keytabs.

* UI no longer shows zero datasets in the case that some failed to load
In 0.7.0 and previous releases, the UI would show no datasets in the Datasets page
list, even if only one failed to load. In 0.7.1, the UI will show all datasets
that were loaded without error.

# Bug Fixes

# 0.6.2 (Dec 2017)

0.6.2 contains a single fix for kerberized clusters. All CDAS services have been updated
to be more robust to non-renewable keytabs. Previously, services may become unstable
when the kerberos tickets expired when using these kind of keytabs.

# 0.7.0 (Nov 2017)

0.7.0 is a major release.

## New Features

**JSON structured audit logs**

By default, the planner audit log is now output as json in the planner logs. The new
format contains more information, is much easier to parse and will be stable over time.
For more information on its schema and how to use it, see the [docs](Auditing.md).

**Support for Hive SerDes**

Prior versions of CDAS had limited support for tables which require a custom Hive SerDe
to read. In this release, we extended the support to all the CDAS supported types, the
ability to read SerDe libraries from S3 and builtin, HiveQL compatible support in the
various CDAS `hive-ddl` APIs. For more information, see [here](ExtendingCDAS.md).

**Support for external views**

In prior version of CDAS, views created in Cerebro had to be evaluated in CDAS. This is
critical for views which enforce data security related transformations. For example, if
the view anonymizes user data, the view must be evaluated in CDAS before it is returned
to the client. In this release, we added support for `external views`, which are used
to store views to store data transformations and have no security implications. These
views are used strictly for tracking what can be evaluated in CDAS or in the compute
application.
For more details, see [here](SupportedSQL.md).

**Support to drop permissions when dropping catalog objects**

Dropping a database, table or view does not drop the permissions on the object. This
means if the same object with the same name is created, it will retain the permissions
from the dropped object. This pattern is sometimes used, for example in ETL, where
the permissions and catalog object (re)creation are decoupled. This behavior is not
ideal in other cases. In this release we extended the `DROP [DATABASE|TABLE|VIEW]`
DDL commands to support optionally dropping the associated permissions as well. If not
specified, this command is backwards compatible and keeps the previous semantics.
For more information see [this](SupportedSQL.md).

**Dynamic REST API scan page size**

Previously the REST scan page size was fixed (by default 10000 records). This can be
problematic for tables with very many columns and requests would cause timeouts or
other bad behavior. In this release, the page size is dynamic (up to 10000 by default)
and adjusts based on how long the requests are taking.

**Improved client connection related errors**

Previously, the errors related to failed connections from the Java client libraries
required looking at the CDAS service logs. These errors should now be returned to
the client. Note that this requires updating the client library to the latest version
(beta-9) as well. Older clients are compatible and will continue to work, but may not
see the improved error reporting in all cases.

**Filtered dataset search in the UI**

The Cerebro UI now has enhanced dataset search capabilities, allowing users to search
for datasets by name and/or database name, as well as filter by any set of databases.

**Dataset access inspection in the UI**

Dataset stewards (or anyone with all access to a dataset) can inspect which groups or set
of groups has access to their datasets as well as which fields in those datasets.

## Bug Fixes

* Fixed an issue where the 30-second timeout for session\_ids issued
by the /api/scanpage endpoint was starting from the beginning of a query,
resulting in the user being "charged" for system time. This was corrected
and the timer now starts when CDAS begins returning data to the user.

* Fixed /api/scanpage endpoint so that for fetching more than 10,000 records, the total number of
records to be fetched only needs to be specified on the first call. Subsequent calls can use the
session_id to return the remaining records in batches of up to 10,000 entries. If the record number
is specified in subsequent calls, it will be ignored.

* Unable to see a database if the user has only been granted access to that database's
columns. This issue has been resolved and the user can properly see a database even if
they only have partial access to objects in the database.

* Allow dropping views even if its metadata becomes invalid. In previous versions, it
would sometimes not be possible to drop views if its metadata became invalid. This can
happen for example, if the base table for the views are deleted. These views can now
be dropped.

* Fixed an issue where permission granted to a top-level S3 bucket was not being
propagated to sub-directories.

* Fixed an issue were detailed error messages were not being returned to clients for
server-side errors (they were being overwritten with generic error messages).

* Fixed an issue where Hive in EMR 5.8 and later would not work with CDAS.
EMR 5.8.0 upgraded Hive from 2.1.1 to 2.3.0 which introduced a backward compatibility
breaking api change, which has been addressed.

### Incompatible and Breaking Changes

* The records parameter for the /api/scanpage REST endpoint now indicates
the total number of records that the query should return. This had
previously indicated the batch size to be used for each page. Results are
now returned in batches of up to 10,000 entries.

* Number of services in standalone cluster reduced from 8 to 7. In the release, the
earlier version of the UI was completely removed, reducing the number of services by 1.
In 0.6.x, this service was running but not externally exposed by default (for example,
even in 0.6.x, endpoints did not report the earlier UI).

* Root user required to run kubectl commands on kubernetes master.
Previously, `kubectl` could be run as any user, for example, `ec2-user`. It is now required
to be `root` to run `kubectl`.

* Existing launch scripts will not work with 0.7.0. We have enhanced our validation of
launch scripts when they are registered during the cerebro_cli "environments create"
call. The changes required to perform that validation necessitates that all launch scripts
must to be replaced with ones based on the new template provided in the 0.7.0 release.
Launch scripts based on earlier templates will cause the "environments create" call
to fail. All of the values that need to be configured have been moved to a dedicated section
towards the top of the script in the new template, so porting older launch files should
be fairly quick. Launch scripts based on the 0.7.0 template are backwards compatible with
older CDAS releases.

## Known issues

**After installing a new version of DM, upgrading a component in existing cluster does not work

The workaround is to upgrade the existing cluster component(s) to the newer version before newer
DM is installed and restarted.

** Java 9 is not supported

There is an issue with the new module changes in Java 9 that will be addressed in future
versions of CDAS.

# 0.5.3  (November 2017)

0.5.3 is a minor release consisting of backports of select fixes from the
0.6.1 release. Those patches are:
* Invalid JSON encountered when retrieving cluster status
* Incorrect user when using /api/scanpage endpoint
* add_date function does not work with view creation
* REST server scaling issues

# 0.6.1 (Oct 2017)

0.6.1 is a minor release and we recommend all 0.6.0 users upgrade.

## New Features

**OAUTH integration**

The Web UI now supports authentication using OAUTH. If configured, users will be
redirected to the identity provider's login page, for example logging in with their
gmail account.

**Improved SSL support**

Some clients (e.g. latest version of chrome) require the REST server to have a DNS
domain name (instead of IP) if SSL is enabled as additional security. In this release, we
added a configuration to specify the DNS name for the REST server. This is not required
for SSL to be enabled and not all clients require the server to be configured this way.

This configuration is `CEREBRO_SSL_FQDN`. For example:

```shell
export CEREBRO_SSL_FQDN=cluster1.cloud.com
```

Note that due to our traffic routing, this can be the DNS name of any machine in the
cluster, for example, the CNAME for the cluster.

## Bug Fixes

**Support for EMR up to 5.9**
Hive in EMR 5.8, introduced a non backwards compatible change which caused issues for
older versions (0.6 and before) of Cerebro EMR clients. This has been fixed in 0.6.1
and now supports all versions from 5.3 to 5.9.

# 0.6.0 (Sep 2017)

0.6.0 is a major release. It includes major new features and numerous improvements
across the Cerebro services.

## New Features

**New Web UI**

The Web UI has been revamped with a new look-and-feel, enhanced stability, and
several new features, including improvements to dataset discoverability, metadata, and
account information.

Datasets are now searchable from the dataset browser; search for datasets by name.

Dataset information has been expanded. We now explicitly display which columns the
current user has access to and which groups grant access to those columns without
access. We also show which columns are partitioning columns. Finally, a description
of how to integrate a dataset with `R` has been added.

On the home page, account details are displayed, including the token for the current
user, which groups the current user belongs to, the roles the current user has,
and the groups granting those roles.

See the [docs](WebUI.md) for more information.

**Significantly improved integration with EMR**

EMR integration has been significantly improved, allowing better pushdown into CDAS
across the engines, improved multi-tenant user experience, work scheduling, etc. See
[EMR docs](EMRIntegration.md) for more details.

**Support for SSL**

This release adds SSL support to the REST server and WebUI. If configured, users should
switch to https, whenever interacting with either of these services.

## Minor Features

- Added cli commands to specify the number of planners. See
[docs](ClusterAdmin.md) for more details.
- Added cli commands to specify additional arguments for the planners and workers.
- Improved load balancing for worker tasks. Users should see more consistent load on
workers, particularly in the case when there are a smaller number of total tasks.
- Significantly reduced memory usage when executing joins.
- Improved install times. Cerebro binary sizes have been significantly reduced.

## Incompatible and breaking changes

**Deprecating specifying user token as part of URL for REST server**

We will be deprecating supporting specifying user tokens as part of the URL in a
subsequent release and recommend users start switching now when using the REST server.
For example, instead of querying `rest-server-host:port/api/databases?user=<TOKEN>`,
clients should instead specify the token as part of the Authorization header. See these
[docs](Authentication.md) for more details.

**WebUI port renamed**

The name of the webui port has been renamed from `cerebro_catalog_ui:webui` to
`cerebro_web:webui`. This value is used when configuring ports
(`CEREBRO_PORT_CONFIGURATION`) as well as the output from listing the service endpoints.

**Permission roles are now consistently case insensitive**

Case sensitivity in roles was inconsistent and now we've made them consistently case
insensitive. This means that commands such as `CREATE ROLE admin_role` and
`CREATE ROLE ADMIN_ROLE` now are identical.

**Output of `cerebro_cli clusters nodes` changed**

The output is no space separated and not commas to make it easier to interop with
ecosystem tools. If this was consumed from scripts, they may need to be updated.

## Known issues

**Dataset preview in Web UI or Catalog REST API shows 0s instead of NULLs**

When navigating to a particular dataset in the Web UI and clicking "Show Preview",
if the value of a particular cell is NULL, it is displayed as 0. This issue
additionally exists when querying the Catalog REST API at `/api/scanpage/<dataset>`.

The workaround to determine the correct value of the cell is to query the dataset
records via alternate Cerebro clients, like `dbcli`.

**Unable to upgrade an existing cluster from 0.5 to 0.6**

The install binary format has changed in 0.6, meaning clusters prior to 0.6 will not
be able to handle the binary images. Note that the metadata stored in a 0.5 cluster *can*
be read by a 0.6 cluster. Users can create a new 0.6 cluster instead. If upgrading an
existing cluster is important, contact us and we can manually do this.

**Unable to see databases if user has only been granted columns to objects in database**

If a user has been granted only partial access to all objects (table or views)
in a database, they are not able to see the database or any of the contents in it. Users
are only able to properly see the objects if they've been granted full access to at
least one table or view in that database (at which point the access controls work as
expected).

Workaround: create a dummy table in these database and grant users full select on this
table.

**Hive in EMR does not support all DDL commands**

The Hive Cerebro integration for EMR does not currently support all DDL commands. It
does not support GRANT/REVOKE statement and ALTER TABLE statements.

Workaround: use the DbCli or connect through a kerberized Hive installation.

# 0.4.3 and 0.5.1 Release Notes (August 2017)

0.4.3 and 0.5.1 are minor patch release that contain significant performance fixes
as well as critical fixes for the Hive EMR integration.

It is recommended that all 0.4.x and 0.5.x users upgrade.

In particular:

- Significant speedups handling tables with larger number of partitions
- Improved column pruning and predicate pushdown when using Hive in EMR.

## New Features

The EMR integration has been updated on the configs that are required for better
Spark and Hive integration. In particular, we recommend specifying the Cerebro
planner.hostports config for *Spark's* hive-site.xml config. This has been
updated in the EMR. [docs](SupportedSQL.md)

We've also updated the client versions to 0.5.1 and EMR clusters should be
bootstrapped with this version (from 0.5.0).

# 0.5.0 Release Notes (July 2017)

0.5.0 is a  major release. It contains all the bug fixes from 0.4.1 and 0.4.2 and
numerous other bug fixes.

## New Features

**JSON Web Token and SSO support**

This release adds support for authentication with CDAS using JSON Web Tokens(JWT).
These tokens can verified by CDAS using either public/private keys or via an
external SSO server. This can be used in addition to or instead of kerberos
authentication. If an external SSO server is configured, Cerebro can also be
configured to generated SSO tokens in our webui.

**Support for joins with views**

Support has been added to create views which contain joins. Previously, views only
support filters and projections. This enables use cases where the sensitive data
needs to be joined against a (dynamic) whitelist. After creating the view, the
identical grant/revoke statements can be used to control access to it. The kind
of joins we enable is limited, see [docs](SupportedSQL.md) for more details.

**Improved support for EMR, including Hive and Presto**

While previous versions could support EMR, we've improved the integration experience,
providing a bootstrap action which enables deeper integration. See the
[EMR Integration docs](EMRIntegration.md) for details.

**Simplified Install Process**

DeploymentManager config verification has been improved significantly to catch
more configuration issues as well as report the issues more intuitively. Please
let us know how to improve this further. We've also removed the steps to upload
any config files to S3 manually as part of the install.

**Hadoop client updates**

This release corresponds to the beta-6 release of the Hadoop client libraries. While
previous clients are API compatible, we advise upgrading to this version.

## Incompatible and breaking changes
**Planner_worker service port renamed**

The cerebro_planner_worker service ports have been split into cerebro_planner and
cerebro_worker. Specifically:
  - cerebro_planner_worker:planner is now cerebro_planner:planner
  - cerebro_planner_worker:worker is now cerebro_worker:worker

This will need to be updated in the CEREBRO_PORT_CONFIGURATION value as well as the
output of 'cerebro_cli clusters list.'

**Java client token authentication change**

Previously, Hadoop java clients (e.g. MapReduce, Spark, Pig, etc) needed to specify
the service name if they were using token authentication. This service name had to
match the principal of the cerebro cluster. For example, if the Cerebro cluster had
the kerberos principal 'cdas/service@REALM', the service name had to be 'cdas'. This
value could change from CDAS cluster to CDAS cluster which can be difficult.

In 0.5.0, we've update it so this value should always be 'cerebro' and we recommend
that clients not set it at all (it is the default in the updated client jars). Clients
that were setting it will see connection failures.

For example, in spark, applications should remove specifying:
'spark.recordservice.delegation-token.service-name'.

**Environment variable name change**

The environment variable CEREBRO_JWT_SERVICE_TOKEN_FILE has been replaced with
CEREBRO_SYSTEM_TOKEN. Users upgrading from 0.4.5 will need to update this config.

## Known issues

**Unable to see databases if user has only been granted columns to objects in database**

If a user has been granted only partial access to all objects (table or views)
in a database, they are not able to see the database or any of the contents in it. Users
are only able to properly see the objects if they've been granted full access to at
least one table or view in that database (at which point the access controls work as
expected).

Workaround: create a dummy table in these database and grant users full select on this
table.

**Hive in EMR does not support all DDL commands**

The Hive Cerebro integration for EMR does not currently support all DDL commands. It
does not support GRANT/REVOKE statement and ALTER TABLE statements.

Workaround: use the DbCli or connect through a kerberized Hive installation.

# 0.4.2 Release Notes (July 2017)

0.4.2 contains a critical bug fix for users running a newer kernel with the Stack Guard.
protection. This will result in the CDAS services (planner and worker) crashing on start.

Kernels with this version are known to be affected: kernel-3.10.0-514.21.2.el7.x86_64.
We recommend all 0.4.x users upgrade.

For more information, see [this](https://access.redhat.com/solutions/3091371)

# 0.4.5 Release Notes

## June-2017

0.4.5 contains support support for JSON Web Tokens (JWT) for authentication. Users not
using JWTs do not need to upgrade to this version. For details on how to configure JWT
support, see the install docs.

### Incompatible and breaking changes
**Planner_worker service port renamed**

The cerebro_planner_worker service ports have been split into cerebro_planner and
cerebro_worker. Specifically:
  - cerebro_planner_worker:planner is now cerebro_planner:planner
  - cerebro_planner_worker:worker is now cerebro_worker:worker

This will need to be updated in the CEREBRO_PORT_CONFIGURATION value as well as the
output of 'cerebro_cli clusters list.'

# 0.4.1 Release Notes

## June-2017

0.4.1 contains bug fixes for 0.4.0 and it is recommended to upgrade all 0.4.0 clusters.

## Upgrading
First, upgrade the DeploymentManager. While the upgrade is happening, existing CDAS
clusters will continue to be operational.

```
cd /opt/cerebro # Or where your existing install directory is.

# Get the tarball from S3.
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.4.1/deployment-manager-0.4.1.tar.gz

# Extract the bits.
rm -f deployment-manager
tar xzf deployment-manager-0.4.1.tar.gz && rm deployment-manager-0.4.1.tar.gz && ln -s deployment-manager-0.4.1 deployment-manager

# Restart the DeploymentManager
/opt/cerebro/deployment-manager/bin/deployment-manager

# Upon restarting, the new DeploymentManager will take a few seconds to health check
# the existing services.
```

When all the existing clusters report READY, upgrade those clusters one by one. For
each cluster, run:
```
cerebro_cli clusters upgrade --version=0.4.1 <CLUSTER_ID>
```

This will take a few minutes to download the updated binaries and restart the cluster
afterwards. The existing services will be operational while the download is occurring.
See the cluster admin docs for more details.

## Bug Fixes
  - Fixes to the Tableau WDC connector to be tolerant to catalog errors.
  - Fixes for users specifying a custom 'core-site.xml' or 'hive-site.xml' config. In
    0.4.0, we were not picking up these configs files correctly.

## New Features
In addition, we've added a new feature to allow the user to configure CNAMEs for
service endpoints. This is useful for example, if end users should only access the
cdas_rest_server API endpoint behind a CNAME.
In the example below, the service endpoint `cdas_rest_server:api` maps to the CNAME
`cname1.example.com`

Update the CEREBRO_SERVICE_CNAME_MAP config in your env.sh file as follows:
```shell
$ export CEREBRO_SERVICE_CNAME_MAP="<service_endpoint_host>:<service_endpoint_port>:<CNAME>"
```

In the case of the above example this becomes:
```shell
$ export CEREBRO_SERVICE_CNAME_MAP="cdas_rest_server:api:cname1.example.com"
```
Once set, restart your DeploymentManager.

**NOTE:** If you upgrade your DeploymentManager to 0.4.1, make sure you upgrade CDAS to
0.4.1 as well.

# 0.4.0 Release Notes

## May-2017

### New Features

**Cluster Administration**

Cluster administration has been significantly enhanced to protect your cluster from
accidental termination, scaling an existing cluster, and upgrading to newer versions
of CDAS components.
See [Cluster Administration](ClusterAdmin.md) for further details.

**SQL Statement Processing**

The Record Service daemon and catalog-admin rest endpoint scan and scanpage APIs now
process SQL statements through a POST interface.

**Database Command Line Interface (CLI)**

End-user database and dataset functionality is made available through a command line (CLI)
tool, dbcli.  The tool enables users to acquire tokens, list databases, list datasets in
a database, show the schema for a dataset (describe), view a sample of data, create tables
and grant permissions through Hive DDL.

See [Database CLI](DbCLI.md) for details.

**Basic Authentication using LDAP**

With this release, Basic authentication using LDAP is introduced, which should
allow Cerebro users to authenticate using their Active Directory credentials.

The user now has an option to either use the REST API or the new
web-based login UI to get their Cerebro token using their Active Directory
username and password.

See the [LDAP Basic Auth Document](LdapAuthentication.md) for details.

### Changes
**cerebro_cli utility**

The following subcommands were added:
* `cerebro_cli agents state`
    * lists the state of all agents, grouped by cluster
* `cerebro_cli clusters nodes <cluster id>`
    * lists the nodes that constitute the indicated cluster
* `cerebro_cli clusters set_default_version
      --version <version>
      --components <componenta:version,componentb:version> <clusterID>`
    * Sets the version to be used by clusters that are subsequently created.
      The version flag sets the version of CDAS to install and the
      components flag allows for specific components to run a different, likely higher,
      version of that component. This command wipes out any existing version and
      component settings. The --version argument must be a valid version,
      --components is also required but can be an empty string.

**AWS Region Configuration**

Deployment Manager will now detect the AWS region that it is running in if one is not
configured via the AWS_DEFAULT_REGION value. The result is cached, requiring a
Deployment Manager restart (following a change in your configuration) if you want to
manage a cluster in another AWS region.

**Kerberos**

DeploymentManager allows a kerberos principal for the REST API to be explicitly specified.
Previously this was assumed to be derived from the service principal
(i.e. HTTP/<service_host>).
See [Kerberos](KerberosClusterSetup.md) docs for more details.

**Deployment Manager**

The S3_STAGING_DIR environment variable is now validated during Deployment Manager
startup.
Improved reporting of issues that arise during agent startup.
Configured service port uniqueness is now enforced during Deployment Manager startup.

**Using Encrypted S3 buckets**

Writes to S3 buckets now set the server-side encryption flag on the S3 write or copy
request if the S3_STAGING_ENCRYPTION configuration is set to true.

### Incompatible and Breaking Changes
**Change to CEREBRO_KERBEROS_KEYTAB_FILE config**

Previously, this config used to be the basename of the keytab file and the user was
required to upload the file to Cerebro's S3 staging directory. In this release, this
config has been updated to be the full path to the keytab. The path can be on the
DeploymentManager machine (e.g. /etc/keytabs/cerebro.keytab) or on S3. No steps are
now required to upload the keytab to the staging directory.

The prior config will no longer validate and the DeploymentManager will not
start up. Users coming from a previous release will need to update their configs.
For example, by changing:
```
export CEREBRO_KERBEROS_KEYTAB_FILE=cerebro.keytab
# to
export CEREBRO_KERBEROS_KEYTAB_FILE=/path/on/deployment-manager/cerebro.keytab
```

**Rename database to db**

Some REST APIs used the field name 'database' and others used the field name 'db'.
All APIs were changed to consistently use the term 'db'  This impacts the following APIs:

* api/datasets [POST]
* api/datasets/{name} [POST]

For detail see: [Catalog REST API](CatalogApi.md).

### Known issues
**Errors during Deployment Manager configuration file writes prevent restart**

If the configuration file for Deployment Manager is not correctly written to RDS (due to
an issue occurring during the write), then the Deployment Manager  will not be able to
start again. The workaround is to manually update (correct) the underlying configuration
file.

**Unable to delete a launching cluster**

In some cases, it is not possible to immediately terminate a cluster that is launching
and the delete will only go into effect after it is done launching. To get the cluster
to delete immediately, manually terminating/shutting down the launching machines will
work.

# 0.3.0 Release Notes

## February-2017
This is the feature complete release candidate of CDAS.

### New Features
**Tableau Cerebro Catalog Integration**

You can access data stored in Cerebro using Tableau.
See [Tableau WDC](TableauWDC.md) for details.

**Catalog UI**

Beta release of the catalog webui. You can see the datasets that are in the system and
how to read them from a variety of integration points. Just navigate to the
cerebro_catalog_ui:webui end point and log in with your user token.

**Catalog REST API Integration**

Changes were made to the Catalog REST API.  See [Catalog API](CatalogApi.md) and the
[tutorial](CatalogApiTutorial.md) for further details.

**Installation Process**

The installation process has been enhanced by providing customizable templates for
launching EC2 instances and initializing cluster nodes.  See:
[Installation Guide](Install.md), "Starting up a CDAS cluster" for details.

**Authentication**

With this release, all Cerebro services can run with authentication enabled end-to-end.
See: [Authentication](Authentication.md)
for further details. This includes non-kerberized clients (for example the catalog webui)
using tokens.

For information on setting up a Kerberized cluster, see:
[Kerberized Cluster Setup](KerberosClusterSetup.md)

### Changes
**Admin Dashboard**

The Kubernetes admin dashboard has been upgraded to version 1.5.1 from version 1.4.2.
See [Kubernetes Quickstart](KubernetesDashboardQuickStart.md) for details.

**Kubernetes**

Kubernetes has been upgraded to version 1.5.3 from version 1.4.2.

### Incompatible and Breaking Changes
Renamed *cerebro\_catalog\_ui* to *cdas\_rest\_server*.  This is a port configuration
change and will require users to update their env file. Note that this point will also
need to be exposed.

Installation instructions moved components from /var/run/cerebro to /etc/cerebro. Prior
versions of the install script recommended you place various files (on the
DeploymentManager machine) in /var/run/cerebro.
If you have built scripts and automation following those steps, those should be adapted
to use /etc/cerebro instead.

### Known issues
Catalog UI sometimes does not refresh databases correctly. Refresh from the browser as a
workaround.

# 0.2.0 Release Notes

## 02-03-2017
0.2.0 Cerebro CDAS release makes significant improvements on usability, security and
reliability.

### New Features
**Installation**

Install process has been further simplified with fewer steps and faster deployment.
Configuration steps include examples. See `Install.md`
Logging improvements assist in faster problem determination, if any.

**Server**

Deployment Manager(DM) server has evolved to an agent architecture. Each of the cluster
nodes will now run a DM agent to deploy and launch Kubernetes services.

See `Install.md` for details.

**Security**

With this release, REST api to deployment manager can be secured using Kerberos.
Along with Kerberos authentication, authorization may be configured for admin access to deployment manager.
See `Security.md` for additional details.

**Admin Dashboard**

You may now use the Kubernetes admin dashboard for managing the Cerebro cluster.
See `KubernetesDashboardQuickStart.md` for details.

**CLI interface**

cerebro_cli has new commands to interact with the DM REST API and the agents.
See `CerebroCLI.md` for details. `Install.md` has a few examples.
Run `cerebro_cli help` to see the much improved command line options.

### Incompatible Changes
No known incompatibilities exist.

### Known issues
**Cluster create fails occasionally**

This manifests with an error message like:
Unreachable external machine: 10.1.10.101:8085.
Expecting Cerebro agent to be running at: 10.1.10.101:8085.

The workaround is to rerun the command few seconds later.

**Duplicate IP address in cluster machine list will cause launch failures**
If an IP address appears more than once in the machines list, the install process
will fail.

The workaround is to ensure that there are no dups.

# Release Notes

## 12-6-2016
Multiple updates were made on how to interact with the Cerebro catalog. Some of these
changes are not backwards compatible.

### New Features
**CLI interface for catalog**

The REST API was intended for programmatic access. While it is possible to use it
interactively (using curl), it is not very user friendly. We have added a CLI that
sits on top of the REST API. It provides identical capabilities to going against the
REST API directly.

**Permissions API**

We added a permissions API to both the REST API and CLI which can be useful to examine
the aggregate result of the policies that have been set. It is useful to answer
questions such as:
  - What datasets (and pieces of them) does this particular user/group have?
  - What are all the users/groups that have access to this dataset?

### Incompatible Changes
**Policy API has changed**.

The endpoints are different (/api/grant-policy, /api/revoke-policy). The arguments are
largely the same. It is no longer necessary to delete policies and not possible to view
them.

### Known issues:
**Creating the dataset with 'storage_url' currently requires the dataset to be parquet.**

The workaround is to specify the request using hiveql.

**Granting a policy with filters is disabled**

This causes compatibility issues with clients such as Impala. The workaround is to register
the dataset with the filter and grant the policy on the new dataset.
