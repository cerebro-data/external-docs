# Data Formats

This document describes how Cerebro handles data types and values. We differentiate between the two in that
data types are used when specifying schemas (for example, during a 'create table' call) and values are
the data that exists in a given row within a table.

## Currently supported data types
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

See the NOTES section at the bottom of this page for more information on types.


## Conversions

Cerebro must convert both values as well as data types in
some situations, based on the storage format and the compute engine being used.
Some platforms do not not support the full range of types that CDAS does.

### Parquet and Spark DataFrames

These are the conversions that occur when working with Parquet data or Spark DataFrames values.

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

## NOTES
* The string data type is stored as a binary blob and not interpreted in any way.
* There is currently an issue preventing the BINARY type from being used. The STRING type
can be used and is an alias for BINARY.
* There is currently an issue preventing the REAL type from being used. The FLOAT type
can be used and is an alias for REAL.
