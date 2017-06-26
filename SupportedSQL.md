# Supported SQL
CDAS allows datasets to be defined using SQL. This is typically done by creating
base datasets and then defining views on top of them. CDAS is not a full massively
parallel processing (MPP) analytics engine and only a subset of SQL is allowed. This
document describes the subset that is supported.

## Data model and flavor of SQL
CDAS in general supports the identical data model as Apache Hive and is compatible with
HiveQL.

## SELECT and PROJECT
CDAS fully supports SELECT and PROJECT queries, including filtering, projecting a
subset of columns or applying data transformations to columns.

## JOINs
CDAS supports a limited set of joins for the purpose of restricting access to specific
rows for particular users. A canonical use case would be having a fact dataset for
user transactions, which contains a column for the user id. Another, much smaller
dataset, contains the set of user ids which allow analytics to be done on their
activity. CDAS would support filtering the transactions dataset by creating a view
that is a join over the two.

The specific limitations are:
  - Only INNER and LEFT (out, semi, anti) joins are allowed.
  - The smaller tables must be under a maximum configured size. If the smaller tables
    exceed this size, the request will fail at runtime.

Subquery rewrites are supported but must be executable subject to the same constraints.

TODO: add the config flags to set to control this


