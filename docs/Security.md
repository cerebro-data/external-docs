# Secure DeploymentManager REST API Access

This document explains how to enable Kerberos-based authentication and admin rights
authorization for the DeploymentManager REST APIs.

## Kerberos Authentication

In this section, we will cover:

- [Prerequisites to enable Kerberos on the DeploymentManager REST API server](#prerequisites-to-enable-kerberos-on-the-deploymentmanager)
- [Setting up the required configurations](#setting-up-the-required-configurations)
- [Verify Kerberos authentication using curl](#verifying-kerberos-authentication-using-curl)

### Prerequisites to enable Kerberos on the DeploymentManager

Before we begin this section, make sure that your organization has a KDC configured and
available as you will need to configure your SNP or *ServiceNamePrincipal* with the KDC.

**SNP:** The ServiceNamePrincipal is divided into three parts, explained below, in the
format of `primary/instance@<KDC_REALM>`:

- `primary` - This is typically HTTP.
- `instance` - Hostname for the DM REST API Server.
- `KDC_REALM` - The Kerberos realm. This is generally in upper-case letters.

#### Setting up the required configurations

To enable Kerberos on the DeploymentManager REST API Server, set the following two
environment variables:
- **DM_PRINCIPAL** - The SNP goes here. For example, if your DeploymentManager is
hosted at `http://hostname:8085`
  - DM_PRINCIPAL should be `HTTP/hostname@<KDC_REALM>`
  - *Please ensure* that the DM_PRINCIPAL is a 3-part Kerberos Principal and has the 3
  components: primary, instance and KDC_REALM.
- **DM_KEYTAB_FILE** - The absolute path to the Keytab file corresponding to the SNP
    and DM_PRINCIPAL specified above.

Both of these environment variables must be properly configured to enable Kerberos for
your DeploymentManager REST API Server. In case the keytab file specified is not present
at the location, or only one of the configurations is present, an appropriate error
message will be returned.

In order to run the DeploymentManager with Kerberos authentication disabled, make sure
neither of these variables are set before starting the DeploymentManager.

In addition, make sure **/etc/krb5.conf** has the correct `default_realm` and `realm`
information.

#### Verifying Kerberos state at startup

When you start the DeploymentManager with Kerberos enabled, you should see the following
line in the logs:

```
Deployment Manager REST API Server running with Kerberos enabled
```

When you start the DeploymentManager with Kerberos disabled, you should see the following
line in the logs:

```
Deployment Manager REST API Server running with Kerberos disabled
```

### Verifying Kerberos authentication using curl

Once you have the DeploymentManager running with Kerberos, follow the steps below to
verify Kerberos authentication:

- **Obtain a TGT:**

  Make sure you have a valid TGT or *ticket-granting ticket* for the client principal
  you intend to use for authentication. The way to do this is using the `kinit` command.
  For example, for the client principal `john@<KDC_REALM>` that you intend to use for
  authentication, do the following:

  **If you know the password for the principal, the command is:**

  ```shell
  $ kinit <principal_name>
  ```

  *Example*:

  ```shell
  $ kinit john
  ```

  **If you have the keytab for the principal, the command is:**

  ```shell
  $ kinit -kt <path_to_keytab> <principal_name>
  ```

  *Example*:

  ```shell
  $ kinit -kt /Users/john/john.keytab john
  ```

- **Run curl commands:**

  The following curl command should return the client name:

  ```shell
  $ curl --negotiate -u : <HOSTNAME>:<PORT>/api/kerberos/client
  ```

  *Example*

  ```shell
  $ curl --negotiate -u : hostname:8085/api/kerberos/client

  Authenticated client name is: john
  ```

  If the output is the client name expected, Kerberos authentication setup for
  DeploymentManager REST API Server is now complete.

## DM Administrator authorization

Currently the DeploymentManager REST API Server supports an administrator role. In this
section we will cover the following:

- [Setting up users and groups with administrator privileges](#setting-up-users-and-groups-with-administrator-privileges)
- [Verifying administrator authorization using curl](#verifying-admin-authorization-using-curl)
- [Authorization of authenticated vs unauthenticated clients](#authorization-of-authenticated-vs-unauthenticated-clients)

### Setting up users and groups with administrator privileges

The DeploymentManager requires the following environment variable to be set, in order to
enable Administrator authorization on the DeploymentManager REST API server:

- **DM_ADMIN_USERS** - The names of all individual users and Unix groups that have
administrator privileges to the DeploymentManager go here as a CSV. For example, if the
user *john* is an admin, and all users in the group *sysadmins* are admins, the following
value should be set: ```john,sysadmins```

### Verifying admin authorization using curl

- **Curl commands for a server running with Kerberos enabled:**

  - Once the DeploymentManager is running with DM_ADMIN_USERS set, follow the steps
  [above](#verifying-kerberos-authentication-using-curl) to `kinit` and get a *TGT* for
  the client.
  - The following curl commands should return the client's admin authorization status:

    ```shell
    # This REST API is for Kerberos authenticated clients only
    $ curl --negotiate -u : hostname:8085/api/kerberos/isAdmin

    true
    ```

    ```shell
    # The general API endpoint for authorization
    # Assuming kinit john on client
    $ curl -H "Content-Type: application/json" -X POST -d '{"user":"sally","token":""}' --negotiate -u : hostname:8085/api/system/isAdmin

    john is admin
    ```

  - For the call to `/api/system/isAdmin`, if the server is Kerberized, we check for an
  authenticated client principal, irrespective of the user supplied in the POST request.

- **Curl commands for server running with Kerberos disabled:**

  - The following curl command should return the client's admin authorization status:

    ```shell
    $ curl -H "Content-Type: application/json" -X POST -d '{"user":"john","token":"xyz"}' --negotiate -u : hostname:8085/api/system/isAdmin

    john is admin
    ```

### Authorization of authenticated vs. unauthenticated clients.

Keep in mind the following points:

- Authentication and authorization are independent.
- If the client is Kerberos authenticated, then the Kerberos principal used by the client
  is used to authorize the user as an Admin,
  **irrespective of the user information stored in the request.**
- If the client is not Kerberos authenticated, the user information provided in the
  request is used to authorize the client.
- In either case, **the user being authorized must be a valid Unix user.** If this is not
  the case, an appropriate error message will be returned.
