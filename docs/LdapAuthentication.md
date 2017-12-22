# Secure Cerebro REST API Access using LDAP

Cerebro now supports LDAP Basic Authentication. This document explains how to allow
Cerebro users to authenticate using their Active Directory credentials using Basic
authentication.

## LDAP Basic Authentication

In this section we will cover:

- [Prerequisites to enable LDAP Basic Authentication on Cerebro](#prerequisites-to-enable-ldap-basic-authentication-on-cerebro)
- [Setting up LDAP related configurations](#setting-up-ldap-related-configurations)
- [Verify LDAP Basic Authentication using curl](#verify-ldap-basic-authentication-using-curl)
- [Use the Web UI to authenticate and get a Cerebro token](#use-the-web-ui-to-authenticate-and-get-cerebro-token)
- [Exploring LDAP related logs](#exploring-ldap-related-logs)

### Prerequisites to enable LDAP Basic authentication on Cerebro

You will need the following pieces of information for CDAS to support LDAP:

- **Hostname**
This must be the IP address or the hostname of the Active Directory server
instance you are using.
- **Port Number**
This is the port number on which the Active Directory instance is listening. If none is
specified the This default, `389`, is used.
- **Base DN**
This refers to the Base Distinguished Name used by the AD server to look for users.

### Setting up LDAP related configurations

To enable LDAP Basic authentication for CDAS access, set the following environment
variables in your `/etc/cerebro/env.sh` file:

- **CEREBRO_LDAP_HOST**
eg. `example.com` or `127.0.0.4`.
- **CEREBRO_LDAP_PORT**
This has a **default value of 389** if not set.
- **CEREBRO_LDAP_BASE_DN**
**Please note:** Cerebro accepts the base DN in a particular notation as described here.
If your base DN is of the format `dc=example,dc=com`, the value to be passed in should
be `example.com`

For clarity, in your `/etc/cerebro/env.sh` file, the following variables need to be
uncommented and set:

```
# Set to enable ldap basic auth
# export CEREBRO_LDAP_HOST=YOUR_AD_HOSTNAME
# export CEREBRO_LDAP_PORT=YOUR_AD_PORT
# export CEREBRO_LDAP_BASE_DN=YOUR_AD_BASE_DN
```

For example, for an LDAP Host at `example.com:324`, and Base DN of `dc=example,dc=com`,
the LDAP related variables will look like:

```
# Set to enable ldap basic auth
export CEREBRO_LDAP_HOST=example.com
export CEREBRO_LDAP_PORT=324
export CEREBRO_LDAP_BASE_DN=example.com
```

### Verify LDAP Basic authentication using curl

Before we begin this section, make sure you have a set of LDAP credentials that you
intend to use for authentication:

- **Check the LDAP connection using CURL**:

  The following curl command should return the authenticated client name

    ```shell
    $ curl <CDAS_REST_SERVER>/api/health-authenticated -u <AD_USERNAME>
    Enter host password for user '<AD_USERNAME>':
    {
      "health": "ok",
      "token": null,
      "user": "<AD_USERNAME>"
    }

    # An example for Catalog REST server running at localhost:5000, and AD username
    # "testuser" is as follows
    $ curl localhost:5000/api/health-authenticated -u testuser
    Enter host password for user 'testuser':
    {
      "health": "ok",
      "token": null,
      "user": "testuser"
    }
    ```

- **Authenticate user and get token using CURL**:

  The following curl command should return a Cerebro token on successful authentication

    ```shell
    $ curl -X POST <CDAS_REST_SERVER>/api/get-token -u <AD_USERNAME>
    Enter host password for user '<AD_USERNAME>':
    {
      "token": "<Token>"
    }

    # An example for Catalog REST server running at localhost:5000, and AD username
    # "testuser" is as follows
    $ curl -X POST localhost:5000/api/get-token -u testuser
    Enter host password for user 'testuser':
    {
      "token": "AAdzYXVyYWJoAAdzYXVyYWJoigFcIb5C_IoBXEXKxvwBAg$$.xvm3yXp3_CZX377y0BqFVBmjKIY$"
    }
    ```

### Use the Web UI to authenticate and get a Cerebro token

It is now possible to get a Cerebro token using the Cerebro Web UI. Point your browser to
the login page in the Web UI and enter your credentials.

### Exploring LDAP related logs

To verify if the LDAP related values are set as intended, and for LDAP related debugging,
look for the following logs in your Catalog Admin container. You should see the following
lines in your Catalog REST server logs:

```shell
# the general format of expected logs
LDAP configurations initialized
LDAP Server: <LDAP_HOST>:<LDAP_PORT>
LDAP Server Base DN: <base DN in 'dc=x' format>

# assuming your LDAP server is at 127.0.0.4:389 and base DN is "example.com"
LDAP configurations initialized
LDAP Server: 127.0.0.4:389
LDAP Server Base DN: dc=example,dc=com
```

If the above lines are not present, your Catalog instance did not start with LDAP Basic
authentication enabled.
