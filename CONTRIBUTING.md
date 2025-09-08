# Contributing to Diffyne

We welcome contributions to Diffyne! This document provides guidelines for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project adheres to a code of conduct that we expect all contributors to follow. Please be respectful and constructive in all interactions.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Set up the development environment
4. Make your changes
5. Submit a pull request

## Development Setup

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Git

### Setup Steps

```bash
# Clone your fork
git clone https://github.com/your-username/Diffyne.git
cd Diffyne

# Build the project
./mvnw clean install

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run
```

## How to Contribute

### Types of Contributions

We welcome various types of contributions:

- **Bug fixes**: Fix issues and improve stability
- **Features**: Add new functionality
- **Documentation**: Improve or add documentation
- **Tests**: Add or improve test coverage
- **Performance**: Optimize existing code

### Before You Start

1. Check existing issues to avoid duplicating work
2. For major features, create an issue first to discuss the approach
3. Ensure your contribution aligns with the project's goals

## Pull Request Process

1. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the coding standards

3. **Add tests** for new functionality

4. **Run the test suite**:
   ```bash
   ./mvnw test
   ```

5. **Update documentation** if needed

6. **Commit your changes** with clear, descriptive messages:
   ```bash
   git commit -m "Add feature: description of what you added"
   ```

7. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Create a pull request** with:
   - Clear title and description
   - Reference to any related issues
   - Description of changes made
   - Any breaking changes

## Coding Standards

### Java Code Style

- Use Java 21 features appropriately
- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Organize imports alphabetically and remove unused imports
- Use Spring Boot annotations appropriately (@Service, @RestController, etc.)
- Follow RESTful API design principles
- Use constructor injection for Spring dependencies
- Prefer immutable objects where possible
- Use slf4j for logging
- Handle exceptions properly with meaningful error messages
- Use Lombok annotations to reduce boilerplate code

### Code Organization

- Keep methods focused and concise
- Use meaningful names for variables, methods, and classes
- Add JavaDoc comments for public APIs
- Organize code into logical packages following the existing structure

### REST API Guidelines

- Use appropriate HTTP methods (GET, POST, PUT, DELETE)
- Return appropriate HTTP status codes
- Use consistent URL patterns
- Include proper error responses

## Testing

### Test Requirements

- Write unit tests for all new functionality
- Maintain or improve test coverage
- Use meaningful test names that describe what is being tested
- Include both positive and negative test cases
- Test edge cases and error conditions

### Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=YourTestClass

# Run a specific test method
./mvnw test -Dtest=YourTestClass#yourTestMethod
```

### Test Structure

- Use the existing test structure as a guide
- Place unit tests in `src/test/java`
- Use `@SpringBootTest` for integration tests
- Mock external dependencies appropriately

## Reporting Issues

When reporting issues, please include:

1. **Clear description** of the problem
2. **Steps to reproduce** the issue
3. **Expected behavior** vs actual behavior
4. **Environment details** (Java version, OS, etc.)
5. **Error messages** or logs if applicable
6. **Sample code** if relevant

## Questions?

If you have questions about contributing, feel free to:

- Open an issue for discussion
- Reach out to the maintainers
- Check existing documentation and issues for answers

Thank you for contributing to Diffyne!