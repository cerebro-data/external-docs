# Cluster Sizing Guide

The purpose of this document is to help in planning for the resources needed to run a
Cerebro cluster. It is useful for system architects and related roles that are responsible
to determine how many servers are needed for a CDAS setup, and the respective size of
these machines in respect to their role.

Table of Contents:

* [General Information](#general-information)
* [Cloud Providers](#cloud-providers)
  * [Amazon Web Services](#amazon-web-services)

## General Information

A Cerebro CDAS cluster is foremost a data access framework. It has active components
that are running on managed servers. These components include those that handle metadata,
such as database and dataset information, access policies and cluster state. And they
include data processing components that are responsible for moving the data between
storage backends and frontend consumers.

Operating a CDAS cluster is mostly automated, with a management and orchestration layer,
and the actual CDAS services hosted within. Each server in cluster is assigned one or
more service, or *role*, as described in the next section.

### Cluster Service Roles

Each machine in the cluster is responsible to run one, or more, of the following roles:

* Deployment Manager (DM)

  This service bootstraps the rest of the CDAS cluster. It has requirements to store the
  managed cluster details into a relational database, and be able to install the CDAS
  software artifacts on the remaining cluster machines. If necessary, and used in the
  case of IaaS providers, the Deployment Manager is also responsible to execute a machine
  provisioning script, which commonly is using the provider supplied command-line tools
  to bring up the necessary infrastructure.

* CDAS Catalog

  The Catalog is required to serve the database and dataset information, including the
  dataset schemas and access policies. All of this information is stored in a related
  database and served internally to the remaining CDAS components as necessary.

* CDAS Planner

  The Planner receives the initial request to process a query. It uses the Catalog and
  cluster information to generate a plan which can be executed distributed across the
  Worker machines. The Planner is lightweight in comparison and does not handle any data
  from the available datasets.

* CDAS Worker

  The Worker role is responsible for the heavy lifting, moving the data between the
  calling client and the underlying, federated storage systems. No data is stored on disk
  during this task, leaving the onus on network, memory, and CPU resources.

* CDAS REST Server

  Access for non-native clients and the Web UI to internal resources, such as the Catalog,
  is provided by the REST Server. It has an API that allows HTTP clients to access
  datasets, although this is not the recommended way for high-throughput data processing.
  It is most suited for accessing or updating the Catalog metadata.

* CDAS Web UI

  The Web UI provides access to the Catalog information, and enables users to discover
  and browse databases and datasets. It is an interactive tool that is mostly concerned
  with metadata.

There are also more technical roles, which are required to orchestrate a CDAS cluster.
None of the these roles are part of the data path, and therefore requires only moderate
resources. The roles are:

* Kubernetes Master

  The CDAS roles are managed by Kubernetes, a tool for automated deployment, scaling and
  management of containerized applications. It has a master/worker architecture, where
  the master is responsible for the so-called *Kubernetes control plane*, which is the
  main controlling unit of the managed machines. The master provides essential services,
  such as the controller manager, scheduler, and REST-based API server.

* Kubernetes Worker

  The worker role is where the CDAS components are hosted. That is, based on the
  scheduling and assignment of the Kubernetes master, the worker nodes will execute the
  containerized services provided by CDAS. All cluster services (i.e. everything except
  the DeploymentManger) run on the kubernetes workers.

* Canary

  CDAS also comes with its own monitoring for service availability and status. The Canary
  role is an auxiliary service that knows how to check the main CDAS services, and is
  responsible to update the Deployment Manager regarding the status of all managed CDAS
  clusters.

* ZooKeeper

  Finally, for various services there is a need to employ a distributed and reliable
  consensus and membership subsystem, which is provided by ZooKeeper.

### Cluster Node Types

Since the above roles are assigned to shared nodes, we can combine the resource
requirements into the following major classes:

* Deployment Manager (DM)

  This is a single role machine that hosts the Deployment Manager role.

  Requirements:
  * Moderate resources
  * Database access

* Kubernetes Master (KM)

  Also a single role machine, hosting the Kubernetes control plane.

  Requirements:
  * Moderate resources

* Worker Node (Worker)

  These machines run all the remaining roles, as scheduled and assigned by the Deployment
  Manager and Kubernetes master subsequently.

  Requirements:
  * Elevated resources
  * Database access for certain roles (CDAS Catalog)

Dependent on the use of IaaS, for example in form of a cloud provider, or bare metal,
the node types require specific features. This includes, but is not limited to, number of
CPUs and cores, network bandwidth and connectivity, storage capacity and throughput,
and available main memory.

Most node types require only moderate resources, and it is often enough to ensure that
any possible virtualization (for example, when running a cloud environment) is not causing
any undue strain on the basic resources. An exception to the rule is the Worker, which
needs memory when executing server-side joins (see [Supported SQL](SupportedSQL.md)
for details on what joins are executed within CDAS). These joins need to load the joined
table into memory, *after* any projection and filtering of that table has taken place.

For example, assume you have a table with 100M rows and 100 columns that is used to
filter records from other user tables. Assuming a particular join is reducing the
number of rows of the filter table to 1M and the column count to two as part of the
`JOIN` statement in the internal Cerebro view, and one of the column is a `LONG` with 4
bytes, while the other column is a `STRING` with an average length of 10 bytes, you will
need 1M * (4 + 10) bytes, or roughly 14MB for the filter table in the Worker memory.

In addition, if you are running about 10 queries concurrently, using the same filter
table and assumed projection and filtering applied to it, then you will need 10 * 14MB,
or 140MB of available memory on each Worker machine (this assumes all Workers are equally
used in the query execution). The default memory per Worker per query is set to 128MB,
and suffices in this example, though the concurrency is driving the combined memory usage
up. The more concurrent queries you are running on a Worker, the more memory is needed.

### Cluster Types

Broadly speaking, in practice there are often two types of environments set up with
and for CDAS nodes:

* Proof-of-Concept/Development Environment (PoC/DEV)

  Typically, the minimal amount of resources are assigned to a development environment.
  For provisioning that assigned coarser grained resources classes, it is important to
  not under-provision the system resources. For example, it has been observed that using
  a shared development database with heavily limited concurrent connection may cause
  I/O errors that may severely reduce the CDAS functionality, or could lead to service
  outages.

* Production Environment (PROD)

  The resources needed for production are a factor of the requirements of the business
  users. In other words, each resource has a certain maximum capacity, and only
  parallelization is going to solve this limitation. Each node should not be overloaded
  either, that is, its workload should only use up to 80% of the theoretical maximum,
  leaving crucial resources for the operating system itself. Otherwise the node could
  become unresponsive or fail completely.

  Conversely, assigning too many resources may drive up cost, which is most likely a
  cause for concern longterm.

We will discuss the various offerings and resource types below in the context of these
cluster types, setting boundaries for allocation ranges (where applicable).

## Cloud Providers

Cloud providers typically do not offer configuration of the low-level system resources,
such as memory or CPU cores, directly, but instead bundle these into some kind of
resource classes. For example, the selection of a (virtual) server type will define the
available level of the essential system resources implicitly.

Commonly for cloud services, compute and storage (which includes object and database
storage) features are configured separately. On top of that other related resources,
such as load balancing or DNS can be configured as needed. For the purpose of this
document, we will primarily look into compute and storage resources, but mention others
where necessary.

### Amazon Web Services

Amazon Web Services (AWS) offers a plethora of hosted services, making it a popular
choice for companies and enterprises that want to forfeit on managing their own IT
resources, or where the pace of innovation has overtaken the internal procurement and
provisioning cycles. Spinning up virtual machines is available at the click of a button
in EC2, the *elastic compute cloud* service of AWS.

Regarding CDAS, it is important to select the appropriate EC2 instance types for the
mentioned cluster and node types, so that the low-level resources are available either
at a minimal level, or are not wasted due to over-provisioning. The selection also varies
based on the environment CDAS is installed into.

The following discussed compute and storage requirements separately.

#### Compute Instance Types

For production-level CDAS clusters on EC2, the following is recommended (especially
for cluster nodes):

* Hardware Virtual Machines (HVM)

  Select instance types and AMIs that use
  [*HVM virtualization*](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/virtualization_types.html),
  instead of the slower paravirtual machine (PV) option.

* Avoid EBS Only

  For lowest latencies, use instance types that have local storage (which is SSD in
  most cases). This will reduce for any operating system-level tasks to have an impact
  on CDAS services.

* Placement Groups

  Put all instances into a
  [*Placement Group*](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/placement-groups.html),
  which optimizes the network connectivity between those nodes. While instances usually
  observe up to 10Gbps, placing the instances into a placement group, up to
  25Gbps are possible.

* Enhanced Networking

  Use instance types that allow to make use of [*Enhanced Networking*](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/enhanced-networking.html), which
  improves on the observed packet per second (PPS) performance, and lowers network jitter
  and latencies.

* S3 Storage

  From a cost/performance standpoint, using the AWS *Simple Storage Service* (S3) is
  the best option. Using instance storage, like EBS or ephemeral storage, is imposing
  more risk of complicating data management, compared to the disadvantage of S3 being
  slightly slower. Tests have shown that S3 can be read at 600 MB/s per EC2 instance.
  This is 60% of the theoretical limit of a 10GigE network uplink.

As far as CDAS using EC2 is concerned, for Worker nodes there is a requirement for
larger `JOIN` operations to have access to as much memory as possible, especially for
situations where concurrency is high (that is, many queries being executed at the same
time). The M5 class EC2 instances, while always using EBS as their instance storage,
can be sufficiently scaled to accommodate these requirements. In addition, the EBS
connectivity is optimized and better than other instance/EBS combinations.

Conversely, the M3 class instances, which offer SSD instance storage, are limited in
their total memory to only 30GB, which is too restrictive. They also do not support
enhanced networking.

Another option are the R3/R4 type instances. These are memory optimized and scale up
to 488GB memory. The `r4.2xlarge` and `r4.4xlarge` types are a good starting point for
production clusters, giving CDAS twice the amount of memory over the M5 types with the
same number of cores.

On the other hand the C5 class types are putting an emphasis on more cores to memory.
The shift the ratio the other direction, giving half as much memory compared to M5
instances with the same amount of cores.

For CDAS, most of the work done is network based, which means that CPU cores are
important, but so is memory for caching of intermediate data in memory. Our
recommendation therefore are, as a starting point, with the option to increase
(or decrease) the instance sizes, or switch to a more memory or core heavy instance type:

| Service | Cluster Type | Node Type | Instance Type |
| :-----: | ------------ | --------- | ------------- |
| EC2 | PoC/DEV | DM | t2.medium |
| EC2 | PoC/DEV | Worker | t2.xlarge |
| EC2 | PROD | DM | m5.xlarge |
| EC2 | PROD | Worker | m5.2xlarge |

#### Storage Instance Types

In practice, most data lakes in AWS follow the above recommendations and use S3 as
their storage layer. The additional advantage is that you can suspend or stop the compute
resources at any time, for the purpose of saving cost for example, without the danger of
not being able to keep the data, or being able to access it without a running compute
cluster.

The following lists the common choices for both compute local (albeit being network
attached storage) and database related instance types.

##### Instance Storage

* Ephemeral Storage

  The local filesystem in an instance is - as far as it is available for a given EC2
  type - only used by the operating system. Should an instance fail, its ephemeral
  storage is reset on restart (hence the name).

* Elastic Block Store (EBS)

  For persistent storage across EC2 instance restarts, EBS is commonly used to attach
  storage containers that hold their data until they are permanently deleted. EBS is
  often used as scratch space for running instances and the services they host. Some E
  C2 instances only support EBS volumes, in which case it is the only option to run
  such a virtual machine.

  If possible, and as discussed earlier, when the utmost performance is wanted, choosing
  an EC2 instance that supports SSD-backed EBS volumes, and offers EBS optimized
  network connectivity, is the primary choice.

* Simple Storage Service (S3)

  As discussed above, S3 is a separate, object store-based service, that can act as a
  storage source and target for EC2 instances. The difference is that S3 can be accessed
  by other services too, without the need to run any additional compute resources.
  S3 itself comes in two types, the regular one and a longterm storage optimized version.
  Only the former is recommended as far as CDAS is concerned.

##### Database Storage

* Relational Database Service (RDS)

  The number of concurrent connections to an RDS instance is determined by the amount of
  memory the underlying EC2 instance has. In more general terms, RDS is using EC2 virtual
  machines to provision a service, which is the difference between
  IaaS (infrastructure-as-a-service) and PaaS (platform-as-a-service). Since concurrent
  connections to a database require a certain amount of memory, the available EC2 memory
  is used to compute the maximum connection count at provisioning time. It is possible to
  modify that setting for a given instance, for example increasing (or decreasing) it to
  match the requirements. The side effects of doing so are not predictable, and
  therefore it is *not* recommended to do that.

  For example, an RDS `db.t2.small` instance, which is a based on `t2.small` EC2 instance,
  has 2 GB of memory available, limiting the number of connections to about 170.
  The formula is documented and discussed online:

  > [Serverfault](https://serverfault.com/questions/862387/aws-rds-connection-limits):
  "The current RDS MySQL max_connections setting is default by
  {DBInstanceClassMemory/12582880}, if you use t2.micro with 512MB RAM, the
  max_connections could be (512*1024*1024)/12582880 ~= 40, and so on."

  The recommendation is to determine the number of connections needed for a database
  instance - which includes any other shared usage - and select an instance type for RDS
  that allows for at least 1.5 times as many connections.
