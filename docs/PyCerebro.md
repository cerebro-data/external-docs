# PyCerebro

PyCerebro is a native python library for clients interacting with CDAS. It is similar to
the Java libraries and calls the lower level CDAS services: the library for the most
part directly interacts with the planner and worker services. There are alternate ways
interact with CDAS from python, in particular via the REST API but this library will
provide more control and better performance. For simple applications, the REST API may
be sufficient but we recommend the native library for reading larger volumes of data.

Note: this library is currently in preview phase. The APIs are subject to change and
the performance characteristics are not in its final state.

PyCerebro requires 0.8.1+ of the server to be fully functional. If running against an
older server, scans will fail with a message to upgrade the server.

## Dependencies

Required:
* Python 3.4+ with `easy_install` and `pip`.
* linux: gcc (with c++ support)
* `six`, `bit_array`, `thriftpy`

Optional:
* `numpy`, `pandas`

The optional packages are required to use the scan APIs, but not the metadata related
ones.

Note that python2 is not supported and there are no plans to.

## Simple installing from pypi

The python library is available on (pypi)[https://pypi.python.org/pypi/pycerebro/]. This
can be installed using pip. This assumes that python3 is already installed on the system.

```shell
curl "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
sudo python3 get-pip.py
sudo pip3 install pycerebro

# For the optional packages (this can take a while):
sudo pip3 install pandas
```

To confirm the install is successful, try importing it from the interpreter and getting
the version.

```python
import cerebro.cdas
cerebro.cdas.version()
# Should output the version string, for example 0.8.1
```

Note that the pandas install may fail due to its system dependencies. For more information
see the pandas [docs](https://pandas.pydata.org/pandas-docs/stable/install.html) or the
docs below.

## Installing with easy_install

If pypi or pip are not accessible on the network, it is possible to install the client
from Cerebro's release location in S3. After installing the dependencies, download the
library and install it with `easy_install`.

```shell
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.8.1/client/pycerebro.egg
easy_install --user pycerebro.egg
# Or, to install it system wide
[sudo] easy_install pycerebro.egg
```

Below we have some examples on how to install the dependencies in two different Amazon
Web Services (AWS) based environments. Depending on your network restrictions, these
may have to be adapted to use your package managers.

### Fully install on fresh rhel7 machine

This assumes a minimal rhel7 machine, for example the base rhel7 AMI on Amazon AWS.

```shell
# Basic python install and dependencies, this satisfies the requirements.
sudo rpm -ivh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
sudo yum install -y gcc-c++ python34.x86_64 python34-devel
curl "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
sudo python3 get-pip.py
sudo pip3 install six bit_array thriftpy
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.8.1/client/pycerebro.egg
sudo easy_install pycerebro.egg

# Optional packages (installing pandas can take a while)
pip3 install Cython numpy pandas
```

### Setup dependencies on an fresh Amazon Elastic MapReduce (EMR) machine

Note that this machine typically has python2 and python3 installed. PyCerebro is only
supported for python3.

```shell
curl "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
sudo python3 get-pip.py
sudo /usr/local/bin/pip3 install six bit_array thriftpy
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.8.1/client/pycerebro.egg
sudo easy_install-3.4 pycerebro.egg

# Optional packages (installing pandas can take a while)
sudo /usr/local/bin/pip3 install numpy pandas
```

## Overview

In a typical application, the user will first create a context object. The context object
represents state that is shared CDAS connections (and therefore also requests), such as
user credentials. From the context object, the user can create connection objects which
can then be used to execute DDL and scan requests against CDAS.

On top of these objects, the library provides utilities to read an entire dataset into
pandas, for further data manipulation.

As a simple end to end example that reads the first dataset from the `cerebro_sample`
database:

```python
from cerebro import context
ctx = context()
with ctx.connect(host='localhost', port=12050) as conn:
    dataset = conn.list_dataset_names('cerebro_sample')[0]
    pd = conn.scan_as_pandas(dataset)
    print(pd)
```

## API docs

Documentation for the API is hosted
[here](https://cerebro-data.github.io/external-docs/index.html). This includes
documentation for each of the APIs in detail.

## Authentication

PyCerebro supports authentication using kerberos or tokens. It also supports connecting
to unauthenticated servers, which should only be used in development. Authentication
information is stored in the `context` object.

### No configuration specified

When the context objects is created, it will either either automatically configure token
based authentication or no authentication. If a token for the current user is available
(typically in `~/.cerebro/token`), then token auth will be configured. To check,

```python
from cerebro import context
ctx = context()
ctx.get_auth() # Will be None or 'TOKEN'

# To disable authentication:
ctx.disable_auth()
```

### Enabling token auth

To set the user token, call `enable_token_auth()` specifying either the `token_str`
or `token_file` argument. `token_str` should be the token text. `token_file` is the
path to a file which contains the token.

```python
from cerebro import context
ctx = context()
ctx.enable_token_auth(token_str='super-secret-token-string')
# OR
ctx.enable_token_auth(token_file='/path/to/super/secret')
```

### Enabling kerberos

To enable kerberos, call `enable_kerberos()`, specifying the service principal name.
This assumes the user has already run `kinit` locally. The caller must specify the
service name (i.e. first part of principal) and can optionally specify the hostname.

For example, to connect to a server with principal `cerebro/service@CEREBRO.COM`:

```python
from cerebro import context
ctx = context()
ctx.enable_kerberos('cerebro', host_override='service')
```

## Metadata APIs

**connect()**

Creates a connection to CDAS. Callers should call `close()` when done or use
the `with` scoped cleanup. To create a connection, call `connect()` on the context
object. It takes as arguments

* host: host name (of the planner)
* port: port (of the planner)

**get_databases()**

This function takes no arguments and returns all the databases the user has access to.

**get_dataset_names(db)**

This function returns the names of all the datasets in a database. A database name
must be specified.

As an example to collect the names of all the datasets in the catalog:

```python
from cerebro import context
ctx = context()
# Configure auth if necessary
with ctx.connect(host='localhost', port=12050) as conn:
    all_datasets = []
    for db in conn.list_databases():
        all_datasets.append(conn.list_dataset_names(db))
    print(all_datasets)
```

**plan()**

This is a low level API to plan a scan request. This function takes:

* request, str: Fully qualified dataset name or SQL statement to scan. Note that if SQL
is specified, it is subject to the same SQL restrictions that CDAS supports.

The result of this API is typically sent to the worker and contains an internally
serialized binary payload. The result does contain some low level information that
may be useful:

```python
from cerebro import context
ctx = context()
with ctx.connect(host='localhost', port=12050) as conn:
    result = conn.plan('cerebro_sample.users')
    print(result.warnings)          # Warnings that were generated while planning
    print(len(result.tasks))        # Total number of worker tasks that will need to run
    print(len(result.schema.cols))  # Number of columns in result schema

# it's also possible to pass a supported SQL request
with ctx.connect(host='localhost', port=12050) as conn:
    result = conn.plan('select mask_ccn(ccn) from cerebro_sample.users')
    assert len(result.schema.cols) == 1
```

**execute_ddl(sql)**

This API takes a single string argument that is the SQL string. The supported SQL
is the same as any direct CDAS API call. The result set is a table, returned as a list
of list of strings (row major).

As an example to list the roles and format the output using prettytable
(`pip3 install prettytable`):

```python
from cerebro import context
from prettytable import PrettyTable
ctx = context()
with ctx.connect(host='localhost', port=12050) as conn:
    result = conn.execute_ddl('show roles')
    t = PrettyTable()
    for row in result:
        t.add_row(row)
    print(t)
```

## Scanning data

PyCerebro supports two scan apis, `scan_as_pandas()` and `scan_as_json()`. They behave
identically except in the result structure. `scan_as_pandas()` returns the result as
a pandas DataFrame. `scan_as_json()` returns the result as a list of json objects.

We recommend users that need to do further processing to use the `scan_as_pandas()`
API as it can be much faster.

Both APIs take as arguments:

* request, str: Fully qualified dataset name or SQL statement to scan.
* max_records, int, optional: Maximum number of records to return. Default is unlimited.

For example, to scan the `cerebro_sample.sample` dataset as json:

```python
from cerebro import context
ctx = context()
with ctx.connect(host='localhost', port=12050) as conn:
    results = conn.scan_as_json('cerebro_sample.sample')
    print(results)
```

To return the first 10000 records as a pandas DataFrame from the user's ccn number:

```python
from cerebro import context
ctx = context()
with ctx.connect(host='localhost', port=12050) as conn:
    df = conn.scan_as_pandas('SELECT ccn from cerebro_sample.users', max_records=10000)
    df.describe()
```
