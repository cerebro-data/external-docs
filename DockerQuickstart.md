# Docker Quickstart
This document describes how to run Cerebro Data Access Service (CDAS) in quickstart
mode using docker. This will start CDAS in single-node mode which can for example,
be run on a development machine. The purpose is to experiment with the CDAS APIs
and explore the CDAS capabilities.

### Prerequisites
  - Docker version 1.11+
  - If using Docker Machine, it needs to be running.

### Download
Down the cerebro-quickstart.sh script.
```shell
$ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/quickstart/cerebro-quickstart.sh && chmod +x cerebro-quickstart.sh
```

### Starting/stopping the quickstart container
```shell
$ cerebro-quickstart.sh start
```
This should take a few seconds to start the container. This will launch all of the CDAS
services and expose them on their default ports.

To stop the container:
```
$ cerebro-quickstart.sh stop
```

Only one instance of this container can be running at a time, you will need to stop the
container before trying again.

### Ports
The container will use these ports on the docker host. They can be configured by setting
the corresponding environment variable (e.g. export CDAS_WEB_PORT=1234).
  - CDAS_WEB_PORT: WebUI/REST Api port. Defaults to 11050.
  - CDAS_PLANNER_PORT: Planner RPC port. Defaults to 12050.
  - CDAS_WORKER_PORT: Worker RPC port. Defaults to 13040.
  - HMS_PORT: Catalog Hive Metastore Client port. Defaults to 9083.
  - SENTRY_PORT: Catalog Sentry Client port. Defaults to 30911.

### Troubleshooting
***Cannot connect to the Docker daemon. Is the docker daemon running on this host?***
Make sure docker is running. If you have not set up sudo-less docker, you will need
to run this script as root.

***Error getting IP address: Host is not running***
Make sure the docker machine VM is running.

***docker: Error response from daemon: Conflict.***
The container is already running. First run 'cerebro-quickstart.sh stop'.
