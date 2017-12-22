# Extending CDAS

Users can extend Cerebro Data Access Service (CDAS)'s functionality in two ways. CDAS'
supports running user defined functions (UDFs) and Hive Serialization/Deserialization
libraries (SerDes). UDFs and SerDes provide powerful ways to extend the capabilities of
CDAS. Example use cases include:

- UDF: A custom anonymization or data encryption algorithm
- UDF: Complicated filters that are more naturally expressed in code vs SQL
- SerDe: Support reading a file format (i.e. Multi-delimited CSV) not supported by CDAS
- SerDe: Support a custom in-house file format

## Security

UDFs and SerDes run in the same process with the same permissions as the rest of CDAS,
typically running as the system user. In particular these libraries have access to all
the data that CDAS has and a malicious library can potentially access data that is
currently being processed. We assume the libraries are *trusted* and currently the only
measure to protect against ill-behaving libraries is to restrict who can register them.

Only users who are catalog admins are allowed to register and unregister UDFs and SerDes.
It is not possible to delegate this capability to other users (i.e. in the same way the
permission to grant can be delegated).

As a best practice, we also recommend that the location of the libraries be secured to
prevent another user from replacing it with a malicious binary (i.e. most users should
not have write access to that location.)

## UDFs

To use UDFs, the steps are:

- Register the UDF in the CDAS catalog. Only the catalog admins can do this.
- Create views (or issue queries) that use the UDF with typical SQL statements. Any user
can use the UDF.
- Read data from the view. The UDF will be evaluated by CDAS before the data is returned.

As an example for this document, we will use a UDF that accepts strings and masks all
characters in it. For example `mask('hello')-> 'xxxxx'`.

