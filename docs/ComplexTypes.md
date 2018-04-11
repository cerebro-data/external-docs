# Complex Types

This document describes CDAS's support for complex types. Complex types usually consist
of:

* structs
* lists
* maps
* unions

CDAS currently only supports structs and will support lists and maps soon. There is no
plan to support unions (see exception for Avro below).

## File format support

Complex types are only support when the underlying data files are in Parquet or Avro
format.

## Struct

CDAS supports struct types with up to 100 levels of nesting. Struct types are useful
to model nested data, such as logs or event data that originated as json. As a running
example, let's use this json data as an example

```json
{
  "uid": 100,
  "user" : "alice",
  "address": {
    "city": "san francisco",
    "state": "ca"
  },
  "age": 25
}
```

### CREATE DDL

The syntax for creating the table is compatible with HiveQL, using the Struct type
and listing the fields inside. For this schema:

```sql
CREATE EXTERNAL TABLE users(
  uid BIGINT,
  user STRING,
  address STRUCT<
    city: STRING,
    state: STRING>,
  age INT
) STORED AS AVRO
LOCATION 's3://cerebrodata-test/rs-complex-users/'
```

Describing the table should look something like:

```sql
> describe users;
uid	bigint
name	string
address	struct<
  city:string,
  state:string
>
age	int
```

### Scanning via REST API

The rest API has always returned json and fully supports nested types. For example,
curling this endpoint now returns:

```shell
> curl REST_SERVER:PORT/scan/users?format
curl localhost:11050/scan/users?format
[
    {
        "uid": 100,
        "user": "alice",
        "address": {
            "city": "san francisco",
            "state": "ca"
        },
        "age": 25
    },
    {
        "uid": 101,
        "user": "bob",
        "address": {
            "city": "seattle",
            "state": "wa"
        },
        "age": 25
    }
]
```

### Scanning via SparkSQL

Support for struct types behaves as expected in Spark, integrating with Spark's
record type. For example, valid queries on this table include:

```scala
  sc.sql(s"""
           |CREATE TEMPORARY TABLE users
           |USING com.cerebro.recordservice.spark.DefaultSource
           |OPTIONS (RecordServiceTable 'users')
  """.stripMargin)

  // Just returns the count
  sc.sql("SELECT count(*) from users").collect()

  // Selects just the first column, the type of this is BIGINT
  sc.sql("SELECT uid from users").collect()

  // Selects the first column and a field in the struct. This 'flattens' the nested
  // type. The result type of this is [BIGINT, STRING]
  sc.sql("SELECT uid, address.city from users").collect()

  row = sc.sql("SELECT id, s1.f1 from struct_t").collect()(1)
  assert(row.get(0) == 234)
  assert(row.get(1) == "field2")

  // Selects all the fields from the data. The top level record consists of 4
  // fields: [BIGINT, STRING, STRUCT, INT]
  val row = sc.sql("SELECT * from struct_t").collect()(0)  // Row for 'alice'
  assert(row.get(0) == 100)
  assert(row.get(1) == "alice")
  assert(row.get(2).asInstanceOf[Row](0) == "seattle")
  assert(row.get(2).asInstanceOf[Row](1) == "wa")
  assert(row.get(3) == 25)
```

### Scanning via Hive

The struct type maps to hive's struct type. For example, to describe the schema:

```sql
hive> describe users
OK
uid                 	bigint
user                	string
address             	struct<city:string,state:string>
age                 	int
```

Here are a few example of some scans:

```shell
hive> select * from users;
OK
{"city":"san francisco","state":"ca"}	25
{"city":"seattle","state":"wa"}	25

hive> select uid, `user` from users;
OK
100	alice
101	bob

hive> select uid, address from users;
OK
100	{"city":"san francisco","state":"ca"}
101	{"city":"seattle","state":"wa"}

hive> select uid, address.city from users;
OK
100	san francisco
101	seattle

hive> select * from users where uid = 100
OK
100	alice	{"city":"san francisco","state":"ca"}	25

hive> select address.city, count(*) from users group by address.city
OK
seattle	1
san francisco	1

hive> select * from users where address.state = 'wa'
OK
101	bob	{"city":"seattle","state":"wa"}	25
san francisco	1
```

### Scanning via Presto

Presto has support for struct types. The behavior should be a typical presto experience.

```shell
presto> describe recordservice.default.users;
 Column  |               Type               | Extra | Comment
---------+----------------------------------+-------+---------
 uid     | bigint                           |       |
 user    | varchar                          |       |
 address | row(city varchar, state varchar) |       |
 age     | integer                          |       |
```

Here are a few example of some scans:

```shell
presto> select * from recordservice.default.users;
 uid | user  |            address             | age
-----+-------+--------------------------------+-----
 100 | alice | {city=san francisco, state=ca} |  25
 101 | bob   | {city=seattle, state=wa}       |  25
(2 rows)

presto> select uid, user from recordservice.default.users;
uid | user
-----+-------
 100 | alice
 101 | bob
(2 rows)

presto> select uid, address from recordservice.default.users;
 uid |            address
-----+--------------------------------
 100 | {city=san francisco, state=ca}
 101 | {city=seattle, state=wa}
(2 rows)

presto> select uid, address.city from recordservice.default.users;
 uid |     city
-----+---------------
 100 | san francisco
 101 | seattle
(2 rows)

presto> select * from recordservice.default.users where uid = 100;
uid | user  |            address             | age
-----+-------+--------------------------------+-----
 100 | alice | {city=san francisco, state=ca} |  25

presto> select address.city, count(*) from recordservice.default.users group by address.city
     city      | _col1
---------------+-------
 san francisco |     1
 seattle       |     1

presto> select * from recordservice.default.users where address.state = 'wa';
 uid | user |         address          | age
-----+------+--------------------------+-----
 101 | bob  | {city=seattle, state=wa} |  25
(1 row)

## Union (Avro)

Avro uses unions to represent nullable types by expressing it as a union of the NULL
type and the actual type. All types in CDAS are nullable and this specific use of Avro
unions are supported. Other uses, such as a union between a string and int, are not
supported.

## Enums (Avro)

Enum is a type specific to Avro which Avro considers as a complex type. We do not
support enum as a catalog type (i.e. it is not possible to create a table and specify
a column as enum). Instead, create the table with the type `STRING` and the enum
will be transparently converted. This is the identical behavior as Hive.