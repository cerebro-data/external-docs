# Supported SQL

CDAS enables the use of SQL for defining datasets. This is done by creating base
datasets and then defining views. CDAS is not a full massively parallel
processing (MPP) analytics engine. Only a subset of SQL is allowed. This document
describes the supported SQL subset.

CDAS in general supports the identical data model as Apache Hive and is generally
compatible with HiveQL.
Supported functionality may differ depending on the client.

* [Data Definition Statements](#data-definition-language-ddl-statements)
* [Data Manipulation Statements](#data-manipulation-language-dml-statements)

## Data Definition Language (DDL) Statements

CDAS generally supports and is compatible with HiveQL DDL statements. In some
cases, CDAS is not compatible. In other cases, the supported SQL has been extended for
CDAS-specific capabilities - including all statements that modify the catalog
and do not read any data (e.g. `CREATE`, `DROP`, `ALTER`).

### MSCK Repair

CDAS does not support the Hive `MSCK REPAIR TABLE [table_name]`. Instead it supports
the alternative, `ALTER TABLE [table_name] RECOVER PARTITIONS`. This command behaves
identically, otherwise, and automatically adds partitions to the table, based on the
storage directory structure.

### Extensions

**DROP DATABASE/DROP TABLE can optionally drop permissions**

There are use cases where it is valid to retain or drop permissions when the
corresponding catalog object (db, table, or view) is dropped. CDAS extends the
`DROP DATABASE` and `DROP TABLE/VIEW` statements to optionally specify whether the
associated permissions should also be dropped.

```sql
DROP DATABASE [IF EXISTS] db [CASCADE] [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP TABLE [IF EXISTS] [db.]tbl [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP VIEW [IF EXISTS] [db.]v [(INCLUDING | EXCLUDING) PERMISSIONS];
```

If `INCLUDING PERMISSIONS` is specified, the corresponding permissions are also
dropped. Otherwise, permissions are *not* dropped, but are applied to future catalog
objects with that name. If `CASCADE` is specified, then all permissions on the tables
and views in the database are dropped.

It is recommended that users default to the `INCLUDING PERMISSIONS` behavior and update
existing workflows so as to not rely on permissions being retained longer than the object
for which they are created.

For users to drop the permissions, they must have grant permissions on the catalog
object. For example, to be able to drop a database and its permissions, the user must
be able to issue grant/revoke statements on the database. The user needs to be a catalog
admin or having been granted grant permissions.

**CREATE TABLE AS SELECT**

CDAS supports tables created by this method with the following restrictions:
the table must be created from Hive in EMR and the Hive warehouse must have been
configured to use S3 (as opposed to using the EMR-local HDFS cluster).

> **Note:** This functionality is not supported from `dbcli`.

**Registering Hive Serialization/Deserialization (SerDe) Libraries**

See [Extending CDAS](ExtendingCDAS.md) for the DDL grammar and other SerDe
considerations.

**Creating User Defined Functions (UDFs)**

See [Extending CDAS](ExtendingCDAS.md) for the DDL grammar and other UDF considerations.

**Internal verses External Views**

Cerebro views can be defined as either internal or external. This distinction defines
how Cerebro evaluates data at runtime. It could have a profound effect when evaluating
joins between tables and views.

In both internal and external cases, data resides in their source systems. Cerebro managed
data continues to be managed in Cerebro. External views continue to be managed
by their non-Cerebro source. The primary difference between internal and external views is
that external data is not evaluated during a CDAS query. External data does not have
fine-grained access control, UDFs, and other features that Cerebro provides to managed
datasets.

It is because of this property that joins are handled differently in internal views verses
external views.

***Internally Defined Joined Views***

Cerebro manages the join of internal views created from two tables or views, internal or
external, at the query level. Cerebro evaluates the join prior to it being sent to
the analytics/compute engine for further processing. This allows for fine-grained access
control and UDF functionality to be applied to the entire view, regardless of where the
source data resides.

> **Note:** CDAS is not a compute engine. Full SQL functionality is not available through the
CDAS SQL interface. The use of a compute engine for full analytics functionality is required.
For a list of known SQL incompatibilities, refer to the
[Known Incompabilities](#known-incompatibilities) section in this document.

***Externally Defined Joined Views***

External views created of two tables or views, internal or external, are evaluated in a slightly
different way. Data managed by CDAS continues to be evaluated within the CDAS cluster. But,
the join between the two tables or views occurs in the analytics/compute engine. The advantage
of this approach is that CDAS continues to provide fine-grained access control and UDFs on
CDAS managed data, while allowing the sometimes heavy compute of a join to be done outside the
CDAS system.

This approach requires an external analytics/compute engine, such as Hive or Spark to complete
the join prior to execution.

**Creating External Views**

This section provides a number of example common `EXTERNAL` view uses:

To create views that do not require evaluation in CDAS, an external view can be used:

```sql
CREATE EXTERNAL VIEW random_user_subset AS SELECT * FROM all_users WHERE rand() % 10 = 0
```

> **Note:** Views on aggregate functions need to be created as `EXTERNAL` views, since the
aggregates are computed in compute applications like Hive or Spark.

```sql
CREATE EXTERNAL VIEW maxRevenue, minRevenue AS SELECT min(revenue), max(revenue)
FROM cal_sales WHERE region = 'california'
```

Since compute applications do not accept the "`EXTERNAL` view" syntax, it can be executed
using dbcli or Cerebro Web UI.

By default, views without `EXTERNAL` are evaluated in Cerebro, maintaining backwards
compatibility.

**LAST PARTITION**

CDAS extends the SQL grammar to easily restrict a table scan to just the last partition.
For example, if the data is partitioned by day, `LAST PARTITION` can be used to always
return the results for the last day.

`LAST PARTITION` is added as an additional clause to a table name. For example

  ```sql
  SELECT * from part_tbl; -- Returns all partitions
  SELECT * from part_tbl(LAST PARTITION); -- Returns the last partition
  ```

The last partition is last partition of the table after sorting by the partition. Note
that this may not be the most recent partition from when data was most recently added.
It is *not* the last modified partition.

As an example, if the table is partitioned by year, month, day, it will always return
the last date regardless of when the partitions were added or when the data in the
partitions changed.

The last partition computation occurs after partition pruning is done. With the running
example of a table partitioned by year, month, day:

  ```sql
   -- Returns the last partition
  SELECT * from part_tbl(LAST PARTITION);

  -- Return the last partition in this year.
  SELECT * from part_tbl(LAST_PARTITION) where year = 2010;

  -- Return the last partition which is in June.
  SELECT * from part_tbl(LAST_PARTITION) where month = 6;
  ```

Since `LAST PARTITION` is specified per table, it is possible to specify it for each
table in a query independently. For example:

  ```sql
  SELECT * from t1(LAST PARTITION)
  JOIN t2 ...
  JOIN t3(LAST PARTITION)
  ```

#### Limitations

* LAST PARTITION can only be used in internal views as other compute engines may not be
able to support it.

* LAST PARTITION can only be used for base tables. For example, this query will not work.
A workaround is to create the view in the catalog.

  ```sql
  SELECT * from (SELECT * from t) (LAST PARTITION) -- Does not work

  -- Instead, it is recommended to do:
  CREATE VIEW t_last_partition as SELECT * from t (LAST PARTITION);
  SELECT * from t_last_partition;
  ```

### Known Incompatibilities

**Stricter Type Promotion**

Hive/HiveQL is very permissive in type promotions allowing implicit conversions
between most types. In CDAS, only lossless type promotion is implicit (e.g. INT -> BIGINT).
Explicit casts may need to be added for existing SQL statements.

**Disallowing Explicit Partitioning Clause When Creating Views**

Hive/HiveQL is used for creating views with an explicit partitioning clause.

For example:

  ```sql
  CREATE VIEW v as SELECT ... FROM base_tbl
  PARTITIONED BY c1
  ```

CDAS does not allow partitioning to be specified for views. Partitioning is instead
inferred based on the view statement and base table. The
partitioning on the base table is preserved for the view.

This is disallowed. It is unclear what the semantics are, if the partitioning specified
in the view is different from the base table, and what the resulting performance
implications might be.

## Data Manipulation Language (DML) Statements

CDAS is not a distributed SQL engine. It only supports a subset of SQL statements. It
does not support the other DML statements (e.g. `INSERT`, `DELETE`, `UPDATE`, etc). For
`SELECT` statements, only a subset of the SQL standard is supported. A typical
configuration is to run a SQL engine (e.g Spark or Presto) on top of CDAS.

`SELECT` statements with projection and filters are fully supported.

`COUNT(*)` with no grouping is the only *aggregation* supported. Multiple records are
returned for this query, each containing a partial count.
Summing up all the counts returns the complete result.

### JOINs

When using `VIEW`s, CDAS supports a limited set of joins for the purpose of restricting
access to specific rows for particular users. A canonical use case could be having a fact
dataset for user transactions, which contains a column for the user id. Another, much
-smaller dataset, contains the set of user ids which allow analytics to be done on their
activity. CDAS would support filtering the transactions dataset by creating a view that
is a join over the two.

The specific limitations are:

- Only `INNER` and `LEFT` (optionally with `OUTER`, `SEMI`, `ANTI`) joins are allowed.
- The smaller tables must be under a maximum configured size. If the smaller tables
exceed the maximum configured size, the request fails at runtime.

Subquery rewrites are supported but must be executable subject to the same constraints.

#### Configurations

Configurations can be specified at cluster creation time by using the CLI.

By default, joins are enabled with a maximum memory of 128MB per join.

**Disabling entirely**

```shell
cerebro_cli clusters create --plannerConfigs "enable_joins=false" ...
```

**Controlling the maximum memory allowed per join**

```shell
cerebro_cli clusters create --workerConfigs "join_max_mem=<value in bytes>" ...
```

See the [Cluster Sizing](ClusterSizing.md) document for more information on how
much memory joins need and how that affects cluster node requirements.