CDAS supports UDFs written against the Hive [interface](https://github.com/apache/hive/blob/master/ql/src/java/org/apache/hadoop/hive/ql/exec/UDF.java).
These JARs should be compatible with Hive, with no additional steps.

For this UDF, the code might look like:

```java
package com.cerebro.hiveudf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * UDF which masks all characters from the input string.
 *
 * SELECT mask('hello')
 * > 'xxxx'
 */
public class MaskUDF extends UDF {
  public Text evaluate(Text t) {
    if (t == null) return null;
    byte[] result = new byte[t.getBytes().length];
    for (int i = 0; i < result.length; i++) {
      result[i] = 'x';
    }
    return new Text(result);
  }
}
```

### Registering the UDF

To register the UDF, use one of our client tools (e.g. `dbcli`) which allow executing DDL
statements against CDAS, using the `CREATE FUNCTION` statement.

```sql
CREATE FUNCTION [IF NOT EXISTS] [db_name.]function_name([arg_type[, arg_type...])
  RETURNS return_type
  LOCATION 's3_path'
  SYMBOL='class name of UDF'
```

Note that function overloading is supported. Functions can have the same name with
different signatures.

For example, with the `MaskUDF` it would be:

```sql
CREATE FUNCTION cerebro_sample.mask(STRING)
  RETURNS STRING
  LOCATION 's3://cerebrodata-public-east/udfs/mask-udf.jar'
  SYMBOL='com.cerebro.hiveudf.MaskUDF'
```

To drop the UDF, use the `DROP FUNCTION` statement:

```sql
DROP FUNCTION [IF EXISTS] [db_name.]function_name([arg_type[, arg_type...])

-- For example
DROP FUNCTION cerebro_sample.mask(STRING)
```

## Using the UDF directly

If directly issuing SQL against our planner, the UDF can be used like any other builtin.
For example:

```sql
SELECT record, cerebro_sample.mask(record) FROM cerebro_sample.sample
```

| record        | cerebro_sample.mask(record) |
| ------------- |:-------------:|
|This is a sample test file.|xxxxxxxxxxxxxxxxxxxxxxxxxxx|
|It should consist of two lines.|xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx|

## Using the UDF from views

We expect in many cases, users will access CDAS through another SQL tool, which may or
may not know how to handle the UDFs and the most common way will be to "hide" the UDF
behind a CDAS view. In the on-going example if we wanted to protect the
`cerebro_sample.sample` dataset with the UDF, we would create a view that applies the
UDF to the columns in it, and then grant access to the view. For example:

```sql
CREATE VIEW cerebro_sample.secure_sample as
SELECT cerebro_sample.mask(record) as record
FROM cerebro_sample.sample
-- GRANT SELECT ON table cerebro_sample.secure_sample to ROLE <ROLE>
```

Users can then select from this view, and the UDF is automatically applied.

```sql
SELECT * from cerebro_sample.secure_sample
```

| record        |
| ------------- |
|xxxxxxxxxxxxxxxxxxxxxxxxxxx|
|xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx|

In this case the UDF is completely hidden from the compute tool talking to CDAS. The tool
does not know (and therefore have access to the UDF binary in anyway).

### Errors

**User is not admin**

In this case, the user is not a catalog admin. He/she will see:

`AuthorizationException: User 'dev-user' does not have privileges to CREATE/DROP functions.`

**Dropping a function that is being used**

In this case, the views that depend on it will fail. The error will be:

`AnalysisException: cerebro_sample.mask() unknown.`

## Limitations

**Cannot grant on UDF**

We currently do not provide access controls on the UDF. Only admins can create them, and
then all users can use them. It is not possible to grant usage of a UDF to a particular
user/group.

**Cannot issue CREATE/DROP FUNCTION through hive CLI/beeline**

The hive client integration currently does not support this DDL statement. This is
expected to be added in the very near future.

## SerDes

CDAS supports a subset of valid Hive SerDes. The SerDes must use the `text` serialization
meaning that the file format consists of line by line text, with some arbitrary
serialization for each line. Examples of SerDes that are supported are:

- Regex SerDe (each line's contents can be extracted by RegEx)
- Thrift SerDe (each line is a serialized thrift object)
- Json SerDe (each line is json)
- Multi-Delimiter CSV SerDe (each line has a CSV structure, with many options)

Example of SerDes that are not supported include file formats which for sophisticated
file structure:

- SequenceFile
- Avro
- Parquet
- Orc

SequenceFile, Avro and Parquet are natively supported by CDAS and does not need a SerDe.

### Using a SerDe

The SerDe library must be specified when creating the table. The DDL to use a SerDe is
extends the `CREATE TABLE` statement, with additional options. The path to the SerDe jar
must be specified (can be any URI, typically the S3 path), the fully qualified classpath
and optionally any additional properties for the SerDe. The table can be partitioned or
created with comments, just like any other table.

```sql
CREATE [EXTERNAL] [IF NOT EXISTS] TABLE <tbl>(<SCHEMA>)
ROW FORMAT SERDE '<PATH TO SERDE JAR>' SYMBOL '<CLASS NAME OF SERDE>'
[WITH SERDEPROPERTIES('<key>' = 'value')]
[LOCATION | COMMENT | ...]
```

For example, to use Hive's [RegexSerDe](https://github.com/apache/hive/blob/trunk/contrib/src/java/org/apache/hadoop/hive/contrib/serde2/RegexSerDe.java),
assuming the library is available at `s3://my-company/serdes/regex-serde.jar`:

```sql
CREATE TABLE apachelog (
  host STRING,
  identity STRING,
  user STRING,
  ts STRING,
  request STRING,
  status STRING,
  size STRING,
  referrer STRING,
  agent STRING)
ROW FORMAT
SERDE 'org.apache.hadoop.hive.serde2.RegexSerDe'
JAR_PATH 's3://my-company/serdes/regex-serde.jar'
WITH SERDEPROPERTIES (
  "input.regex" = "([^]*) ([^]*) ([^]*) (-|\\[^\\]*\\]) ([^ \"]*|\"[^\"]*\") (-|[0-9]*) (-|[0-9]*)(?: ([^ \"]*|\".*\") ([^ \"]*|\".*\"))?"
)
```

### Tutorial for using Ç (Cedilla-C) as a delimiter

As an end to end example, we'll load a dataset in S3 which uses the [Cedilla-C](https://en.wikipedia.org/wiki/Ç)
as the delimiter, using Hive's [MultiDelimitSerde](https://github.com/apache/hive/blob/0af6cb42725659740a022044c6cc464ef1cf4e6b/contrib/src/java/org/apache/hadoop/hive/contrib/serde2/MultiDelimitSerDe.java).

The folder being used in this tutorial is available publicly at
```s3://cerebro-datasets/cedilla_sample/```.

The folder contains a file named `cedilla_sample.txt`, the contents of which are
```
1Ç23
Ç45

```


The steps are:
```shell
# Create the database
$ ./dbcli dataset hive-ddl "create database if not exists multibyte_db"

# Create a table that uses Ç as the field delimiter
$ ./dbcli dataset hive-ddl "CREATE EXTERNAL TABLE multibyte_db.cedilla_sample(
  int1 int,
  int2 int)
ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.MultiDelimitSerDe'
WITH SERDEPROPERTIES ('field.delim' = 'Ç')
STORED AS TEXTFILE
LOCATION 's3://cerebro-datasets/cedilla_sample/'"

# Read from the table to make sure everything works
$ ./dbcli dataset cat multibyte_db.cedilla_sample

# The output expected is
--- multibyte_db.cedilla_sample ---
[
    {
        "int1": 1,
        "int2": 23
    },
    {
        "int1": 0,
        "int2": 45
    }
]
```

## Permission Cascading

When permissions are granted on S3 URIs, they are applied to any subdirectories contained
within the directory that the permissions are granted on. This allows access to be granted
on a top-level S3 bucket or any arbitrarily deep subdirectory.
