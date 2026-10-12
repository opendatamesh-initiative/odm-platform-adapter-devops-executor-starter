# Starter executor

Reference implementation of the DevOps executor v2 API. It simulates pipeline runs in memory for end-to-end tests and demos. Runs are dropped when the process stops.

The service listens on port **9080**.

## Requirements

- Java 21
- Maven 3.6+ (or the included `./mvnw` wrapper)

## Run

```bash
./mvnw spring-boot:run
```

## Endpoints

| Method | Path | Request | Response |
| --- | --- | --- | --- |
| `POST` | `/api/v2/up/executor/tasks/start` | JSON body with `executorParameters` and `pipelineParameters` | `{ "providerRunId": "starter-<uuid>" }` |
| `GET` | `/api/v2/up/executor/tasks/status?providerRunId=` | Query parameter `providerRunId` | `{ "providerRunId", "status" }` where `status` is `RUNNING`, `SUCCEEDED`, or `FAILED` |
| `GET` | `/api/v2/up/executor/tasks/logs?providerRunId=` | Query parameter `providerRunId` | `{ "content", "generatedAt" }` |

A start request without `executorParameters` returns **400**, and the body names `executorParameters`. An unknown `providerRunId` returns **404**.

`executorParameters` carries `repositoryKey`, `dataProductRepo` (`providerType`, `providerBaseUrl`, `externalIdentifier`, `name`, `ownerId`, `ownerType`, `remoteUrlHttp`, `defaultBranch`), `ref` (`name`, `type`), and `pipelineIdentifier`. Unknown JSON properties are ignored. No activity or task identifiers are required.

## Simulation

Defaults (`executor.simulation` in `application.yml`):

- `run-duration`: `5s`
- `outcome`: `SUCCEEDED`

A run stays `RUNNING` until `startedAt` plus the planned duration, then reports the planned outcome.

Two reserved pipeline parameters override those defaults for that run. The starter reads them the way a pipeline reads its own inputs.

- `starter.outcome`: `SUCCEEDED` or `FAILED`, compared case-insensitively. Any other value keeps the configured default.
- `starter.durationSeconds`: a non-negative integer number of seconds. Any other value keeps the configured default.

The logs endpoint returns one block. It lists the run id, repository name, `repositoryKey`, ref name and type, pipeline identifier, pipeline parameters as received, the names of request headers whose names start with `x-odm-` (never the header values), the start and end times, and the outcome.

Each call writes one info log line that contains only the provider run id. Header values, secret values, and parameter values are never written to the process log.

## DevOps configuration

Declare the starter on the DevOps server (`application-dev.yml`, and the same shape in `application-test.yml`):

```yaml
odm:
  utility-plane:
    executor-services:
      starter:
        address: http://localhost:9080
        execution-mode: full-control
```
