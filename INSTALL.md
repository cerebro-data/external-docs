# Deployment Manager Install
The Deployment Manager needs to be installed to use Cerebro. After it is installed and
running, it will be responsible for managing and starting up all the remaining Cerebro
components.

## Installing from tar

The Deployment Manager can be installed on an existing machine. This can be a vm or
physical machine, either on prem or on AWS.

### Prerequisites
The machine:
  - Needs to run linux
  - Have Java 7+ installed.
  - Python2.7+ Installed
    - aws client installed. ('pip install awscli')
  - Machine needs to have IAM-Manager credentials. This can either come from the machine
    if it is in AWS or configured on the machine. Deployment Manager uses standard AWS
    credentials chain.

### Download
First, download the Deployment Manager tar ball from one of the S3 locations:
```
wget https://s3-us-west-2.amazonaws.com/cerebrodata-release/deployment-manager-0.1.tar.gz
OR
wget https://s3.amazonaws.com/cerebrodata-release-useast/deployment-manager-0.1.tar.gz
```

```
# Unpack the tar
tar xzf deployment-manager-0.1.tar.gz
```

### Logging Configuration
Configure the logging and install directories for the DeploymentManager to use. The
logging directory must be local and the install directory can be local or on S3, depending
on the durability of this machine. If using the defaults, make the directories and ensure
the Deployment Manager has access.

```
[sudo] mkdir -p /var/log/cerebro
[sudo] mkdir -p /var/run/cerebro
[sudo] chmod 700 /var/run/cerebro

# Deployment Manager user needs exclusive access to those directories. If those directories# are created as different user than the Deployment Manager user, run:
[sudo] chown <user running deployment manager> /var/log/cerebro
[sudo] chown <user running deployment manager> /var/run/cerebro
```

**Alternatively**, the log and install directory can be changed with these environment
variables, before starting up the DeploymentManager

```
export DEPLOYMENT_MANAGER_LOG_DIR=/tmp/<some dir>
export DEPLOYMENT_MANAGER_INSTALL_DIR=s3://<bucket>
```

### S3 Configuration
The Deployment Manager requires a bucket in S3 for its internal use. This is used
to stage binaries as well as logging. The Deployment Manager role as well as the
Data Access Roles needs read and write access to this location. This can be the
same directory as DEPLOYMENT_MANAGER_INSTALL_DIR if it is already a bucket in S3.
If not, set it with:
```
export CEREBRO_S3_STAGING_DIR=s3://<bucket>
```

### RDS configuration
The Deployment Manager also requires a database to be created in RDS. This can be
pre-created or the Deployment Manager will provision it on its own if it is not. If
you would like an RDS that requires options that are not available in the
DeploymentManager, you can provision your own. We require MySQL 5.6+.

If it is preconfigured, it can be set by environment variable with:

```
export CEREBRO_DB_URL=<host:port>[/db]
export CEREBRO_DB_USERNAME=<user>
export CEREBRO_DB_PASSWORD=<password>
```

### Ports
By default, the server will run on port 8080. To change this, set this in your
environment before starting up the service:
```
export CEREBRO_SERVER_HOSTPORT=0.0.0.0:<port>
```

### Running
To run, simply run
```
bin/deployment-manager
```
This will run the Deployment Manager as a daemon. To logs will be output as configured,
by default they can be viewed with.
```
tail -f /var/log/cerebro/deployment-manager.log
```

To verify it is up and running, you can simply curl the endpoint. If this fails,
look at the logs.
```
curl <hostport>/api/system/status
# By default,
curl localhost:8080/api/system/status
# This should return 'Running'
```

## Installing from containers
The Deployment Manager can also be installed from a docker image on ECS. It should run
as a service with a single instance. The image is available from docker hub.

```
cerebro/deployment-manager:latest
```

### Install
To install, simply follow the normal ECS steps, first provisioning an EC2 machine that is
compatible with ECS. The container should be started with these configs, set via
environment variables.

#### Configs

**CEREBRO_INSTALL_DIR**

This is required and specifies the S3 directory for Cerebro to store install files. This
should be a path in S3. Cerebro needs read and write access to this location.

**CEREBRO_DB_URL**

This is optional and specifies the database the installer should use. This needs to be a
MYSQL instance with the database already created. An example of this url could be:
```
jdbc:mysql://cerebro-db.us-west-2.rds.amazonaws.com:3306/installDb
```

If this is not specified, the Deployment Manager will launch its own.

**WATCHER_LOG_DST_DIR**

This is the logging directory for the deployment manager. This should be configured to a
bucket in S3. If specified, logs will be persisted to this directory, otherwise they are
lost if the container is killed.

#### Ports
  - 8080: This port must be exposed. It is the port that requests to the deployment
    manager are made.
  - 5005: This port is optional. It serves the most recent logs as well as diagnostic
    information.

## CLI
The Deployment Manager can be most easily controlled from the CLI. To install the
CLI, download the package from:

```
wget https://s3-us-west-2.amazonaws.com/cerebrodata-release/cerebro_cli-0.1.0-cp27-none-linux_x86_64.whl
OR
wget https://s3.amazonaws.com/cerebrodata-release-useast/cerebro_cli-0.1.0-cp27-none-linux_x86_64.whl
```

To install:
```
pip install ./cerebro_cli-0.1.0-cp27-none-linux_x86_64.whl
```

### Quick Start
After installing the CLI, to quickly get started, run:
```
cerebro_cli configure --server=<host:port of Deployment Manager>
```

