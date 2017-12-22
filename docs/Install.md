# Cerebro Install

This document describes the Cerebro install process. The result should be a fully
operational Cerebro cluster consisting of the DeploymentManager and a running Cerebro
Data Access Service (CDAS) cluster.

The various sections included are:

* [Prerequisites](prerequisites)
* [Installing the Deployment Manager](installing-the-deploymentmanager)
* [Starting up a CDAS Cluster](starting-up-a-cdas-cluster)
* [Cerebro Upgrades](cerebro-upgrade)

## Prerequisites

Before you can install the various Cerebro components, the following preparatory
work is required.

### AWS Prerequisites

Cerebro depends on a few AWS services to be set up before installing Cerebro.

- S3 Bucket. Cerebro uses this bucket to store log files as well as stage intermediate
config files. This bucket should be readable and writable by all instances running any
Cerebro components. This bucket will be referred to as `CEREBRO_S3_STAGING_DIR`.
- RDS instance with MySQL 5.6 or Aurora (MpSQL 5.6.x compatible) provisioned. This can
be provisioned with the configuration of your choice. Cerebro instances need read and
write access to this database. This document will refer to this as `CEREBRO_DB_URL`.
- IAM credentials. Cerebro instances need read and write access to the above two. The
DeploymentManager needs the ability to provision instances and the cluster machines
will need credentials to your data. These can be one IAM profile with all the credentials
or two separate role. We will refer to them as `IAM_MANAGER` and `IAM_CLUSTER`.

## Installing the DeploymentManager

The DeploymentManager runs on a machine separate from the machines in the cluster. This
does not need to be its own machine.

### Machine and Package Prerequisites

DeploymentManager machine:

* Needs to run Linux
* Have Java 7+ installed (OpenJDK or Sun JVM).
  * Java 9 is not currently supported due to a known issue
  * Java 8 is recommended as Sun has ceased providing security updates for Java 7
* Machine needs to have `IAM_MANAGER` credentials.
* Minimum instance type should be `t2.small`
* Have `awscli` installed

Cluster machine (this will be created by the deployment manager based on a user-defined
launch script described later):

- RHEL 7: Cerebro will try to install java if not present. If your environment uses
non-standard package management, see additional steps below.
- Machine needs to have `IAM_CLUSTER` credentials.
- Minimum instance type should be `t2.large` with at least 40GB of local storage.

Network:

- We expect full network connectivity between the machines in the cluster and the
DeploymentManager machine.

In addition, you will need to install the Cerebro CLI. The machine running the CLI
needs to have network access to the DeploymentManager and cluster machines. The CLI
can be installed on the DeploymentManager or a local development machine. To run the
CLI you need:

- RHEL 6+ or Ubuntu 14.04+
- OS X Darwin 10.11+ (OS X El Capitan+)

Browser requirements, for Web UI:

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Microsoft Edge (latest)
- Internet Explorer 11 (Compatibility Mode is not supported)

### Installing the AWS Commandline Tool

Here is one way to install `awscli` on the DeploymentManager machine:

```shell
# install tools and awscli
sudo yum install wget
sudo wget https://bootstrap.pypa.io/get-pip.py
sudo python get-pip.py
sudo pip install awscli

# Configure AWS access
aws configure
```

### Getting the Bits

Download and extract the DeploymentManager tarball:

```shell
# Recommended location is /opt/cerebro but can be any location.
sudo mkdir -p /opt/cerebro && cd /opt/cerebro

# Update ownership of the destination directory
echo `whoami` | xargs -I '{}' sudo chown -R '{}' /opt/cerebro

# Get the tarball from S3.
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.7.1/deployment-manager-0.7.1.tar.gz

# Extract the bits.
tar xzf deployment-manager-0.7.1.tar.gz && rm deployment-manager-0.7.1.tar.gz && ln -s deployment-manager-0.7.1 deployment-manager
```

Download the shell binary. This depends on the OS running the CLI.

```shell
# Linux
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.7.1/cli/linux/cerebro_cli && chmod +x cerebro_cli
# OS X
curl -O https://s3.amazonaws.com/cerebrodata-release-useast/0.7.1/cli/darwin/cerebro_cli && chmod +x cerebro_cli
```

### DeploymentManager Logging and Install Directory

Configure the logging and local install directories. These should be paths on the local
file system. The install directory currently needs to be restored if this machine is
moved. These by default are `/var/log/cerebro` and `/etc/cerebro` but can be changed
by setting `DEPLOYMENT_MANAGER_LOG_DIR` and `DEPLOYMENT_MANAGER_INSTALL_DIR` in the
environment.

For a standard install:

```shell
sudo mkdir -p /var/log/cerebro && sudo mkdir -p /etc/cerebro && sudo chmod 700 /etc/cerebro

# DeploymentManager user needs exclusive access to those directories. If those
# directories are created as different user than the DeploymentManager user, run:
echo `whoami` | xargs -I '{}' sudo chown -R '{}' /var/log/cerebro
echo `whoami` | xargs -I '{}' sudo chown -R '{}' /etc/cerebro
```

### Cerebro Configuration

DeploymentManager needs to be configured before it can run. These configurations are
done via environment variables before starting up the server. It is recommended you
copy the template configuration and update it. Steps below assume the
standard install paths were used.

```shell
cp /opt/cerebro/deployment-manager/conf/env-template.sh /etc/cerebro/env.sh
# open and edit env.sh, modifying it as necessary
```

The config script from `/etc/cerebro/env.sh` will automatically be loaded when starting
the DeploymentManager. If this is the path used, it is not necessary to source the script.

NOTE:
The environment variables set in the env.sh script are stored by the deployment manager
when it starts up. When a deployment manager creates a Cerebro cluster, the current values
of those environment variables are applied to the new cluster and the cluster will retain
those values independent of any subsequent configuration changes on the deployment manager.
To change the values used by a deployment manager, you must either update the env.sh script
(assuming that it's in the default location of /etc/cerebro/) or update your environment
variables. Then, restart the deployment manager by running:
```shell
/opt/cerebro/deployment-manager/bin/deployment-manager
```
Clusters created after the deployment manager is restarted will use the new
configuration values.

An existing Cerebro cluster that is restarted via a deployment manager will not pick up
any changes in the deployment manager's configuration. Rather, the Cerebro cluster
will retain the configuration values that were used during that cluster's creation.


**CEREBRO_S3_STAGING_DIR**
This is the `CEREBRO_S3_STAGING_DIR` for logs and install files. It can be anywhere in
s3 that makes sense for your organization. Cerebro will create a number of subdirectories
it requires here.

```shell
# Example:
export CEREBRO_S3_STAGING_DIR=s3://<your cerebro dir>
```

**CEREBRO_DB_URL/CEREBRO_DB_USERNAME/CEREBRO_DB_PASSWORD**
This is the end point and database credentials for `CEREBRO_DB_URL`. `DB_URL` should be
the host/port (typically 3306) of the running mysql instance.

```shell
# Example:
export CEREBRO_DB_URL=cerebro.xyzzzz.rds.amazonaws.com:3306
```

**CEREBRO_DB_NAME**
This is the DB name inside the `CEREBRO_DB_URL` that the DeploymentManager will use. If
this RDS instance is only backing a single DeploymentManager install, this does not need
to be set. Otherwise, each install can have a different database. This does not need to
be pre-created.

```shell
# Example:
export CEREBRO_DB_NAME=cerebro
```

**CEREBRO_CATALOG_ADMINS**
CDAS clusters will, by default, start up with one user which has admin on the system.
The admin users can create and manage roles, grant roles to other users and read all
datasets. This default admin user depends on which authentication mechanism was chosen:

- Kerberos: The admin user is the first part of the kerberos principal
- JWT: The admin user is the subject in the `CEREBRO_SYSTEM_TOKEN`
- Unauthenticated: The admin user is 'root'.

To specify other admin users, specify the comma-separated list of admins and/or groups.

```shell
# Example:
export CEREBRO_CATALOG_ADMINS=admin,username1,admin-group
```

Admins users can grant permissions to other users/groups including the ability to grant
to other users. However, *only* admin users can create new roles.

**CEREBRO_SERVER_HOSTPORT**
This is the host:port to run the DeploymentManager. By default it listens to all
interfaces and runs on port 8085 (e.g. `0.0.0.0:8085`). This port does not need to be
accessible to the typical user to access data but is required the administer Cerebro
clusters.

```shell
# Example:
export CEREBRO_SERVER_HOSTPORT=0.0.0.0:8085
```

**CEREBRO_PORT_CONFIGURATION**
This configuration allows specifying the ports to run the cluster services on. These
ports do need to be available for all the users connecting to Cerebro for metadata and
data. This is a comma-separated list of `service:portname:port number`. Below are the
ports that are required for clients. If they are not specified, Cerebro will pick
randomly available ports to expose these services on.

- cdas_rest_server:api
- cerebro_catalog:hms
- cerebro_catalog:sentry
- cerebro_planner:planner
- cerebro_planner:webui
- cerebro_web:webui
- cerebro_worker:worker
- kubernetes_dashboard:admin_ui

An example configuration would be:

```shell
CEREBRO_PORT_CONFIGURATION="cerebro_worker:worker:7185,cerebro_catalog:sentry:7182"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cdas_rest_server:api:7184"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner:planner:7183"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_planner:webui:7181"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_catalog:hms:7180"
CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,cerebro_web:webui:7186"
export CEREBRO_PORT_CONFIGURATION="$CEREBRO_PORT_CONFIGURATION,kubernetes_dashboard:admin_ui:7350"
```
**KERBEROS**
To enable Kerberos, specify these configs:

- `CEREBRO_KERBEROS_PRINCIPAL`: principal for non-REST Cerebro services.
- `CEREBRO_KERBEROS_HTTP_PRINCIPAL`: principal for REST services, this by convention
should start with `HTTP/` and we highly recommend this for compatibility with exiting
tools.
- `CEREBRO_KERBEROS_KEYTAB_FILE`: Path to keytab file containing both principals. This
path needs to be accessible from the DeploymentManager but can be on the local machine
or in S3.
- `CEREBRO_KERBEROS_ENABLED_REALMS`: List of comma-separated cross realms to accept
connections from. This does not need to be specified if only connections from the
`CEREBRO_KERBEROS_PRINCIPAL` realm is allowed to connect.

```shell
# Example:
export CEREBRO_KERBEROS_PRINCIPAL="cerebro/cname.example.com@REALM.com"
export CEREBRO_KERBEROS_HTTP_PRINCIPAL="HTTP/cname.example.com@REALM.com"
export CEREBRO_KERBEROS_KEYTAB_FILE="/etc/cerebro.keytab"
```

For more information on how to set up a kerberized cluster, see:

- [Kerberos Cluster Setup](KerberosClusterSetup.md)

**JSON Web Token**
To enable authentication using JSON Web Tokens (JWT), specify these configs:

- `CEREBRO_JWT_PUBLIC_KEY`: Path to the public key to decrypt tokens. Must be accessible
on the DeploymentManager machine but can be on the local machine or in S3.
- `CEREBRO_JWT_ALGORITHM`: Algorithm to use to decrypt tokens. This currently must be
"RSA256" or "RSA512"
- `CEREBRO_JWT_AUTHENTICATION_SERVER_URL`: URL that will authenticate JWT tokens.
We will issue a POST call to this URL, specifying the token to authenticate. This cannot
be set if `CEREBRO_JWT_PUBLIC_KEY` is set.
- `CEREBRO_SYSTEM_TOKEN`: Path to a file that just contains the token (on a single line)
that Cerebro services will use to authenticate itself. The subject for this token acts
as the Cerebro system user.

```shell
# Example:

# Either these two
export CEREBRO_JWT_PUBLIC_KEY="/etc/jwt.512.pub"
export CEREBRO_JWT_ALGORITHM="RSA512"
# OR
export CEREBRO_JWT_AUTHENTICATION_SERVER_URL="http://sso/verify-token"

export CEREBRO_SYSTEM_TOKEN="/etc/cerebro.token"
```

**HTTPS**
To enable HTTPS, specify the SSL certificate and private key that Cerebro should use.
Setting these, enables HTTPS on the REST server and web ui.

- `CEREBRO_SSL_CERTIFICATE_FILE`: Path to the certificate file.
- `CEREBRO_SSL_KEY_FILE`: Path to the file with the private key.
- `CEREBRO_SSL_FQDN`: [Optional] Fully qualified domain name for the cluster. If set,
this must be a valid 'Subject Alternate Name' in the certificate. This can be the FQDN
for any node in this cluster (typically the CNAME for the REST service). This is required
for some clients (e.g. newer versions of chrome) which do not allow IP addresses if SSL
is enabled.

```shell
# Example:
export CEREBRO_SSL_CERTIFICATE_FILE=/etc/cerebro.cert
export CEREBRO_SSL_KEY_FILE=/etc/cerebro.key
export CEREBRO_SSL_FQDN=rest-server.cerebro.com
```

**LDAP**
For information on how to set LDAP Basic Auth related environment variables, see:

- [LDAP Basic Auth](LdapAuthentication.md#setting-up-ldap-related-configurations)

For more details on interactions with authenticated Cerebro, see:

- [Authentication](Authentication.md)
- [Security](Security.md)

**OAuth**
Cerebro can be deployed with OAuth enabled for easier Web UI login. For more information,
see:

- [OAuth Guide](OAuthGuide.md)

### Starting the DeploymentManager

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

### Configuring the CLI

With the DeploymentManager running, we can configure the CLI to connect with it. Run

```shell
<path to>/cerebro_cli configure --server <host:port of DeploymentManager>
<path to>/cerebro_cli status
# Should return 'Service available. Status: Running'
```

## Starting up a CDAS Cluster.

With the DeploymentManager running, we can now start up CDAS clusters.

### Configure Machine Settings

Cluster launch is broken up into two parts:

- Actions that need to happen on the DeploymentManager. This is referred to as the
*launch-script*.
- Actions that need to happen on each EC2 machine that is launched. These are referred
to as the *init-scripts*.

The launch-script is required and when called, should provision a new EC2 instance.
This will be run from the DeploymentManager machine. This script should launch the
machine with all the required EC2 configurations described for the 'Cluster machine' in
the prerequisites (e.g. VPC, security groups, IAM Roles, etc). It is also the best place
to tag machines or do any other setup as your organization requires.

The init scripts is an optional list of scripts that will be run when the instance is
launched on the instance machine. This could, for example, install any monitoring
software you already use, configure the machine to custom package repo locations, etc.

We have provided a template launch script in
`/opt/cerebro/deployment-manager/bin/start-ec2-machine-example.sh`. It is recommended you
copy this and adapt it to your organization's requirements. The user-configurable values are
at the top of the file, marked with "USER" in a comment. At a minimum, a subnet ID and security
group ID must be added to the script.

```shell
cp  /opt/cerebro/deployment-manager/bin/start-ec2-machine-example.sh /etc/cerebro/launch-ec2.sh
```

To verify the launch script, we recommend running it with no arguments from the
DeploymentManager machine as the same user as the DeploymentManager, with identical
proxy settings (if any) and identical AWS settings. The script should run successfully
with no arguments and it should output the instance-id and ip addresses of the newly
launched instance.

Launch script validation was recently added to CDAS. This requires that launch scripts
support a "--dryrun" flag and exit with a status of zero when invoked  with that flag.
As example:
```
$ /etc/cerebro/launch-ec2.sh --dryrun
Dry run succeeded
$ echo $?
0
```
The "Dry run succeeded" line is not required.

When a launch script is invoked without the "--dryrun" flag, it is expected to
return a string of the form: ```<instance id>,<public ip>,<private ip>```
NOTE: the public ip value is optional. If your setup does not use it, simply output
```<instance id>,,<private ip>```


With the script built, you can start a cluster from the CLI. We will first create an
environment. An environment captures all the configurations required to launch clusters.
Examples of environments might be 'dev' or 'prod'.

```shell
cerebro_cli environments create --name=<Name> --provider=AWS --launchScript=<Absolute path on DeploymentManager machine> --initScripts=<Comma separated list of init scripts>
# Example:
./cerebro_cli environments create --name=DevEnv --provider=AWS --launchScript=/etc/cerebro/launch-ec2.sh
```

### Creating a Cluster

With the environment created, the next step is to create a cluster in that environment.
Multiple clusters can be created with the same environment.

> **Note:** See [Cluster Types](ClusterTypes.md) for the available option when using
the `--type` parameter.

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

### Restarting Failed Cluster after Addressing Issues

When a Cerebro environment is created, the launch, init scripts and config files for that
environment are copied to the Cerebro install directory. This is to ensure that the
clusters using that environment are unaffected by any new environments or clusters with
different configurations.

If the script or config files in the environment have any errors, then those need to be
corrected and the affected clusters restarted. These include `launch-ec2.sh` and any
init scripts.

The Cerebro install scripts and config files are located at the
`$DEPLOYMENT_MANAGER_INSTALL_DIR/env/<environmentid>` directory. The
`DEPLOYMENT_MANAGER_INSTALL_DIR` environment variable defaults to `/etc/cerebro`.

### Starting Multiple CDAS Catalogs Sharing the Same Metadata

Cerebro catalogs can be configured to share the same underlying metadata. This feature
is supported for "active-passive" configurations. In this case when creating the multiple
clusters, supply the catalogDbNamePrefix argument. Catalogs which share the same name
will share the same metadata. For example, to create two clusters that share metadata:

```shell
./cerebro_cli clusters create --name=prod --numNodes=1 --type=STANDALONE_CLUSTER --environmentid=1 --catalogDbNamePrefix=metadata
./cerebro_cli clusters create --name=prod-backup --numNodes=1 --type=STANDALONE_CLUSTER --environmentid=1 --catalogDbNamePrefix=metadata
```

NOTE: If the `--catalogDbNamePrefix` was not explicitly specified on cluster creation,
then it is the name of the cluster.

## Cerebro Upgrade

Starting with 0.4.0, Cerebro upgrades to newer CDAS versions and patches can be applied
using the `cerebro_cli` command.

See [Cluster Administration](ClusterAdmin.md) for further details.
