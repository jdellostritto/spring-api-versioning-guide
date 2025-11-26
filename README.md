# Spring API Versioning Guide

A Spring Boot tutorial demonstrating API versioning strategies, lifecycle management, and deprecation patterns using content-negotiation via media types.

## Project Overview

This tutorial project demonstrates API versioning, lifecycle management, and deprecation strategies using Spring Boot 3.5.7 with WebFlux, Java 21, and content-negotiation via media types.

**Current Versions:**

- Spring Boot: 3.5.7
- Java: 21 (source and target compatibility)
- Project Version: 0.0.1-SNAPSHOT
- Spring Doc OpenAPI: 2.3.0

## Motivation

You are on the backend team building your company's latest and greatest features. You work with the product team to maintain and advance the backend business logic. A part of your backend is also available for integration with your customer's system. You must provide updated documentation strategies, versioning, and API evolution.

Imagine waiting for every internal and external customer to test and be comfortable with the changes after you update the backend. You've just become a bottleneck that will never scale. As the company grows, you will have to implement API lifecycle practices.

"A stable API lifecycle is an essential part of an effective API governance strategy, as it lays the groundwork for stage-specific policies and processes that support collaboration and enable organizations to maximize the value of their API portfolio." Ref-1.

## Our Goals

The scope of this document is the tactical creation and evolution of an API (in Java), for example. It covers the following:

- URI conventions for our API across API offerings
- Versioning using content Media Types
- API Lifecycle examples with Deprecation Annotations
- Adding a resource
- Moving a resource
- Adding to a representation
- Removing/modifying a representation
- Documenting and Deprecations with OpenAPI/SwaggerUI/JavaDocs

## Current API Structure

### Endpoints

The project currently features two controllers:

**GreetingController** (`/flip/greeting/`)

- `GET /greet` (v1) - Returns greeting with counter (deprecated in 1.3)
  - Media Type: `application/vnd.flipfoundry.greeting.v1+json`
  - Returns: `GreetingDTO` with `id` and `content` fields
  
- `GET /greet` (v2) - Returns greeting without counter (current)
  - Media Type: `application/vnd.flipfoundry.greeting.v2+json`
  - Returns: `GreetingDTOV2` with `content` field only

- `GET /depart` (deprecated in 1.2) - Moved to DepartController
  - Media Type: `application/vnd.flipfoundry.greeting.v1+json`

**DepartController** (`/flip/departing/`)

- `GET /depart` - Returns goodbye message with timestamp (current)
  - Media Type: `application/vnd.flipfoundry.departing.v1+json`
  - Returns: `DepartDTO` with `content` and `date` fields

## Build Configuration

### build.gradle

```gradle
plugins {
    id 'org.springframework.boot' version '3.5.7'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.flipfoundry'
version = '0.0.1-SNAPSHOT'
java {
    sourceCompatibility = '21'
    targetCompatibility = '21'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    /* actuator */
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    /* webflux  */
    implementation 'org.springframework.boot:spring-boot-starter-webflux'

    /* Springdoc  */
    implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'

    /* lombok   */
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    /* test     */
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
}

javadoc {
    destinationDir = file("${buildDir}/docs/javadoc")
    include 'com/flipfoundry/tutorial/api/application/web/**'
    // Suppress warnings about Lombok-generated default constructors lacking documentation
    // These are safe to ignore as Lombok annotations are well-documented
    options.addStringOption('Xdoclint:all,-missing', '-quiet')
    failOnError = false
}
```

### application.yml

```yaml
# ===============================
# = Spring Profiles
# ===============================
spring:
  profiles:
    active: default
    locale: en

# ===============================
# = Server Ports
# ===============================
server:
  port: 8700

```

## Source Code

### DTOs (Data Transfer Objects)

- **[GreetingDTO.java](src/main/java/com/flipfoundry/tutorial/api/application/web/dto/GreetingDTO.java)** (V1 - Deprecated)
  - Contains `id` (counter) and `content` fields
  - Marked as `@Deprecated` as of release 1.3
  - Use `GreetingDTOV2` instead

