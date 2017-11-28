# Kerberized Cluster Setup

This tutorial demonstrates how to set up a Kerberized Cerebro cluster that has end-to-end
authentication enabled. This process generally involves the following three steps:

- Create a keytab with two principals, one with the *service name* as **cerebro** and
another with the *service name* as **HTTP**.
- Set the keytab and principal configs in the environment.

The tutorial breaks the above steps into the following:

- [Prerequisites](#prerequisites)
- [Creation of the Kerberos principals and keytab files](#creation-of-the-kerberos-principals-and-keytab-files)
- [Setting up the credentials](#setting-up-the-credentials)

## Prerequisites

This section lists out the necessary requirements to enable Kerberos for Cerebro.

- **KDC**
  Make sure that you have a KDC set up and available, and can either request for
  credentials, or have access to the KDC to set up credentials yourself.
- **Ensure client side Kerberos packages are installed**
  Try running `kinit` and `klist` and make sure these utilities are installed.
- **Kerberos Configuration File**
  This file is known as the **krb5.conf** file and this contains the information
  needed to authenticate a client against a KDC.
  - Make sure that this file exists in the location **/etc/krb5.conf**.
  - Make sure that this file has the correct `default_realm` and `realm` information.

## Creation of the Kerberos principals and keytab files.

- In order to allow Cerebro's internal services to be authenticated end-to-end, we need
to create the following *2* Kerberos principals, and add them to **a single keytab file**:
    - **Cerebro principal**
    This is of the format `cerebro/<instance>@REALM`. For example, this can be
    `cerebro/cerebro-service@CEREBRO.TEST`
    - **HTTP principal**
    This is of the format `HTTP/<instance>@REALM`. For example, this can be
    `HTTP/cerebro-service@CEREBRO.TEST`
- To do this, log in with kadmin, and run the following command for each of the
principals that needs to be created: `addprinc -randkey <principal_name>`
- Once the principals are created, create a single keytab file which contains both
principals. To do this, log into the kadmin util, and run the following command:
`ktadd -kt <keytab_file_name> <principal_name_1> <principal_name_2>`

```shell
# Log into your kadmin tool
$ sudo kadmin.local
kadmin.local:

# Make sure the required principals don't exist by using the following command
kadmin.local: listprincs
prim1/inst1@CERBERO.TEST
prim2/inst2@CEREBRO.TEST
prim2/inst3@CERBERO.TEST
kadmin.local:

# Create the Cerebro principal.
# If you don't want a password, use the -randkey flag.
# If you would rather have a password, don't use the flag.
kadmin.local: addprinc -randkey cerebro/cerebro-service
WARNING: no policy specified for cerebro/cerebro-service@CEREBRO.TEST; defaulting to no policy
Principal "cerebro/cerebro-service@CEREBRO.TEST" created.
kadmin.local:

# Create the HTTP principal.
kadmin.local: addprinc -randkey HTTP/cerebro-service
WARNING: no policy specified for HTTP/cerebro-service@CEREBRO.TEST; defaulting to no policy
Principal "cerebro/cerebro-service@CEREBRO.TEST" created.
kadmin.local:

# Create the keytab file
kadmin.local: ktadd -kt cerebro.keytab cerebro/cerebro-service HTTP/cerebro-service
Entry for principal cerebro/cerebro-service with kvno 3, encryption type aes256-cts-hmac-sha1-96 added to keytab WRFILE:cerebro.keytab.
Entry for principal HTTP/cerebro-service with kvno 3, encryption type aes256-cts-hmac-sha1-96 added to keytab WRFILE:cerebro.keytab.
kadmin.local:

# Exit the kadmin util
kadmin.local: q
$
```

## Setting up the credentials

- **Ensuring the keytab file has appropriate permissions**
  Once the keytab file, has been created, look at the file permissions using
  `ls -l <KEYTAB_FILE_NAME>` Make sure everyone has read and execute privileges at least,
  and if not, do the following: `sudo chmod 755 <KEYTAB_FILE_NAME>`

- **Verifying the keytab file has both the principals**
  Once the keytab file has the proper permissions, verify that both the principals are
  present. The command to do that is: `$ klist -kt <KEYTAB_FILE_NAME>`

  For example, if the keytab file is called *cerebro.keytab*, the command looks like this:

    ```
    $ klist -kt cerebro.keytab
    Keytab name: FILE:dumdum.keytab
    KVNO Timestamp           Principal
    ---- ------------------- ------------------------------------------------------
      4 03/02/2017 13:27:57 cerebro/cerebro-service@CEREBRO.TEST
      4 03/02/2017 13:27:57 HTTP/cerebro-service@CEREBRO.TEST
    ```

- **Setting the environment variables**
  Set the following environment variables:
    - **CEREBRO_KERBEROS_PRINCIPAL**
      Set this to the Cerebro principal created above.
    - **CEREBRO_KERBEROS_KEYTAB_FILE**
      Set this to the path of the keytab file. This can be a local or remote (s3) path.
    - **CEREBRO_KERBEROS_HTTP_PRINCIPAL**
      Set this to the HTTP principal if the principal is non-standard. This is not
      required if the principals were created with the steps above.

    ```
    # shell
    export CEREBRO_KERBEROS_PRINCIPAL=<principal>
    export CEREBRO_KERBEROS_KEYTAB_FILE=FULL_PATH_TO_KEYTAB_FILE

    # Example:
    $ export CEREBERO_KERBEROS_PRINCIPAL=cerebro/cerebro-service
    $ export CEREBRO_KERBEROS_KEYTAB_FILE=cerebro.keytab

    # If changing the shell file, follow similar steps as above and then do the following:
    $ source /etc/cerebro/env.sh
    ```

## More information

Other docs that might be helpful are:

- [Authentication](Authentication.md)
- [Security](Security.md)
