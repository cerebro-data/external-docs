# Cerebro Install
This document describes the Cerebro install process. The result should be a fully
operational Cerebro cluster consisting of the DeploymentManager and a running
Cerebro Data Access Service (CDAS) cluster.

## AWS prerequisites
Cerebro depends on a few AWS services to be set up before installing Cerebro.
  - S3 Bucket. Cerebro uses this bucket to store log files as well as stage intermediate
    config files. This bucket should be readable and writable by all instances running any
    Cerebro components. This bucket will be referred to as CEREBRO_S3_BUCKET.
  - RDS instance with MySQL5.6 provisioned. This can be provisioned with the configuration
    of your choice. Cerebro instances need read and write access to this database.
    This document will refer to this as CEREBRO_RDS_DB.
  - IAM credentials. Cerebro instances need read and write access to the above two. The
    DeploymentManager needs the ability to provision instances and the cluster machines
    will need credentials to your data. These can be one IAM profile with all the
    credentials or two sepearate role. We will refer to them as IAM_MANAGER and
    IAM_CLUSTER.

## Installing the DeploymentManager
The DeploymentManager runs on a machine separate from the machines in the cluster. This
does not need to be its own machine.

### Machine and package prerequisites
DeploymentManager machine:
  - Needs to run linux
  - Have Java 7+ installed (OpenJDK or Sun JVM).
  - Machine needs to have IAM_MANAGER credentials.

Cluster machine:
  - rhel 7: Cerebro will try to install java if not present. If your environment
    uses non-standard package management, see additional steps below.
  - Machine needs to have IAM_CLUSTER credentials.

In addition, you will need to install the Cerebro CLI. The machine running the CLI
needs to have network access to the DeploymentManager and cluster machines. The CLI
can be installed on the DeploymentManager or a local development machine. To run the CLI
you need:
  - rhel6+ or ubuntu 14.04+
  - OS X Darwin 10.11+ (OS X El Capitan+)

### Getting the bits
Download and extract the DeploymentManager tarball
```shell
# Recommended location is /opt/cerebro but can be any location.
$ mkdir -p /opt/cerebro && cd /opt/cerebro

# Get the tarball from S3.
$ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/deployment-manager-0.2.0.tar.gz

# Extract the bits.
$ tar xzf deployment-manager-0.2.0.tar.gz && rm deployment-manager-0.2.0.tar.gz && ln -s deployment-manager-0.2.0 deployment-manager
```

Download the shell binary. This depends on the OS running the CLI.
```shell
# Linux
$ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/cli/linux/cerebro_cli && chmod +x cerebro_cli
# OSX
$ curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/cli/darwin/cerebro_cli && chmod +x cerebro_cli

# Verify the version
$ ./cerebro_cli version
```

### DeploymentManager logging and install directory
Configure the logging and local install directories. These should be paths on the
local file system. The install directory currently needs to be restored if this machine
is moved. These by default are '/var/log/cerebro' and '/var/run/cerebro' but can be
changed by setting DEPLOYMENT_MANAGER_LOGDIR and DEPLOYMENT_MANAGER_INSTALL_DIR in
the environment.

For a standard install:
```shell
[sudo] mkdir -p /var/log/cerebro
[sudo] mkdir -p /var/run/cerebro
[sudo] chmod 700 /var/run/cerebro

# DeploymentManager user needs exclusive access to those directories. If those
# directories are created as different user than the DeploymentManager user, run:
[sudo] chown <user running deployment manager> /var/log/cerebro
[sudo] chown <user running deployment manager> /var/run/cerebro
```

### Cerebro Configuration
DeploymentManager needs to be configured before it can run. These configurations are
done via environment variables before starting up the server. It is recommended you
copy the template configuration, update it and then source it. Steps below assume
the standard install paths were used.
```shell
$ cp /opt/cerebro/deployment-manager/conf/env-template.sh /var/run/cerebro/env.sh
# open and edit env.sh, modifying it as necessary
$ source /var/run/cerebro/env.sh
```

**CEREBRO_DEFAULT_REGION**
This is the region that Cerebro is running in.
```shell
Example:
export CEREBRO_DEFAULT_REGION=us-west-2
```

**CEREBRO_S3_STAGING_DIR**
This is the CEREBRO_S3_BUCKET for logs and install files.
```shell
Example:
export CEREBRO_S3_STAGING_DIR=s3://cerebro-data
```

**CEREBRO_DB_URL/CEREBRO_DB_USERNAME/CEREBRO_DB_PASSWORD**
This is the end point and db credentials for CEREBRO_RDS_DB. DB_URL should be the
host/port (typically 3306) of the running mysql instance.
```shell
Example:
export CEREBRO_DB_URL=cerebro.xyzzzz.rds.amazonaws.com:3306
```

**CEREBRO_DB_NAME**
This is the DB name inside the CEREBRO_RDS_DB that the DeploymentManager will use.
If this RDS instance is only backing a single DeploymentManager install, this does
not need to be set. Otherwise, each install can have a different database. This does
not need to be pre-created.
```shell
Example:
export CEREBRO_DB_NAME=cerebro
```

**CEREBRO_SERVER_HOSTPORT**
This is the host:port to run the DeploymentManager. By default it listens to all
interfaces and runs on port 8085 (e.g. 0.0.0.0:8085). This port does not need to
be accessible to the typical user to access data but is required the administer Cerebro
clusters.
```shell
Example:
export CEREBRO_SERVER_HOSTPORT=0.0.0.0:8085
```

