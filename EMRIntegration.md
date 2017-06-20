# Cerebro Data Access Service EMR Integration
This documents describes how to use Cerebro Data Access Service (CDAS) from EMR. It
assumes that the CDAS cluster is already running. This describes how to configure each
of the supported EMR services.

In general, we require:
  1. Specifying a bootstrap action which will download our client library on the cluster
     nodes.
  2. Specifying a configuration which configures the client library to use a CDAS install.

An EMR cluster that is using multiple components should apply each configuration.

## Bootstrap action
The bootstrap action will always place the client jars in the hadoop user's home directory,
under /home/hadoop/cdas-libs/ in addition to the component specific library path.

To bootstrap the cluster, run our script specifying the version and components you have
installed. The bootstrap script is located at:
```
s3://cerebrodata-release-useast/utils/emr/cdas-emr-bootstrap.sh
# Usage:
# cdas-emr-bootstrap.sh <cdas version> <list of components>
```

For example, to bootstrap a spark-2.x cluster from the 0.4.0 client release, provide
the arguments "0.4.0 spark-2.x". If running EMR with spark-2 and pig, you can provide
for example "0.4.0 spark-2.x pig".

The complete list of supported components are:
  - spark 1.x
  - spark 2.x
  - hadoop
  - pig

Non-compute components can also be used and do not require any CDAS related steps.
These include:
  - Zeppelin
  - Ganglia
  - ZooKeeper

## End to End example
As an end to end example of starting up an EMR-5.6.0 cluster running just spark 2.x
configured to run against CDAS planner running at 10.1.10.251:12050, you would need
to do:

Pick Spark and specify the Spark specific configs (more details below):
![EMR Config](https://s3.amazonaws.com/cerebro-data-docs/images/EMRConfig.png)

Use our provided bootstrap script and specify spark-2
![EMR Bootstrap](https://s3.amazonaws.com/cerebro-data-docs/images/EMRBootstrap.png)

Once the cluster is up, the user can for example, use the spark shell with:
```
$ spark-shell
scala> import com.cloudera.recordservice.spark._
scala> val context = new org.apache.spark.sql.SQLContext(sc)
scala> context.setConf("recordservice.user", USER'S TOKEN)
scala> val df = context.load(DB.TABLE, "com.cloudera.recordservice.spark")
scala> df.show()
```

The complete commandline to start up a cluster like this would be:
```
aws emr create-cluster --auto-scaling-role EMR_AutoScaling_DefaultRole --applications Name=Spark --bootstrap-actions '[{"Path":"s3://cerebrodata-release-useast/utils/emr/cdas-emr-bootstrap.sh","Args":["0.4.0","spark-2.x"],"Name":"Custom action"}]' --ec2-attributes '{"KeyName":"ssh-key","InstanceProfile":"EMR_EC2_DefaultRole","SubnetId":"subnet-c4de3fa3","EmrManagedSlaveSecurityGroup":"sg-bf88dbd9","EmrManagedMasterSecurityGroup":"sg-b188dbd7"}' --service-role EMR_DefaultRole --release-label emr-5.6.0 --name 'Spark Cluster' --instance-groups '[{"InstanceCount":1,"InstanceGroupType":"CORE","InstanceType":"m3.xlarge","Name":"Core - 2"},{"InstanceCount":1,"InstanceGroupType":"MASTER","InstanceType":"m3.xlarge","Name":"Master - 1"}]' --configurations '[{"Classification":"spark-defaults","Properties":{"spark.recordservice.planner.hostports":"10.1.10.251:12050"},"Configurations":[]}]' --scale-down-behavior TERMINATE_AT_INSTANCE_HOUR --region us-west-2
```

## Per component configs
In the section below, we will detail the configurations required to configure
each supported EMR component.

### Spark
Spark requires a single config which specifies the endpoint(s) for the CDAS planner.
For example, if the planner endpoint is running at "10.1.10.251:12050", then the
required configuration is:

```
[{"Classification":"spark-defaults","Properties":{"spark.recordservice.planner.hostports":"10.1.10.251:12050"}}]
```

## Logging
On the EMR machines, the bootstrapping logs will be located in /var/log/bootstrap-actions/.
This can be helpful if the cluster is not starting up.

## Configs
Configs are generally written to /etc/[component]. These should replicate the
configurations that were specified when the cluster was created.
