# Supported SQL

CDAS allows datasets to be defined using SQL. This is typically done by creating base
datasets and then defining views on top of them. CDAS is not a full massively parallel
processing (MPP) analytics engine and only a subset of SQL is allowed. This document
describes the subset that is supported.

CDAS in general supports the identical data model as Apache Hive and is generally
compatible with HiveQL.

* [Data Definition Statements](#data-definition-language-ddl-statements)
* [Data Manipulation Statements](#data-manipulation-language-dml-statements)

## Data Definition Language (DDL) Statements

CDAS generally supports the HiveQL DDL statements and tries to be compatible. In some
cases, CDAS is not compatible and in others, the supported SQL has been extended for
CDAS specific capabilities. These include all statements that modify the catalog
and do not read any data (e.g. `CREATE`, `DROP`, `ALTER`).

### MSCK Repair

CDAS does not support the Hive `MSCK REPAIR TABLE [table_name]` and instead supports
the alternative, `ALTER TABLE [table_name] RECOVER PARTITIONS`. This command behaves
identically otherwise and automatically add partitions to the table based on the
storage directory structure.

### Extensions

**DROP DATABASE/DROP TABLE can optionally drop permissions**

There are use cases where it is valid to retain or drop permissions when the
corresponding catalog object (db, table, or view) is dropped. CDAS extends the
`DROP DATABASE` and `DROP TABLE/VIEW` statements to optionally specify whether the
associated permissions should be dropped as well.

```sql
DROP DATABASE [IF EXISTS] db [CASCADE] [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP TABLE [IF EXISTS] [db.]tbl [(INCLUDING | EXCLUDING) PERMISSIONS];
DROP VIEW [IF EXISTS] [db.]v [(INCLUDING | EXCLUDING) PERMISSIONS];
```

If `INCLUDING PERMISSIONS` is specified, the corresponding permissions will also be
dropped; otherwise they will *not* be dropped and will be applied to future catalog
objects with that name. If `CASCADE` is specified, then all permissions on the tables
and views in the database will be dropped as well.

We recommend that users default to the `INCLUDING PERMISSIONS` behavior and update
existing workflows to not rely on permissions being retained longer than the object
they are created for.

For users to drop the permissions, they must have grant permissions on the catalog
object. For example, to be able to drop a database and its permissions, the user must
be able to issue grant/revoke statements on the database. The user needs to be a catalog
admin or been granted grant permissions.

**Registering Hive Serialization/Deserialization (SerDe) Libraries**

See [Extending CDAS](ExtendingCDAS.md) for the DDL grammar and other SerDe
considerations.

**Creating User Defined Functions (UDFs)**

See [Extending CDAS](ExtendingCDAS.md) for the DDL grammar and other UDF considerations.

**Internal vs. External Views**

Cerebro views can be defined as either internal or external. This distinction will define
how Cerebro evaluates your data at runtime and will have a profound effect when evaluating
joins between tables and views.

In both internal and external cases, data will reside in their source systems. Cerebro managed
data will continue to be managed in Cerebro while external views will continue to be managed
by their non-Cerebro source. The primary difference between internal and external views is
that external data will not be evaluated during a CDAS query. External data will not have
fine-grained access control, UDFs, and other features that Cerebro can provide to managed
datasets.

It is because of this property that joins are handled different in internal views vs.
external views.

***Internally Defined Joined Views***

Cerebro will manage the join of internal views created of two tables or views, internal or
external, at the query level. Cerebro will evaluate at the join prior to being sent to
the analytics/compute engine for further processing. This allows for fine-grained access control
and UDF functionality to be applied to the entire view, regardless of where the source data
resides.

Note that CDAS is not a compute engine, so full SQL functionality is not available through the
CDAS SQL interface. The use of a compute engine for full analytics functionality will be required.
For a list of known SQL incompatibilities, refer to the
[Known Incompabilities](#known-incompatibilities) section in this document.

***Externally Defined Joined Views***

External views created of two tables or views, internal or external, are evaluated in a slightly
different way. Data managed by CDAS will continue to be evaluated within the CDAS cluster, but
the join between the two tables or views will occur in the analytics/compute engine. The advantage
of this approach is that CDAS will continue to provide fine-grained access control and UDFs on
CDAS managed data, while allowing the sometimes heavy compute of a join to be done outside the
CDAS system.

This approach will require an external analytics/compute engine such as Hive or Spark to complete
the join prior to execution.

**Creating External Views**

This section provides a number of examples of common `EXTERNAL` view uses:

To create views that do not need to be evaluated in CDAS, an external view can be used:

```sql
CREATE EXTERNAL VIEW random_user_subset AS SELECT * FROM all_users WHERE rand() % 10 = 0
```

Note that views on aggregate functions need to be created as `EXTERNAL` views, since the
aggregates are computed in compute applications like Hive or Spark.

```sql
CREATE EXTERNAL VIEW maxRevenue, minRevenue AS SELECT min(revenue), max(revenue)
FROM cal_sales WHERE region = 'california'
```

Since compute applications do not accept the "`EXTERNAL` view" syntax, this may be executed
using dbcli or Cerebro Web UI.

By default, views without `EXTERNAL` are evaluated in Cerebro, maintaining backwards
compatibility.

### Known Incompatibilities

**Stricter Type Promotion**

Hive/HiveQL is very permissive in type promotions allowing implicit conversions
between most types. In CDAS, only lossless type promotion is implicit (e.g. INT -> BIGINT).
Explicit casts may need to be added for existing SQL statements.

**Disallowing Explicit Partitioning Clause When Creating Views**

Hive/HiveQL allows for creating views with an explicit partitioning clause, for example

```sql
CREATE VIEW v as SELECT ... FROM base_tbl
PARTITIONED BY c1
```

CDAS does not allow partitioning to be specified for views. Partitioning is instead
inferred based on the view statement and base table. This typically means that the
partitioning on the base table is preserved for the view.

This is disallowed as it is unclear what the semantics are if the partitioning specified
in the view is different from the base table and what the resulting performance
implications are.

## Data Manipulation Language (DML) Statements

CDAS is not a distributed SQL engine and only supports a subset of SQL statements. It
does not support the other DML statements (e.g. `INSERT`, `DELETE`, `UPDATE`, etc). For
`SELECT` statements, only a subset of the SQL standard is supported. A typical
configuration is to run a SQL engine (e.g Spark or Presto) on top of CDAS.

`SELECT` statements with projection and filters are fully supported.

The only *aggregation* that is supported is `COUNT(*)` with no grouping. In this case
multiple records will be returned for this query, each containing a partial count.
Summing up all the counts returns the complete result.

### JOINs

Using `VIEW`s, CDAS supports a limited set of joins for the purpose of restricting access
to specific rows for particular users. A canonical use case would be having a fact
dataset for user transactions, which contains a column for the user id. Another, much
smaller dataset, contains the set of user ids which allow analytics to be done on their
activity. CDAS would support filtering the transactions dataset by creating a view that
is a join over the two.

The specific limitations are:

- Only `INNER` and `LEFT` (optionally with `OUTER`, `SEMI`, `ANTI`) joins are allowed.
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

See the [Cluster Sizing](ClusterSizing.md) document for more information on how
much memory joins will need and how that is affecting cluster node requirements.
