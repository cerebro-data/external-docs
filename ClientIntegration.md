# Cerebro Data Access Service Integration
This documents describes how to use Cerero Data Access Service (CDAS) from a users
perspective. It describes how Cerebro can be used from various existing tools to:
  - Explore and create new data sets and views.
  - Define fine-grained access policies over datasets.
  - Read datasets.

Note that currently CDAS does not support data writes but many use cases can be
covered by writing with the existing tools and then create a dataset in CDAS.

This document will through various tools we are integrated with. It might not
make sense to do all of the above with each tool.

### HiveServer2/Beeline
HiveServer2 provides a service to run SQL. Typically clients connect to it through
beeline, which gives a traditional SQL client shell. HS2/Beeline is suitable to
do all tasks above. The original hive shell ('hive') is not supported.

From the users point of view, they simply connect to HS2 as always. HS2 in fact
runs unmodified and clients talk to the same HS2 without directly interacting
with Cerebro (HS2 is configured and integrated with Cerebro). Authentication
works exactly as always.

Here is an example that registers and external dataset, creates a role, creates
a view of the data and grants access to that role. In all of these steps, there
should be no difference between how these commands work with or without Cerebro.

##### Setting up the roles quick start
These are quick start steps to set up the admin role which has full access to
the server. The user running these commands needs to have admin to the Cerebro
Sentry Server.

```
beeline> !connect jdbc:hive2://<host:port of hs2>/default;principal=<hs2_principal>
beeline> CREATE ROLE admin_role;
beeline> GRANT ALL ON SERVER server1 TO ROLE admin_role;
beeline> GRANT ROLE admin_role TO GROUP <YOUR ADMIN USER/GROUP>;
```

**Note**: These steps assume a few things about your set up that are no different than 
typical HS2 requirements. The admin user or group that is granted must exist on the unix
system in both cerebro and hs2.

##### Creating a dataset
In the next step, we will create an external dataset for data in S3.
```
beeline> create external table sample (s STRING) LOCATION 's3n://cerebrodata/sample'
beeline> show tables;
beeline> select * from sample;
```

At this point we have added a dataset that is accessible to all Cerebro integrated
clients. By default, only users in the admin_role have access to this dataset and
selecting as other users should fail.

**Note:** These steps also assumes that the beeline client has access to this location. 
This, for example,  involves IAM roles or AWS access keys to be set up if the data is in 
S3.

**Note:** Creating non-external is currently considered undefined behavior and should
not be done.

##### Creating a view and granting access to another role
Finally, we will create a view and grant access to the view to a different set of users.
In this case we will create a view that only returns rows which contain 'test'.
```
beeline> CREATE ROLE test_role;
beeline> GRANT ROLE test_role TO GROUP <YOUR TEST USER/GROUP>;

beeline> CREATE VIEW sample_view as SELECT * FROM sample WHERE s LIKE '%test%';
beeline> SHOW TABLES;
beeline> SELECT * FROM sample_view;

beeline> GRANT SELECT ON TABLE sample_view TO ROLE test_role;
```

At this point the admin group should see the full dataset and the test group
should only see a subset of the records.

The remaining GRANT/REVOKE/DROP are supported and work identically to HS2.

**Note**: Updating permissions can take a few minutes to be reflected everywhere
in the system as policies are cached.

##### REST Scan API
CDAS exposes a REST API that returns data as JSON. This API is only intended to
read data, not to register datasets or update their policies.

**NOTE**: This API is currently (intentially) unauthenticated meaning any user can
pretend to be any other user. This is very temporary for now to facilicate testing
without having to authenticate as other users. If enabled, the API usage is identical
and requires the client be autheticated with kerberos/ldap.

The REST API simply exposes a HTTP endpoint. This endpoint is referenced in other
documents as the planner webui endpoint. To read data, you can simply reach:
```
hostport/scan/<dataset name>
# You can optionally specify how many records with:
<hostport>/scan/<dataset>?records=N

# You can also specify who to run the request as
<hostport>/scan/<dataset>?user=<user>

# Or both, by adding & between the arguments
<hostport>/scan/<dataset>?user=<user>&records=N
```

Continuing the above example with data registered via HiveServer2, we should see:
```
# Read the entire dataset
curl <hostport>/scan/sample

# Read it as the test user, this should fail.
curl <hostport>/scan/sample?user=<test user>

# Read the view as the test user, this should work.
curl <hostport>/scan/sample_view?user=<test user>
```

##### Python Pandas Integration
Reading the data into a panda data frame is very simple with the REST API. 
```
import pandas as pd
df = pd.read_json('http://<hostport>/scan/<dataset>')
```

