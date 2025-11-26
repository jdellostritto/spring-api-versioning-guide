# API Versioning Patterns - Tutorial

A comprehensive guide to implementing API versioning, lifecycle management, and deprecation strategies in Spring Boot microservices.

## Table of Contents

1. [Motivation](#motivation)
2. [Project Goals](#project-goals)
3. [Getting Started](#getting-started)
4. [URI Conventions](#uri-conventions)
5. [API Documentation](#api-documentation)
6. [API Lifecycle Management](#api-lifecycle-management)
7. [Media Type Versioning](#media-type-versioning)
8. [Key Concepts](#key-concepts)
9. [Testing](#testing)

## Motivation

As your backend API grows and evolves, you face a critical challenge: how do you make breaking changes without disrupting existing clients? The solution requires implementing a robust API lifecycle management strategy.

Without proper versioning and deprecation practices, you become a bottleneck:

- Internal teams wait for you to stabilize APIs before integrating
- External partners need guarantees of API stability
- Your service becomes difficult to evolve as the user base grows

**API lifecycle management** is the answer. It allows you to evolve your APIs confidently while maintaining backward compatibility and giving clients clear migration paths.

## Project Goals

This project demonstrates:

- ✅ URI conventions for consistent API paths
- ✅ Versioning using content-type media types (not URL versioning)
- ✅ API lifecycle patterns: adding, moving, and deprecating resources
- ✅ Breaking vs. non-breaking representation changes
- ✅ Comprehensive JavaDoc and deprecation annotations
- ✅ OpenAPI/Swagger integration for API discovery

## Getting Started

### Project Setup

This project uses:

- **Spring Boot 3.5.7** with WebFlux (non-blocking reactive APIs)
- **Java 21** (source and target compatibility)
- **Gradle 9.2.0** for build management
- **SpringDoc OpenAPI 2.3.0** (supports OpenAPI 3.0.x specification)
- **Lombok** for reducing boilerplate code

### Build & Run

```bash
# Build the project
./gradlew clean build

# Run the service
./gradlew bootRun

# Generate JavaDoc
./gradlew javadoc

# Run tests
./gradlew test
```

The service starts on `http://localhost:8700`

## URI Conventions

### Path Structure

```text
/flip/{resource}/{operation}
```

- `flip` = Product category or domain
- `{resource}` = API resource name (greeting, departing, etc.)
- `{operation}` = Specific operation (greet, depart, etc.)

**Examples:**

- `/flip/greeting/greet` - Greeting resource endpoint
- `/flip/departing/depart` - Departing resource endpoint

### Why No Version in URL?

We intentionally avoid versioning in the URL path (e.g., `/v1/resource`). Here's why:

1. **Immutable URLs** - Clients discover resources through consistent URLs
2. **Flexible Evolution** - Individual resources can evolve independently without full API replication
3. **Better Scalability** - Adding a new API version doesn't require duplicating entire URI trees
4. **HATEOAS Compliant** - Follows REST best practices for hypermedia-driven APIs

Read more: [REST API Versioning Best Practices](https://www.baeldung.com/rest-versioning)

## API Documentation

### JavaDoc Best Practices

All classes, methods, and public fields should be documented with JavaDoc. Use these tags:

#### Class-Level Documentation

```java
/**
 * <p>Brief description of the class purpose and responsibilities.</p>
 *
 * <p>Additional context about the class, its evolution, or important notes:</p>
 * Updates 1.1 - Added new endpoint
 * Updates 1.2 - Moved resource to new controller
 * Updates 1.3 - Breaking change to representation
 *
 * @author  <a href="mailto:your.email@example.com">Your Name</a>
 * @version 1.3
 */
```

#### Method-Level Documentation

```java
/**
 * <p>Brief description of what the method does.</p>
 *
 * @param name          Parameter description
 * @return mono         Description of return value
 * @see RelatedClass
 * @since 1.3
 */
```

#### Field-Level Documentation

```java
/**
 * Brief description of the field.
 * @since 1.0
 */
private String fieldName;
```

### Generating Documentation

```bash
./gradlew javadoc
```

Generated docs are available at: `build/docs/javadoc/index.html`

## API Lifecycle Management

### 1. Adding a New Endpoint (Non-Breaking Change)

When adding a new endpoint without breaking existing ones:

1. **Update controller version** - Increment class version in JavaDoc
2. **Mark new endpoint** - Use `@since` tag to document when added
3. **No media type change** - Keep same media type version as new feature is backward compatible

**Example:**

```java
/**
 * @version 1.1  // Updated from 1.0
 */
public class GreetingController {

    /**
     * New endpoint added in this release
     * @since 1.1
     */
    @GetMapping(value = "/status")
    public Mono<StatusDTO> status() { ... }
}
```

### 2. Deprecating an Endpoint

Use both Java `@Deprecated` annotation and JavaDoc `@deprecated` tag:

```java
/**
 * <p>This endpoint has been moved.</p>
 *
 * @deprecated As of release 1.2, use {@link DepartController#depart()} instead
 * @since 1.0
 */
@Deprecated(since = "1.2", forRemoval = true)
@GetMapping(value = "/oldEndpoint")
public Mono<DTO> oldEndpoint() { ... }
```

**Key attributes:**

- `since` - When deprecation started
- `forRemoval` - Whether endpoint will be removed (true) or kept indefinitely (false)

### 3. Moving a Resource to New Controller

Moving an endpoint is a non-breaking change if:

- The representation (DTO) doesn't change
- The new URI is discoverable to clients
- Old endpoint remains available (deprecated)

**Steps:**

1. Create new controller with endpoint
2. Mark old endpoint as `@Deprecated` with link to new location
3. Communicate migration path to API users
4. Keep both endpoints running during deprecation period

### 4. Adding Fields to a DTO (Non-Breaking)

Adding new optional fields to a DTO is backward compatible:

```java
/**
 * <p>DTO with extended fields.</p>
 * @version 1.1
 */
@Data
public class GreetingDTO {
    private long id;
    private String content;
    
    /**
     * New field added in v1.1
     * @since 1.1
     */
    @Nullable
    private String timestamp;  // Null for old clients
}
```

Clients ignore unknown fields, so they continue to function.

### 5. Breaking Changes to Representation

Removing or restructuring DTO fields requires a **new media type version**:

```java
// V2 without id field (breaking change from V1)
/**
 * <p>Greeting response without counter.</p>
 * @version 1.0
 */
@Data
public class GreetingDTOV2 {
    private String content;  // id removed
}

// Controller with both versions
@GetMapping(value = "/greet", produces = "application/vnd.flipfoundry.greeting.v1+json")
public Mono<GreetingDTO> greetV1(...) { ... }

@GetMapping(value = "/greet", produces = "application/vnd.flipfoundry.greeting.v2+json")
public Mono<GreetingDTOV2> greetV2(...) { ... }
```

Clients request the specific version via `Accept` header:

```bash
# V1 with counter
curl -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
     http://localhost:8700/flip/greeting/greet

# V2 without counter
curl -H "Accept: application/vnd.flipfoundry.greeting.v2+json" \
     http://localhost:8700/flip/greeting/greet
```

## Media Type Versioning

### Why Media Types?

Media types allow you to evolve individual resources independently without creating parallel URI trees:

```text
Without Media Type Versioning (URL-based):
/api/v1/resource
/api/v2/resource  <- Must duplicate all resources

With Media Type Versioning:
/api/resource  <- Single URL, multiple versions via Accept header
  Accept: application/vnd.company.resource.v1+json
  Accept: application/vnd.company.resource.v2+json
```

### Media Type Format

```text
application/vnd.{vendor}.{category}.{resource}.v{version}+{suffix}
```

**Example:**

```text
application/vnd.flipfoundry.greeting.v1+json
           └─ flipfoundry  └─ greeting  └─ v1 └─ json
```

### IANA Vendor Tree

Media types are managed by IANA (Internet Assigned Numbers Authority). The vendor tree prefix `vnd.` is used for vendor-specific types:

```text
application/vnd.flipfoundry.greeting.v1+json
application/vnd.flipfoundry.departing.v1+json
```

Reference: [IANA Media Types](https://www.iana.org/assignments/media-types/media-types.xhtml)

## Key Concepts

### Non-Breaking vs. Breaking Changes

| Change | Type | Impact |
|--------|------|--------|
| Add new endpoint | Non-breaking | No client changes needed |
| Add optional field to DTO | Non-breaking | Clients ignore unknown fields |
| Remove required field | Breaking | Need new media type version |
| Rename field | Breaking | Need new media type version |
| Change data type | Breaking | Need new media type version |
| Move endpoint to new URI | Non-breaking | Use HATEOAS; keep old deprecated |

### Deprecation Timeline

1. **Announce** - Document deprecation clearly (JavaDoc + communication)
2. **Continue Support** - Keep endpoint running for 1-2 release cycles
3. **Communicate End-of-Life** - Notify users when endpoint will be removed
4. **Remove** - Delete deprecated endpoint in major version release

### Content Negotiation

When a client requests an endpoint without specifying a media type:

```bash
curl http://localhost:8700/flip/greeting/greet
# Returns 406 Not Acceptable
```

Clients **must** specify the version they support:

```bash
curl -H "Accept: application/vnd.flipfoundry.greeting.v2+json" \
     http://localhost:8700/flip/greeting/greet
# Returns 200 OK with v2 response
```

## Testing

### Test the Endpoints

**V2 Greeting (Current):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=World" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"

# Response:
{"content":"Hello, World!"}
```

**V1 Greeting (Deprecated):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=World" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"

# Response:
{"id":1,"content":"Hello, World!"}
```

**Departing Endpoint:**

```bash
curl -X GET "http://localhost:8700/flip/departing/depart" \
  -H "Accept: application/vnd.flipfoundry.departing.v1+json"

# Response:
{"content":"Goodbye","date":"11/17/2025 19:35:12:456"}
```

**Invalid Media Type (406):**

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet" \
  -H "Accept: application/vnd.flipfoundry.invalid.v1+json"

# Response: 406 Not Acceptable
```

### Run Tests

```bash
./gradlew test
```

All tests should pass with 100% coverage of controller endpoints and DTO contracts.

## Summary

This project demonstrates a professional, scalable approach to API evolution:

✅ **Clear Versioning** - Media types provide flexibility without URL duplication
✅ **Backward Compatibility** - Old clients continue working during deprecation
✅ **Clear Communication** - JavaDoc and deprecation annotations guide developers
✅ **Proper Documentation** - OpenAPI/Swagger integration with well-documented endpoints
✅ **Extensible Patterns** - Strategies scale across multiple resources and versions

## References

- [REST API Versioning Best Practices](https://www.baeldung.com/rest-versioning)
- [REST APIs Must Be Hypertext Driven](https://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven)
- [Spring WebFlux Documentation](https://spring.io/projects/spring-webflux)
- [Project Reactor Documentation](https://projectreactor.io/)
- [IANA Media Types](https://www.iana.org/assignments/media-types/media-types.xhtml)
- [OpenAPI Specification](https://swagger.io/specification/)
