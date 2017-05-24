# Cerebro Database Cli

The Database CLI provides client access to running Cerebro Database services.

## Installation
For a fresh setup, you can install with:

    # Linux
    curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.4.0/cli/linux/dbcli && chmod 755 ./dbcli

    # OSX
    curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.4.0/cli/darwin/dbcli && chmod 755 ./dbcli

    # Verify the download executes
    ./dbcli --help

## Configuration
The server location and/or default database can be specified to the cli through the
'database use' command or can be configured from a configuration file which is helpful
if interacting with the same server and/or database repeatedly. The configuration file
is stored in ~/.cerebro/configs.json.

To configure the location of the catalog service, the file should contain:
```
{
  "catalog_hostport": "<host:port of catalog admin>"
  "database": "<name of the database>"
}
```

## Getting started
To get started, run:

    dbcli --help

Which will display the list of available commands.  To use most commands, you
will need a login token.  Use:

dbcli get-token --help

for the available options.

For all commands entering:

dbcli *command* --help

will display the available options and a brief description of each option.

To see your currently saved configuration options, you can enter:

   dbcli show configs

## Database Related Commands 

    dbcli show status

Will return a response in the form:

Server <host:port> is up and accessible.

When the server is up and available. <host:port> reflects the configured server and port.

    dbcli database list

Will list the databases available to you on the configured server.

    dbcli dataset list

Will list the datasets available to you in the selected database.

