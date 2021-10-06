// IGNORE_BACKEND_FIR: JVM_IR
// TARGET_BACKEND: JVM_IR

// WITH_RUNTIME

// MODULE: app
// FILE: app.kt

import kotlinx.serialization.*
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals

// TODO: for this test to work, runtime dependency should be updated to (yet unreleased) serialization with @MetaSerializable annotation

//@MetaSerializable
@Target(AnnotationTarget.CLASS)
annotation class MySerializable(
    /*@MetaSerializable.Serializer*/ val with: KClass<out KSerializer<*>> = KSerializer::class,
)

//@MetaSerializable
@Target(AnnotationTarget.CLASS)
annotation class MySerializableWithInfo(
    val value: Int,
    val kclass: KClass<*>
)

object MySerializer : KSerializer<Project1> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Project", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Project1) = encoder.encodeString("${value.name}:${value.language}")

    override fun deserialize(decoder: Decoder): Project1 {
        val params = decoder.decodeString().split(':')
        return Project1(params[0], params[1])
    }
}

@MySerializable(MySerializer::class)
class Project1(val name: String, val language: String)

@MySerializable
class Project2(val name: String, val language: String)

@MySerializableWithInfo(123, String::class)
class Project3(val name: String, val language: String)

fun testCustomSerializer() {
//    val string = Json.encodeToString(Project1.serializer(), Project1("name", "lang"))
//    assertEquals("\"name:lang\"", string)
//    val reconstructed = Json.decodeFromString(Project1.serializer(), string)
//    assertEquals("name", reconstructed.name)
//    assertEquals("lang", reconstructed.language)
}

fun testDefaultSerializer() {
//    val string = Json.encodeToString(Project2.serializer(), Project2("name", "lang"))
//    assertEquals("""{"name":"name","language":"lang"}""", string)
//    val reconstructed = Json.decodeFromString(Project2.serializer(), string)
//    assertEquals("name", reconstructed.name)
//    assertEquals("lang", reconstructed.language)
}

fun testSerializableWithInfo() {
//    val descriptor = serializer<Project3>().descriptor
//    val annotation = descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
//    assertEquals(123, annotation.value)
//    assertEquals(String::class, annotation.kclass)
}

fun box(): String {
    testCustomSerializer()
    testDefaultSerializer()
    testSerializableWithInfo()
    return "OK"
}
