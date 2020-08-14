
# CommandMosaic: a Java command pattern framework 

# Introduction

CommandMosaic is a project with the following high-level goals:

  * Promote the implementation of business logic via the Command design pattern.
  
  * Support a uniform programming model across a variety of Java application types,
    regardless of their runtime environment, including serverless cloud providers.
    
  * Decouple the development lifecycle of business logic from any specific 
    target runtime environment so that the same code can run both as encapsulation
    of logic in any generic Java application or exposed as service for remote clients.

  * Provide trivial-to-use, pure Java-based testing capabilities for business logic 
    developed for serverless cloud platforms without using any vendor-specific tooling. 
   
  * Provide first class support for Spring Framework and Spring Boot features 
    (auto-configuration, dependency injection, etc) without mandating their usage.mv
    
# Overview    

The [Command design pattern](https://en.wikipedia.org/wiki/Command_pattern) is a 
well-known pattern described in the ["Gang of Four"](http://wiki.c2.com/?GangOfFour)
[Design Patterns: Elements of Reusable Object-Oriented Software](https://en.wikipedia.org/wiki/Design_Patterns)
book.

This Java library contains an implementation of the command pattern, 
a simple programming interface for passing parameters to commands,
and a minimalistic container for easily exposing commands as a 
simple service with minimal amount of boilerplate code. 

In addition to this, out-of-the-box support is provided for the 
industry standard Spring Frameworks allowing the library consumer
to develop his/her commands as pure Spring Beans, with the rich
set of functionality offered by Spring, like transaction support, 
automatic dependency injection through Spring's `@Autowired` 
annotation etc.

The framework is runtime-agnostic, which means it can be used in practically 
all environments, ranging from a small command line application through 
Java Servlet based systems, any Spring or Spring Boot application, including 
any serverless cloud platform, where  Java is available.

# The API 

Commands are simple Java classes that implement the business logic in
the `execute` method defined in the `Command` interface. Their 
parameters are passed via fields annotated with `@Parameter` annotation, 
which are automatically injected by the framework.

    package sample;

    import com.github.commandmosaic.api.Command;
    import com.github.commandmosaic.api.Parameter;
    import com.github.commandmosaic.api.CommandContext;

    public class GreetingCommand implements Command<String> {

        @Parameter
        String name;

        @Override
        public String execute(CommandContext context) {
            return "Hello " + name;
        }
    }

# Spring support 

Spring is supported out-of-the box: a Command class can also be 
a Spring bean, allowing it to be used according to powerful and 
well-known Spring concepts, including automatic dependency 
injection through Spring's `@Autowired` annotation.    

    package sample;

    import com.github.commandmosaic.api.Command;
    import com.github.commandmosaic.api.Parameter;
    import com.github.commandmosaic.api.CommandContext;
    import org.springframework.beans.factory.annotation.Autowired;

    public class GreetCommand implements Command<String> {

        @Autowired
        private GreetingService greetingService;

        @Parameter
        private String name;

        @Override
        public String execute(CommandContext context) {

            return greetingService.getMessage(name);
        }
    }

# Exposing Commands as a service 

Commands implemented using this library can easily be exposed as a service
for remote clients. Here, the only API operation exposed is dispatching of a 
command: this allows keeping the interface minimal and focusing on the 
business logic instead of writing boilerplate code for exposing operations
for remote consumption.

## Using the CommandDispatcherServer server classes

This library was designed with the goal of exposing commands as services 
with minimum amount of code. The API 
`com.github.commandmosaic.api.server.CommandDispatcherServer` provides
 a layer of abstraction between how a command dispatch request is 
transmitted and represented, and the actual `CommandDispatcher` being used.   

A `CommandDispatcherServer` reads the incoming dispatch request from
a `java.io.InputStream` and writes the outcome of the command execution to 
`java.io.OutputStream`. Any container that can provide incoming messages
as `java.io.InputStream` and can receive the response as bytes being written
to a `java.io.OutputStream` can easily be integrated.

The class `com.github.commandmosaic.core.server.DefaultCommandDispatcherServer`
offers a default implementation for building the dispatcher server. 

Its constructor takes a `CommandDispatcher` that it will use to dispatch 
incoming requests. Its only public method `serviceRequest` reads 
the dispatch requests from the supplied `InputStream` as JSON and writes the 
response back to the `OutputStream` as JSON as well. This allows easy integration
to any request handling mechanism where the application has access to the request
streams. (For example: Apache Netty network servers etc.)

A request is simply a JSON document, with similar structure:

    {
        "command": "Foobar",
        "parameters" : {
            "foo": "Hello there",
            "bar": 42
        },
        "protocol": "CM/1.0"    
    }

## Command names

Exposing the full package structure of the application in remote scenarios would be
especially undesirable. To prevent unwanted coupling and to reduce overall message size,
CommandMosaic uses abbreviated command names, where command names do not contain the 
common root package name prefix configured during the creation of the CommandDispatcher
and use forward slash ("/") instead of the dot (".") package separator.
  
For example, assuming the `CommandDispatcher` used was configured with `org.acme` as the 
root package, the following request would cause the command `org.acme.foo.bar.Foobar` 
to be executed:

    {
        "command": "foo/bar/Foobar",
        "protocol": "CM/1.0"    
    }

## Security 

In remote service cases, having proper security is essential so that commands
can only be executed by authorized clients only.  
The module `commandmosaic-security` contains support features for this requirement.

Two annotations are provided to mark the access levels of each commands:

  * `com.github.commandmosaic.security.annotation.UnauthenticatedAccess`:
    This annotation must be applied to all command classes that should be
    available without authentication.
    
  * `com.github.commandmosaic.security.annotation.RestrictedAccess`
    This annotation must be applied to all command classes that should only
    be available to authenticated clients. Optionally, role based security
    can be implemented by specifying the roles which should have access to 
    the specific command.
     
Access control is implemented via `CommandInterceptor`s: application developers
are required to develop a custom interceptor by extending the framework-provided
class `com.github.commandmosaic.security.interceptor.AbstractSecurityCommandInterceptor`
and configure it for the `CommandDispatcher`. 

`com.github.commandmosaic.security.interceptor.AbstractSecurityCommandInterceptor`
provides a base implementation for security `CommandInterceptor`s: its 
`attemptLogin(CommandContext)` method must be implemented by the end-user.
Such implementations will want to extract the user-defined 
authentication/authorization information from the request via the 
`CommandContext.getAuth()` method.

The user-provided security `CommandInterceptor` must be configured within
`CommandDispatcherConfiguration`, otherwise security will not be enabled. 

    CommandDispatcherConfiguration commandDispatcherConfiguration = CommandDispatcherConfiguration.builder()
        .rootPackage("com.acme.foobar")
        .interceptor(MyCustomSecurityCommandInterceptor.class)
        .build();
    
### Message "auth" field

Each command dispatch request can have a user defined, field "`auth`", which holds
key-value pairs: when security is enabled, this must be provided for all requests
that dispatch a command marked with `@RestrictedAccess` annotation. 

The content of the field is not specified; the only restriction is that it must
be deserializable to a `HashMap`.

For example, the following sample shows passing two fields in the "`auth`" Map field:

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
    
    import com.github.commandmosaic.api.CommandContext;
    import com.github.commandmosaic.security.AuthenticationException;
    import com.github.commandmosaic.security.interceptor.AbstractSecurityCommandInterceptor;
    
    import java.util.Map;
    import java.util.Set;
    
    public class MyCustomSecurityCommandInterceptor extends AbstractSecurityCommandInterceptor {
    
        @Override
        protected Set<String> attemptLogin(CommandContext commandContext) throws AuthenticationException {
            Map<String, Object> auth = commandContext.getAuth();
    
            String username = (String) auth.get("username");
            String password = (String) auth.get("password");
    
            // throw exception if login fails,
            // otherwise retrieve user roles
    
            Set<String> rolesOfTheUser = // retrieve user roles ...
    
            return rolesOfTheUser;
        }
    }
    
## Built-in integrations for exposing commands as a service

The following integrations are provided out-of-the-box:
  * Servlet
  * Spring HTTP Request handlers
  * Amazon AWS Lambda (with and without Spring Boot)


# Overview of use-cases 

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
        <groupId>com.github.commandmosaic</groupId>
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


    package com.github.commandmosaic.plain.sample;
    
    import com.github.commandmosaic.api.Command;
    import com.github.commandmosaic.api.CommandContext;
    import com.github.commandmosaic.api.CommandDispatcher;
    import com.github.commandmosaic.api.Parameter;
    import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
    import com.github.commandmosaic.api.factory.CommandDispatcherFactory;
    import com.github.commandmosaic.plain.PlainCommandDispatcherFactory;
    
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
    
    
See [sample application](sample-apps/commandmosaic-helloworld-sample-app)
    
## Command Pattern within a Spring Boot application

The library provides out-of-the-box support for Spring Boot. You can get the 
libary to automatically initialize by Spring Boot and develop your commands 
as Spring Beans, with the rich set of functionality offered by Spring, like 
transaction support, automatic dependency injection through Spring's 
`@Autowired` annotation etc.

### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>com.github.commandmosaic</groupId>
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
 
See [sample application](sample-apps/commandmosaic-springboot2-sample-app)

## Exposing Commands through a Servlet (without Spring)

Commands can be exposed 


### Dependency

Add the following dependency declaration to your Maven `pom.xml`. 
(or its equivalent in your other preferred build tool), replacing 
LATEST with the available latest version: 

    <dependency>
        <groupId>com.github.commandmosaic</groupId>
        <artifactId>commandmosaic-servlet</artifactId>
        <version>LATEST</version>
    </dependency> 

### Description 

Configure the `com.github.commandmosaic.http.servlet.CommandDispatcherServlet` Servlet 
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
For this, please refer to the features of [commandmosaic-security](security)
module, which offers standardised annotation based access control and an abstract
`CommandInterceptor` base class for plugging in custom authentication and authorization
logic with minimal amount of code.

# Amazon Lambda Support
  
Amazon Lambda (Java) is supported out-of-the-box. Different flavours of this library 
(Plan Java and Spring Boot support) provide abstract AWS `RequestHandler` base 
implementations that dispatch the command specified. You, as the user of the 
framework have to subclass these base implementations with a placeholder class, 
that does nothing apart from passing configuration to the framework-provided 
superclass's constructor. This placeholder class has to be configured as your 
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
            <groupId>com.github.commandmosaic</groupId>
            <artifactId>commandmosaic-aws-lambda-plain-java</artifactId>
            <version>LATEST</version>
        </dependency> 

### Java module declaration

When using the Java Platform Module System ("Java 9+ modules"), add 
similar settings to your `module-info.java` module descriptor file.
(*NOTE: the sample below uses `sampleapp` as module name: do not forget to
substitute your own module name*)

    module sampleapp {
    
        requires com.github.commandmosaic.aws.lambda.plain;
    
        opens sampleapp to
                com.github.commandmosaic.core;
    }

### Description 

Once the dependency is added, create a placeholder Java request handler class in your 
AWS Java lambda application that subclasses  
`com.github.commandmosaic.aws.lambda.plain.PlainLambdaCommandDispatcherRequestHandler`
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
            <groupId>com.github.commandmosaic</groupId>
            <artifactId>commandmosaic-aws-lambda-springboot</artifactId>
            <version>LATEST</version>
        </dependency> 


### Java module declaration

When using the Java Platform Module System ("Java 9+ modules"), add 
similar settings to your `module-info.java` module descriptor file.
(*NOTE: the sample below uses `sampleapp` as module name: do not forget to
substitute your own module name*)

    module sampleapp {
    
        requires com.github.commandmosaic.aws.lambda.springboot;
    
        requires spring.boot.autoconfigure;
        requires spring.boot;
        requires spring.context;
        requires spring.beans;
    
        opens sampleapp to
                spring.core, spring.context, spring.beans,
                com.github.commandmosaic.core;
    }

### Description 

Once the dependency is added, create a placeholder Java request handler class in 
your AWS Java lambda application that subclasses  
`com.github.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler`
and has a no-argument constructor, which invokes the `super` constructor with the desired
configuration, passing the class of the Spring Boot application class and optionally, 
the profiles used.

    package sample;

    import com.github.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler;

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
[commandmosaic-aws-lambda-springboot2-sample-app](sample-apps/commandmosaic-aws-lambda-springboot2-sample-app)

## Monolithic Lambda application?

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

# Spring Boot version

Spring Boot 1.x has been deprecated and is no longer supported by Pivotal.
Please always use Spring Boot 2.x+ versions with this library.

# Samples 
You are encouraged to check the sample applications: please download the projects
and remove the `<parent>...</parent>` section from their `pom.xml` file.

 * [Hello World application](sample-apps/commandmosaic-helloworld-sample-app)
 * [Minimalistic console Spring Boot2 application](sample-apps/commandmosaic-springboot2-sample-app)
 * [Exposing commands via an AWS Lambda function (request handler)](sample-apps/commandmosaic-aws-lambda-springboot2-sample-app)

 
 
