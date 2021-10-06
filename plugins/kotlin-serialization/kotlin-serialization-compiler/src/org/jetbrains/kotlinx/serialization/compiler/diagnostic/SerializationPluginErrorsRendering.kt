/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object SerializationPluginErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("SerializationPlugin")
    override fun getMap() = MAP

    init {
        MAP.put(
            SerializationErrors.INLINE_CLASSES_NOT_SUPPORTED,
            "Inline classes require runtime serialization library version at least {0}, while your classpath has {1}.",
            CommonRenderers.STRING,
            CommonRenderers.STRING,
        )
        MAP.put(
            SerializationErrors.PLUGIN_IS_NOT_ENABLED,
            "kotlinx.serialization compiler plugin is not applied to the module, so this annotation would not be processed. " +
                    "Make sure that you've setup your buildscript correctly and re-import project."
        )
        MAP.put(
            SerializationErrors.ANONYMOUS_OBJECTS_NOT_SUPPORTED,
            "Anonymous objects or contained in it classes can not be serializable."
        )
        MAP.put(
            SerializationErrors.INNER_CLASSES_NOT_SUPPORTED,
            "Inner (with reference to outer this) serializable classes are not supported. Remove @Serializable annotation or 'inner' keyword."
        )
        MAP.put(
            SerializationErrors.EXPLICIT_SERIALIZABLE_IS_REQUIRED,
            "Explicit @Serializable annotation on enum class is required when @SerialName or @SerialInfo annotations are used on its members."
        )
        MAP.put(
            SerializationErrors.SERIALIZABLE_ANNOTATION_IGNORED,
            "@Serializable annotation without arguments can be used only on sealed interfaces." +
                    "Non-sealed interfaces are polymorphically serializable by default."
        )
        MAP.put(
            SerializationErrors.NON_SERIALIZABLE_PARENT_MUST_HAVE_NOARG_CTOR,
            "Impossible to make this class serializable because its parent is not serializable and does not have exactly one constructor without parameters"
        )
        MAP.put(
            SerializationErrors.PRIMARY_CONSTRUCTOR_PARAMETER_IS_NOT_A_PROPERTY,
            "This class is not serializable automatically because it has primary constructor parameters that are not properties"
        )
        MAP.put(
            SerializationErrors.DUPLICATE_SERIAL_NAME,
            "Serializable class has duplicate serial name of property ''{0}'', either in the class itself or its supertypes",
            CommonRenderers.STRING
        )
        MAP.put(
            SerializationErrors.SERIALIZER_NOT_FOUND,
            "Serializer has not been found for type ''{0}''. " +
                    "To use context serializer as fallback, explicitly annotate type or property with @Contextual",
            Renderers.RENDER_TYPE_WITH_ANNOTATIONS
        )
        MAP.put(
            SerializationErrors.SERIALIZER_NULLABILITY_INCOMPATIBLE,
            "Type ''{1}'' is non-nullable and therefore can not be serialized with serializer for nullable type ''{0}''",
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE
        )
        MAP.put(
            SerializationErrors.SERIALIZER_TYPE_INCOMPATIBLE,
            "Class ''{1}'', which is serializer for type ''{2}'', is applied here to type ''{0}''. This may lead to errors or incorrect behavior.",
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE
        )
        MAP.put(
            SerializationErrors.LOCAL_SERIALIZER_USAGE,
            "Class ''{0}'' can't be used as a serializer since it is local",
            Renderers.RENDER_TYPE
        )
        MAP.put(
            SerializationErrors.TRANSIENT_MISSING_INITIALIZER,
            "This property is marked as @Transient and therefore must have an initializing expression"
        )
        MAP.put(
            SerializationErrors.TRANSIENT_IS_REDUNDANT,
            "Property does not have backing field which makes it non-serializable and therefore @Transient is redundant"
        )
        MAP.put(
            SerializationErrors.JSON_FORMAT_REDUNDANT_DEFAULT,
            "Redundant creation of Json default format. Creating instances for each usage can be slow."
        )
        MAP.put(
            SerializationErrors.JSON_FORMAT_REDUNDANT,
            "Redundant creation of Json format. Creating instances for each usage can be slow."
        )
        MAP.put(
            SerializationErrors.INCORRECT_TRANSIENT,
            "@kotlin.jvm.Transient does not affect @Serializable classes. Please use @kotlinx.serialization.Transient instead."
        )
        MAP.put(
            SerializationErrors.REQUIRED_KOTLIN_TOO_HIGH,
            "Your current Kotlin version is {0}, while kotlinx.serialization core runtime {1} requires at least Kotlin {2}. " +
                    "Please update your Kotlin compiler and IDE plugin.",
            CommonRenderers.STRING,
            CommonRenderers.STRING,
            CommonRenderers.STRING
        )

        MAP.put(
            SerializationErrors.PROVIDED_RUNTIME_TOO_LOW,
            "Your current kotlinx.serialization core version is {0}, while current Kotlin compiler plugin {1} requires at least {2}. " +
                    "Please update your kotlinx.serialization runtime dependency.",
            CommonRenderers.STRING,
            CommonRenderers.STRING,
            CommonRenderers.STRING
        )

        MAP.put(
            SerializationErrors.INCONSISTENT_INHERITABLE_SERIALINFO,
            "Argument values for inheritable serial info annotation ''{0}'' must be the same as the values in parent type ''{1}''",
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE
        )

        MAP.put(
            SerializationErrors.MULTIPLE_SERIALIZER_PARAMS,
            "Can not have more than one parameter with @MetaSerializable.Serializer"
        )

        MAP.put(
            SerializationErrors.SERIALIZER_PARAM_WRONG_TYPE,
            "Parameter annotated with @MetaSerializable.Serializer should be of KClass<out KSerializer<*>> type, but was ''{0}''",
            Renderers.RENDER_TYPE
        )

        MAP.put(
            SerializationErrors.SERIALIZABLE_AND_META_ANNOTATION,
            "Class ''{0}'' should not be annotations with both Serializable and meta-serializable annotations. " +
                    "Serializer parameter of meta-serializable annotation will be ignored",
            Renderers.RENDER_TYPE,
        )

        MAP.put(
            SerializationErrors.MULTIPLE_META_ANNOTATIONS,
            "Class ''{0}'' has multiple meta-serializable annotations. " +
                    "Serializer parameter of some of meta-serializable annotations will be ignored",
            Renderers.RENDER_TYPE,
        )
    }
}
