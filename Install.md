# Cerebro Install
This document describes the Cerebro install process. The result should be a fully
operational Cerebro cluster consisting of the DeploymentManager and a running
Cerebro Data Access Service (CDAS) cluster.

## AWS prerequisites
Cerebro depends on a few AWS services to be set up before installing Cerebro.
  - S3 Bucket. Cerebro uses this bucket to store log files as well as stage intermediate
    config files. This bucket should be readable and writable by all instances running any
    Cerebro components. This bucket will be referred to as CEREBRO_S3_STAGING_DIR.
  - RDS instance with MySQL5.6 provisioned. This can be provisioned with the configuration
    of your choice. Cerebro instances need read and write access to this database.
    This document will refer to this as CEREBRO_DB_URL.
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
mkdir -p /opt/cerebro && cd /opt/cerebro

# Get the tarball from S3.
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/deployment-manager-0.2.0.tar.gz

# Extract the bits.
tar xzf deployment-manager-0.2.0.tar.gz && rm deployment-manager-0.2.0.tar.gz && ln -s deployment-manager-0.2.0 deployment-manager
```

Download the shell binary. This depends on the OS running the CLI.
```shell
# Linux
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/cli/linux/cerebro_cli && chmod +x cerebro_cli
# OSX
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.2.0/cli/darwin/cerebro_cli && chmod +x cerebro_cli

# Verify the version
./cerebro_cli version
```

### DeploymentManager logging and install directory
Configure the logging and local install directories. These should be paths on the
local file system. The install directory currently needs to be restored if this machine
is moved. These by default are '/var/log/cerebro' and '/etc/cerebro' but can be
changed by setting DEPLOYMENT_MANAGER_LOGDIR and DEPLOYMENT_MANAGER_INSTALL_DIR in
the environment.

For a standard install:
```shell
[sudo] mkdir -p /var/log/cerebro
[sudo] mkdir -p /etc/cerebro
[sudo] chmod 700 /etc/cerebro

# DeploymentManager user needs exclusive access to those directories. If those
# directories are created as different user than the DeploymentManager user, run:
[sudo] chown <user running deployment manager> /var/log/cerebro
[sudo] chown <user running deployment manager> /etc/cerebro
```

### Cerebro Configuration
DeploymentManager needs to be configured before it can run. These configurations are
done via environment variables before starting up the server. It is recommended you
copy the template configuration, update it and then source it. Steps below assume
the standard install paths were used.
```shell
cp /opt/cerebro/deployment-manager/conf/env-template.sh /etc/cerebro/env.sh
# open and edit env.sh, modifying it as necessary
source /etc/cerebro/env.sh
```

**CEREBRO_AWS_DEFAULT_REGION**
This is the region that Cerebro is running in.
```shell
Example:
export CEREBRO_AWS_DEFAULT_REGION=us-west-2
```

**CEREBRO_S3_STAGING_DIR**
This is the CEREBRO_S3_STAGING_DIR for logs and install files.
```shell
Example:
export CEREBRO_S3_STAGING_DIR=s3://cerebro-data
```

**CEREBRO_DB_URL/CEREBRO_DB_USERNAME/CEREBRO_DB_PASSWORD**
This is the end point and db credentials for CEREBRO_DB_URL. DB_URL should be the
host/port (typically 3306) of the running mysql instance.
```shell
Example:
export CEREBRO_DB_URL=cerebro.xyzzzz.rds.amazonaws.com:3306
```

**CEREBRO_DB_NAME**
This is the DB name inside the CEREBRO_DB_URL that the DeploymentManager will use.
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
  - cdas_rest_server:api
  - cerebro_planner_worker:planner
  - cerebro_planner_worker:webui
  - cerebro_catalog:hms
  - kubernetes_dashboard:admin_ui

An example configuration would be:
```shell
CEREBRO_PORT_CONFIGURATION="cerebro_planner_worker:worker:7185,cerebro_catalog:sentry:7182"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cdas_rest_server:api:7184"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner_worker:planner:7183"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner_worker:webui:7181"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_catalog:hms:7180"
export CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,kubernetes_dashboard:admin_ui:7350"
```

**KERBEROS**
To enable Kerberos, Cerebro needs a keytab and principal. They keytab needs to be uploaded
to S3 at CEREBRO_S3_STAGING_DIR/etc/KEYTAB_FILE_NAME. Then the configurations
CEREBRO_KERBEROS_PRINCIPAL and CEREBRO_KERBEROS_KEYTAB_FILE should be set to principal
and KEYTAB_FILE_NAME
```shell
export CEREBRO_KERBEROS_PRINCIPAL=<principal>
# Note: not the full path in S3, just the base name.
export CEREBRO_KERBEROS_KEYTAB_FILE=KEYTAB_FILE_NAME
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
<path to>/cerebro_cli configure --server <host:port of DeploymentManager>
<path to>/cerebro_cli status
# Should return 'Service available. Status: Running'
```

## Starting up a CDAS cluster.
With the DeploymentManager running, we can now start up CDAS clusters. There are multiple
ways to do this and the following steps are the recommended way. For alternatives, see the
end of this document.

Cluster launch is broken up into two parts:
  - Actions that need to happen on the DeploymentManager. This is referred to as the
    *launch-script*.
  - Actions that need to happen on each EC2 machine that is launched. These are referred
    to as the *init-scripts*.

The launch-script is required and when called, should provision a new EC2 instance. This
will be run from the DeploymentManager machine. This script should launch the machine
with all the required EC2 configurations (e.g. VPC, security groups, IAM Roles, etc). It
is also the best place to tag machines or do any other setup as your organization
requires.

The init scripts is an optional  list of scripts that will be run when the instance is
launched on the instance machine. This could, for example, install any monitoring software
you already use, configure the machine to custom package repo locations, etc.

We have provided a template launch script in
/opt/cerebro/deployment-manager/bin/start-ec2-machine-example.sh. It is recommended you
copy this and adapt it to your organization's requirements.
```shell
cp  /opt/cerebro/deployment-manager/bin/start-ec2-machine-example.sh /etc/cerebro/launch-ec2.sh
# Edit this file. Sections with 'USER' are the most common required customizations.
```

With the script built, you can start a cluster from the CLI. We will first create an
environment. An environment captures all the configurations required to launch
clusters. Examples of environments might be 'dev' or 'prod'.
```shell
cerebro_cli environments create --name=<Name> --provider=AWS --launchScript=<Absolute path on DeploymentManager machine> --initScripts=<Comma separated list of init scripts>
# Example:
./cerebro_cli environments create --name=DevEnv --provider=AWS --launchScript=/etc/cerebro/launch-ec2.sh
```

With the environment created, the next step is to create a cluster in that environment.
Multiple clusters can be created with the same environment.

```shell
cerebro_cli clusters create --name=<Name> --numNodes=<Number of nodes> --type=STANDALONE_CLUSTER --environmentid=<ID from environments create>
Example:
./cerebro_cli clusters create --name=fintech_prod --numNodes=1 --type=STANDALONE_CLUSTER --environmentid=1

