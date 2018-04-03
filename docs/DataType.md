# Data Types

This document describes how Cerebro handles data types and values. We differentiate
between the two in that data types are used when specifying schemas (for example,
during a 'create table' call) and values are the data that exists in a given row
within a table.

## Currently Supported Data Types

* BOOL
* TINYINT
* SMALLINT
* INT
* BIGINT
* FLOAT
* DOUBLE
* STRING
* VARCHAR
* CHAR
* DECIMAL
* TIMESTAMP
* BINARY
* REAL
* STRUCT

See the NOTES section at the bottom of this page for more information on types.

## Conversions

Cerebro must convert both values as well as data types in
some situations, based on the storage format and the compute engine being used.
Some platforms do not not support the full range of types that CDAS does.

### Parquet and Spark DataFrames

These are the conversions that occur when working with Parquet data or Spark
DataFrames values.

| Datatype | Parquet type | Spark Data frame type |
| :--- | :--- | :--- |
| boolean | boolean | BooleanType |
| tinyint | int32 | IntegerType |
| smallint | int32 | IntegerType |
| int | int32 | IntegerType |
| bigint | int64 | LongType |
| float | float | FloatType |
| double | double | DoubleType |
| timestamp | int96 | TimestampType |
| string | byte_array | StringType |
| binary | byte_array | NA |
| decimal | fixed_len_byte_array | createDecimalType() |
| binary | byte_array | BinaryType |
| real | double | DoubleType |
| struct | n/a | StructType |

## Notes

* The string and binary data types are stored as a binary blob and not interpreted in any way.
* REAL type is now supported in CDAS. Since, Hive does not support REAL data type, dbcli may be
used to create a field with REAL datatype. DOUBLE type can be used as an alias for REAL.
