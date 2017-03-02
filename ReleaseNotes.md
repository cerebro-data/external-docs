# 0.3.0 Release Notes

## February-2017
This is the feature complete release candidate of CDAS.

### New Features
**Tableau Cerebro Catalog Integration**
You can access data stored in Cerebro using Tableau.
See [Tableau WDC](https://github.com/cerebro-data/external-docs/blob/master/TableauWDC.md) for details.

**Catalog UI**
Alpha release of the catalog webui. You can see the datasets that are in the sytem and how to read
them from a veriety of integration points. Just navigate to the catalog ui webui and point and log
in with your user token.

**Catalog REST API Integration**  
Changes were made to the Catalog REST API.  See [Catalog API](https://github.com/cerebro-data/external-docs/blob/master/CatalogApi.md) and the [tutorial](https://github.com/cerebro-data/external-docs/blob/master/CatalogApiTutorial.md) for further details.

**Installation Process**  
The installation process has been enhanced by providing customizable templates for
launching EC2 instances and initializing cluster nodes.  See:
[Installation Guide](https://github.com/cerebro-data/external-docs/blob/master/Install.md), "Starting up a CDAS cluster" for details.

**Authentication**
With this release, all Cerebro services can run with authentication enabled end-to-end. See:
[Authentication](https://github.com/cerebro-data/cerebro/blob/master/docs/user/Authentication.md)
for further details. This includes non-kerberized clients (for example the catalog webui) using
tokens.

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

