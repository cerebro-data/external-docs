# Cerebro Database Cli

The Database CLI provides client access to running Cerebro Database services.

## Prequisites
The CLI requires either python3+ or python2.7+. The CLI is supported on linux and OSX.
Afterwards, install the required packages:

    $ [sudo] pip install -r ./db_requirements.txt
    $ ./dbcli --help

For a fresh setup, you can install with:

    $ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/cli/0.1/db_requirements.txt
    $ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/cli/0.1/dbcli && chmod 755 ./dbcli

    # Install pip if you have not
    $ curl -O https://bootstrap.pypa.io/get-pip.py
    $ [sudo] python get-pip.py

    # Install CLI dependencies
    $ [sudo] pip install -r ./db_requirements.txt

    # Database CLI is ready to use
    $ ./dbcli --help

## Configuration
The service locations and/or default database can be specified to the cli through the
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

dbcli <command> --help

will display the available options and a brief description of each option.

To see your currently saved configuration options, you can enter:

   dbcli show configs

## Database CLI getting started

    dbcli show status

Will return a response in the form:

Server <host:port> is up and accessible.

When the server is up and available. <host:port> reflects the configured server and port.

    dbcli database list

Will list the databases available to you on the configured server.
