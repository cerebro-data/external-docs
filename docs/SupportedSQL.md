# Supported SQL

CDAS allows datasets to be defined using SQL. This is typically done by creating base
datasets and then defining views on top of them. CDAS is not a full massively parallel
processing (MPP) analytics engine and only a subset of SQL is allowed. This document
describes the subset that is supported.

CDAS in general supports the identical data model as Apache Hive and is generally
compatible with HiveQL.

* [Data Definition Statements](#data-definition-language-ddl-statements)
* [Data Manipulation Statements](#data-manipulation-language-dml-statements)

## Data Definition Language (DDL) statements

CDAS generally supports the HiveQL DDL statements and tries to be compatible. In some
cases, CDAS is not compatible and in others, the supported SQL has been extended for
CDAS specific capabilities. These include all statements that modify the catalog
and do not read any data (e.g. create, drop, alter).

### MSCK Repair

CDAS does not support the Hive `MSCK REPAIR TABLE [table_name]` and instead supports
the alternative, `ALTER TABLE [table_name] RECOVER PARTITIONS`. This command behaves
identically otherwise and automatically add partitions to the table based on the
storage directory structure.

### Extensions

**DROP DATABASE/DROP TABLE can optionally drop permissions**

There are use cases where it is valid to retain or drop permissions when the
corresponding catalog object (db, table, or view) is dropped. CDAS extends the
DROP DATABASE and DROP TABLE/VIEW statements to optionally specify whether the
associated permissions should be dropped as well.

```sql
DROP DATABASE [IF EXISTS] db [CASCADE] [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP TABLE [IF EXISTS] [db.]tbl [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP VIEW [IF EXISTS] [db.]v [(INCLUDING | EXCLUDING) PERMISSIONS];
```

If `INCLUDING PERMISSIONS`, the corresponding permissions will also be dropped;
otherwise they will *not* be dropped and will be applied to future catalog objects with
that name. If `CASCADE` is specified, then all permissions on the tables and views in the
database will be dropped as well.

We recommend that users default to the `INCLUDING PERMISSIONS` behavior and update
existing workflows to not rely on permissions being retained longer than the object
they are created for.

For users to drop the permissions, they must have grant permissions on the catalog
object. For example, to be able to drop a database and its permissions, the user must
be able to issue grant/revoke statements on the database. The user needs to be a catalog
admin or been granted grant permissions.

**Registering Hive Serialization/Deserialization (SerDe) libraries**

See this [document](ExtendingCDAS.md) for the DDL grammar and other SerDe considerations.

**Creating User Defined Functions (UDFS)**

See this [document](ExtendingCDAS.md) for the DDL grammar and other UDF considerations.

**Creating external views**

By default, views created in the Cerebro catalog are evaluated in CDAS. This means that
clients reading the view, get the data only after the view transformations are applied.
This property is critical for views that enforce security. For example, a view that
filters out users that are inactive:

```sql
CREATE VIEW active_users_only AS SELECT * FROM all_users WHERE active = true
```

Must be evaluated in CDAS, even if the tool reading from CDAS is able to understand and
evaluate that predicate.

However, there are other use cases where the views just store non-security related data
transformations. In this case, it can be useful to return the view definition to the
compute application. This allows views to be created that implement SQL functionality
not supported in CDAS as well as potentially deeper integration with the compute
engine's query optimization.

To create views that do not need to be evaluated in CDAS, an external view can be
created. For example:

```sql
CREATE EXTERNAL VIEW random_user_subset AS SELECT * FROM all_users WHERE rand() % 10 = 0
```

By default, views without `EXTERNAL` are evaluated in Cerebro, maintaining backwards
compatibility.

### Known incompatibilities

**Stricter type promotion**

Hive/HiveQL is very permissive in type promotions allowing implicit conversions
between most types. In CDAS, only lossless type promotion is implicit (e.g. INT -> BIGINT).
Explicit casts may need to be added for existing SQL statements.

**Disallowing explicit partitioning clause when creating views**

Hive/HiveQL allows for creating views with an explicit partitioning clause, for example

```sql
CREATE VIEW v as SELECT ... FROM base_tbl
PARTITIONED BY c1
```

CDAS does not allow partitioning to be specified for views. Partitioning is instead
inferred based on the view statement and base table. This typically means that the
partitioning on the base table is preserved for the view.

This is disallowed as it is unclear what the semantics are if the partitioning specified
in the view is different than the base table and the performance implications.

## Data Manipulation Language (DML) statements

CDAS is not a distributed SQL engine and only support a subset of SQL statements. It does
not support the other DML statements (e.g. INSERT, DELETE, UPDATE, etc). For SELECT
statements, only a subset of the SQL standard is supported. A typical configuration is
to run a SQL engine (e.g Spark or Presto) on top of CDAS.

SELECT statements with projection and filters are fully supported.

The only AGGREGATION that is supported is `count(*)` with no grouping. In this case
multiple records will be returned for this query, each containing a partial count.
Summing up all the counts returns the complete result.

### JOINs

CDAS supports a limited set of joins for the purpose of restricting access to specific
rows for particular users. A canonical use case would be having a fact dataset for user
transactions, which contains a column for the user id. Another, much smaller dataset,
contains the set of user ids which allow analytics to be done on their activity. CDAS
would support filtering the transactions dataset by creating a view that is a join over
the two.

The specific limitations are:

- Only INNER and LEFT (out, semi, anti) joins are allowed.
- The smaller tables must be under a maximum configured size. If the smaller tables
exceed this size, the request will fail at runtime.

Subquery rewrites are supported but must be executable subject to the same constraints.

#### Configurations

Configurations can be specified at cluster creation time via the CLI.

By default, joins are enabled with a maximum memory of 128MB per join.

**Disabling entirely**

```shell
cerebro_cli clusters create --plannerConfigs "enable_joins=false" ...
```

**Controlling the maximum memory allowed per join**

```shell
cerebro_cli clusters create --workerConfigs "join_max_mem=<value in bytes>" ...
```
