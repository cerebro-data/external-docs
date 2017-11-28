# Secure Cerebro REST API Access using OAuth

The Cerebro REST API supports OAuth integration, primarily to enable easier Web UI
access. This document explains how to set up the REST API with OAuth to make the Web UI
login easier.

## OAuth Basics

In setting up the REST API with basic OAuth integration, the REST API will be able to
accept an OAuth code token and exchange it for a Cerebro token automatically. This
allows the Web UI to redirect a Cerebro user from the Cerebro Login Page to a
preconfigured OAuth service provider (e.g. Google, Ping, etc.), enabling the user to
login from the external OAuth service. Once logged in, the Web UI uses the CDAS REST
API to exchange the resulting OAuth token for a Cerebro token to gain access to the
Cerebro Web UI. The user can also then copy his or her token to use it for any Cerebro
integration point.

## Prerequisites to enable OAuth

You will need the following pieces of information about your OAuth service provider:

- **Client ID**
  This is the OAuth client ID that the service provider requires for authenticating
  against Cerebro. OAuth service providers allow you to create new clients; as part of
  this, you will obtain a Client ID.
- **Authentication URI**
  This is the URI that the OAuth service requires the user to be redirected to when
  requesting authentication. Typically, it renders an HTML login page for that service
  provider. Once the user has successfully interacted with this page, the user is
  redirect back to Cerebro with an ephemeral `access code`.
- **Token URI**
  This is the URI that Cerebro must use to exchange the `access code` for a service
  provider `token`. This exchange happens in the REST API.
- **Client Secret**
  This value is provided by the OAuth service provider as part of setting up a new
  client. It is required for Cerebro to establish trust when attempting the token
  exchange.
- **Redirect URIs**
  This is a list of URIs that will be recognized as acceptable URIs to navigate to
  once the Authentication URI flow is over. These need to be configured in the OAuth
  service provider in the client configuration. Typically, this is just one URI: the
  location of the Cerebro Web UI's login page, e.g. `https://my-cerebro-host:8083/login`.
- **Origin URIs**
  These URIs are the same as the Redirect URIs, without the `/login` part. For example
  `https://my-cerebro-host:8083`. These values also need to be configured in the OAuth
  service provider, so it will allow redirects from the Cerebro Web UI.
- **OAuth Scopes (Optional)**
  The scopes determine which information the OAuth service provider will allow the user
  to access once the token is obtained. Cerebro needs only very basic information, namely
  the subject (`sub`) in order to generate a Cerebro token. Typically, the default here
  (`openid profile email`). If there are other scopes required by your OAuth service
  provider to obtain the `sub`, you will need to set them.
- **Subject Endpoint (Optional)**
  In some deployments, there may be a need to not encode the subject (`sub`) in the JWT
  response from the OAuth token exchange. In this case, Cerebro needs to know the
  endpoint to call to retrieve the subject, which is the unique user ID that Cerebro
  can use to generate the token. This endpoint is expected to return a JSON response
  with `sub` being a key on the top-level JSON object, e.g. `{"sub": "abc123", ...}`.

### Setting up OAuth configurations

#### client_secrets.json

Once you have the above information, you will need to create a JSON file with this
information encoded in it. The JSON file has the following form:

```json
{
  "web": {
    "client_id": <Your Client ID>,
    "auth_uri": <Your Authentication URI>,
    "token_uri": <Your Token URI>,
    "client_secret": <Your Client Secret>,
    "redirect_uris": [<Your Redirect URIs>, ...],
    "javascript_origins": [<Your Origin URIs>, ...],
  }
}
```

For example, if Google OAuth were to be used, after setting up a new Google OAuth
project, we might have the following file:

```json
{
  "web": {
    "client_id": "example12345.apps.googleusercontent.com",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://accounts.google.com/o/oauth2/token",
    "client_secret": "very-secret",
    "redirect_uris": [
      "https://ec2-1234.compute.amazonaws.com:8083/login"
    ],
    "javascript_origins": [
      "https://ec2-1234.compute.amazonaws.com:8083"
    ]
  }
}
```

Save this file to `/etc/cerebro/client_secrets.json`.

#### Environment variables

Cerebro looks for certain environment variables to be set in order to enable OAuth. In
your `/etc/cerebro/env.sh` file, set the following environment variables:
 - **CEREBRO_OAUTH_SECRETS**, to `/etc/cerebro/client_secrets.json`, or wherever the
 `client_secrets.json` file is located. Setting this environment variable enables
 Cerebro OAuth.
 - **CEREBRO_OAUTH_SCOPES** (optional). Space-separated list of scopes required to
 obtain the `sub`. This is typically OK to leave unset, in which case the default is
 `"openid profile email"`.
 - **CEREBRO_OAUTH_SUB_ENDPOINT** (optional). URI of the endpoint where the JSON
 response with the `sub` is. This endpoint must also accept the OAuth token obtained
 from the token exchange. e.g. `https://user-data-endpoint:9999/api/openid.profile`

As an example, the OAuth part of your `env.sh` might look like this:

```shell
export CEREBRO_OAUTH_SECRETS="/etc/cerebro/client_secrets.json"
export CEREBRO_OAUTH_SCOPES="openid profile"
export CEREBRO_SUB_ENDPOINT="https://user-data-endpoint:9999/api/openid.profile"
```

### Verify OAuth

Once you've deployed Cerebro with OAuth enabled, verify that it's working properly.

- **Check the OAuth connection using CURL**:
  The following curl command should return the authentication URI (with client secrets as
  params) in the `oauth_url` section:

  ```shell
  $ curl <CDAS REST Server Host/Port>/api/info
  {
    ...,
    "oauth_url": "<your authentication URI plus client secrets params>"
  }
  ```

  For example:

  ```shell
  {
    "oauth_url": "https://accounts.google.com/o/oauth2/auth?scope=openid+profile+email&redirect_uri=.../login&response_type=code&client_id=abc123.apps.googleusercontent.com&access_type=offline"
  }
  ```

### Use the Web UI

Navigate to the Cerebro Web UI and there should be a new login option on the login page:
"Login with OAuth". Upon clicking it, the UI should redirect to your OAuth service
provider's login screen. Upon successful login, the UI should redirect back to the
Cerebro Web UI and log you in.

### Troubleshooting

If you encounter issues, look in the logs of the CDAS REST Server container, which will
typically contain information about what went wrong.

Additionally, verify the JSON validity of `client_secrets.json`. If it is not
well-formed, the CDAS REST Server cannot read it.

Finally, ensure Cerebro information is probably configured in the OAuth service
provider, e.g. Cerebro's Web UI host/port is configured as a valid redirect URI, the
scopes are valid, etc.