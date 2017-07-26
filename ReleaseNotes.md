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
of joins we enable is limited, see
[docs](https://github.com/cerebro-data/external-docs/blob/master/SupportedSQL.md)
for more details.

**Improved support for EMR, including Hive and Presto**

While previous versions could support EMR, we've improved the integration experience,
providing a bootstrap action which enables deeper integration. See the
[EMR Integration docs](https://github.com/cerebro-data/external-docs/blob/master/EMRIntegration.md)
for details.

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

If a user has been granted granted only partial access to all objects (table or views)
in a database, they are not able to see the database or any of the contents in it. Users
are only able to properly see the objects if they've been granted at full access to at
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
See [Cluster Administration](https://github.com/cerebro-data/external-docs/blob/master/ClusterAdmin.md)
for further details.

**SQL Statement Processing**  
The Record Service daemon and catalog-admin rest endpoint scan and scanpage APIs now
process SQL statements through a POST interface.

**Database Command Line Interface (CLI)**  
End-user database and dataset functionality is made available through a command line (CLI)
tool, dbcli.  The tool enables users to acquire tokens, list databases, list datasets in
a database, show the schema for a dataset (describe), view a sample of data, create tables
and grant permissions through Hive DDL.  

See [Database CLI](https://github.com/cerebro-data/external-docs/blob/master/DbCLI.md)
for details.

**Basic Authentication using LDAP**  
With this release, Basic authentication using LDAP is introduced, which should
allow Cerebro users to authenticate using their Active Directory credentials.

The user now has an option to either use the REST API or the new
web-based login UI to get their Cerebro token using their Active Directory
username and password.  

See the [LDAP Basic Auth Document](https://github.com/cerebro-data/external-docs/blob/master/LdapAuthentication.md) for details.

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
See [Kerberos](https://github.com/cerebro-data/external-docs/blob/master/KerberosClusterSetup.md)
docs for more details.

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

For detail see: [Catalog REST API](https://github.com/cerebro-data/external-docs/blob/master/CatalogApi.md).

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
See [Tableau WDC](https://github.com/cerebro-data/external-docs/blob/master/TableauWDC.md) for details.

**Catalog UI**
Beta release of the catalog webui. You can see the datasets that are in the system and
how to read them from a variety of integration points. Just navigate to the
cerebro_catalog_ui:webui end point and log in with your user token.

**Catalog REST API Integration**  
Changes were made to the Catalog REST API.  See [Catalog API](https://github.com/cerebro-data/external-docs/blob/master/CatalogApi.md) and the [tutorial](https://github.com/cerebro-data/external-docs/blob/master/CatalogApiTutorial.md) for further details.

**Installation Process**  
The installation process has been enhanced by providing customizable templates for
launching EC2 instances and initializing cluster nodes.  See:
[Installation Guide](https://github.com/cerebro-data/external-docs/blob/master/Install.md), "Starting up a CDAS cluster" for details.

**Authentication**
With this release, all Cerebro services can run with authentication enabled end-to-end. See:
[Authentication](https://github.com/cerebro-data/external-docs/blob/master/Authentication.md)
for further details. This includes non-kerberized clients (for example the catalog webui) using
tokens.  
For information on setting up a Kerberized cluster, see: [Kerberized Cluster Setup](https://github.com/cerebro-data/external-docs/blob/master/KerberosClusterSetup.md)

### Changes
**Admin Dashboard**  
The Kubernetes admin dashboard has been upgraded to version 1.5.1 from version 1.4.2.
See [Kubernetes Quickstart](https://github.com/cerebro-data/external-docs/blob/master/KubernetesDashboardQuickStart.md)
for details.

**Kubernetes**  
Kubernetes has been upgraded to version 1.5.3 from version 1.4.2.

### Incompatible and Breaking Changes
Renamed *cerebro\_catalog\_ui* to *cdas\_rest\_server*.  This is a port configuration change and will
require users to update their env file. Note that this point will also need to be exposed.

Installation instructions moved components from /var/run/cerebro to /etc/cerebro. Prior versions of
the install script recommended you place various files (on the DeploymentManager machine) in /var/run/cerebro.
If you have built scripts and automation following those steps, those should be adapted to use /etc/cerebro
instead.

### Known issues
Catalog UI sometimes does not refresh databases correctly. Refresh from the browser as a workaround.

# 0.2.0 Release Notes

## 02-03-2017
0.2.0 Cerebro CDAS release makes significant improvements on usability, security and reliability.

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
