# Catalog REST API tutorial

This tutorial demonstrates how to use the catalog admin REST API. It does
so using curl. It likely makes sense to use a higher level REST API for
more complex use cases.

In this tutorial we will

1. Register a new dataset from an existing path.
2. Add policies which specify cell (column and record) level permissions
3. Revoke granted policies.
4. Delete the dataset.

In the tutorial, we assume the server does not have authentication enabled.
This is obviously not sure. At the end, we show how the requests would change
with authentication enabled.

## Verify the server is up and running

There are multiple ways to start up the Cerebro clusters. You will either need
to start a STANDALONE_CLUSTER or a CATALOG_CLUSTER with at least one DATA_ACCESS_CLUSTER.
The steps in this tutorial can work with any of those configurations. We simply need
the endpoint for the REST server: `cdas_rest_server:api`.

To verify both are running, curl both endpoints

```shell
curl <cdas_rest_server>/api/health
```

This should return a json object indicating the health is ok.

## Registering a dataset

For the tutorial, we will use an dataset that Cerebro has publicly available
in AWS S3. Feel free to adapt the dataset to one you have available.

```shell
curl -H "Content-Type: application/json" -X PUT -d
'{"name":"products", "database":"tutorial", "storage_url":"s3://cerebrodata/products"}' <cdas_rest_server>/api/datasets
```

This registers the dataset in the 'tutorials' database called products. It is
backed by data stored in S3. By default, only the owning user has access.

In this particular case, the dataset is self describing so it is ready to be
read. In other cases, the PUT request can specify the HiveQL that is necessary
to create the dataset. This allows the creating to specify the schema, file
format, delimiters, etc.

## Reading it

Cerebro provides many ways to access the data. We'll start with the simplest
of using the REST endpoint. We've registered the dataset with the fully qualified
name 'tutorial.products'. We can simply issue a GET call to the planner endpoint.

```shell
curl <cdas_rest_server>/api/scan/tutorial.products
```

This should return the first few records of this dataset.
