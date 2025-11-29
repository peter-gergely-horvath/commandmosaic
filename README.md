# CommandMosaic

**A Java framework for building services using the Command pattern**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Why CommandMosaic?

While REST APIs work well for resource-oriented operations, modeling every business operation as CRUD (Create, Read, Update, Delete) can become challenging with complex domain logic:

- **Domain mismatch**: Not all operations map to CRUD. A booking system needs `CancelBooking`, not `DELETE /booking` – the booking must remain for audit purposes with a compensating entry.
- **Boilerplate overhead**: Each new feature requires creating controllers, defining routes, writing delegation logic, and updating API documentation.
- **Testing complexity**: Integration testing serverless functions requires vendor-specific tooling and complex setup.

CommandMosaic takes a different approach: **one dispatch endpoint, many commands**.

## What You Get

- **Single entry point**: One API endpoint handles all operations. Add features by implementing commands – no routing, no controllers, no configuration changes.
- **Write once, run anywhere**: Same code runs in plain Java, servlets, Spring Boot, or AWS Lambda. Test Lambda functions with plain JUnit.
- **Built-in security**: Declarative, annotation-based access control. Secure commands individually without touching infrastructure.
- **Spring-native**: Commands are Spring beans with full support for `@Autowired`, transactions, and the entire Spring ecosystem.
- **Minimal boilerplate**: Focus on business logic, not plumbing.

## Quick Example

### Define a command

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessPayment implements Command<PaymentResult> {

    @Autowired
    private PaymentService paymentService;

    @Parameter
    private String orderId;
    
    @Parameter
    private BigDecimal amount;

    @Override
    public PaymentResult execute(CommandContext context) {
        return paymentService.processPayment(orderId, amount);
    }
}
```

### Invoke it

**From Java code:**
```java
commandDispatcher.dispatchCommand(new ProcessPayment(orderId, amount), context);
```

**From a REST client:**
```bash
curl -X POST https://api.example.com/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "command": "ProcessPayment",
    "parameters": {
      "orderId": "ORD-123",
      "amount": 99.99
    },
    "protocol": "CM/1.0"
  }'
```

That's it. No routes to define, no controllers to write, no API Gateway configuration to update.

## Architecture

CommandMosaic separates **what** to execute from **how** it's invoked:

```
Client Request → CommandDispatcher → Command.execute() → Response
```

Commands are:
- **Self-contained**: Encapsulate logic and parameters
- **Testable**: Pure Java objects that run in JUnit
- **Portable**: Same code across servlet, Spring Boot, AWS Lambda
- **Discoverable**: Named by convention (package structure)

## When to Use CommandMosaic

**Good fit:**
- Complex business domains that don't map cleanly to CRUD
- Applications with many small operations (50+ endpoints)
- Serverless architectures where you want to minimize API Gateway configuration
- Teams that want rapid feature development without infrastructure changes
- Applications requiring uniform security policies across operations

**Maybe not:**
- Pure CRUD applications with simple resource management
- Public APIs that must conform to REST conventions
- When you need fine-grained HTTP method semantics (GET caching, PUT idempotency, etc.)

## Getting Started

### Installation

Add the dependency matching your runtime environment:

```xml
<!-- Plain Java -->
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-plain-java</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- Spring Boot -->
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-spring-boot-autoconfigure</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- AWS Lambda (Spring Boot) -->
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-aws-lambda-springboot</artifactId>
    <version>2.0.0</version>
</dependency>
```

See [Which Dependency](#which-dependency-do-you-need) for the complete matrix.

### Plain Java Setup

```java
CommandDispatcherConfiguration config = CommandDispatcherConfiguration.builder()
    .rootPackage("com.example.commands")
    .build();

CommandDispatcherFactory factory = PlainCommandDispatcherFactory.getInstance();
CommandDispatcher dispatcher = factory.getCommandDispatcher(config);

// Execute commands
String result = dispatcher.dispatchCommand(new GreetingCommand("Alice"), null);
```

### Spring Boot Setup

Configure the dispatcher in your Spring configuration:

```java
@Configuration
public class AppConfig {
    
