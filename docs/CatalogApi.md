# Catalog REST API

This document describes the REST API of the Cerebro Catalog. This is intended for clients
that want to leverage all of the Cerebro functionality. In addition to this, clients can
connect using existing APIs, such as the Hive Metastore API.

The purpose of this API is to provide programmatic access to interact with the catalog.
For users that want to interactively access the catalog, it is recommended to use the
`dbcli`, which provides a more traditional CLI experience built on top of these
 APIs.

## Authentication

Unless otherwise specified, the APIs require users to be authenticated. In general,
authentication can be done via Kerberos or by tokens. For token based authentication,
simply specify the token in the auth header: `authorization: Bearer <TOKEN>`.

See the [authentication document](Authentication.md)
for details on how to get tokens and check if token authentication is working.

## SSL

If ssl is enabled, all the calls should be made against https, instead of http. They are
otherwise unchanged.

## Executing Hive DDL

Endpoint: `/api/hive-ddl` [POST]

This API allows you to execute HiveQL DDL statements. This can be used to create
datasets, create roles, issues grants, etc. The purpose of this API is to be compatible
with [beeline](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL).

The POST request takes as a parameter:

```
{
    "query" [String]: Required, HiveQL DDL statement.
}
```

Example:

```shell
curl -H "Content-Type: application/json" -X POST -d '{"query":"show databases"}' localhost:5000/api/hive-ddl
```

As is the case with most SQL dialects, user names containing a dash need to be escaped.
This is accomplished by wrapping the username in backticks.

Example:

```shell
curl -H "Content-Type: application/json" -X POST -d '{"query":"create role `user-one`"}' localhost:5000/api/hive-ddl
```


## Listing databases

Endpoint: `/api/databases` [GET]<br/>
Endpoint: `/api/databases` [POST]

The POST request takes as a parameter:

```
{
  "filter" [String]: Optional, filter on the name of databases to return. For example,
      'log*' returns all databases that start with 'log'.
}
```

## Listing datasets

Endpoint: `/api/datasets` [GET]<br>
Endpoint: `/api/datasets` [POST]

The POST request takes as a parameter:

```
{
  "db" [String]: Optional, database to retrieve datasets from. Default is 'default'.
  "filter" [String]: Optional, filter on the name of datasets to return. For example,
      'log*' returns all datasets that start with 'log'.
}
```

## Details of a dataset

Endpoint: `/api/datasets/{name}` [POST]

Returns: Dataset information as json. This includes the schema as well as other
information.

```
{
  'db' [String]: Database containing this dataset.
  'name' [String]: Name of dataset
  'schema' [List]: List of columns
}
```

Example:

```
curl -X POST localhost:5000/api/datasets/cerebro_sample.sample
```

## Scanning a dataset

Endpoint: `/api/scan/{name}` [GET] <br>
Endpoint: `/api/scanpage/{name}` [GET]< <br>
Endpoint: `/api/scan` [POST] <br>
Endpoint: `/api/scanpage` [POST]

Returns dataset as json. The scan API will only return the initial rows. Scanpage returns
a handle that can be used to retrieve all the records.

Example:

```shell
curl localhost:5000/api/scan/cerebro_sample.sample
```

The POST request takes as a parameter:

```
{
  "query" [String]:   "SQL Query to execute"
}
```

Example:

```
curl -X POST -H 'Content-Type: application/json' \
-d '{"query" : "select uid, ccn from cerebro.sample.users"}' localhost:5000/api/scan
```

The `scanpage` API accepts two optional argument `records=`, which is the total number
of records to return for the query, and `session_id`. The API returns records in batches
of up to 10,000.

NOTE: in releases prior to 0.7.0, the records parameter was used to specify the
batch size.

The `session_id` value is used on subsequent queries to return successive batches of
records. It must be omitted on the first query.

NOTE: session ids are only valid for 30-seconds, starting from the time that the CDAS
cluster starts returning data. That timer is reset each time a query is received for
a given session_id.

```shell
$ curl <cdas_rest_server_endpoint>/api/scanpage/products?user=presto
$ curl <cdas_rest_server_endpoint>/api/scanpage/products?user=presto&records=25000
$ curl <cdas_rest_server_endpoint>/api/scanpage/products?user=presto&session_id=77480ad07d743bb1:b4f7822f036c6c91
```

Each returned object contains:

```
{
  'records' [List]: Each entry is an object containing the field names, types and values of each record.
  'session_id' [String]: Key used to return subsequent 'pages'.  Each page contains up to 10,000 entries.  When the final page is returned, 'session_id' is "-1".
}
```
