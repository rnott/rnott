![rnott-mock](../images/mock.png)

A mock service project that can be used for both stress/performance and unit/feature testing use cases. The service is a Jetty server instance that allows endpoints and potential responses to be configured.

## Configuration

### Example
In the example below, a hypothetical service is configured to have a single endpoint `/path/to/get/something` that can respond
with one of three different responses.

```json
[
	{
		"uri": "/path/to/get/something",
		"method": "GET",
		"status": 200,
		"delay": 0,
		"response": [
			{
				"percentile": 98,
				"delay": 700,
				"headers": {
					"Cache-Control": "private",
					"Content-Type": "text/json; charset=utf-8",
					"Server": "192.168.1.1",
					"X-Powered-By": "Jetty 9.2",
					"Date": "${date.now('EEE, dd MMM yyyy HH:mm:ss z')}",
					"Connection": "Keep-Alive"
				},
				"body":"file:/Users/me/Documents/test/sample.txt"
			},
			{
				"percentile": 1,
				"delay": 100,
				"status":500,
				"headers": {
					"Cache-Control": "private",
					"Content-Type": "text/html; charset=utf-8",
					"Server": "192.168.1.1",
					"X-Powered-By": "Jetty 9.2",
					"Date": "${date.now('EEE, dd MMM yyyy HH:mm:ss z')}",
					"Connection": "close"
				},
				"body": "<html><body><div>Status: failed Message: Testing service errors</div></body></html>"
			},
			{
				"percentile": 1,
				"delay": 5000,
				"headers": {
					"Cache-Control": "private",
					"Content-Type": "text/json; charset=utf-8",
					"Server": "",
					"X-Powered-By": "",
					"Date": "${date.now('EEE, dd MMM yyyy HH:mm:ss z')}",
					"Connection": "Keep-Alive"
				},
				"body": {"foo":"foo","bar":"bar", "foobar": ["foo","bar"]}
			}
		]
	}
]
```

## Stand-alone Execution
The mock service can be launched either from the Maven project or as an executable JAR file. All arguments begin with '--'. 
The following arguments are available:

*	config

	path to the service configuration file.
*	host

	the address the server will `bind` to, defaults to `127.0.0.1`.
*	port

	the port number the server will listen on, defaults to `8080`.
*	trace

	enable logging of incoming service requests, defaults to `false`
*	debug

	log verbose debugging information, defaults to `false`
*	captureEnabled

	enable capture of request/response information that can be queried later, defaults to `false`
	TODO: document capture query

### Executable JAR
This style of execution is suitable when you simply want to use the service. Download the JAR file and run Java from a shell:

>`$ java -jar rnott-mock-<version>.jar --config="<path-to-service-config>" --host=<interface> --port=<port-number> --trace=true|false --debug=true|false --captureEnabled=true|false`

### Maven
This style of execution targets the developer actively working on this project, as it enables an edit, compile, test, run workflow
with a single command. From the project base directory, run Maven from a shell:

>`$ mvn exec:java -Dexec.args="--config="<path-to-service-config>" --host=<interface> --port=<port-number> --trace=true|false --debug=true|false --captureEnabled=true|false"`

