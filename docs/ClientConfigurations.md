# Cerebro Client Configurations

This document describes configurations available to the client for Hadoop ecosystem tools.

Note: when the configs are set in spark, they are prefixed with `spark.`. For example,
the config `recordservice.kerberos.principal`, when configured for spark, should
be `spark.recordservice.kerberos.principal`. This is true for all configs.

## Specifying configs

How configs are specified depends on the tool being used and uses the tool's standard
configuration mechanisms.

### Spark

Configs can be specified:

- via the commandline to spark-submit/spark-shell with --conf
- set in spark-defaults, typically in /etc/spark-defaults.conf
- can be set in the application, via the SparkContext (or related) objects

### Hive

Configs can be specified:

- via the commandline to beeline with --hiveconf
- set on the class path in either hive-site.xml or core-site.xml
- set in the beeline session using the `SET` command

## Required configs

**recordservice.planner.hostports**

This is always required and is a comma separated list of host:ports where the CDAS
planners are running.

## Authentication configs

**recordservice.kerberos.principal**

This is the principal of the planner service to connect to. This is a 3 part
principal: `SERVICE_NAME/SERVICE_HOST@REALM`, for example,
`cerebro/planner.cerebro.com@CEREBRO.COM`. This is required if the client is
authenticating with CDAS using kerberos.

**recordservice.delegation-token.token**

This is the token string for this user. CDAS can be configured to accept multiple kinds
of tokens but it is the same config for clients.

**recordservice.delegation-token.service-name**

This is only required for versions < 0.4.5 and should *not* be set on newer versions.
This must be set if token based auth is being used and should match the SERVICE_NAME
portion of the planner principal. In the above example, this value would be `cerebro`.

Note: If both the token and principal is specified, the client will only authenticate
using the token.

## Network related configs

These configs are often not required and the defaults should suffice. These can be
adjusted if the the client observes timeout behavior.

**recordservice.planner.retry.attempts**

Optional configuration for the maximum number of attempts to retry RPCs with planner.

**recordservice.worker.retry.attempts**

Optional configuration for the maximum number of attempts to retry RPCs with worker.

**recordservice.planner.retry.sleepMs**

Optional configuration for sleep between retry attempts with planner.

**recordservice.worker.retry.sleepMs**

Optional configuration for sleep between retry attempts with worker.

**recordservice.planner.connection.timeoutMs**

Optional configuration for timeout when initially connecting to the planner service.

**recordservice.worker.connection.timeoutMs**

Optional configuration for timeout when initially connecting to the worker service.

**recordservice.planner.rpc.timeoutMs**

Optional configuration for timeout for planner RPCs (after connection is established).

**recordservice.worker.rpc.timeoutMs**

Optional configuration for timeout for worker RPCs (after connection ie established).

## Performance related configs

These settings can fine tune the performance behavior. It is generally not needed to set
these as the server will compute a good value automatically.

**recordservice.task.fetch.size**

Optional configuration option for performance tuning that configures the max number of
records returned when fetching results from the workers.

**recordservice.task.plan.maxTasks**

Optional configuration for the hinted maximum number of tasks to generate per PlanRequest.