To ensure it is connected properly, run:
```
cerebro_cli system info
```

This should print some basic information about the Deployment Manager, which as the
version it is running. The next step is to login with your account to the Deployment
Manager.
```
cerebro_cli login
```

This will prompt you for your username and password. After these steps are done, you
can begin launching and running clusters.

To see objects that are running, you can run:
```
cerebro_cli environments list
cerebro_cli clusters list
```

To create objects, you can run
```
cerebro_cli environments create
cerebro_cli clusters create
```

You will need to specify multiple arguments to these calls.

### Quick tutorial with the CLI
These commands can be run to start up a cluster running CDAS from the CLI. This
assumes that the DeploymentManger has just been installed and nothing has been
done.
```
# First, create an environment. We will use --inheritConfigs when creating the
# environment. This flag inherits the configuration of the DeploymentManager,
# which may not be the same as the CDAS cluster when deployed in production.
cerebro_cli configure --server=<host:port> of DeploymentManager.
cerebro_cli environments create --name=SampleEnvironment --inheritConfigs=True --provider=aws

# If this is the first time, the environment will be created with id '1'. If
# it is not, you will have to change the environmentid in the commands below.
# The environment should be listable now. You should see the newly created
# environment.
cerebro_cli environments list

# You can also see a detailed output of what the environment looks like.
cerebro_cli environments list --detail

# Next, create a cluster. This will provision and get the cluster running. This
# step involves provisioning instances in EC2 and can take ~10minutes. Again, it is
# assumed this cluster is created with id '1', if not you will have to update the
# steps below.
cerebro_cli clusters create --name=TestCluster --environmentid=1 --numNodes=1 --type=TEST_CLUSTER

# You can check the status of the cluster with
cerebro_cli clusters status --clusterid=1

# This should so the cluster as 'Provisioning'. You can see more details, including
# how long it has been in this state with
cerebro_cli clusters status --clusterid=1 --detail

# It might be convenient to run this with 'watch' until the state transitions to
# 'READY'. At this point you can also log into your EC2 console and should see
# the new instances being provisioned. The instances should be tagged with the
# name of the cluster you specified ('TestCluster' by default).
# This step can take around 10 minutes, depending how quickly the instances can
# be provisioned.
watch cerebro_cli clusters status --clusterid=1

# At this point the cluster is up and running. You can see the external endpoints
# of the cluster.
# This should return a list of hostports where the service can be reached. It
# will look something this and you can curl the endpoint.
cerebro_cli clusters endpoints --clusterid=1
{
    "canary:webport": [
        "54.190.197.176:30848"
    ]
}

# Curling that endpoint should return some basic information.
curl 54.190.197.176:304848
{"current_time": "2016-10-13-18-16-23", "uid": "8300d46e-6da8-4d09-bfe9-780d74f204ae", "start_time": "2016-10-13-18:13:39"}

# To delete the cluster, you can just run
cerebro_cli clusters delete --clusterid=1
```

### Creating a data access test cluster.
The steps above created a simple canary cluser which can be helpful to verify the
install is working. The canary cluster is not running any of the data related services.
There are a few ways to do this.

#### Creating a standlone cluster
A standalone cluster is intended for testing and launches the data access service
and catalog service in the same cluster.

To start this cluster, simply create it as before but passing it the STANDLONE_CLUSTER
type.
```
cerebro_cli clusters create --name=StandaloneTestCluster --environmentid=1 --numNodes=1 --type=STANDALONE_CLUSTER
```

Identical commands can be used to monitor its status and wait for it to be ready. It
typically takes a bit longer to spin up the additional services. When it is ready,
you can get the endpoints as before and it should output something like this.
```
{
    "cerebro_catalog:log-server": [
        "52.25.90.228:30565"
    ],
    "cerebro_catalog:hms": [
        "52.25.90.228:31684"
    ],
    "cerebro_planner_worker:planner": [
        "52.25.90.228:30654"
    ],
    "cerebro_catalog_ui:webui": [
        "52.25.90.228:31212"
    ],
    "cerebro_planner_worker:webui": [
        "52.25.90.228:32190"
    ],
    "cerebro_catalog:sentry": [
        "52.25.90.228:32608"
    ],
    "cerebro_planner_worker:worker": [
        "52.25.90.228:31988"
    ],
    "canary:webport": [
        "52.25.90.228:31464"
    ]
}
```

#### Catalog and Multiple Data Access Clusters
In production scenarios we recommend running the catalog service independently, with
multiple data access service clusters connected to it. This matches a model similar
to EMR, where the compute clusters are more transient but the catalog should always
be up.

The steps below will create 1 catalog cluster and 2 different data access clusters.
The data access clusters can be scaled and terminated independently.
```
# Launch a catalog cluster. Assume this returns clusterid=1
cerebro_cli clusters create --name=Catalog --environmentid=1 --numNodes=1 --type=CATALOG_CLUSTER

# Next, launch a 10 node data access cluster. Note that we need to specify the catalog id.
cerebro_cli clusters create --name=DataScienceCluster --environmentid=1 --numNodes=10 --type=DATA_ACCESS_CLUSTER --catalogClusterId=<id of catalog cluster>

# We can continue to launch more data access clusters as we'd like
cerebro_cli clusters create --name=LargeEtl --environmentid=1 --numNodes=50 --type=DATA_ACCESS_CLUSTER --catalogClusterId=<id of catalog cluster>
```
