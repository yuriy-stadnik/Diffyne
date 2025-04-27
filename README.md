# Diffyne

Diffyne is a powerful tool for comparing data across different sources with a REST-first approach. It enables users to identify discrepancies between data sets quickly and efficiently.

## Overview

Diffyne allows you to:

- Compare data from multiple REST API endpoints
- Detect differences between data sources without direct database connections
- Identify records that exist in one source but not the other
- Find field-level differences within matching records
- Configure tolerance levels for numeric comparisons
- Schedule and manage comparison jobs

## Key Features

- **REST-First Architecture**: All data access is through REST APIs, with no direct database connections
- **Flexible Comparison**: Configure key fields, fields to compare, and tolerance levels
- **Multiple Source Types**: Support for comparing data from different source types (REST APIs, Kafka, etc.)
- **In-Memory Storage**: Uses in-memory repositories for efficient runtime storage
- **Comprehensive Analysis**: Detailed reports showing matched, mismatched, and unique records
- **Field-Level Differences**: Identifies exactly which fields differ between records

## Usage Examples

### Compare Two REST API Sources

```bash
curl -X POST "http://localhost:8080/api/direct-comparisons/rest-api" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceOneUrl": "http://localhost:8080/rjp/query?connection=DB1&sqlQuery=select%20*%20from%20customer%20where%20id%3C800",
    "sourceOneParams": {
      "authToken": "<YOUR_SOURCE_API_TOKEN>",
      "recordsPath": "data.items"
    },
    "sourceTwoUrl": "http://localhost:8080/rjp/query?connection=DB2&sqlQuery=select%20*%20from%20customer%20where%20id%3C800",
    "sourceTwoParams": {
      "authToken": "<YOUR_TARGET_API_TOKEN>",
      "recordsPath": "data.items"
    },
    "comparisonConfig": {
      "keyFields": ["id"],
      "fieldsToCompare": ["name", "email", "status", "balance"],
      "toleranceLevels": {
        "balance": 0.1
      },
      "ignoreCase": true
    }
  }'
```

## Setup & Configuration

### Requirements

- Java 21
- Spring Boot 3.4.4
- Maven

### Running the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Project Structure

- **Controllers**: REST endpoints for triggering comparisons and retrieving results
- **Services**: Core comparison and data extraction logic
- **Connectors**: Adapters for different data source types (REST, Kafka, etc.)
- **Models**: Data structures for comparisons, results, and differences
- **Repositories**: In-memory storage interfaces for runtime data

## Testing

The project includes comprehensive unit and integration tests for all components.

```bash
# Run all tests
./mvnw test

# Run a specific test
./mvnw test -Dtest=DirectComparisonControllerTest
```