# Advanced installations

This document describes advanced Cerebro installs. This document assumes the user is
familiar with the base install and will describe changes relative to that process.

## External Hive Metastore

In a typical CDAS install, CDAS will start up and manage a Hive Metastore (HMS)
compatible service. If instead, you'd like to have CDAS use an existing, externally
managed HMS, you can do so. In this configuration, CDAS will simply read and write
from the external HMS but all other behavior is unchanged.

CDAS is compatible with Hive 1.1.x and 1.2.x (CDH5.7+).

To use an external HMS, add this config to your DeploymentManager configs,
typically `/etc/cerebro/env.sh`.

```shell
export CEREBRO_EXTERNAL_HMS_CONFIG=<path to hive-site.xml>
```

The value should be a full path to the hive-site.xml client config for the external HMS.
The path must be accessible from the DeploymentManager process but can be either local
or remote (e.g. S3). After setting the config and restarting the DeploymentManager, newly
created clusters will use the external HMS and restarting existing clusters will update
to use the external HMS config.

It is possible to have multiple CDAS clusters use the same external HMS, subject to the
concurrency settings of your HMS.
