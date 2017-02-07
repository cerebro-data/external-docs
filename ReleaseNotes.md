# Release Notes

## 12-6-2016
Multiple updates were made on how to interact with the cerebro catalog. Some of these
changes are not backwards compatible.

### New Features
**CLI interface for catalog**
The REST API was intended for programmatic access and while it is possible to use it
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

