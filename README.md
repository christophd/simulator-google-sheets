Google Sheets Simulator ![Logo][1]
================

This is a standalone simulator application for [Google sheets v4 REST API](https://developers.google.com/sheets/api/reference/rest/).
The simulator is a normal Spring Boot web application and can be started as standalone server with embedded Tomcat application server. 
Its simulation capabilities are based on the [Citrus][2] framework and primarily implemented to fit the [Syndesis](https://github.com/syndesisio/syndesis) project requirements. 

Read the basic Citrus simulator [user manual](https://citrusframework.org/citrus-simulator/) for general information.

Clients are able to access the following simulator endpoints:

* POST /v4/spreadsheets
* GET /v4/spreadsheets/{spreadsheetId}
* PUT /v4/spreadsheets/{spreadsheetId}/values/{range}
* GET /v4/spreadsheets/{spreadsheetId}/values/{range}
* GET /v4/spreadsheets/{spreadsheetId}/values:batchGet
* POST /v4/spreadsheets/{spreadsheetId}/values/{range}:append
* POST /v4/spreadsheets/{spreadsheetId}/values/{range}:clear
 
When the simulator receives a new request on of these endpoints the simulator will respond with a valid response in respect to the Google spreadsheet v4 API. The scenarios are designed to
respond with proper test data according to the request information. This means that spreadsheetId, range, majorDimension identifiers will be mapped to the response.

In addition to these generic scenarios you can add your very customized and specific scenario that provides a proper response to your request.

Request mapping
---------

First of all the simulator identifies the simulator scenario based on a mapping key that is extracted from the incoming request. Based
on that operation key the respective simulator scenario is executed for providing a proper response message.

The simulator identifies the simulator scenario based on following mapping keys on incoming request messages:

* Spreadsheet id: Scenarios that map to a very specific spreadsheet id are mapped first. 
  The spreadsheet id is extracted from the request uri path parameters (e.g. /v4/spreadsheets/**{spreadsheetId}**/values:batchGet)
* Spreadsheet title: Each request payload is evaluated with a JsonPath expression (`$.properties.title`) which maps to scenarios that are using the sheet title as mapping key.
* Request mapping annotations: Identifies the scenario based on Http method and resource path on server 
  (e.g. `@RequestMapping(value = "/v4/spreadsheets/{spreadsheetId}/values/{range}", method = RequestMethod.GET)`). These are the generic scenarios that are mapped in case no other mapping strategy has
  successfully mapped a scenario to execute.
* Default scenario: In case no of the above mapping strategies worked a default scenario is executed. The default scenario is returning a `Http 500 - Internal Server Error`.  

Once the simulator scenario is identified the respective test logic builder is executed. The scenario is able to validate the incoming request and extrac more information from the request data (e.g. headers, path parameters etc.).
The response messages is able to reference dynamic values from the request and the simulator is able to perform complex response generating logic. 

Each scenario may execute more complex steps in order to provide proper simulation behavior.

Quick start
---------

You can build the simulator application locally with Maven:

```
mvn clean install
```

This will compile and package all resources for you. After that you are able to run the simulator on your local system with:

```
mvn spring-boot:run
```

Open your browser and point to [http://localhost:8443](http://localhost:8443). You will see the simulator user interface with all available scenarios and 
latest activities. 

You can execute some predefined Citrus integration tests now in order to get some interaction with the simulator. Open the Maven project in your favorite IDE and
run the tests in `src/test/java`. You should see the tests calling operations on the simulator in order to receive proper responses. The simulator user interface should track those
interactions accordingly.

You can also run the tests from command line with:

```
mvn verify -Pintegration
```

Simulator UI
---------

The simulator provides a small web user interface that provides some information on the latest activities such as executed scenarios and exchanged messages. You can access the UI via browser pointing to:

```
https://localhost:8443
```

Google sheets client adjustments
---------

By default the Google sheets client library connects to the production endpoint [https://sheets.googleapis.com/](https://sheets.googleapis.com/). In order to use the simulator with
your software components we need to adjust the root URL in your system under test so your software components connect to the local simulator instead of the productive API. 
You can do this with some configuration on the Google Sheets client builder API:

```java
JsonFactory jsonFactory = new JacksonFactory();
HttpTransport transport = new NetHttpTransport.Builder()
                                              .doNotValidateCertificate()
                                              .build();

Credential credential = new GoogleCredential.Builder()
        .setJsonFactory(jsonFactory)
        .setTransport(transport)
        .setClientSecrets(clientId, clientSecret)
        .build();

credential.setAccessToken("cd887efc-7c7d-4e8e-9580-f7502123badf");
credential.setRefreshToken("bdbbe5ec-6081-4c6c-8974-9c4abfc0fdcc");

Sheets clientBuilder = new Sheets.Builder(transport, jsonFactory, credential)
                                         .setRootUrl("https://localhost:8443/")
                                         .build();
```

The listing above describes what you have to do in order to connect to the simulator which is obviously running on the same machine on port `8443` in this case.

First fo all we create a proper credential that is able to authenticate with the simulator server. The simulator uses SSL by default and supports OAuth2 authentication mechanism. 

So in terms of SSL certificates the simulator ships a self-signed certificate that must be trusted in your client application. The most straight forward way to trust the certificate is
to disable the certificate validation on the client. You can do this on the `NetHttpTransport` as shown in the sample above:

```java
HttpTransport transport = new NetHttpTransport.Builder()
                                              .doNotValidateCertificate()
                                              .build();
``` 

Of course this option should only be used in testing environments. You should not use this option in production environments!

You can also explicitly trust the simulator certificate in your `NetHttpTransport` as follows:

```java
HttpTransport transport = new NetHttpTransport.Builder()
                                              .trustCertificatesFromJavaKeyStore(
                                                      getClass().getResourceAsStream("google-simulator.jks"),
                                                      "keyStorePassword")
                                              .build();
``` 

You can find the key store and password in the simulator application properties:

```properties
# The path to the keystore containing the certificate
server.ssl.key-store=classpath:io/syndesis/simulator/google-simulator.jks
server.ssl.key-store-provider=SUN
server.ssl.key-store-type=JKS
server.ssl.key-store-password=secret
server.ssl.key-password=secret
server.ssl.key-alias=googleapis
```

Next thing to set up is the access and refresh token for the OAuth2 authentication. The simulator provides some default access tokens that do not loose validity. You can use those tokens in order
to get most convenient access.

You can find/adjust the default tokens in the application properties:

```properties
simulator.oauth2.client.accessToken=cd887efc-7c7d-4e8e-9580-f7502123badf
simulator.oauth2.client.refreshToken=bdbbe5ec-6081-4c6c-8974-9c4abfc0fdcc
``` 

Finally we set the root URL of the client library to point to the local simulator instance running on the very same machine on port `8443`.

```java
Sheets clientBuilder = new Sheets.Builder(transport, jsonFactory, credential)
                                         .setRootUrl("https://localhost:8443/")
                                         .setApplicationName(applicationName)
                                         .build();
```

OAuth2 authentication
---------

By default the simulator requires OAuth2 authentication. This is to make developer experience as close to the real Google Sheets API as possible. The simulator provides some predefined
access and refresh tokens that are valid for clients to authenticate. These default tokens are the most comfortable way to get access to the simulator.

However you can also get new access tokens via following grant types:

* client_credentials via `/oauth/token` and clientId and clientSecret
* password via `/oauth/token` and username password
* refresh_token via `/oauth/token` and valid refresh token
* authorization_code via `/oauth/authorize` and `/oauth/token`

In addition to that the simulator provides check token endpoints via `/oauth/check_token`. You can see running samples in the
predefined integration tests in `src/testjava/` in test `io.syndesis.simulator.SimulatorOAuthSecurityIT`.

Run as Docker container
---------

You can run the simulator application as a container in Docker. First of all you need to build the Docker images with:

```
mvn docker:build
```

After that you are ready to run the image in Docker with:

```
mvn docker:start
```

This starts up a new container with the simulator application running in Docker.

Run in Openshift
---------

The simulator application is able to run as POD in a Kubernetes environment such as Openshift. Most easy way to setup a local Openshift cluster is to follow the instructions for [Minishift]() 
which is a single node cluster on your local machine.

When running in Openshift be sure to login to the project via `oc` CLI as next step. You can copy the login command from the upper right drop down menu when viewing the Openshift console in your browser.

You can open the Openshift console with Minishift in your browser using this command:

```
minishift console
```

The login command looks like this:

```
oc login https://192.168.64.10:8443 --token=[token]
```

Ip address and token may differ according to your local installation. As an alternative you can login with username password credentials:

```
oc login -u developer
```

Now everything is ready for the simulator deployment to your local Minishfit Openshift cluster. By default the simulator is built and deployed in a project named `syndesis`. You can adjust this project name in the Maven
POM configuration in the properties section:

```
<openshift.namespace>syndesis</openshift.namespace>
```

Now let's finally build the simulator image on the cluster.

```
mvn package fabric8:build -Pfabric8
``` 

This `fabric8:build` command is automatically triggering a new build on the Openshift cluster. You can review the build in the Openshift web console, too. 

After that we are ready to deploy.

```
mvn fabric8:deploy -Pfabric8
```

This step will create all resources in Openshift needed to run the simulator as POD in your local cluster. The Maven command uses a profile `fabric8` which will automatically read some cluster information
and create some build properties on the fly. These properties are:

* openshift.domain (something like `192.168.64.10.nip.io`)
* openshift.registry (something like `172.30.1.1:5000`)

You can also leave out the profile and set the properties explicitly on the deploy command:

```
mvn fabric8:deploy -Dopeshift.domain=myopenshift.domain.io -Dopenshift.registry=172.30.1.1:5000
```

This performs the complete deployment in your local Openshift cluster. After that you see new deployment configs, services, pod and route in the Openshift web console. You can access the simulator in your browser via:

```
https://simulator-google-sheets.192.168.64.10.nip.io
```

The IP address may be different on your installation. You can see the simulator URL via Openshift console, too.

Information
---------

Read the [user manual](https://citrusframework.org/citrus-simulator/) for detailed instructions and features.
For more information on Citrus see [citrusframework.org][2], including a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
