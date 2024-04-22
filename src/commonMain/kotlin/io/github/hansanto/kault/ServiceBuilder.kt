package io.github.hansanto.kault

import io.github.hansanto.kault.extension.addURLChildPath
import io.ktor.client.HttpClient

/**
 * Marks the annotated class as a DSL marker.
 * Avoid access to the parent builder class from the DSL.
 * ```kotlin
 * VaultClient { // annotated with @KaultDsl
 *    subBuilder { // annotated with @KaultDsl
 *      subBuilder2 { // possible because it's a method of subBuilder
 *
 *      }
 *      subBuilder { // not possible because it's a method of VaultClient builder and he's not accessible from subBuilder's scope
 *
 *      }
 *    }
 * }
 * ```
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
public annotation class KaultDsl

/**
 * Alias for defining a DSL function that applies a set of operations on a given object.
 * The generic type T represents the type of the object on which the DSL operations are applied.
 *
 * ```kotlin
 * fun example(body: BuilderDsl<Example>) // fun example(body: @KaultDsl Example.() -> Unit)
 * ```
 **/
public typealias BuilderDsl<T> = @KaultDsl T.() -> Unit

/**
 * Alias for defining a DSL function that applies a set of operations on a given object.
 * The generic type T represents the type of the object on which the DSL operations are applied.
 * The generic type A represents the type of the argument that the DSL function receives.
 *
 * ```kotlin
 * fun example(body: BuilderDslWithArg<Example, String>) // fun example(body: @KaultDsl Example.(String) -> Unit)
 * ```
 */
public typealias BuilderDslWithArg<T, A> = @KaultDsl T.(A) -> Unit

/**
 * ServiceBuilder is an interface that defines the contract for building a service instance.
 *
 * @param T the type of the service that will be built.
 */
@KaultDsl
public abstract class ServiceBuilder<T> {
    /**
     * Base path of the service.
     */
    public abstract var path: String

    /**
     * Builds an instance of the service.
     * The final path of the instance is built by concatenating the parentPath and the path of the instance.
     * If the parentPath is null, the final path is the same as the path of the instance.
     * If the parentPath is not null, the final path is the concatenation of the parentPath and the path of the instance.
     *
     * @param client The client to interact with API.
     * @param parentPath The optional parent path used for building the final path of the instance.
     * @return A new instance.
     */
    public fun build(client: HttpClient, parentPath: String?): T =
        buildWithCompletePath(client, parentPath?.addURLChildPath(path) ?: path)

    /**
     * Builds an instance of type T using the provided HttpClient and the concatenation of the parentPath and the path of the instance.
     * @param client The client to interact with API.
     * @param completePath Concatenation of the parentPath and the [path] of the instance that should be used as a base path for the requests.
     * @return The instance of type T that was built.
     */
    protected abstract fun buildWithCompletePath(client: HttpClient, completePath: String): T
}
