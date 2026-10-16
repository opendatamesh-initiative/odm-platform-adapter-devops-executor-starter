# ODM Platform DevOps Executor Starter

> Reference implementation of the DevOps executor v2 API for the [Open Data Mesh Platform](https://dpds.opendatamesh.org/) —
> a stateless executor that simulates pipeline runs for end-to-end tests and demos.

<p align="center">
  <a href="https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/actions/workflows/ci.yml"><img src="https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat-square" alt="License: Apache 2.0"></a>
</p>

<p align="center">
  <a href="https://openjdk.org/"><img src="https://img.shields.io/badge/Java-21-ED8B00.svg?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21"></a>
  <a href="https://quarkus.io/"><img src="https://img.shields.io/badge/Quarkus-3.40-4695EB.svg?style=flat-square&logo=quarkus&logoColor=white" alt="Quarkus 3.40"></a>
</p>

<p align="center">
  <a href="#what-the-starter-does">What the starter does</a> ·
  <a href="#quick-start-local">Quick start</a> ·
  <a href="#run-with-docker">Docker</a> ·
  <a href="#executor-api">Executor API</a> ·
  <a href="#simulation">Simulation</a> ·
  <a href="#contributing">Contributing</a>
</p>

---

## What the starter does

The [DevOps server](https://github.com/opendatamesh-initiative/odm-platform-pp-devops-server) does not run pipelines itself: it calls an **executor** to start a pipeline, follow its status and logs, and cancel it. This starter is a ready-to-run executor that **simulates** those runs.

- Runs live **in memory** and are dropped when the process stops. There is no database: `quarkus.datasource.active` and `quarkus.hibernate-orm.active` are `false`.
- Each run stays `RUNNING` for a planned duration, then reports a planned outcome. Both are configurable, globally or per run.
- Use it to test the DevOps server end to end, to demo the platform, or as a **base to build a real executor**.

---

## Quick start (local)

**Requirements:** Java **21** · Maven **3.9+** (or the included `./mvnw`)

```bash
git clone https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter.git
cd odm-platform-adapter-devops-executor-starter

./mvnw quarkus:dev
```

The executor listens on port **9080** (`quarkus.http.port` in `application.yaml`).

Other builds:

```bash
./mvnw package                                  # runner in target/quarkus-app/
java -jar target/quarkus-app/quarkus-run.jar

./mvnw test                                     # @QuarkusTest; %test sets executor.simulation.run-duration to 200ms
```

---

## Run with Docker

The image runs a **native** executable on port **9080**. Build the native binary first (Mandrel/GraalVM locally, or a container build):

```bash
./mvnw package -DskipTests -Dnative -Dquarkus.native.container-build=true

docker build -t odm-platform-adapter-devops-executor-starter .
docker run --rm -p 9080:9080 odm-platform-adapter-devops-executor-starter
```

To run the native binary directly:

```bash
./target/odm-platform-adapter-devops-executor-starter-*-runner
```

---

## Executor API

| Method | Path                               | Request                                                      | Response                                |
| :----- | :--------------------------------- | :----------------------------------------------------------- | :-------------------------------------- |
| `POST` | `/api/v2/up/executor/tasks/start`  | JSON body with `executorParameters` and `pipelineParameters` | `{ "providerRunId": "starter-<uuid>" }` |
| `POST` | `/api/v2/up/executor/tasks/status` | `{ "executorParameters", "providerRunId" }`                  | `{ "providerRunId", "status" }`         |
| `POST` | `/api/v2/up/executor/tasks/logs`   | `{ "executorParameters", "providerRunId" }`                  | `{ "content", "generatedAt" }`          |
| `POST` | `/api/v2/up/executor/tasks/cancel` | `{ "executorParameters", "providerRunId" }`                  | empty **200**                           |

`status` is one of `RUNNING`, `SUCCEEDED`, `FAILED`, `CANCELED`.

`executorParameters` carries:

- `repositoryKey`
- `dataProductRepo`: `providerType`, `providerBaseUrl`, `externalIdentifier`, `name`, `ownerId`, `ownerType`, `remoteUrlHttp`, `defaultBranch`
- `ref`: `name`, `type`
- `pipelineIdentifier`

Unknown JSON properties are ignored. No activity or task identifiers are required.

| Condition                                              | Result                                       |
| :----------------------------------------------------- | :------------------------------------------- |
| Request without `executorParameters` (any endpoint)    | **400**, the body names `executorParameters` |
| Request without `providerRunId` (status, logs, cancel) | **400**, the body names `providerRunId`      |
| Unknown `providerRunId`                                | **404**                                      |
| Cancel of a run that has already ended                 | **409**                                      |

---

## Simulation

Defaults (`executor.simulation` in `application.yaml`):

| Property       | Default     |
| :------------- | :---------- |
| `run-duration` | `5s`        |
| `outcome`      | `SUCCEEDED` |

A run stays `RUNNING` until `startedAt` plus the planned duration, then reports the planned outcome.

Two reserved pipeline parameters override those defaults for a single run. The starter reads them the way a pipeline reads its own inputs.

| Parameter                 | Values                                                                                  |
| :------------------------ | :-------------------------------------------------------------------------------------- |
| `starter.outcome`         | `SUCCEEDED` or `FAILED`, case-insensitive. Any other value keeps the configured default |
| `starter.durationSeconds` | A non-negative integer number of seconds. Any other value keeps the configured default  |

**Logs.** The logs endpoint returns one block with the run id, repository name, `repositoryKey`, ref name and type, pipeline identifier, pipeline parameters as received, the **names** of request headers starting with `x-odm-` (never their values), the start and end times, and the outcome.

**Logging.** Each call writes one info log line that contains only the provider run id. Header values, secret values and parameter values are never written to the process log.

---

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

---

## Contributing

Contributions are welcome.

1. Fork the repository and create a feature branch
2. Make your changes with clear commits
3. Open a pull request against the main branch

Bugs, questions, or proposals → [open an issue](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/issues).

---

## License

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Part of the [Open Data Mesh Initiative](https://github.com/opendatamesh-initiative).
