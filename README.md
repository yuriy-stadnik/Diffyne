# Diffyne

Diffyne is a powerful tool for comparing data across different sources with a REST-first approach. It enables users to identify discrepancies between data sets quickly and efficiently.

## Overview

Diffyne allows you to:

- Compare data from multiple REST API endpoints
- Detect differences between data sources
- Identify records that exist in one source but not the other
- Find field-level differences within matching records
- Map fields between sources with different naming conventions or schemas
- Configure tolerance levels for numeric comparisons
- Schedule and manage comparison jobs

## Key Features

- **REST-First Architecture**: All data access is through REST APIs
- **Flexible Comparison**: Configure key fields, fields to compare with name mapping support for different schemas, and tolerance levels
- **Multiple Source Types**: Support for comparing data from different source types (REST APIs, Kafka, etc.)
- **In-Memory Storage**: Uses in-memory repositories for efficient runtime storage
- **Comprehensive Analysis**: Detailed reports showing matched, mismatched, and unique records
- **Field-Level Differences**: Identifies exactly which fields differ between records
- **Field Name Mapping**: Map fields between sources with different schemas or naming conventions

## Usage Examples

### Compare Two REST API Sources

```bash
curl -X POST "http://localhost:8081/api/direct-comparisons/rest-api" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceOneUrl": "https://api.source1.com/customers",
    "sourceOneParams": {
      "authToken": "<YOUR_SOURCE_API_TOKEN>",
      "recordsPath": "data.items"
    },
    "sourceTwoUrl": "https://api.source2.com/users",
    "sourceTwoParams": {
      "authToken": "<YOUR_TARGET_API_TOKEN>",
      "recordsPath": "users"
    },
    "comparisonConfig": {
      "keyFields": ["id"],
      "fieldsToCompare": {
        "name": "name",
        "email": "email", 
        "status": "status",
        "balance": "balance",
        "source_specific_field": "target_different_field_name"
      },
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

### Configuration

The application runs with default settings and requires no additional configuration. You can customize behavior by creating an `application-local.properties` file in `src/main/resources/` if needed:

```properties
# Server configuration
server.port=8081

# Logging configuration
logging.level.com.syv.data.Diffyne=INFO
```

### Running the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Note**: The application is designed with a REST-first approach and uses in-memory storage for all operations.

## Project Structure

- **Controllers**: REST endpoints for triggering comparisons and retrieving results
- **Services**: Core comparison and data extraction logic
- **Connectors**: Adapters for different data source types (REST, Kafka, etc.)
- **Models**: Data structures for comparisons, results, and differences
- **Repositories**: In-memory storage interfaces for runtime data

## Field Mapping

Diffyne supports comparing data sources with different field naming conventions through field mapping. This is achieved by configuring the `fieldsToCompare` parameter as a map instead of an array:

```json
"fieldsToCompare": {
  "source_field1": "target_field1",
  "source_field2": "target_field2",
  "common_field": "common_field"
}
```

In this example:
- `source_field1` in the source data will be compared with `target_field1` in the target data
- `source_field2` in the source data will be compared with `target_field2` in the target data
- Fields with the same name in both sources can be mapped with the same name

This feature is particularly useful when:
- Two systems use different naming conventions for the same data
- You need to compare data across different API schemas
- You're comparing data from systems that have evolved differently over time

## Testing

The project includes comprehensive unit and integration tests for all components.

```bash
# Run all tests
./mvnw test

# Run a specific test
./mvnw test -Dtest=DirectComparisonControllerTest
```