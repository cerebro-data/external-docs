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
See 'KubernetesDashboardQuickStart.md` for details.

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

