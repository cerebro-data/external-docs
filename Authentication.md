# Cerebro Data Access Service Authentication

All Cerebro services are authenticated. We provide two mechanism for authentication:

1. Kerberos
2. Cerebro Tokens

Kerberos authentication is required and is used to bootstrap the token mechanism. Token
based authentication is optional, depending on the needs of the client application. We
recommend using Kerberos authentication if possible (e.g. Hadoop integration) and
using token based authentication for non-kerberized clients (e.g. python).

Note that to get a token, you are required to first connect with a kerberized connection.
You cannot get a user token otherwise.

## Enabling authentication
Authentication is enabled by setting CEREBRO_KERBEROS_PRINCIPAL and
CEREBRO_KERBEROS_KEYTAB_FILE when starting Cerebro.

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

# For example, if the Cerebro service kereros principal is HTTP/cerero-service@REALM and the 
# server was running on 1.1.1.1 on port 7000, the connection string would be
$ curl --negotiate -u : --resolve cerebro-service:7000:1.1.1.1 http://cerebro-service:7000/api/health-authenticated
```

If using the requests-kerberos python library, this can be achieved by specyfing the
hostname_override override option. In this example, you would specify 'cerebro-service'
for the value.

## Token authentication
Tokens are suitable when accessing through a client that may not have have a kerberized
connection to CDAS. In this case, the user can request a token and make requests
using the token. CDAS will resolve the token to the user that originally requested it.

The token can be used to authenticate all calls to the REST server by additionally
providing it to the REST API.

### Getting a token
To get a token, call the get-token REST API. Note you must be kerberos authenticated.

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

## Using the token
To use the token, append '?user=<token>' to the request URL. For example, to scan data:
```shell
$ curl <CDAS_REST_HOST:PORT>/api/scan/<dataset>?user=<token>
```
Similarly, to get the databases:
```shell
$ curl <CDAS_REST_HOST:PORT>/api/databases?user=<token>
```

### Canceling the token
To cancel the token, call this REST API. Note this requires kerberos credentials and
you can only cancel your own tokens.
```shell
$ curl --negotiate -u : -X DELETE <CDAS_REST_HOST:PORT>/api/cancel-token/<token>
```