    @Bean
    public CommandDispatcherConfiguration commandDispatcherConfig() {
        return CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .build();
    }
}
```

Inject and use:

```java
@Service
public class BusinessService {
    
    @Autowired
    private CommandDispatcher commandDispatcher;
    
    public void doSomething() {
        Result result = commandDispatcher.dispatchCommand(
            new SomeCommand(params), context
        );
    }
}
```

### Expose as HTTP Endpoint (Spring Boot)

Create a controller that delegates to the dispatcher:

```java
@RestController
@RequestMapping("/api")
public class CommandResource {

    private final CommandDispatcherServer dispatcherServer;

    public CommandResource(CommandDispatcherServer dispatcherServer) {
        this.dispatcherServer = dispatcherServer;
    }

    @PostMapping("/dispatch")
    public void dispatch(InputStream is, OutputStream os) throws IOException {
        dispatcherServer.serviceRequest(is, os);
    }
}
```

Request format:

```json
{
  "command": "ProcessPayment",
  "parameters": {
    "orderId": "ORD-123",
    "amount": 99.99
  },
  "protocol": "CM/1.0"
}
```

## Security

Commands are secured with annotations:

```java
// Public access
@Access.IsPublic
public class GetProductCatalog implements Command<List<Product>> {
    // ...
}

// Requires authentication
@Access.RequiresAnyOfTheAuthorities
public class UpdateProfile implements Command<Void> {
    // ...
}

// Role-based access
@Access.RequiresAnyOfTheAuthorities({"ADMIN", "MANAGER"})
public class DeleteUser implements Command<Void> {
    // ...
}
```

Implement authentication by extending `DefaultSecurityCommandInterceptor`:

```java
public class JwtSecurityInterceptor extends DefaultSecurityCommandInterceptor {
    
    @Override
    protected Set<String> attemptLogin(CommandContext context) 
            throws AuthenticationException {
        Map<String, Object> auth = context.getAuth();
        String token = (String) auth.get("token");
        
        // Validate token, extract roles
        Claims claims = jwtService.validateToken(token);
        return new HashSet<>(claims.get("roles", List.class));
    }
}
```

Register the interceptor:

```java
CommandDispatcherConfiguration config = CommandDispatcherConfiguration.builder()
    .rootPackage("com.example.commands")
    .interceptor(JwtSecurityInterceptor.class)
    .build();
```

Clients pass authentication data in the `auth` field:

```json
{
  "command": "DeleteUser",
  "parameters": { "userId": 123 },
  "auth": { "token": "eyJhbGc..." },
  "protocol": "CM/1.0"
}
```

## AWS Lambda Deployment

CommandMosaic minimizes Lambda complexity: **one handler, one API Gateway endpoint, infinite operations**.
applications is using RESTful web services: that is, mapping HTTP URL patterns
### Benefits

- **Single API endpoint**: Add features without updating API Gateway configuration
- **Test without AWS tooling**: Commands run in plain JUnit tests
- **Unified codebase**: Same commands work in Lambda, servlets, and Spring Boot
- **Cold start optimization**: One function to warm up, not dozens

### Spring Boot Lambda Setup

1. Add dependency:
	
```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-aws-lambda-springboot</artifactId>
    <version>2.0.0</version>
</dependency>
```

2. Create request handler:

```java
package com.example;
    Create/Read/Update/Delete a resource pattern
import org.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler;
    working with the interface more complex (e.g. consider AWS API Gateway and Lambda)
public class AppRequestHandler extends SpringBootLambdaCommandDispatcherRequestHandler {
    
    public AppRequestHandler() {
        super(Application.class); // Your Spring Boot application class
allows the remote clients to request the execution of a command. Security is managed at 
}
```

3. Configure Lambda function with handler: `com.example.AppRequestHandler`
keeping the interface minimal and focusing on the business logic instead of writing boilerplate 
4. Deploy. Your commands are now accessible via API Gateway.

### Plain Java Lambda Setup

For non-Spring deployments:
to the dispatch handler via HTTP POST with similar structure:
```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-aws-lambda-plain-java</artifactId>
    <version>2.0.0</version>
</dependency>
```
        "protocol": "CM/1.0"    
```java
public class AppRequestHandler extends PlainLambdaCommandDispatcherRequestHandler {
    
