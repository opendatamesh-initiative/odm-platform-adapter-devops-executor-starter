# Dummy Executor API

## Overview

The **Dummy Executor API** is a simple RESTful service built with Spring Boot to test ODM Task. It provides an endpoint
to submit tasks that are executed asynchronously. The service processes each task, simulates a delay, and optionally
sends a callback with the result.

## Project Structure

- **Controller**: Handles incoming HTTP requests and delegates processing to the service layer.
- **Service**: Contains the business logic for task execution and callback handling.
- **Resources**: Defines the data structures used for tasks and task results.

## Key Features

- Asynchronous task execution using `@Async`.
- Simulated processing delay to mimic real-world operations.
- Callback mechanism to notify external services with the task results.

## Technologies Used

- **Spring Boot** for building the REST API.
- **Spring Scheduling** for asynchronous method execution.
- **RestTemplate** for making HTTP calls.
- **SLF4J & Logback** for logging.

## How to Set Up

### Prerequisites

- **Java 11 or higher**
- **Maven 3.6+**
- **Spring Boot** (embedded)
- **ODM Platform**

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/dummy-executor-api.git
   cd dummy-executor-api

## How To Use

### 1. ODM

To use the executor, the DevOps and Registry modules need to be properly configured and running. For a complete guide on
ODM, visit [ODM Documentation](https://dpds.opendatamesh.org/).

### 2. DevOps Module Application File Setup

Within the `properties` file of the ODM DevOps module, include the following property:

```yaml
utilityPlane:
  executorServices:
    dummy:
      active: true
      address: http://localhost:9080
      checkAfterCallback: false
```

### 3. Create a Data Product

Once the ODM Registry server is operational, define a data product and its corresponding version. To create a Data Product, execute a POST request on the following endpoint: `/api/v1/pp/registry/products`    with the payload:

```json
{
  "fullyQualifiedName": "urn:org.opendatamesh:dataproducts:demo-domain-dpc",
  "description": "Description",
  "domain": "demoDomain"
}
```

### 4. Create a Data Product Version

After successfully creating a Data Product, create a version by executing a POST request on the endpoint `/api/v1/pp/registry/products/:id/versions`, where `:id` is the identifier of the previously created data product. The request body should be structured as follows:
```json
{
  "dataProductDescriptor": "1.0.0",
  "info": {
    "fullyQualifiedName": "urn:org.opendatamesh:dataproducts:demo-domain-dpc",
    "domain": "demoDomain",
    "name": "demo-domain-dpc",
    "version": "1.0.0",
    "displayName": "DEMO Domain DP",
    "description": "DEMO",
    "x-businessUnit": "Demo",
    "x-product": "Payroll",
    "x-subdomain": "Payroll",
    "x-scope": "externalData",
    "owner": {
      "id": "demo@demo.com",
      "name": "John Doe"
    },
    "contactPoints": []
  },
  "interfaceComponents": {
    "inputPorts": [],
    "outputPorts": []
  },
  "internalComponents": {
    "lifecycleInfo": {
      "dev": [
        {
          "service": {
            "$href": "dummy"
          },
          "template": {
            "specification": "spec",
            "specificationVersion": "2.0",
            "definition": {}
          },
          "configurations": {
            "params": {},
            "stagesToSkip": []
          }
        }
      ]
    }
  }
}
```

### 5. Start DevOps Server

Ensure that the DevOps server for ODM is started and running correctly.

### 6. Start Activity on Blindata

Navigate to the data product version detail page within Blindata and select "Plan Activity" to create an activity. Note that activities must be pre-defined in the internalComponents.lifecycleInfo section of the data product descriptor.

Once the activity is planned, click "Start Activity" on the activity detail page in Blindata to commence the process.