# Overview

| Element                                                                    | Usage                            |
|----------------------------------------------------------------------------|----------------------------------|
| [![](https://img.shields.io/badge/Kotlin-orange?logo=kotlin)](#kotlin)     | Development language             |
| [![](https://img.shields.io/badge/Coroutines-orange?logo=kotlin)](#kotlin) | Asynchronous programming library |
| [![](https://img.shields.io/badge/Gradle-blue?logo=gradle)](#gradle)       | Build tool                       |
| [![](https://img.shields.io/badge/Docker-blue?logo=docker)](#docker)       | Test environment                 |

## Getting started

### Gradle

![](https://img.shields.io/badge/require-black)

> [!TIP]
> We provide a [Gradle wrapper](gradlew) to build the project.
> You can use it to avoid installing Gradle on your machine.

To check if the Gradle wrapper is available:

```shell
./gradlew -v
```

All the dependencies are defined in [settings.gradle.kts](settings.gradle.kts) file and used
in [build.gradle.kts](build.gradle.kts) file.

### Kotlin

![](https://img.shields.io/badge/require-black)

We use [Kotlin](https://kotlinlang.org/) to create this multiplatform library with great performance by the use
of [coroutines](https://kotlinlang.org/docs/coroutines-overview.html).

The version of Kotlin is defined in [settings.gradle.kts](settings.gradle.kts) file.

### Docker

![](https://img.shields.io/badge/optional-black)
[![](https://img.shields.io/badge/docker-install-blue?logo=docker)](https://www.docker.com/)
[![](https://img.shields.io/badge/docker--compose-install-blue?logo=docker)](https://docs.docker.com/compose/)

Docker is only used during the execution of [tests](src/commonTest).

A [docker-compose.yml](docker-compose.yml) file is provided to start a Vault server.

For the moment, the use of [TestContainers](https://www.testcontainers.org/) (or another solution) is not possible
because of the lack of support for Kotlin Multiplatform.
If one day it is possible, we will use it to avoid the use of a `docker-compose` file.

## Commands

### Build

To build the project:

```shell
./gradlew assemble
```

### Test

The tests are located in the [src/commonTest](src/commonTest) directory.

You need to start a Vault server before running the tests:

```shell
docker-compose up -d
```

To run the tests:

```shell
./gradlew test
```

If you want to stop the Vault server:

```shell
docker-compose down
```

#### Coverage

We use [Kover](https://github.com/Kotlin/kotlinx-kover) to generate the coverage report.

To generate the HTML report:

```shell
./gradlew koverHtmlReport
```

To generate the XML report:

```shell
./gradlew koverXmlReport
```

### Linter

We use [Ktlint](https://github.com/JLLeitschuh/ktlint-gradle) to lint the code.

To format the code:

```shell
./gradlew ktlintFormat
```

To check the formatting:

```shell
./gradlew ktlintCheck
```

## Design

We follow the [Vault API documentation](https://developer.hashicorp.com/vault/api-docs) paths to create the
corresponding
services.

For example, for the [developer.hashicorp.com/vault/api-docs/**auth/approle
**](https://developer.hashicorp.com/vault/api-docs/auth/approle) url, the service is
located in the [auth/approle](src/commonMain/kotlin/io/github/hansanto/kault/auth/approle) package.

That allows retrieving easily the service you need to use according to the Vault API documentation.
