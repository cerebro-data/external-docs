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

### Verify the server is up and running
There are multiple ways to start up the Cerebro clusters. You will either need
to start a STANDALONE_CLUSTER or a CATALOG_CLUSTER with at least one DATA_ACCESS_CLUSTER.
The steps in this tutorial can work with any of those configurations. We simply need 
the endpoints for the different components to be available. We need:
  1. cerebro_catalog_ui:webui
  2. cerebro_planner_worker:webui or cerebro_planner:webui

The remaining steps will use <catalog_endpoint> for 1. and <planner_endpoint> for 2.

To verify both are running, curl both endpoints
```
curl <catalog_endpoint>/api/health
curl <planner_endpoint>/api/health
```
This should return a json object indicating the health is ok.

### Registering a dataset. 
For the tutorial, we will use an dataset that Cerebro has publicly available 
in AWS S3. Feel free to adapt the dataset to one you have available.

Again, we do not recommend directly using curl to communicate with the server, this
is just for illustrative purposes.

```
$ curl -H "Content-Type: application/json" -X PUT -d 
'{"name":"products", "storage_url":"s3://cerebrodata/products"}' localhost:5000/api/datasets
```
This registers the dataset called 'products' back by data stored in S3. By default,
only the owning user hash access.

In this particular case, the dataset is self describing so it is ready to be
read. In other cases, the PUT request can specify the HiveQL that is necessary
to create the dataset. This allows the creating to specify the schema, file
format, delimiters, etc.

### Reading it
Cerebro provides many ways to access the data. We'll start with the simplest
of using the REST endpoint. We've registed the dataset with the name 'products'. We can simply 
issue a GET call to the planner endpoint.

```
$ curl <planner_endpoint>/scan/products
```
This should return the first few records of this dataset.

### Defining access policies.
We'll add 3 policies for 3 different users. The users must already exist on the system. We will
  - Give the Hadoop just access to the category column.
  - Give the presto user access to just the url column.
  - Give the zeppelin user access to the category column after a transformation is applied AND rows are filtered.
 
```
$ curl -H "Content-Type: application/json" -X PUT -d '{"users":"hadoop","dataset":"products","projection":"category"}' localhost:5000/api/policies
$ curl -H "Content-Type: application/json" -X PUT -d '{"users":"presto","dataset":"products","projection":"url"}' localhost:5000/api/policies
$ curl -H "Content-Type: application/json" -X PUT -d '{"users":"zeppelin","dataset":"products","projection":"upper(category)", "filters":"category < \"m\""}' localhost:5000/api/policies
```

### Reading it.
Again, we can read it with the curl api. Each of these commands should return
different data with the above policies enforced.

```
$ curl <planner_endpoint>/scan/products?user=hadoop
$ curl <planner_endpoint>/scan/products?user=presto
$ curl <planner_endpoint>/scan/products?user=zeppelin
```
