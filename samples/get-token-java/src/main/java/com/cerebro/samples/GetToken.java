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
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
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
 * The user can login using:
 *   - User already has ticket, set via KRB5CCNAME in the environment to the cache file.
 *   - User logs in with their KDC password.
 */
public class GetToken {
  public static final class KerberosHttpClient {
    private final boolean debug;
    private Subject subject;

    public KerberosHttpClient(String user, String krb5ConfFile, String keytabFile,
        boolean debug) throws LoginException {
      this.debug = debug;
      if (debug) {
        System.setProperty("sun.security.spnego.debug", "true");
        System.setProperty("sun.security.krb5.debug", "true");
      }
      if (krb5ConfFile != null) {
        System.setProperty("java.security.krb5.conf", krb5ConfFile);
      }
      login(user, keytabFile);
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
     * Gets the kerberos credentials for the user.
     */
    private void login(final String user, final String keytab) throws LoginException {
      Configuration config = new Configuration() {
        @SuppressWarnings("serial")
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
          Map<String, String> configs = new HashMap<String, String>();
          String ticketCache = System.getenv("KRB5CCNAME");
          if (keytab != null) {
            configs.put("keyTab", keytab);
            configs.put("principal", user);
            configs.put("useKeyTab", "true");
            configs.put("storeKey", "true");
            configs.put("doNotPrompt", "true");
            configs.put("useTicketCache", "true");
            configs.put("renewTGT", "true");
          } else if (ticketCache != null) {
            configs.put("ticketCache", ticketCache);
            configs.put("renewTGT", "true");
            configs.put("useTicketCache", "true");
            configs.put("doNotPrompt", "true");
          } else {
            configs.put("principal", user);
            configs.put("doNotPrompt", "false");
            configs.put("useTicketCache", "false");
          }

          if (debug) {
            configs.put("debug", "true");
          }

          return new AppConfigurationEntry[] {
            new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, configs)
          };
        }
      };

      Set<Principal> princ = new HashSet<Principal>(1);
      princ.add(new KerberosPrincipal(user));
      Subject sub = new Subject(false, princ, new HashSet<Object>(), new HashSet<Object>());
      LoginContext lc = new LoginContext("", sub, new CallbackHandler() {
        @Override
        public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
          for (Callback cb: callbacks) {
            if (cb instanceof PasswordCallback) {
              PasswordCallback pcb = (PasswordCallback)cb;
              pcb.setPassword(System.console().readPassword("%s's password: ", user));
            }
          }
        }
      }, config);
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
    if (args.length < 2 || args.length > 5) {
      System.err.println(
          "Usage GetToken <host:port> <username> [krb5 conf] [debug] [keytab]");
      System.exit(1);
    }

    final String URL = "http://" + args[0] + "/api/";
    final String USER = args[1];
    final String CONF = args.length >= 3 ? args[2] : null;
    final boolean DEBUG = args.length >= 4 && args[3].equalsIgnoreCase("debug");
    final String KEYTAB = args.length >= 5 ? args[4] : null;

    KerberosHttpClient client = new KerberosHttpClient(USER, CONF, KEYTAB, DEBUG);

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