# List the running clusters. The newly created cluster should be visible.
cerebro_cli clusters list

# This should show the cluster as 'Provisioning'. You can see more details, including
# how long it has been in this state using the cerebro_cli. This assumes the cluster
# as id '1'.
# If not, get the id from 'clusters list'.
cerebro_cli clusters status --detail 1

# It might be convenient to run this with 'watch' until the state transitions to
# 'READY'.
# This step will take a few minutes to provision the machines, install CDAS and start
# up all the services.
watch cerebro_cli clusters status 1

# At this point all of Cerebro is up and running. To see the externally configured
# end points, run
cerebro_cli clusters endpoints 1
```

## Starting a CDAS cluster from a list of provisioned machines
It is also possible to start a CDAS cluster from a list of already running machines.
This is not recommended as the DeploymentManager does not launch them. This means
that scaling and some fault tolerance capabilities will not work.

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
Example:
aws ec2 run-instances --image-id ami-43cf7d23 --iam-instance-profile Name=CerebroClusterRole --instance-type t2.medium --key-name prod-infra-key --count 2 --subnet-id subnet-64de3f03 --security-group-ids sg-30d8e657 --no-associate-public-ip-address --user-data file:///opt/cerebro/deployment-manager/bin/install-dm.sh
```

The AMI needs to be running rhel7 and must have the IAM_CLUSTER role. This is also a
good place to set up anything else the machine requires (i.e. Centrify) as typical of
your cluster machines.

**Note: the only external package install-dm.sh requires is installing java. It does so by
simply running 'yum install java' which will install openjdk-8 by default rhel7 AMI's.
This is perfectly compatible with Cerebro. However, if your organization requires custom
repo locations, you will need to update this boostrap script.**

The new instance takes a few minutes to set up Cerebro. You can check the status of
each instance using the cerebro_cli waiting for it to either return INSTALLING or
WAITING_TO_START.
```shell
./cerebro_cli agent <HOST:PORT of instance> state
Example:
./cerebro_cli agent 10.1.10.101 state
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
cerebro_cli clusters create --name=<Name> --type=STANDALONE_CLUSTER --machinesList=<machines or absoluate path to file with machine list>
Example:
./cerebro_cli clusters create --name=fintech_prod --type=STANDALONE_CLUSTER --machinesList=10.1.10.101,10.1.10.102
cerebro_cli clusters list

# This should show the cluster as 'Provisioning'. You can see more details, including
# how long it has been in this state using the cerebro_cli. This assumes the cluster
# as id '1'.
# If not, get the id from 'clusters list'.
cerebro_cli clusters status --detail 1

# It might be convenient to run this with 'watch' until the state transitions to
# 'READY'.
# This step will take a couple of minutes as all the services get started.
watch cerebro_cli clusters status 1

# At this point all of Cerebro is up and running. To see the externally configured
# end points, run
cerebro_cli clusters endpoints 1
```
