# Kault

[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) library
using [coroutines](https://kotlinlang.org/docs/coroutines-overview.html) to interact
with [Hashicorp Vault API](https://www.hashicorp.com/products/vault).

## Installation

Replace `{version}` with the latest version number on Maven central.

[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/io.github.hansanto/kault/badge.svg?style=for-the-badge&logo=appveyor)](https://search.maven.org/search?q=g:io.github.hansanto+kault)

### Gradle (groovy)

```groovy
repositories {
    mavenCentral()
}
```

---

```groovy
dependencies {
    // for common environment
    implementation 'io.github.hansanto:kault:{version}'
    // for jvm environment
    implementation 'io.github.hansanto:kault-jvm:{version}'
    // for js environment
    implementation 'io.github.hansanto:kault-js:{version}'
    // for native environment
    implementation 'io.github.hansanto:kault-native:{version}'
}
```

### Gradle (kotlin)

```kotlin
repositories {
    mavenCentral()
}
```

---

```kotlin
dependencies {
    // for common environment
    implementation("io.github.hansanto:kault:{version}")
    // for jvm environment
    implementation("io.github.hansanto:kault-jvm:{version}")
    // for js environment
    implementation("io.github.hansanto:kault-js:{version}")
    // for native environment
    implementation("io.github.hansanto:kault-native:{version}")
}
```

### Maven

```xml

<dependencies>
    <!-- for common environment -->
    <dependency>
        <groupId>io.github.hansanto</groupId>
        <artifactId>kault</artifactId>
        <version>{version}</version>
    </dependency>
    
    <!-- for jvm environment -->
    <dependency>
        <groupId>io.github.hansanto</groupId>
        <artifactId>kault-jvm</artifactId>
        <version>{version}</version>
    </dependency>

    <!-- for js environment -->
    <dependency>
        <groupId>io.github.hansanto</groupId>
        <artifactId>kault-js</artifactId>
        <version>{version}</version>
    </dependency>

    <!-- for native environment -->
    <dependency>
        <groupId>io.github.hansanto</groupId>
        <artifactId>kault-native</artifactId>
        <version>{version}</version>
    </dependency>
</dependencies>
```

## Usage

### Create a Vault client

> [!TIP]
> The client builder offers a lot of options to configure the client (header, path, authentication, etc.)

````kotlin
import io.github.hansanto.kault.VaultClient

val client = VaultClient {
    url = "http://localhost:8200"

    // All options below are optional
    namespace = "my-namespace" // Enterprise plan feature
    auth {
        token = "existing-token" // Can be set here or get after from API
        appRole {
            path = "approle"
        }
        // Other options ...
    }
    // Other options ...
}
````

### Generate a token

````kotlin
TODO
````

### Read a secret

````kotlin
TODO
````

### Write a secret

````kotlin
TODO
````
