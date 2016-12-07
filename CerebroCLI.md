# Cerebro Cli

The CLI provides client access to running Cerebro services.

## Prequisites
The CLI requires either python3+ or python2.7+. The CLI is supported on linux and OSX.
Afterwards, install the required packages:

    $ [sudo] pip install -r ./requirements.txt
    $ ./cerebro_cli --help

For a fresh setup, you can install with:

    $ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/cli/0.1/requirements.txt
    $ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/cli/0.1/cerebro_cli && chmod 755 ./cerebro_cli

    # Install pip if you have not
    $ curl -O https://bootstrap.pypa.io/get-pip.py
    $ [sudo] python get-pip.py

    # Install cli dependencies
    $ [sudo] pip install -r ./requirements.txt

    # CLI is ready to use
    $ ./cerebro_cli --help

## Configuration
The service locations can be specified to the cli as a command line flag (run --help) 
or can be configured from a configuration file which is helpful if interacting with 
the same server repeatedly. The configuration file is stored in ~/.cerebro/configs.json.

To configure the location of the catalog service, the file should contain:
```
{
  "catalog_hostport": "<host:port of catalog admin>"
}
```

## Getting started
To get started, run:

    cerebro_cli help

This will help you login to the server. Afterwords you can run:

    cerebro_cli commands

To see the available commands. To get more details:

    cerebro_cli <command> help

## Catalog getting started
The catalog commands can be seen with:

    cerebro_cli catalog help

To ensure the CLI is able to reach the server, you can run:

    cerebro_cli catalog status

This should return 'ok'.
