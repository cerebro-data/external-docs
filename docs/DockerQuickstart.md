# Docker Quickstart

This document describes how to run Cerebro Data Access Service (CDAS) in quickstart
mode using Docker. This will start CDAS in single-node mode which can for example, be
run on a development machine. The purpose is to experiment with the CDAS APIs and explore
the CDAS capabilities.

### Prerequisites

- Docker version 1.11+
- If using Docker Machine, it needs to be running.

### Download

Download the cerebro-quickstart.sh script:

```shell
$ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/quickstart/cerebro-quickstart.sh && chmod +x cerebro-quickstart.sh
```

### Starting/stopping the quickstart container

```shell
$ cerebro-quickstart.sh start
```

This should take a few seconds to start the container (Note: This requires for all Docker
images to be local, otherwise the images will be downloaded on first start, requiring
more time). This will launch all of the CDAS services and expose them on their default
ports.

To stop the container:

```shell
$ cerebro-quickstart.sh stop
```

Only one instance of this container can be running at a time, you will need to stop the
container before trying again.

### Sample Data

The quickstart CDAS will come with a few sample datasets that will already be set up.
The quickstart set up will create the `cerebro_sample` database with a few tables in
it. You should be able to read them through the various CDAS APIs. For example, using
the REST API, you can do:

```shell
$ curl localhost:11050/scan/cerebro_sample.sample
[
    {
        "record": "This is a sample test file."
    },
    {
        "record": "It should consist of two lines."
    }
]
```

### Testing with local data

The quickstart container can be started to share with data from the host file system.
Currently, it is only supported to share a single directory from the host. This can be
specified when starting up the quickstart image.

```shell
$ cerebro-quickstart.sh start -mount <Absolute path on host file system>
```

This directory will be available *inside* the container under `/data`. This directory
is *shared* (and not copied) meaning changes on the host will be instantly reflected
inside the container.

For example, if we had two datasets (dataset1, dataset2) on the local file system in
`/tmp/datasets`, we could make those accessible to the quickstart container like so:

```shell
$ ls /tmp/datasets
dataset1/  dataset2/
$ cerebro-quickstart.sh start -mount /tmp/datasets
# Datasets are accessible within container under /data. For example, these DDL
# commands will create the metadata over the datasets.
# CREATE EXTERNAL TABLE dataset1(...) LOCATION 'file:///data/dataset1'
# CREATE EXTERNAL TABLE dataset2(...) LOCATION 'file:///data/dataset2'
```

Note: There are performance issues with sharing the file system on Mac OS X so this is
not suitable for testing large amounts of data. See the Docker site for more details: https://docs.docker.com/docker-for-mac/osxfs/#/performance-issues-solutions-and-roadmap

### Ports

The container will use these ports on the Docker host. They can be configured by setting
the corresponding environment variable (e.g. export CDAS_WEB_PORT=1234).

- `CDAS_WEB_PORT`: WebUI/REST Api port. Defaults to 11050.
- `CDAS_PLANNER_PORT`: Planner RPC port. Defaults to 12050.
- `CDAS_WORKER_PORT`: Worker RPC port. Defaults to 13040.
- `HMS_PORT`: Catalog Hive Metastore Client port. Defaults to 9083.
- `SENTRY_PORT`: Catalog Sentry Client port. Defaults to 30911.

### Troubleshooting

***Cannot connect to the Docker daemon. Is the Docker daemon running on this host?***

Make sure Docker is running. If you have not set up sudo-less Docker, you will need
to run this script as root.

***Error getting IP address: Host is not running***

Make sure the Docker machine VM is running.

***Docker: Error response from daemon: Conflict.***

The container is already running. First run `cerebro-quickstart.sh stop`.
