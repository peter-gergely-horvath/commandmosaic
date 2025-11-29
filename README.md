# CommandMosaic

**A Java framework for building services using the Command pattern**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Table of Contents

- [Why CommandMosaic?](#why-commandmosaic)
- [Key Features](#key-features)
- [Quick Example](#quick-example)
- [Core Concepts](#core-concepts)
- [Getting Started](#getting-started)
  - [Choosing Your Integration](#choosing-your-integration)
  - [Plain Java Applications](#plain-java-applications)
  - [Spring Boot Web Applications](#spring-boot-web-applications)
  - [Servlet-Based Applications (Java EE/Jakarta EE)](#servlet-based-applications-java-eejakarta-ee)
  - [AWS Lambda Functions](#aws-lambda-functions)
- [Security](#security)
- [Advanced Topics](#advanced-topics)
  - [Why Commands Should NOT Be Annotated with @Component](#why-commands-should-not-be-annotated-with-component)
  - [Command Interceptors](#command-interceptors)
  - [Command Discovery](#command-discovery)
- [Sample Applications](#sample-applications)

## Why CommandMosaic?

While REST APIs work well for resource-oriented operations, modeling every business operation as CRUD (Create, Read, Update, Delete) can become challenging with complex domain logic:

- **Domain mismatch**: Not all operations map to CRUD. A booking system needs `CancelBooking`, not `DELETE /booking` – the booking must remain for audit purposes with a compensating entry.
- **Boilerplate overhead**: Each new feature requires creating controllers, defining routes, writing delegation logic, and updating API documentation.
- **Testing complexity**: Integration testing serverless functions requires vendor-specific tooling and complex setup.

CommandMosaic takes a different approach: **one dispatch endpoint, many commands**.

## Key Features

- **Single entry point**: One API endpoint handles all operations. Add features by implementing commands – no routing, no controllers, no configuration changes.
- **Write once, run anywhere**: Same code runs in plain Java, servlets, Spring Boot, or AWS Lambda. Test Lambda functions with plain JUnit.
- **Built-in security**: Declarative, annotation-based access control. Secure commands individually without touching infrastructure.
- **Spring-native**: Commands are Spring beans with full support for `@Autowired`, transactions, and the entire Spring ecosystem.
- **Minimal boilerplate**: Focus on business logic, not plumbing.
- **Flexible integration**: Add to existing applications (EJB, JSP) via servlets or build greenfield projects with Spring Boot or Lambda.

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

## Core Concepts

Before diving into integration, let's understand the key concepts:

### Commands
Commands are self-contained units of business logic that implement the `Command<T>` interface:

```java
public class ProcessPayment implements Command<PaymentResult> {
    @Parameter
    private String orderId;
    
    @Parameter
    private BigDecimal amount;

    @Override
    public PaymentResult execute(CommandContext context) {
        // Your business logic here
        return new PaymentResult(...);
    }
}
```

### Command Dispatcher
The `CommandDispatcher` is responsible for:
- Discovering commands in your specified package(s)
- Validating command access permissions
- Deserializing parameters and invoking commands
- Handling errors and serializing responses

### Request Format
All integrations use the same JSON request format:

```json
{
  "command": "ProcessPayment",
  "parameters": {
    "orderId": "ORD-123",
    "amount": 99.99
  },
  "auth": {
    "token": "your-auth-token"
  },
  "protocol": "CM/1.0"
}
```

## Getting Started

### Choosing Your Integration

CommandMosaic supports multiple runtime environments. Choose the one that matches your application:

| Integration Type | Use Case | Artifact ID |
|-----------------|----------|-------------|
| **Plain Java** | Standalone applications, internal command patterns | `commandmosaic-plain-java` |
| **Spring Boot** | Modern web applications, REST APIs | `commandmosaic-spring-boot-autoconfigure` |
| **Servlet** | Legacy Java EE/Jakarta EE apps, JSP apps, EJB servers | `commandmosaic-servlet` |
| **AWS Lambda (Plain)** | Serverless functions without Spring | `commandmosaic-aws-lambda-plain-java` |
| **AWS Lambda (Spring Boot)** | Serverless functions with Spring Boot | `commandmosaic-aws-lambda-springboot` |

---

## Integration Guides

### Plain Java Applications

**Use this when:** You want to use the Command pattern within a standalone Java application without exposing commands as a web service.

#### 1. Add Dependency

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-plain-java</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### 2. Configure and Create Dispatcher

```java
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.factory.CommandDispatcherFactory;
import org.commandmosaic.plain.PlainCommandDispatcherFactory;

// Configure the dispatcher
CommandDispatcherConfiguration config = CommandDispatcherConfiguration.builder()
    .rootPackage("com.example.commands")  // Only commands in this package are allowed
    .build();

// Create the dispatcher
CommandDispatcherFactory factory = PlainCommandDispatcherFactory.getInstance();
CommandDispatcher dispatcher = factory.getCommandDispatcher(config);

// Execute commands
GreetingCommand command = new GreetingCommand("Alice");
String result = dispatcher.dispatchCommand(command, null);
```

#### 3. Implement Your Commands

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;

public class GreetingCommand implements Command<String> {
    
    @Parameter
    private String name;
    
    public GreetingCommand() {
        // Required no-arg constructor
    }
    
    public GreetingCommand(String name) {
        this.name = name;
    }
    
    @Override
    public String execute(CommandContext context) {
        return "Hello, " + name + "!";
    }
}
```

**Best for:** Internal application logic, organizing code with Command pattern, testing command logic.

📚 **[See complete sample application →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-helloworld-sample-app)**

---

### Spring Boot Web Applications

**Use this when:** You're building a modern web application with Spring Boot and want to expose commands via REST API.

#### 1. Add Dependency

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-spring-boot-autoconfigure</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### 2. Configure CommandMosaic

Create a configuration class with the dispatcher configuration:

```java
package com.example.config;

import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandMosaicConfig {
    
    @Bean
    public CommandDispatcherConfiguration commandDispatcherConfig() {
        return CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .build();
    }
}
```

#### 3. Expose Commands via REST Endpoint

Create a controller that delegates to the dispatcher:

```java
package com.example.api;

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

#### 4. Implement Commands as Spring Beans

Commands are Spring-managed beans with full dependency injection support:

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class CreateOrder implements Command<OrderResult> {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Parameter
    private String customerId;
    
    @Parameter
    private List<OrderItem> items;
    
    @Override
    @Transactional
    public OrderResult execute(CommandContext context) {
        // Full Spring features: dependency injection, transactions, etc.
        // Commands are created as prototype-scoped beans automatically
        Order order = orderService.createOrder(customerId, items);
        paymentService.initializePayment(order);
        return new OrderResult(order.getId());
    }
}
```

> **Important:** Do NOT annotate commands with `@Component` or `@Service`. Commands have `@Parameter` fields that receive per-request values, so they must be created fresh for each execution. CommandMosaic automatically registers commands as **prototype-scoped beans** in Spring's bean factory, ensuring thread-safety and proper parameter injection.

#### 5. Call Your API

```bash
curl -X POST http://localhost:8080/api/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "command": "CreateOrder",
    "parameters": {
      "customerId": "CUST-123",
      "items": [{"productId": "PROD-1", "quantity": 2}]
    },
    "protocol": "CM/1.0"
  }'
```

**Best for:** Modern microservices, REST APIs, applications requiring Spring ecosystem features (JPA, transactions, security, etc.).

📚 **[See complete sample application →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-springboot2-sample-app)**

---

### Servlet-Based Applications (Java EE/Jakarta EE)

**Use this when:** You have an existing application running on traditional application servers (Tomcat, Jetty, WildFly, WebLogic, WebSphere) and want to add CommandMosaic without migrating to Spring Boot. Perfect for:
- Legacy JSP applications
- EJB-based applications
- Traditional Java EE/Jakarta EE applications
- Gradual modernization of existing systems

#### 1. Add Dependency

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-servlet</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### 2. Configure Servlet in web.xml

Add the CommandMosaic servlet to your `web.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <servlet>
        <servlet-name>CommandDispatcherServlet</servlet-name>
        <servlet-class>org.commandmosaic.http.servlet.CommandDispatcherServlet</servlet-class>
        
        <!-- Required: Root package for commands -->
        <init-param>
            <param-name>commandDispatcherRootPackage</param-name>
            <param-value>com.example.commands</param-value>
        </init-param>
        
        <!-- Optional: Command interceptors for security, logging, etc. -->
        <init-param>
            <param-name>commandDispatcherInterceptors</param-name>
            <param-value>com.example.security.AuthInterceptor,com.example.logging.LoggingInterceptor</param-value>
        </init-param>
        
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>CommandDispatcherServlet</servlet-name>
        <url-pattern>/api/dispatch</url-pattern>
    </servlet-mapping>
</web-app>
```

#### 3. Implement Your Commands

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;

public class ProcessLegacyTransaction implements Command<TransactionResult> {
    
    @Parameter
    private String accountId;
    
    @Parameter
    private BigDecimal amount;
    
    @Override
    public TransactionResult execute(CommandContext context) {
        // Your business logic - can call EJBs, use JNDI lookups, etc.
        InitialContext ctx = new InitialContext();
        LegacyService service = (LegacyService) ctx.lookup("java:app/LegacyService");
        
        return service.processTransaction(accountId, amount);
    }
}
```

#### 4. Call Your API

```bash
curl -X POST http://localhost:8080/yourapp/api/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "command": "ProcessLegacyTransaction",
    "parameters": {
      "accountId": "ACC-789",
      "amount": 150.00
    },
    "protocol": "CM/1.0"
  }'
```

**Integration Strategy:**
- Deploy alongside existing servlets and JSPs
- Gradually migrate functionality to commands
- Keep existing code running while adding new features with CommandMosaic
- No need to rewrite the entire application

**Best for:** Modernizing legacy applications, adding new API endpoints to existing systems, gradual migration strategies.

---

### AWS Lambda Functions

CommandMosaic is ideal for AWS Lambda because it minimizes infrastructure complexity: **one Lambda function, one API Gateway endpoint, unlimited operations**.

#### Benefits for Lambda

- **Single API endpoint**: Add features without updating API Gateway configuration
- **Test without AWS tooling**: Commands run in plain JUnit tests
- **Unified codebase**: Same commands work in Lambda, servlets, and Spring Boot
- **Cold start optimization**: One function to warm up instead of dozens
- **Cost efficiency**: Fewer Lambda functions = simpler billing and management

#### Option A: AWS Lambda with Spring Boot

**Use this when:** You want the full Spring ecosystem in Lambda (dependency injection, Spring Data, transactions, etc.).

##### 1. Add Dependency

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-aws-lambda-springboot</artifactId>
    <version>2.0.0</version>
</dependency>
```

##### 2. Create Lambda Request Handler

```java
package com.example;

import org.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler;

public class AppRequestHandler extends SpringBootLambdaCommandDispatcherRequestHandler {
    
    public AppRequestHandler() {
        super(Application.class); // Your @SpringBootApplication class
    }
}
```

##### 3. Configure CommandMosaic in Spring

```java
package com.example;

import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandDispatcherConfiguration commandDispatcherConfig() {
        return CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .build();
    }
}
```

##### 4. Implement Commands

Commands are Spring beans with full Spring support:

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessOrder implements Command<OrderResult> {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Parameter
    private String orderId;
    
    @Override
    public OrderResult execute(CommandContext context) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
            
        order.process();
        orderRepository.save(order);
        notificationService.sendConfirmation(order);
        
        return new OrderResult(order);
    }
}
```

> **Important:** Commands should NOT be annotated with `@Component`. They are automatically registered as prototype-scoped beans by CommandMosaic.

##### 5. Module Configuration (Java 9+)

If using Java modules, add to `module-info.java`:

```java
module com.example.app {
    requires org.commandmosaic.aws.lambda.springboot;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;

    opens com.example to
        spring.core, spring.context, spring.beans,
        org.commandmosaic.core;
}
```

##### 6. Deploy to Lambda

1. Package your application as a fat JAR
2. Create Lambda function with handler: `com.example.AppRequestHandler`
3. Configure API Gateway to proxy requests to Lambda
4. Deploy

📚 **[See complete sample application →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-aws-lambda-springboot2-sample-app)**

---

#### Option B: AWS Lambda with Plain Java

**Use this when:** You want minimal dependencies and fastest cold starts without Spring overhead.

##### 1. Add Dependency

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-aws-lambda-plain-java</artifactId>
    <version>2.0.0</version>
</dependency>
```

##### 2. Create Lambda Request Handler

```java
package com.example;

import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.aws.lambda.plain.PlainLambdaCommandDispatcherRequestHandler;

public class AppRequestHandler extends PlainLambdaCommandDispatcherRequestHandler {
    
    public AppRequestHandler() {
        super(CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .build());
    }
}
```

##### 3. Implement Commands

```java
package com.example.commands;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;

public class GetWeather implements Command<WeatherResult> {
    
    @Parameter
    private String city;
    
    @Override
    public WeatherResult execute(CommandContext context) {
        // Your business logic
        // Note: No Spring dependency injection, manage dependencies manually
        WeatherService service = new WeatherService();
        return service.getWeather(city);
    }
}
```

##### 4. Module Configuration (Java 9+)

```java
module com.example.app {
    requires org.commandmosaic.aws.lambda.plain;

    opens com.example to
        org.commandmosaic.core;
}
```

##### 5. Deploy to Lambda

Same deployment process as Spring Boot version, but with faster cold starts and smaller package size.

**Best for:** Serverless architectures, minimizing AWS configuration, rapid feature deployment, cost optimization.

---

### Testing Your Commands

One of CommandMosaic's key benefits is testability. Commands are plain Java objects that can be tested without any container:

```java
@Test
public void testPaymentProcessing() {
    // Arrange
    ProcessPayment command = new ProcessPayment();
    command.setOrderId("ORD-123");
    command.setAmount(new BigDecimal("99.99"));
    
    // Act
    PaymentResult result = command.execute(null);
    
    // Assert
    assertNotNull(result.getTransactionId());
    assertEquals("SUCCESS", result.getStatus());
}
```

For integration testing with Spring:

```java
@SpringBootTest
public class CommandIntegrationTest {
    
    @Autowired
    private CommandDispatcher dispatcher;
    
    @Test
    @Transactional
    public void testCreateOrderWithDatabase() {
        CreateOrder command = new CreateOrder();
        command.setCustomerId("CUST-123");
        command.setItems(Arrays.asList(new OrderItem("PROD-1", 2)));
        
        OrderResult result = dispatcher.dispatchCommand(command, null);
        
        assertNotNull(result.getOrderId());
    }
}
```

**Lambda functions can be tested the same way** - no AWS-specific tooling required!

---

## Security

## Security

CommandMosaic provides declarative, annotation-based security that works consistently across all runtime environments.

### Securing Commands with Annotations

Control access at the command level using simple annotations:

#### Public Access

```java
import org.commandmosaic.security.Access;

@Access.IsPublic
public class GetProductCatalog implements Command<List<Product>> {
    @Parameter
    private String category;
    
    @Override
    public List<Product> execute(CommandContext context) {
        // Anyone can access this command
        return productService.getProducts(category);
    }
}
```

#### Authenticated Access

```java
@Access.RequiresAuthentication  // Any authenticated user
public class UpdateProfile implements Command<Void> {
    @Parameter
    private String email;
    
    @Parameter
    private String phoneNumber;
    
    @Override
    public Void execute(CommandContext context) {
        // Only authenticated users can access
        String userId = context.getUserId();
        userService.updateProfile(userId, email, phoneNumber);
        return null;
    }
}
```

#### Role-Based Access

```java
@Access.RequiresAnyOfTheAuthorities({"ADMIN", "MANAGER"})
public class DeleteUser implements Command<Void> {
    @Parameter
    private String userId;
    
    @Override
    public Void execute(CommandContext context) {
        // Only ADMIN or MANAGER roles can access
        userService.deleteUser(userId);
        return null;
    }
}
```

### Implementing Authentication

Create a security interceptor by extending `DefaultSecurityCommandInterceptor`:

```java
package com.example.security;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JwtSecurityInterceptor extends DefaultSecurityCommandInterceptor {
    
    private final JwtService jwtService;
    
    public JwtSecurityInterceptor() {
        this.jwtService = new JwtService();
    }
    
    @Override
    protected Set<String> attemptLogin(CommandContext context) 
            throws AuthenticationException {
        // Extract authentication data from request
        Map<String, Object> auth = context.getAuth();
        if (auth == null || !auth.containsKey("token")) {
            throw new AuthenticationException("No authentication token provided");
        }
        
        String token = (String) auth.get("token");
        
        try {
            // Validate token and extract user info
            Claims claims = jwtService.validateToken(token);
            
            // Extract roles/authorities from token
            List<String> roles = claims.get("roles", List.class);
            
            return new HashSet<>(roles);
            
        } catch (JwtException e) {
            throw new AuthenticationException("Invalid token: " + e.getMessage());
        }
    }
}
```

### Registering the Security Interceptor

#### Plain Java / Plain Lambda

```java
CommandDispatcherConfiguration config = CommandDispatcherConfiguration.builder()
    .rootPackage("com.example.commands")
    .interceptor(JwtSecurityInterceptor.class)
    .build();
```

#### Spring Boot / Spring Boot Lambda

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public JwtSecurityInterceptor jwtSecurityInterceptor() {
        return new JwtSecurityInterceptor();
    }
    
    @Bean
    public CommandDispatcherConfiguration commandDispatcherConfig() {
        return CommandDispatcherConfiguration.builder()
            .rootPackage("com.example.commands")
            .interceptor(JwtSecurityInterceptor.class)
            .build();
    }
}
```

#### Servlet (web.xml)

```xml
<servlet>
    <servlet-name>CommandDispatcherServlet</servlet-name>
    <servlet-class>org.commandmosaic.http.servlet.CommandDispatcherServlet</servlet-class>
    
    <init-param>
        <param-name>commandDispatcherRootPackage</param-name>
        <param-value>com.example.commands</param-value>
    </init-param>
    
    <init-param>
        <param-name>commandDispatcherInterceptors</param-name>
        <param-value>com.example.security.JwtSecurityInterceptor</param-value>
    </init-param>
</servlet>
```

### Client Authentication

Clients pass authentication data in the `auth` field of the request:

```json
{
  "command": "DeleteUser",
  "parameters": {
    "userId": "USER-123"
  },
  "auth": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "protocol": "CM/1.0"
}
```

The `auth` field accepts any key-value pairs. Common patterns:

**JWT Token:**
```json
"auth": {
  "token": "eyJhbGc..."
}
```

**Username/Password:**
```json
"auth": {
  "username": "john.doe",
  "password": "secret123"
}
```

**API Key:**
```json
"auth": {
  "apiKey": "ak_live_123456789"
}
```

**Multiple credentials:**
```json
"auth": {
  "apiKey": "ak_live_123456789",
  "deviceId": "device-xyz",
  "sessionToken": "sess_abc123"
}
```

### Security Flow

1. Client sends request with `auth` data
2. CommandMosaic calls your `attemptLogin()` method
3. Your interceptor validates credentials and returns roles
4. CommandMosaic checks if returned roles match command's `@Access` annotation
5. If authorized, command executes; otherwise, `AuthorizationException` is thrown

### Integration with Spring Security (Optional)

For Spring Boot applications, you can integrate with Spring Security:

```java
@Component
public class SpringSecurityInterceptor extends DefaultSecurityCommandInterceptor {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Override
    protected Set<String> attemptLogin(CommandContext context) 
            throws AuthenticationException {
        Map<String, Object> auth = context.getAuth();
        String token = (String) auth.get("token");
        
        // Use Spring Security's authentication
        Authentication authentication = 
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(token, null)
            );
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }
}
```

---

## Advanced Topics

### Why Commands Should NOT Be Annotated with @Component

**Important Design Principle:** Commands in Spring-based integrations should **never** be annotated with `@Component`, `@Service`, or any other stereotype annotation that would register them as Spring beans.

#### The Problem

Commands have `@Parameter` fields that receive different values for each request:

```java
public class ProcessPayment implements Command<PaymentResult> {
    @Parameter
    private String orderId;    // Different for each request!
    
    @Parameter
    private BigDecimal amount;  // Different for each request!
    
    // ...
}
```

If you annotate this with `@Component`:
- Spring creates a **singleton** bean by default
- The same command instance is reused across all requests
- Parameter values from one request would leak into another request
- **Race conditions and data corruption** occur in multi-threaded environments

#### How CommandMosaic Solves This

The `SpringCommandExecutor` automatically registers commands as **prototype-scoped beans**:

```java
// From SpringCommandExecutor.java
if (!beanFactory.containsBeanDefinition(commandClassName)) {
    GenericBeanDefinition gbd = new GenericBeanDefinition();
    gbd.setBeanClass(commandClass);
    gbd.setScope(BeanDefinition.SCOPE_PROTOTYPE);  // ← Key: Fresh instance per request
    beanFactory.registerBeanDefinition(commandClassName, gbd);
}
```

For each command execution:
1. A **new command instance** is created (prototype scope)
2. Spring injects dependencies (`@Autowired` services)
3. CommandMosaic injects parameters (`@Parameter` fields)
4. The command executes with isolated state
5. The instance is discarded after execution

#### What You CAN Inject

Commands can use `@Autowired` to inject **singleton services**:

```java
public class CreateOrder implements Command<OrderResult> {
    
    @Autowired
    private OrderService orderService;        // ✓ Singleton service - safe to inject
    
    @Autowired
    private PaymentService paymentService;    // ✓ Singleton service - safe to inject
    
    @Parameter
    private String customerId;                 // ✓ Per-request parameter
    
    @Override
    @Transactional  // ✓ Spring AOP works correctly
    public OrderResult execute(CommandContext context) {
        // Each execution gets a fresh command instance with its own parameters
        // but shares the same service instances (which are stateless singletons)
    }
}
```

#### Summary

- ❌ **Never** use `@Component` on commands
- ✓ Commands are automatically registered as prototype-scoped beans
- ✓ Use `@Autowired` for singleton services
- ✓ Use `@Parameter` for per-request data
- ✓ Spring features (transactions, AOP) work correctly

### Using CommandDispatcherServer

For custom integrations, use `CommandDispatcherServer` to handle HTTP-style requests:

```java
import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.core.server.DefaultCommandDispatcherServer;

CommandDispatcher dispatcher = // ... create dispatcher
CommandDispatcherServer server = new DefaultCommandDispatcherServer(dispatcher);

// Read from InputStream, write to OutputStream
server.serviceRequest(inputStream, outputStream);
```

This abstraction allows integration with any framework that provides request/response streams (Netty, Vert.x, etc.).

### Command Interceptors

Create custom interceptors for cross-cutting concerns:

```java
public class LoggingInterceptor implements CommandInterceptor {
    
    @Override
    public <T> T intercept(Command<T> command, CommandContext context, 
                           InterceptorChain<T> chain) throws Exception {
        long start = System.currentTimeMillis();
        
        try {
            T result = chain.proceed(command, context);
            long duration = System.currentTimeMillis() - start;
            log.info("Command {} executed in {}ms", 
                     command.getClass().getSimpleName(), duration);
            return result;
        } catch (Exception e) {
            log.error("Command {} failed", command.getClass().getSimpleName(), e);
            throw e;
        }
    }
}
```

Register multiple interceptors:

```java
CommandDispatcherConfiguration config = CommandDispatcherConfiguration.builder()
    .rootPackage("com.example.commands")
    .interceptor(LoggingInterceptor.class)
    .interceptor(MetricsInterceptor.class)
    .interceptor(SecurityInterceptor.class)
    .build();
```

### Command Discovery

Commands are discovered by package scanning. The command name is derived from:

**Option 1: Simple class name**
```java
package com.example.commands;

public class ProcessPayment implements Command<PaymentResult> {
    // Invoked as: "ProcessPayment"
}
```

**Option 2: Full package path**
```java
package com.example.commands.payments;

public class ProcessRefund implements Command<RefundResult> {
    // Can be invoked as: "payments/ProcessRefund"
}
```

### Module System Support (Java 9+)

When using Java Platform Module System, ensure your module opens packages to CommandMosaic:

```java
module com.example.app {
    requires org.commandmosaic.spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    
    // Open command packages for reflection
    opens com.example.commands to org.commandmosaic.core;
}
```

---

## Sample Applications

Complete working examples are available:

| Sample | Description | Link |
|--------|-------------|------|
| **Hello World** | Plain Java command pattern | [View →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-helloworld-sample-app) |
| **Spring Boot Web App** | REST API with Spring Boot | [View →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-springboot2-sample-app) |
| **AWS Lambda** | Serverless with Spring Boot | [View →](https://github.com/peter-gergely-horvath/commandmosaic/tree/master/sample-apps/commandmosaic-aws-lambda-springboot2-sample-app) |

### Running Sample Applications

To run sample applications:

1. Download the sample project
2. Open the `pom.xml` file
3. **Remove** the `<parent>...</parent>` section
4. **Uncomment** the sections marked for standalone use
5. Adjust `groupId`/`artifactId` as needed
6. Run `mvn clean install`

---

## Dependency Reference

Choose **one** dependency based on your use case:

| Use Case | Artifact ID | Version |
|----------|-------------|---------|
| Plain Java (no web) | `commandmosaic-plain-java` | 2.0.0 |
| Spring Framework | `commandmosaic-spring` | 2.0.0 |
| Spring Boot | `commandmosaic-spring-boot-autoconfigure` | 2.0.0 |
| Servlet (Java EE/Jakarta EE) | `commandmosaic-servlet` | 2.0.0 |
| AWS Lambda (Plain Java) | `commandmosaic-aws-lambda-plain-java` | 2.0.0 |
| AWS Lambda (Spring Boot) | `commandmosaic-aws-lambda-springboot` | 2.0.0 |

**Maven example:**

```xml
<dependency>
    <groupId>org.commandmosaic</groupId>
    <artifactId>commandmosaic-spring-boot-autoconfigure</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Spring Boot Version Requirements

CommandMosaic requires **Spring Boot 2.x or later**. Spring Boot 1.x is not supported (as it has been deprecated by Pivotal).

---

## Architecture Considerations

### Monolithic vs. Microservices

CommandMosaic applications can be considered "monolithic" in that all commands live in one codebase. However, this approach offers several advantages:

**Benefits:**
- ✅ All application logic in one place
- ✅ Easy refactoring with IDE support
- ✅ Single API to maintain
- ✅ Shared code and utilities
- ✅ Simplified deployment (one artifact)

**Trade-offs:**
- ⚠️ All commands scale together
- ⚠️ Larger deployment artifact
- ⚠️ Cannot independently version commands

**When this works well:**
- Small to medium teams
- Related business domain
- Rapid feature development priorities
- Cost-sensitive projects (fewer running services)

**When to use separate services:**
- Very large teams with independent deployment requirements
- Different scaling needs per feature
- Different tech stacks required
- Strict service boundaries needed

### Lambda Considerations

For AWS Lambda, CommandMosaic's single-function approach differs from the microservices ideal of "one function per operation." Consider:

**Advantages:**
- Simpler API Gateway configuration
- Easier testing (no AWS-specific tooling)
- Potentially lower costs (fewer functions)
- Faster development iteration

**Trade-offs:**
- Larger function size (longer cold starts)
- All operations share timeout/memory limits
- Cannot optimize individual operations

**Best practices:**
- Keep command packages focused and modular
- Use multiple CommandMosaic functions for distinctly different domains
- Monitor function metrics to identify bottlenecks
- Consider splitting if cold start time becomes an issue

---

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues on [GitHub](https://github.com/peter-gergely-horvath/commandmosaic).

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
