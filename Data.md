# Data Types
This document describes how Cerebro handles data types.

## Conversions
Cerebro must convert data  representations in some situations.

### Parquet
These are the  conversions that occur when working with Parquet data

| Datatype | Parquet type |
| :--- | :--- |
| null | boolean |
| boolean | boolean|
| tinyint | int32 |
| smallint | int32 |
| int | int32 |
| bigint | int64 |
| float | float |
| double | double |
| timestamp | int96 |
| string | byte_array |
| date | byte_array |
| datetime | byte_array |
| binary | byte_array |
| decimal | fixed_len_byte_array |

