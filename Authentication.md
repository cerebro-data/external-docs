# Cerebro Data Access Service Authentication

All Cerebro services are authenticated. We provide two mechanisms for authentication:

1. Kerberos & Cerebro Tokens
2. JSON Web Tokens (JWT)

Users can configure either or both options. If both are configured, clients can
authenticate using either method.

# Cerebro Tokens
Kerberos authentication is required and is used to bootstrap Cerebro tokens. Token
based authentication is optional, depending on the needs of the client application. We
recommend using Kerberos authentication if possible (e.g. Hadoop integration) and
using token based authentication for non-kerberized clients (e.g. python).

Note that to get a Cerebro token, you are required to first connect with a kerberized
connection. You cannot get a user token otherwise.

# JWT Tokens

Cerebro can use JSON Web Tokens (JWT) for authentication.
These tokens can either be generated externally and provided to CDAS, or CDAS can be
configured to work with an external service (via REST) to acquire and validate JWTs.
If only JWTs are used for authentication, CDAS will also require that a system token be
generated for it to use to authenticate internal services. This would, for example,
be a token with 'cerebro' as the subject.


# Enabling authentication
Kerberos authentication is enabled by setting CEREBRO_KERBEROS_PRINCIPAL and
CEREBRO_KERBEROS_KEYTAB_FILE when starting Cerebro.

Using JWT keys for service authentication is configured by setting SYSTEM_TOKEN
when starting Cerebro.

## Kerberos
We assume that the user already has a ticket granting ticket (i.e. has run kinit) before
any of these commands. You can verify with klist.

```shell
$ klist
Credentials cache: API:4A5E4713-9A7F-4010-8845-BE1B2700981A
        Principal: YOU@REALM

  Issued                Expires               Principal
Feb 28 11:32:44 2017  Feb 28 21:32:42 2017  krbtgt/REALM
```

### Hadoop integration
With all Hadoop analytics tools, clients will need to configure
'recordservice.kerberos.principal' to be the value of CEREBRO_KERBEROS_PRINCIPAL in their
client configs. This can be set in mapred-site.xml or yarn-site.xml.

## REST client integration
There are multiple clients that can be used to call the REST API. We demonstrate
in this document using curl as it is very available but we recommend using a language
specific library if possible For example, for python, we recommend using the
[request_kerberos package](https://github.com/requests/requests-kerberos).


### Getting a ticket
To connect from curl, add the option '--negotiate -u :' to the command. For example:
```shell
$ curl --negotiate -u : CDAS_REST_SERVER_HOST:PORT/api/health-authenticated
{
  "health": "ok",
  "token": null,
  "user": "YOU@REALM"
}
```

## Kerberos principals
Many REST clients, including curl, assume the REST server's kerberos principal to
be: HTTP/<ip/dns name of server>@REALM. Cerebro does not have this requirement and
can use any service name and host as the principal. If you have not configured
Cerebro to have the expected principal, you will need to specify additional
arguments to curl. The command above should instead be:
```
$ curl --negotiate -u : --resolve <CEREBRO_PRINCIPAL_SERVICE_HOST>:PORT:<IP_ADDRESS_OF_REST_SERVER> http://<CEREBRO_PRINCIPAL_SERVICE_HOST>/api/health-authenticated

# For example, if the Cerebro service kerberos principal is HTTP/cerebro-service@REALM and
# the server was running on 1.1.1.1 on port 7000, the connection string would be
$ curl --negotiate -u : --resolve cerebro-service:7000:1.1.1.1 http://cerebro-service:7000/api/health-authenticated
```

If using the requests-kerberos python library, this can be achieved by specifying the
hostname_override override option. In this example, you would specify 'cerebro-service'
for the value.

## Cerebro token authentication
Tokens are suitable when accessing through a client that may not have have a kerberized
connection to CDAS. In this case, the user can request a token and make requests
using the token. CDAS will resolve the token to the user that originally requested it.

The token can be used to authenticate all calls to the REST server by additionally
providing it to the REST API.

### Getting a Cerebro token
To get a Cerebro token, call the get-token REST API. Note you must be kerberos
authenticated.

```shell
$ curl --negotiate -u : -X POST <CDAS REST HOST:PORT>/api/get-token
{
  "token": "AARub25nABFub25nQENFUkVCUk8uVEVTVIoBWoZGWxKKAVqqUt8SAQI$.pklsqRlTrFFyEPSHVjItxqBrZ28$"
}
```

You can verify the token with (this does not need a kerberized connection):
```shell
$ curl <CDAS REST HOST:PORT>/api/get-user/AARub25nABFub25nQENFUkVCUk8uVEVTVIoBWoZGWxKKAVqqUt8SAQI$.pklsqRlTrFFyEPSHVjItxqBrZ28$
{
  "user": "<YOU>"
}
```

### Canceling a Cerebro token
To cancel the token, call this REST API. Note this requires kerberos credentials and
you can only cancel your own tokens.
```shell
$ curl --negotiate -u : -X DELETE <CDAS_REST_HOST:PORT>/api/cancel-token/<token>
```


## Using the token
To use the token, append '?user=<token>' to the request URL. For example, to scan data:
```shell
$ curl <CDAS_REST_HOST:PORT>/api/scan/<dataset>?user=<token>
```

Similarly, to get the databases:
```shell
$ curl <CDAS_REST_HOST:PORT>/api/databases?user=<token>
```


## JWT
JWTs can be used in two different ways: 1) by providing the services with both the public
key used to sign the tokens and the algorithm that was used (RSA256, RSA512, etc.) or
2) by configuring two remote endpoints, one for acquiring tokens and another for
validating tokens.

For either of these approaches, if you are also using JWT for authenticating communication
between services, generate a token with the subject "cerebro" that can be read by the
method that you setup.

example:
export SYSTEM_TOKEN=/etc/cerebro.token

### Public Key Approach
To configure the public key, the environment variable JWT_PUBLIC_KEY should be a full path
to the public key. NOTE: this key must be in openssl PKCS#8 format. To configure the
algorithm, the environment variable JWT_ALGORITHM should be set to a string indicating
the algorithm used. Currently support algorithms are "RSA256", "RSA512".

example:
export JWT_PUBLIC_KEY=/etc/id_rsa.512.pub
export JWT_ALGORITHM=RSA512

### External Endpoint Approach
To configure the external service approach, you will need to configure an endpoint for
validating tokens via the JWT_AUTHENTICATION_SERVER_URL configuration.
Optionally, if you want users to be able to acquire tokens (via the 'dbcli get-token'
command, for example), you can configure an endpoint that will accept a REST
request with the following fields in the body: "username", "password"
The configuration for the external token-granting endpoint is SSO_URL.

example:
export JWT_AUTHENTICATION_SERVER_URL=http://10.1.11.153:8900/idp/userinfo.openid
export SSO_URL=http://10.1.11.153:8900/as/token.oauth2

## Using a JWT with curl
To list the databases via curl, the JWT can be passed in the authorization header.

```shell
$ curl <CDAS_REST_HOST:PORT>/api/databases -H 'authorization: Bearer <token>'
```