    public AppRequestHandler() {
        super(CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .build());
    }
}
```
For example, assuming the `CommandDispatcher` used was configured with `org.acme` as the 
### Testing Lambda Functions Locally
to be executed:
No AWS tooling required:

```java
@Test
public void testPaymentProcessing() {
    CommandDispatcher dispatcher = // ... create dispatcher
## Security 
    ProcessPayment command = new ProcessPayment();
    command.setOrderId("ORD-123");
    command.setAmount(new BigDecimal("99.99"));
The module [commandmosaic-security](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/security) 
    PaymentResult result = dispatcher.dispatchCommand(command, null);

    assertNotNull(result.getTransactionId());
}
```

Or test the full HTTP flow with `CommandDispatcherServer` in a servlet container.

### Module Declaration (Java 9+)

For Spring Boot Lambda:

```java
module com.example.app {
    requires org.commandmosaic.aws.lambda.springboot;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
`org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor`
    opens com.example to
        spring.core, spring.context, spring.beans,
        org.commandmosaic.core;
}
```

The user-provided security `CommandInterceptor` must be configured within
`CommandDispatcherConfiguration`, otherwise security will not be enabled. 

    CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
        .rootPackage("com.acme.foobar")
        .interceptor(MyCustomSecurityCommandInterceptor.class)
        .build();
    
### Message "auth" field

Each command dispatch request can have a user defined, field "`auth`", which holds
key-value pairs: when security is enabled, this must be provided for all requests
that dispatch a command marked with `@RestrictedAccess` annotation. 

The content of the field is user-defined key value pairs; the only restriction is 
that it must be deserializable to a `HashMap`.

For example, the following sample shows passing two fields in the "`auth`" Map field,
`username` and `password`. 

    {
      "command": "foo/bar/Foobar",
      "auth": {
        "username": "foo",
        "password": "bar"
      },
      "protocol": "CM/1.0"
    }  


These values can be extracted from the `CommandContext` via the `Map`
returned by `CommandContext.getAuth()` method. The user's implementation
of `attemptLogin(CommandContext)` must authenticate the request: throw an
`AuthenticationException` in case login fails, otherwise return the roles
associated with the user. (The rest of the security management is handled
by the framework code in `AbstractSecurityCommandInterceptor`)
    
    package sample;
    
    import org.commandmosaic.api.CommandContext;
    import org.commandmosaic.security.AuthenticationException;
    import org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor;
    
    import java.util.Map;
    import java.util.Set;
    
    public class MyCustomSecurityCommandInterceptor extends AbstractSecurityCommandInterceptor {
    
        @Override
        protected Set<String> attemptLogin(CommandContext commandContext) throws AuthenticationException {
            Map<String, Object> auth = commandContext.getAuth();
    
            String username = (String) auth.get("username");
            String password = (String) auth.get("password");
    
            // throw AuthenticationException if login fails, otherwise retrieve user roles
    
            Set<String> rolesOfTheUser = // retrieve user roles ...
    
            return rolesOfTheUser;
        }
    }
    
## Built-in integrations for exposing commands as a service

The following integrations are provided out-of-the-box:
  * Servlet
  * Spring HTTP Request handlers
  * Amazon AWS Lambda (with and without Spring Boot)

## Using the CommandDispatcherServer server classes

This library was designed with the goal of exposing commands as services 
with minimum amount of code. The API 
`org.commandmosaic.api.server.CommandDispatcherServer` provides
 a layer of abstraction between how a command dispatch request is 
transmitted and represented, and the actual `CommandDispatcher` being used.   

A `CommandDispatcherServer` reads the incoming dispatch request from
a `java.io.InputStream` and writes the outcome of the command execution to 
`java.io.OutputStream`. Any container that can provide incoming messages
as `java.io.InputStream` and can receive the response as bytes being written
to a `java.io.OutputStream` can easily be integrated.

The class `org.commandmosaic.core.server.DefaultCommandDispatcherServer`
offers a default implementation for building the dispatcher server. 

Its constructor takes a `CommandDispatcher` that it will use to dispatch 
incoming requests. Its only public method `serviceRequest` reads 
the dispatch requests from the supplied `InputStream` as JSON and writes the 
response back to the `OutputStream` as JSON as well. This allows easy integration
to any request handling mechanism where the application has access to the request
streams. (For example: Apache Netty network servers etc.)


# Implementing the Command Pattern

## Command Pattern within a Plain Java application 

The simplest case is using the `CommandDispatcher` API for implementing
Command Pattern in a Java application, without exposing commands as a service.
Here, one only uses the library for organizing code in a modular and re-usable 
structure.  

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>org.commandmosaic</groupId>
        <artifactId>commandmosaic-plain-java</artifactId>
        <version>LATEST</version>
    </dependency> 

### Description 

For this use-case, one constructs a 
`CommandDispatcherConfiguration` object using its builder and then calls
uses the corresponding "plain-Java" `CommandDispatcherFactory` to construct
a `CommandDispatcher`. The `rootPackage` specified acts as a restriction for the 
`CommandDispatcher`: it will only accept commands that are located in the specified
package or any of its subpackages. In the sample below, the `dispatchCommand`
method call would throw an Exception for any commands that are outside
of the root package `com.acmecorp.foobarapp.sample`. This explicit definition
ensures only the intended commands can be executed by a given `CommandDispatcher`.
At the same time, one application can host an arbitrary number of `CommandDispatcher`s.


    package org.commandmosaic.plain.sample;
    
    import org.commandmosaic.api.Command;
    import org.commandmosaic.api.CommandContext;
    import org.commandmosaic.api.CommandDispatcher;
    import org.commandmosaic.api.Parameter;
    import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
    import org.commandmosaic.api.factory.CommandDispatcherFactory;
    import org.commandmosaic.plain.PlainCommandDispatcherFactory;
    
    public class SampleApplication {
    
        public static void main(String[] args) {
    
            CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
                    .rootPackageFromClass(SampleApplication.class)
                    .build();
    
            CommandDispatcherFactory factory = PlainCommandDispatcherFactory.getInstance();
            CommandDispatcher commandDispatcher = factory.getCommandDispatcher(configuration);
    
            GreetingCommand greetingCommand = new GreetingCommand("John Smith");
            String result = commandDispatcher.dispatchCommand(greetingCommand, null);
    
            System.out.println(result);
        }
    
        public static class GreetingCommand implements Command<String> {
    
            @Parameter
            private String name;
    
            public GreetingCommand() {
                // no argument constructor is required for the framework
            }
    
            public GreetingCommand(String name) {
                this.name = name;
            }
    
            @Override
            public String execute(CommandContext context) {
                return "Hello " + name;
            }
        }
    }
    
    
See [sample application](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-helloworld-sample-app)
    
## Command Pattern within a Spring Boot application

The library provides out-of-the-box support for Spring Boot. You can get the 
library to automatically initialize by Spring Boot and develop your commands 
as Spring Beans, with the rich set of functionality offered by Spring, like 
transaction support, automatic dependency injection through Spring's 
`@Autowired` annotation etc.

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>org.commandmosaic</groupId>
        <artifactId>commandmosaic-spring-boot-autoconfigure</artifactId>
        <version>LATEST</version>
    </dependency>

### Description 

With the `commandmosaic-spring-boot-autoconfigure` you can rely on
Spring Boot library auto-configuration: simply create a `CommandDispatcherConfiguration` 
and expose it as a Spring bean. 


	@Bean
	public CommandDispatcherConfiguration springCommandDispatcherConfiguration() {
		return CommandDispatcherConfiguration.builder()
				.rootPackage("com.acme.foobar.commands")
				.build();
	}

Once done, you can simply rely on standard Spring auto-wiring to receive the reference to
the Spring-aware `CommandDispatcher` instance.


    @Service
	public class FoobarServiceImpl implements FoobarService {

		private final CommandDispatcher commandDispatcher;

		@Autowired
		public FoobarServiceImpl(CommandDispatcher commandDispatcher) {
			this.commandDispatcher = commandDispatcher;
		}
		
		// ... use the CommandDispatcher
		
	}
 
See [sample application](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-springboot2-sample-app)

# Exposing Commands from a Java application

## Exposing Commands through a Servlet (without Spring)

Commands can be exposed as a lightweight REST service,
where only one operation, the dispatching of a command is published.
The implementation is inside a framework-provided servlet class
that expects the same JSON document format as other runtime environments
use. The users of this library has to configure CommandDispatcherServlet
bundled within the framework, after which they have to just start
writing their command implementation. 

A request is simply a JSON document, with similar structure:

    {
        "command": "Foobar",
        "parameters" : {
            "foo": "Hello there",
            "bar": 42
        },
        "protocol": "CM/1.0"    
    }

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>org.commandmosaic</groupId>
        <artifactId>commandmosaic-servlet</artifactId>
        <version>LATEST</version>
    </dependency> 

### Description 

Configure the `org.commandmosaic.http.servlet.CommandDispatcherServlet` Servlet 
provided by the framework for the desired URL and configure it using initialization
parameters according to the following:

  * The **root package** of the CommandDispatcher MUST be specified in a
    String initialization parameter, the name of which is stored in 
    `CommandDispatcherServlet.COMMAND_DISPATCHER_ROOT_PACKAGE`
    
  * Optionally, **command interceptor classes** can be specified as
    comma separated list of fully qualified class names passed in a String 
    initialization parameter, the name of which is stored in 
    `CommandDispatcherServlet.COMMAND_DISPATCHER_ROOT_PACKAGE`

You will likely want to secure access to particular commands and
implement proper authentication and access management (authorization).
For this, please refer to the features of [commandmosaic-security](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/security)
module, which offers standardised annotation based access control and an abstract
`CommandInterceptor` base class for plugging in custom authentication and authorization
logic with minimal amount of code.

## Exposing Commands from a Spring Boot application

Commands can be exposed as a lightweight REST service,
where only one operation, the dispatching of a command is published.
The library provides out-of-the-box support for Spring Boot. You can get the 
library to automatically initialize by Spring Boot and develop your commands 
as Spring Beans, with the rich set of functionality offered by Spring, like 
transaction support, automatic dependency injection through Spring's 
`@Autowired` annotation etc.

A request is simply a JSON document, with similar structure:

    {
        "command": "Foobar",
        "parameters" : {
            "foo": "Hello there",
            "bar": 42
        },
        "protocol": "CM/1.0"    
    }


### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>org.commandmosaic</groupId>
        <artifactId>commandmosaic-spring-boot-autoconfigure</artifactId>
        <version>LATEST</version>
    </dependency>

### Description 

With the `commandmosaic-spring-boot-autoconfigure` you can rely on
Spring Boot library auto-configuration: simply create a `CommandDispatcherConfiguration` 
and expose it as a Spring bean. 

    package sample.app.config;
    
    import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    
    @Configuration
    public class SampleAppConfig {
    
        /* other configuration entries... */
    
    	@Bean
    	public CommandDispatcherConfiguration springCommandDispatcherConfiguration() {
    		return CommandDispatcherConfiguration.builder()
    				.rootPackage("com.acme.foobar.commands")
    				.build();
    	}
    
    }

Once done, you can simply expose the `CommandDispatcherServer`
through a Spring Web REST RestController class similar to the one below.
(Again: we rely on Spring to configure the dependencies for us.)

    package sample;
    
    import org.commandmosaic.api.server.CommandDispatcherServer;
    
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    
    @RestController
    @RequestMapping("/api")
    public class CommandResource {
    
        private final CommandDispatcherServer commandDispatcherServer;
    
        public CommandResource(CommandDispatcherServer commandDispatcherServer) {
            this.commandDispatcherServer = commandDispatcherServer;
        }
    
    
        @PostMapping("/cmd")
        public void processCommand(InputStream is, OutputStream os) throws IOException {
            commandDispatcherServer.serviceRequest(is, os);
        }
    }

# Servlerless Cloud with Amazon Lambda

Amazon Lambda (Java) is supported out-of-the-box. Different flavours of this library 
(Plain Java and Spring Boot support) provide abstract AWS `RequestHandler` base 
implementations that dispatch the command specified. 
  
The benefit of using CommandMosaic with Amazon Lambda is the small interface
footprint. You deploy you Lambda application with one AWS RequestHandler and
configure AWS API Gateway with one HTTP resource. You then can add further 
commands to your code base without having to even touch the existing AWS 
API Gateway configuration as the only operation exposed is dispatching a 
command. These commands can easily be tested in a Junit / TestNG test or
you can use the standard Servlet or Spring HttpRequestHandler containers to 
start a mock server for an full-blown integration test, which will provide 
exactly the same behaviour as the container running in the cloud (as the 
core behaviour is shared across all runtime environments).
   
When using CommandMosaic with Amazon Lambda, you, as the user of the 
framework have to subclass the appropriate base `RequestHandler` class with a 
placeholder class, that does nothing apart from passing configuration to the 
framework-provided superclass's constructor. This placeholder class has to be configured as your 
lambda function: the framework provides the behaviour, while your placeholder 
sub-class provides the configuration only. 

Once this is done, you can start implementing your `Command` classes and then 
package your application. The framework-provided `RequestHandler` base 
dispatches incoming requests to the corresponding commands. All you have to
do is:
  1. Implement your business logic as commands
  2. Package the application properly for AWS Lambda and deploy it
  1. Configure the source trigger (e.g. AWS API Gateway) for your lambda function 
 
You need to ensure that the application is packaged according to AWS requirements. 
You are encouraged to create an AWS Java lambda application skeleton using 
AWS Maven archetype and then add the corresponding library dependency. 
 
## Plain Java AWS Lambda function (without Spring)

Use this if you want to implement your AWS Lambda function without
using Spring or Spring Boot at all.

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

        <dependency>
            <groupId>org.commandmosaic</groupId>
            <artifactId>commandmosaic-aws-lambda-plain-java</artifactId>
            <version>LATEST</version>
        </dependency> 

### Java module declaration

When using the Java Platform Module System ("Java 9+ modules"), add 
similar settings to your `module-info.java` module descriptor file.
(*NOTE: the sample below uses `sampleapp` as module name: do not forget to
substitute your own module name*)

    module sampleapp {
    
        requires org.commandmosaic.aws.lambda.plain;
    
        opens sampleapp to
                org.commandmosaic.core;
    }

### Description 

Once the dependency is added, create a placeholder Java request handler class in your 
AWS Java lambda application that subclasses  
`org.commandmosaic.aws.lambda.plain.PlainLambdaCommandDispatcherRequestHandler`
and has a no-argument constructor, which invokes the `super` constructor with the 
desired `CommandDispatcherConfiguration` configuration object. 

Once this is done, you can start implementing your `Command` classes and then 
package your application. Your `Command` classes are Spring beans, so you
can easily use Spring `@Autowired` depdency injection and other Spring features.

The framework-provided `RequestHandler` base 
dispatches incoming requests to the corresponding commands. All you have to
do is:
  1. Implement your business logic as commands
  2. Package the application properly for AWS Lambda and deploy it
  1. Configure the source trigger (e.g. AWS API Gateway) for your lambda function 
 
NOTE: You need to ensure that the application is packaged according to AWS requirements. 
You are encouraged to create an AWS Java lambda application skeleton using 
AWS Maven archetype and then add the corresponding library dependency. 

## Using Spring Boot 2.x+

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

        <dependency>
            <groupId>org.commandmosaic</groupId>
            <artifactId>commandmosaic-aws-lambda-springboot</artifactId>
            <version>LATEST</version>
        </dependency> 


### Java module declaration

When using the Java Platform Module System ("Java 9+ modules"), add 
similar settings to your `module-info.java` module descriptor file.
(*NOTE: the sample below uses `sampleapp` as module name: do not forget to
substitute your own module name*)

    module sampleapp {
    
        requires org.commandmosaic.aws.lambda.springboot;
    
        requires spring.boot.autoconfigure;
        requires spring.boot;
        requires spring.context;
        requires spring.beans;
    
        opens sampleapp to
                spring.core, spring.context, spring.beans,
                org.commandmosaic.core;
    }

### Description 

Once the dependency is added, create a placeholder Java request handler class in 
your AWS Java lambda application that subclasses  
`org.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler`
and has a no-argument constructor, which invokes the `super` constructor with the desired
configuration, passing the class of the Spring Boot application class and optionally, 
the profiles used.

    package sample;

    import org.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler;

    public class SampleApplicationRequestHandlerSpringBootLambda extends SpringBootLambdaCommandDispatcherRequestHandler {

        public SampleApplicationRequestHandlerSpringBootLambda() {
            super(SampleApplication.class);
        }
    }

Once this is done, you can start implementing your `Command` classes and then 
package your application. Your `Command` classes are Spring beans, so you
can easily use Spring `@Autowired` depdency injection and other Spring features.

The framework-provided `RequestHandler` base 
dispatches incoming requests to the corresponding commands. All you have to
do is:
  1. Implement your business logic as commands
  2. Package the application properly for AWS Lambda and deploy it
  1. Configure the source trigger (e.g. AWS API Gateway) for your lambda function 
 
NOTE: You need to ensure that the application is packaged according to AWS requirements. 
You are encouraged to create an AWS Java lambda application skeleton using 
AWS Maven archetype and then add the corresponding library dependency. 

Please check the sample application for a fully working project setup:
[commandmosaic-aws-lambda-springboot2-sample-app](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-aws-lambda-springboot2-sample-app)

## Are we building a monolithic Lambda application?

The official recommendation from Amazon regarding Lambda functions is to use 
a separate function for every single operation and use further of their orchestration
services (e.g. AWS Step Functions) to manage the cooperation, fully distributing and de-coupling
microservices to the point where they are not maintained in the same code repository anymore.

Applications written using this library indeed can be considered as a monolithic application, 
however in my opinion this approach can actually be better in a number of cases, as
 1. You have all the application logic in place 
 2. Refactorings are trivial with the help of a good IDE
 3. You only have one API to maintain, which can expose a large number of operations
 4. You still can use AWS services (e.g. API Gateway) to host multiple instances of the same application
 



# Which dependency do you need

Please select the artifactId based on the following table. 
You always want to pick **only one** of the following dependencies:

| Use-case                           | Dependency artifactId                    |
| -----------------------------------|:----------------------------------------:|
| Plain Java application, no Spring  | `commandmosaic-plain-java`               |
| Java application with Spring       | `commandmosaic-spring`                   |
| Java application with Spring Boot  | `commandmosaic-spring-boot-autoconfigure`|
| AWS Java Lambda, no Spring         | `commandmosaic-aws-lambda-plain-java`    |
| AWS Java Lambda, with Spring Boot  | `commandmosaic-aws-lambda-springboot`    |

# Spring Boot version required

As 
[Spring Boot 1.x has been deprecated and is no longer supported by Pivotal](https://spring.io/blog/2019/08/06/it-is-time-goodbye-spring-boot-1-x), 
Spring Boot 1.x is not (and will not be) supported at all.

Please always use Spring Boot 2.x+ versions with this library. 

# Samples 
You are encouraged to check the sample applications: please download the sample application 
projects referenced below. Before you can work with the sample project, **you need to make
adjustments to the project `pom.xml` file**:

 1. Remove the `<parent>...</parent>` section  
 2. Uncomment the sections commented out: this provides a working build configuration 
 3. Adjust groupId/artifactId to your needs 

 * [Hello World application](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-helloworld-sample-app)
 * [Minimalistic console Spring Boot2 application](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-springboot2-sample-app)
 * [Exposing commands via an AWS Lambda function (request handler)](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-aws-lambda-springboot2-sample-app)

 
 