**CEREBRO_PORT_CONFIGURATION**
This configuration allows specifying the ports to run the cluster services on. These
ports do need to be available for all the users connecting to Cerebro for metadata
and data. This is a comma-separated list of service:portname:port number. Below are
the ports that are required for clients. If they are not specified, Cerebro will
pick randomly available ports to expose these services on.
  - cerebro_planner_worker:worker
  - cerebro_catalog:sentry
  - cerebro_catalog_ui:webui
  - cerebro_planner_worker:planner
  - cerebro_planner_worker:webui
  - cerebro_catalog:hms

An example configuration would be:
```shell
CEREBRO_PORT_CONFIGURATION="cerebro_planner_worker:worker:7185,cerebro_catalog:sentry:7182"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_catalog_ui:webui:7184"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner_worker:planner:7183"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner_worker:webui:7181"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_catalog:hms:7180"
export CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,kubernetes_dashboard:admin_ui:7350"
```

**KERBEROS**
To enable Kerberos, Cerebro needs a keytab and principal. They keytab needs to be uploaded
to S3 at CEREBRO_S3_BUCKET/etc/KEYTAB_FILE_NAME. Then the configurations
CEREBRO_KERBEROS_PRINCIPAL and CEREBRO_KERBEROS_KEYTAB_FILE should be set to principal
and KEYTAB_FILE_NAME
```shell
export CEREBRO_KERBEROS_PRINCIPAL=<principal>
# Note: not the full path in S3, just the base name.
export CEREBRO_KEYTAB_FILE=KEYTAB_FILE_NAME
```

## Starting the DeploymentManager
After setting those environment variables and sourcing them, simply run:
```shell
/opt/cerebro/deployment-manager/bin/deployment-manager
# This should output 'System up and running' and the server hostport.
```

This step is not expected to take more than 30 seconds and usually indicates a
configuration issue if the DeploymentManager does not come up in time. The
DeploymentManager runs as a daemon and the logs can be viewed with:
```shell
less /var/log/cerebro/deployment-manager.log
```
If there are configuration issues, they should be available at the end of the log.

## Configuring the CLI
With the DeploymentManager running, we can configure the CLI to connect with it. Run
```shell
$ <path to>/cerebro_cli configure --server <host:port of DeploymentManager>
$ <path to>/cerebro_cli status
# Should return 'Service available. Status: Running'
```

## Starting up a CDAS cluster.
With the DeploymentManager started, the next step is to provision the EC2 instances
which will run CDAS.

These steps need to be done for each CDAS cluster you want to deploy. The overall steps
are:
1. Provision the EC2 machines as you typically would, using the Cerebro provided script
   as the start up (e.g. user-data) script. The instance should be spun up in the VPC,
   subnet with your required security groups and tagged as required.
2. Create the CDAS services to run on those machines, by supplying the machines IP
   addresses to the CLI. Cerebro currently requires CDAS clusters to have at least 2
   machines.

### Provisioning the machines
The instances should be provisioned specifying
/opt/cerebro/deployment-manager/bin/install-dm.sh as the --user-data section. This script
will bootstrap the initial Cerebro components on the newly provisioned machine. An example
of the EC2 launch command is:
```shell
aws ec2 run-instances \
  --image-id <AMI> \
  --instance-type <INSTANCE_TYPE>
  ...
  --user-data file:///opt/cerebro/deployment-manager/bin/install-dm.sh
```

The AMI needs to be running rhel7 and must have the IAM_CLUSTER role. This is also a
good place to set up anything else the machine requires (i.e. Centrify) as typical of
your cluster machines.

**Note: the only external package install-dm.sh requires is installing java. It does so by
simply running 'yum install java' which will install openjdk-8 by default rhel7 AMI's.
This is perfectly compatible with Cerebro. However, if your organization requires custom
repo locations, you will need to update this boostrap script.**

The new instance takes a few minutes to set up Cerebro. You can check the status of
the instance using the cerebro_cli waiting it to either return INSTALLING or
WAITING_TO_START.
```shell
$ ./cerebro_cli agent <HOST:PORT of instance> state
```
Instances can be started in parallel waiting for all them to reach either of these
states. When all the instances have, you can continue.

### Initializing the cluster from the machines
At this point the CDAS cluster machines are initialized and ready to run the Cerebro
services. We will need the IPs of all the machines created from the previous step. This
list can either be supplied via the commandline (comma separated) or from a local file
(each IP on a separate line).
```
# Start all the Cerebro services
$ cerebro_cli clusters create --name=<Name> --type=STANDALONE_CLUSTER --machinesList=<machines or absoluate path to file with machine list>
$ cerebro_cli clusters list

# This should so the cluster as 'Provisioning'. You can see more details, including
# how long it has been in this state using the cerebro_cli. This assumes the cluster
# as id '1'.
# If not, get the id from 'clusters list'.
$ cerebro_cli clusters status --detail 1

# It might be convenient to run this with 'watch' until the state transitions to
# 'READY'.
# This step will take a couple of minutes as all the services get started.
$ watch cerebro_cli clusters status 1

# At this point all of Cerebro is up and running. To see the externally configured
# end points, run
$ cerebro_cli clusters endpoints 1
```
