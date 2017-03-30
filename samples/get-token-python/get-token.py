#!/usr/bin/python

# Sample script to authenticate with REST server using python. This requires
# the requests_kerberos python package:
#   pip install requests_kerberos
#
# Usage:
# ./get-token.py --service-principal=<SERVICE PRINCIPAL> --hostport=<host:port>
# On OSX, you will also need to specify the user's principal
# ./get-token.py --service-principal=<SERVICE PRINCIPAL@REALM> --hostport=<host:port> --user-principal=<user@REALM>
#

from optparse import OptionParser
import requests
import sys

from requests_kerberos import HTTPKerberosAuth, OPTIONAL

parser = OptionParser()
parser.add_option("--service-principal", dest="service_principal",
  default="HTTP/service@CEREBRO.TEST",
  help="Principal of REST service.")
parser.add_option("--user-principal", dest="user_principal",
  default=None,
  help="Principal of authenticated user. This is typically just <user>@REALM")
parser.add_option("--hostport", dest="hostport", default="localhost:5000",
  help="Host:port of service to connect to .")
options, args = parser.parse_args()

def handle_response(url, response):
  if response.status_code != 200:
    print("REST call failed: " + response.text)
    print("URL: " + url)
    sys.exit(1)
  return response.json()

if __name__ == '__main__':
  service_principal = options.service_principal.replace('@', '/').split('/')
  if len(service_principal) != 3:
    print("Invalid service principal. Expecting <service-name>/<service-host>@<REALM>")
    sys.exit(1)

  server_name = options.hostport.split(':')[0]
  os.environ['NO_PROXY'] = server_name
  os.environ['no_proxy'] = server_name

  AUTH = HTTPKerberosAuth(mutual_authentication=OPTIONAL,
      service=service_principal[0], hostname_override=service_principal[1],
      principal=options.user_principal)

  try:
    print("Attempting unauthenticated health check...")
    url = 'http://' + options.hostport + '/api/health'
    health_response = requests.get(url)
    result = handle_response(url, health_response)
    print("  success: " + str(result))

    print("Attempting to authenticate...")
    url = 'http://' + options.hostport + '/api/health-authenticated'
    authenticate_response = requests.get(url, auth=AUTH)
    result = handle_response(url, authenticate_response)
    print("  success: " + str(result))

    print("Attempting to get token...")
    url = 'http://' + options.hostport + '/api/get-token'
    token_response = requests.post(url, auth=AUTH)
    result = handle_response(url, token_response)
    print("  success: " + str(result))
  except:
    print("Unable to connect to server: " + options.hostport)
