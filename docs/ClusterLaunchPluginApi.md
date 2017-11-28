# Cluster Launch Plugin API

The DeploymentManager provides a plugin API to customize how machines are launched.
The steps are split into:

- Launch script: Run on the DeploymentManager to launch machine.
- Init scripts: Run on the launched machines.

This document describes the detailed API for both kind of scripts.

## General

The scripts need to be available to the DeploymentManager. They can either be files on a
shared FileSystem (including S3) or available on the DeploymentManager machine.
The DeploymentManager will make a copy of the scripts so subsequent edits do not
affect existing uses.

The scripts can be written in any language and we do not inspect them. We simply run
them. We recommend writing them in bash as it will run without requiring bootstrapping
(e.g. installing python).

The scripts should return 0 on success and non-zero on error. The scripts should print
to stderr for diagnostics (ignored by Cerebro except for logging) and print to stdout
the result. Cerebro parses the output from standard out and it must be output exactly
as described in this document.

## Launch script

This script is required and is responsible for starting a new instance when called.
Examples of actions in this script are:

- Launch the EC2 machine in the desired VPC, with ssh keys, correct security group.
- Tag the machine as required.

This script must accept a single argument and this must be passed as the `--user-data`
argument when launching the instance.

This script should print to standard out the `instance_id`. The script *cannot* output
anything else to standard out.

### Template

We recommend looking at the template file that comes as part of the Cerebro install.
This is by default in `/opt/cerebro/deploymentmanager/bin/start-ec2-machine-example.sh`.
This script can help you get started. The sections in it marked by `USER` are the most
common customizations.

## Init scripts

The DeploymentManager allows you to specify a list of scripts that should be run on the
instance when it is first provisioned. This is equivalent to running all of these scripts
as part of the `--user-data` AWS EC2 configuration. These scripts are run in the order
they are provided and run before any Cerebro instance setup scripts. We've provided an
example that will set up the package repo to an internal location:
`/opt/cerebro/deploymentmanager/bin/setup-repo-example.sh`

These scripts should return non-zero on error and can log to stdout or stderr.
