# UDFs

Cerebro Data Access Service (CDAS) supports running user defined functions (UDFs). UDFs
provide a powerful way to extend the capabilities of CDAS. Example use cases include:

- A custom anonymization or data encryption algorithm
- Complicated filters that are more naturally expressed in code vs SQL

## Security

UDFs run in the same process with the same permissions as the rest of CDAS, running as
the system user. In particular UDFs have access to all the data that CDAS has and a
malicious UDF can potentially access data that is currently being processed. We assume the
UDF is *trusted* and currently the only measure to protect against ill-behaving UDFs is
to restrict who can register them.

Only users who are catalog admins are allowed to register and unregister UDFs. It is
not possible to delegate this capability to other users (i.e. in the same way the
permission to grant can be delegated).

As a best practice, we also recommend that the location of the UDF binary be secured
to prevent another user from replacing it with a malicious binary (i.e. most users
should not have write access to that location.)

## Overview

To use UDFs, the steps are:

- Register the UDF in the CDAS catalog. Only the catalog admins can do this.
- Create views (or issue queries) that use the UDF with typical SQL statements. Any user
  can use the UDF.
- Read data from the view. The UDF will be evaluated by CDAS before the data is returned.

As an example for this document, we will use a UDF that accepts strings and masks all
characters in it. For example `mask('hello')-> 'xxxxx'`.

## Hive UDFs

CDAS supports UDFs written against the Hive
[interface](https://github.com/apache/hive/blob/master/ql/src/java/org/apache/hadoop/hive/ql/exec/UDF.java).
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

## Registering the UDF

To register the UDF, use one of our client tools (e.g. dbcli) which allow executing DDL
statements against CDAS, using the CREATE FUNCTION statement.

```sql
CREATE FUNCTION [IF NOT EXISTS] [db_name.]function_name([arg_type[, arg_type...])
  RETURNS return_type
  LOCATION 's3_path'
  SYMBOL='class name of UDF'
```

Note that function overloading is supported. Functions can have the same name with
different signatures.

For example, with the MaskUDF it would be:

```sql
CREATE FUNCTION cerebro_sample.mask(STRING)
  RETURNS STRING
  LOCATION 's3://cerebrodata-public-east/udfs/mask-udf.jar'
  SYMBOL='com.cerebro.hiveudf.MaskUDF'
```

To drop the UDF, use the DROP FUNCTION statement:

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

In this case the UDF is completely hidden from the compute tool talking to CDAS. The
tool does not know (and therefore have access to the UDF binary in anyway).

## Errors

**User is not admin**

In this case, the user is not a catalog admin. He/she will see:

AuthorizationException: User 'dev-user' does not have privileges to CREATE/DROP functions.

**Dropping a function that is being used**

In this case, the views that depend on it will fail. The error will be:

AnalysisException: cerebro_sample.mask() unknown.

## Limitations

**Cannot grant on UDF**

We currently do not provide access controls on the UDF. Only admins can create them, and
then all users can use them. It is not possible to grant usage of a UDF to a particular
user/group.

**Cannot issue CREATE/DROP FUNCTION through hive CLI/beeline**

The hive client integration currently does not support this DDL statement. This is
expected to be added in the very near future.
