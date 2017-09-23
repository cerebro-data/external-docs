# Data Types
This document describes how Cerebro handles data types.

## Conversions
Cerebro must convert data  representations in some situations.

### Parquet and Spark DataFrames
These are the  conversions that occur when working with Parquet data or
Spark DataFrames

| Datatype | Parquet type | Spark Data frame type |
| :--- | :--- | :--- |
| null | boolean | NA |
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
