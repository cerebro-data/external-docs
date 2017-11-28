# Kubernetes Dashboard Quickstart

This document describes how to use the Kubernetes Administration Dashboard. Kubernetes
Dashboard enables the cluster administrator to get overall health of the cluster and
get details of each node, pod and service that is part of the cluster.

### Version

- Current version of Kubernetes is 1.5.3
- The admin dashboard version is 1.5.1

### Dashboard Web

The dashboard web interface is accessible through the same port on the IP address of
any of the cluster nodes. To get the hostport, first get the cluster id, by running get
clusters command, followed by get details for the cluster.

```shell
$ cerebro_cli agent <masterip> kubernetes-info
Example:
$ cerebro_cli agent 10.1.10.101 kubernetes-info
```

Note down the dashboardPort value. Using your favorite browser launch the dashboard with
URL similar to the following example.

Example:  http://10.1.10.101:31779

### Basic navigation

The navigation panel on the left connects to the `default` namespace. CDAS services are
launched in this namespace. `kube-system` namespace will show the Kubernetes system pods
and services.

Kubernetes deploys and schedules containers in groups called pods. A pod typically has
one or more containers that provides a CDAS service.

You may use the dashboard ui to list the various CDAS services and confirm that the
desired number of pods is running for each service. Typically, you would run one
`cerebro-planner` pod and several `cerebro-worker` pods depending on your workload needs.

For example, a 10-node CDAS cluster would have the following pods:

- 1 canary pod
- 1 cerebro-catatog pod
- 1 cdas-rest-server pod
- 1 zookeeper pod
- 1 cerebro-planner pod
- 9 cerebro-worker pods

Let's try a few of sample dashboard operations:

1. Keeping the namespace as `default`, click on `Deployments` on the left navigation
panel. Makes sure that all CDAS services are running. This should include
`cerebro-catalog`, `cerebro-web`, and `cerebro-planner`.
2. Click on `Pods` on the left navigation panel Make sure that the list is similar to
the example above, including the desired number of pods.
3. Click on `Pods`. Scroll to the right of the `cerebro-planner` pod and click on the
icon with small horizontal bars. This will show the logs in a separate tab/window on
your browser. The logs are a quick way to detect any issues with the pod.

### Additional Information

Refer to https://kubernetes.io/docs/ for documentation and tutorials on Kubernetes.
