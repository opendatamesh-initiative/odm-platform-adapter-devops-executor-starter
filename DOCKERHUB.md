# ODM Platform DevOps Executor Starter

The DevOps Executor Starter is the reference implementation of the DevOps executor API of [Open Data Mesh Platform](https://github.com/opendatamesh-initiative) V2. It doesn't run real pipelines: it **simulates** them. The [DevOps server](https://github.com/opendatamesh-initiative/odm-platform-pp-devops-server) calls it, in `full-control` mode, to start a run, read its status and logs, and cancel it; each run stays `RUNNING` for a planned time, then reports a planned outcome. Use the image to test the DevOps server end to end or to demo the platform, and use the [source code](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter) as a starting point to build an executor for a real CI/CD tool. For GitLab CI/CD, use the [GitLab executor](https://hub.docker.com/r/opendatamesh/odm-platform-adapter-devops-executor-gitlab).

- **Source code:** [opendatamesh-initiative/odm-platform-adapter-devops-executor-starter](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter)
- **Documentation:** [README](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/blob/master/README.md)
- **Issues:** [GitHub issues](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/issues)

## Tags

Each [GitHub release](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/releases) publishes one image tagged with its version, without the `v` prefix:

```bash
docker pull opendatamesh/odm-platform-adapter-devops-executor-starter:<version>
```

There is no `latest` tag: always pin a version. Tag `1.0.0` is the ODM Platform V1 version, a Spring Boot dummy executor for the V1 task API, and is configured differently.

## Requirements

| Dependency | Required | Notes |
| --- | --- | --- |
| [DevOps server](https://hub.docker.com/r/opendatamesh/odm-platform-devops) | Yes | It calls the executor. See [DevOps configuration](#devops-configuration). |

No database, CI/CD tool or credentials are needed.

## Quick start

```bash
docker run --name odm-executor-starter -p 9080:9080 \
  opendatamesh/odm-platform-adapter-devops-executor-starter:<version>
```

No variable is required. Try a run without the DevOps server:

```bash
curl -X POST http://localhost:9080/api/v2/up/executor/tasks/start \
  -H 'Content-Type: application/json' \
  -d '{ "executorParameters": { "ref": { "name": "main", "type": "BRANCH" } },
        "pipelineParameters": { "starter.outcome": "FAILED" } }'
```

The response holds a `providerRunId` (`starter-<uuid>`); pass it with `executorParameters` to `/status`, `/logs` and `/cancel`. See [Executor API](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/blob/master/README.md#executor-api).

## Simulation

A run stays `RUNNING` for `executor.simulation.run-duration`, then reports `executor.simulation.outcome`. Two reserved pipeline parameters override them for a single run:

| Pipeline parameter | Values |
| --- | --- |
| `starter.outcome` | `SUCCEEDED` or `FAILED`, case-insensitive. Any other value keeps the configured outcome. |
| `starter.durationSeconds` | A non-negative integer number of seconds. Any other value keeps the configured duration. |

The logs of a run show what the executor received: repository, ref, pipeline identifier and parameters, and the **names** of the `x-odm-` request headers, never their values. Details: [Simulation](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/blob/master/README.md#simulation).

Runs are kept in memory: they're lost when the container stops, and they aren't shared between replicas, so run a single instance.

## DevOps configuration

Declare the executor on the DevOps server under `odm.utility-plane.executors.services`:

```yaml
odm:
  utility-plane:
    executors:
      services:
        starter:
          address: http://odm-executor-starter:9080
          execution-mode: full-control
```

The executor name (`starter` here) is the one used by the descriptor tasks.

## Environment variables

### Image

The image runs a Quarkus native executable on Red Hat UBI 9 minimal, as user `1001`. There is no JVM, so `JAVA_OPTS` has no effect, and the image defines no variables of its own: configure it with the application properties below.

### Application

Every property can be set as an environment variable, using the Quarkus naming rules (for example, `executor.simulation.outcome` becomes `EXECUTOR_SIMULATION_OUTCOME`).

| Property | Description | Default |
| --- | --- | --- |
| `quarkus.http.port` | HTTP port. | `9080` |
| `executor.simulation.run-duration` | How long a run stays `RUNNING`, for example `30s`. | `5s` |
| `executor.simulation.outcome` | Final status of a run: `SUCCEEDED` or `FAILED`. | `SUCCEEDED` |

## Ports and volumes

- **Port:** `9080` (HTTP), or the value of `quarkus.http.port`.
- **Volumes:** none. Runs are kept in memory only.

## Learn more

- [Executor API](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-starter/blob/master/README.md#executor-api)
- [DevOps server](https://github.com/opendatamesh-initiative/odm-platform-pp-devops-server)
- [GitLab executor](https://github.com/opendatamesh-initiative/odm-platform-adapter-devops-executor-gitlab), a real executor for GitLab CI/CD

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
