# Cerebro Database Command-Line Interface

The Database Command-line Interface (CLI) provides client access to running Cerebro
Database services.

## Installation

For a fresh setup, you can install with:

```shell
# Linux
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.7.1/cli/linux/dbcli && chmod 755 ./dbcli

# OSX
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.7.1/cli/darwin/dbcli && chmod 755 ./dbcli

# Verify the download executes
./dbcli --help
```

## Configuration

The server location and/or default database can be specified to the CLI through the
`database use` command or can be configured from a configuration file which is helpful
if interacting with the same server and/or database repeatedly. The configuration file
is stored in `~/.cerebro/configs.json`.

To configure the location of the catalog service, the file should contain:

```shell
{
  "cdas_rest_server": "<host:port of catalog admin>"
  "database": "<name of the database>"
}
```

## Quick Start

Below are the set of commands to get started with the DBCli and demonstrate some of
Cerebro's capabilities. In this tutorial, authentication is done using tokens that
have already been created.

```shell
# Configure DBCLI as admin user, verify it can see the sample tables
./dbcli database --cdas_rest_server <host:port of catalog admin> use cerebro_sample
./dbcli set-token <TOKEN>
./dbcli set-ssl enable (only required if SSL is enabled in CDAS)
./dbcli show configs
./dbcli database list
./dbcli dataset cat sample

# We, by default, create a single role, the admin_role, which has system wide access. As
# the admin, you can create and grant roles. We'll create a role, "test_role" and grant
# that to users in the "test" group.
./dbcli dataset hive-ddl "show tables in cerebro_sample"
./dbcli dataset hive-ddl "create role test_role"
./dbcli dataset hive-ddl "show roles"
./dbcli dataset hive-ddl "grant role test_role to group test"

# Next, we will give SELECT (aka read) access to the sample table to this new role"
./dbcli dataset hive-ddl "grant select on table cerebro_sample.sample to role test_role"

# Switch to a user that only has the test role, they should now be able to read from
# the sample table. You'll notice they don't have access to the other tables and dbs.
<switch to user with group 'test'>
# Run
./dbcli set-token <TOKEN>
./dbcli dataset list
./dbcli dataset cat sample
# Trying to read from the users table will fail with an authorization error as this role
# has only been granted one table.
./dbcli dataset cat users

# To register new databases/datasets, use the database hive-ddl command. This
# is HiveQL compatible. For example:
./dbcli dataset hive-ddl "create database test_db"
./dbcli dataset hive-ddl "create external table test_db.new_table(...) LOCATION 's3://...'"
```

## Getting started

To get started, run:

```
dbcli --help
```

Which will display the list of available commands. To use most commands, you will need
a login token. Use:

```
dbcli get-token --help
```

for the available options.

For all commands entering

```
dbcli *command* --help
```

will display the available options and a brief description of each option.

To see your currently saved configuration options, you can enter:

```
dbcli show configs
```

## Database Related Commands

```
dbcli show status
```

Will return a response in the form:

```
Server <host:port> is up and accessible.
```

When the server is up and available. `<host:port>` reflects the configured server
and port.

```
dbcli database list
```

Will list the databases available to you on the configured server.

```
dbcli dataset list
```

Will list the datasets available to you in the selected database.
