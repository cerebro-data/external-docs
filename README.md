# User Documentation

Welcome to the _User Documentation_ collection for the Cerebro software stack. The
documents are grouped into sections that reflect their purpose.

Table of Contents:

* [Release Information](#release-information)
* [Installation](#installation)
* [Administration](#administration)
* [Integration](#integration)
* [Developers](#developers)

## Release Information

* [Release Notes][relnotes] - Information about this release.

## Installation

  * [Installation Guide][install] - The main installation guide.
  * [Advanced Installation Option][adinstall] - Additional, advanced installation options.
  * [Kerberos][kerberosclustersetup] - Describes how to kerberize a Cerebro cluster.
  * [LDAP Authentication][ldapauthn] - For integration of an LDAP service for authentication.
  * [OAuth Authentication][oauthguide] - For integration of an OAuth service for authentication.
  * [DeploymentManager REST Security][security] - Information on how to enable Kerberos for the DeploymentManager API.
  * [Cluster Types][clustertypes] - Lists all of the supported cluster types and the
  services they include.

## Administration

* [Auditing][auditing] - Describes the audit logging performed by the Cerebro services.
* [Cluster Administration][clusteradmin] - Explains how to administrate a running Cerebro cluster.
* [Cluster Launch API][clusterlaunchpluginapi] - For custom cluster launch functionality.
* [Database CLI][dbcli] - This command line tool enables interaction with the Cerebro database.
* [Kubernetes Dashboard Quickstart][kubernetesdashboardquickstart] - Introduction to the Kubernetes dashboard.
* [Web UI][webui] - The Cerebro Web UI is explained in detail.

## Integration

* [Cloudera CDH Integration][cdhintegration] - How to integrate Cerebro with CDH.
* [Client Integration][clientintegration] - How to integrate Cerebro with Hadoop clients.
* [EMR Integration][emrintegration] - How to integrate Cerebro with AWS EMR.
* [Tableau Integration][tableauwdc] - How to integrate Cerebro with Tableau.

## Developers

* [Authentication][authn] - Describes the various client authentication options.
* [Docker Quickstart][dockerquickstart] - For developers, can be used to try out CDAS APIs.
* [Catalog API][catapi] - Documents the Catalog REST API.
* [Client Configuration][clientconfig] - Details about client configuration options.
* [Supported Data Types][data] - Lists the supported data types.
* [Extending CDAS][extendingcdas] - Introduces the concepts of UDFs and SerDes.
Describes S3 permission inheritance.
* [Supported SQL][supportedsql] - Describes the Cerebro supported SQL syntax.

<!-- internal link references -->
[adinstall]: docs/AdvancedInstall.md
[auditing]: docs/Auditing.md
[authn]: docs/Authentication.md
[catapi]: docs/CatalogApi.md
[cdhintegration]: docs/CDHIntegration.md
[clientconfig]: docs/ClientConfigurations.md
[clientintegration]: docs/ClientIntegration.md
[clusteradmin]: docs/ClusterAdmin.md
[clusterlaunchpluginapi]: docs/ClusterLaunchPluginApi.md
[clustertypes]: docs/ClusterTypes.md
[data]: docs/Data.md
[dbcli]: docs/DbCLI.md
[dockerquickstart]: docs/DockerQuickstart.md
[emrintegration]: docs/EMRIntegration.md
[extendingcdas]: docs/ExtendingCDAS.md
[install]: docs/Install.md
[kerberosclustersetup]: docs/KerberosClusterSetup.md
[kubernetesdashboardquickstart]: docs/KubernetesDashboardQuickStart.md
[ldapauthn]: docs/LdapAuthentication.md
[oauthguide]: docs/OAuthGuide.md
[relnotes]: docs/ReleaseNotes.md
[security]: docs/Security.md
[supportedsql]: docs/SupportedSQL.md
[tableauwdc]: docs/TableauWDC.md
[webui]: docs/WebUI.md