- **[GreetingDTOV2.java](src/main/java/com/flipfoundry/tutorial/api/application/web/dto/GreetingDTOV2.java)** (Current)
  - Contains `content` field only
  - Breaking change from V1: removed `id` field
  - Current recommended version

- **[DepartDTO.java](src/main/java/com/flipfoundry/tutorial/api/application/web/dto/DepartDTO.java)**
  - Contains `content` and optional `date` fields
  - Used by the `/flip/departing/depart` endpoint
  - Date field populated with current timestamp in ISO format

### Controllers

- **[GreetingController.java](src/main/java/com/flipfoundry/tutorial/api/application/web/controller/GreetingController.java)**
  - Implements `/flip/greeting/greet` endpoint with V1 and V2 versions
  - V1 endpoint (deprecated): Returns greeting with atomic counter
  - V2 endpoint (current): Returns greeting without counter
  - Also contains deprecated `/flip/greeting/depart` endpoint (moved to `DepartController`)

- **[DepartController.java](src/main/java/com/flipfoundry/tutorial/api/application/web/controller/DepartController.java)**
  - Implements `/flip/departing/depart` endpoint
  - Returns goodbye message with current timestamp
  - Replaces deprecated endpoint from `GreetingController`

## Testing the API

### Run the Service

```bash
./gradlew bootRun
```

### Test Endpoints with curl

**Greeting V2 (Current):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=World" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
```

Response:

```json
{"content":"Hello, World!"}
```

**Greeting V1 (Deprecated):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=World" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
```

Response:

```json
{"id":1,"content":"Hello, World!"}
```

**Departing Endpoint (Current):**

```bash
curl -X GET "http://localhost:8700/flip/departing/depart" \
  -H "Accept: application/vnd.flipfoundry.departing.v1+json"
```

Response:

```json
{"content":"Goodbye","date":"11/13/2025 14:30:45:123"}
```

**Greeting Depart Endpoint (Deprecated - moved to `/flip/departing/depart`):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/depart" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
```

Response (deprecated as of release 1.2):

```json
{"content":"Goodbye"}
```

> **Note:** This endpoint is deprecated as of release 1.2 and has been moved to `GET /flip/departing/depart`. Use the new endpoint instead.

### Invalid Media Type (406 Not Acceptable)

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet" \
  -H "Accept: application/vnd.flipfoundry.invalid.v1+json"
```

Response: 406 Not Acceptable

## Build and Test

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests GreetingWebApplicationTests

# Generate JavaDoc
./gradlew javadoc
```

## Key Concepts

### URI Conventions

Base path format: `/flip/{resource}/`

- `flip` = product category/cell
- `{resource}` = resource name (greeting, departing, etc.)

Example paths:

- `/flip/greeting/greet` - Greeting resource endpoint
- `/flip/departing/depart` - Departing resource endpoint

### Media Type Versioning

Format: `application/vnd.flipfoundry.{resource}.v{version}+json`

Examples:

- `application/vnd.flipfoundry.greeting.v1+json` - Greeting V1 (with id counter)
- `application/vnd.flipfoundry.greeting.v2+json` - Greeting V2 (without id counter)
- `application/vnd.flipfoundry.departing.v1+json` - Departing endpoint

### API Lifecycle Management

1. **Adding Endpoints**: Add new endpoint alongside existing ones, increment controller version
2. **Breaking Changes**: Create new media type version, mark old one as deprecated
3. **Deprecation**: Use `@Deprecated` annotation and JavaDoc `@deprecated` tags
4. **Representation Changes**:
   - Non-breaking: Add optional fields (clients ignore unknown fields)
   - Breaking: Create new DTO version with different media type
5. **Moving Resources**: Move endpoint to new controller, keep deprecated endpoint with redirect

## References

- [REST API Versioning Best Practices](https://www.baeldung.com/rest-versioning)
- [REST APIs Must Be Hypertext Driven](https://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven)
- [Spring WebFlux Documentation](https://spring.io/projects/spring-webflux)
- [Project Reactor Documentation](https://projectreactor.io/)
- [IANA Media Types](https://www.iana.org/assignments/media-types/media-types.xhtml)
