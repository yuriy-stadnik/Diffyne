# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Test Commands
- Build: `./mvnw clean install`
- Run application: `./mvnw spring-boot:run`
- Run all tests: `./mvnw test`
- Run single test: `./mvnw test -Dtest=TestClassName#methodName`
- Run with specific profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

## Architecture Guidelines
- REST-only approach: This project uses REST APIs for data access, no direct database connections
- In-memory repositories are used for data storage during runtime
- Use connectors to access external data sources through their APIs

## Code Style Guidelines
- Java version: 21
- Use Java's standard naming conventions (camelCase for methods/variables, PascalCase for classes)
- Organize imports alphabetically, remove unused imports
- Use Spring Boot annotations appropriately (@Service, @RestController, etc.)
- Follow RESTful API design principles for controllers and client connections
- Use constructor injection for Spring dependencies
- Prefer immutable objects where possible
- Include meaningful JavaDoc comments for public APIs
- Use slf4j for logging
- Handle exceptions properly with meaningful error messages
- Use Spring's data validation for input validation
- Implement interfaces with in-memory solutions