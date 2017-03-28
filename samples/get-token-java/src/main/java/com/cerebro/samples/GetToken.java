// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cerebro.samples;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Utility class that demonstrates how to authenticate with the REST API using kerberos.
 * This assumes that the user already has a ticket (i.e. has run kinit already).
 */
public class GetToken {
  public static final class KerberosHttpClient {
    private final boolean debug;
    private Subject subject;

    public KerberosHttpClient(String user, String krb5ConfFile, boolean debug)
        throws LoginException {
      this.debug = debug;
      if (debug) {
        System.setProperty("sun.security.spnego.debug", "true");
        System.setProperty("sun.security.krb5.debug", "true");
      }
      if (krb5ConfFile != null) {
        System.setProperty("java.security.krb5.conf", krb5ConfFile);
      }
      login(user);
    }

    /**
     * Calls the request, returning the response. Returns null if there was an IO error.
     */
    public HttpResponse call(final HttpUriRequest request)
        throws LoginException {
      return Subject.doAs(subject, new PrivilegedAction<HttpResponse>() {
        @Override
        public HttpResponse run() {
          try {
            HttpClient spnegoHttpClient = buildSpengoHttpClient();
            return spnegoHttpClient.execute(request);
          } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
          }
        }
      });
    }

    /**
     * Gets the kerberos credentials for the client (aka user). This assumes that
     * there is already a ticket with the user as the principal.
     */
    private void login(String user) throws LoginException {
      Configuration config = new Configuration() {
        @SuppressWarnings("serial")
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
          return new AppConfigurationEntry[] {
            new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
              AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>() {
                {
                  put("useTicketCache", "true");
                  put("doNotPrompt", "true");
                  put("isInitiator", "true");
                  put("debug", debug ? "true" : "false");
                }
              })
          };
        }
      };

      Set<Principal> princ = new HashSet<Principal>(1);
      princ.add(new KerberosPrincipal(user));
      Subject sub = new Subject(false, princ, new HashSet<Object>(), new HashSet<Object>());
      LoginContext lc = new LoginContext("", sub, null, config);
      lc.login();
      subject = lc.getSubject();
    }

    private HttpClient buildSpengoHttpClient() {
      HttpClientBuilder builder = HttpClientBuilder.create();
      Lookup<AuthSchemeProvider> authSchemeRegistry =
          RegistryBuilder.<AuthSchemeProvider>create().
              register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true, false)).build();
      builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
      BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
        @Override
        public Principal getUserPrincipal() {
          return null;
        }
        @Override
        public String getPassword() {
          return null;
        }
      });
      builder.setDefaultCredentialsProvider(credentialsProvider);
      return builder.build();
    }
  }

  public static void main(String[] args)
      throws UnsupportedOperationException, IOException, LoginException {
    if (args.length != 2) {
      System.err.println("Usaage GetToken <host:port> <username>");
      System.exit(1);
    }

    final boolean DEBUG = false;
    final String URL = "http://" + args[0] + "/api/";
    final String USER = args[1];
    KerberosHttpClient client = new KerberosHttpClient(USER, null, DEBUG);

    // Verify the unauthenticated health API works.
    System.out.println("Verifying unauthenticated health API...");
    HttpResponse health = client.call(new HttpGet(URL + "health"));
    System.out.println(IOUtils.toString(health.getEntity().getContent()));

    // Verify authenticated health-api works.
    System.out.println("Verifing authenticated health API...");
    HttpResponse health2 = client.call(new HttpGet(URL + "health-authenticated"));
    System.out.println(IOUtils.toString(health2.getEntity().getContent()));

    // Get user token
    System.out.println("Getting access token...");
    HttpResponse token = client.call(new HttpPost(URL + "get-token"));
    System.out.println(IOUtils.toString(token.getEntity().getContent()));
  }
}