// Copyright 2020 Strixpyrr
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package strixpyrr.specular.models.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uy.klutter.core.common.initializedWith
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

// Code Blocks

internal const val literal = "%L"
internal const val charLiteral = "'%L'"
internal const val escapedString = "%S"

internal inline fun codeBlock(
	formatCapacity: Int = 128,
	argumentCapacity: Int = 4,
	format: CodeBlockFormatScope.() -> Unit
) = CodeBlockFormatScope(formatCapacity, argumentCapacity).build(format)

@Suppress("NOTHING_TO_INLINE")
internal class CodeBlockFormatScope(
	private val formatBuilder: StringBuilder,
	private val argumentList: ArrayList<Any?>
)
{
	constructor(formatCapacity: Int, argumentCapacity: Int) :
		this(StringBuilder(formatCapacity), ArrayList(argumentCapacity))
	
	val format get() = formatBuilder.toString()
	val arguments get() = argumentList.toTypedArray()
	
	fun add(format: String) = apply { formatBuilder.append(format) }
	
	fun add(format: String, vararg arguments: Any?) = apply()
	{
		formatBuilder.append(format)
		argumentList.ensureCapacity(argumentList.size + arguments.size)
		arguments.forEach(argumentList::add)
	}
	
	inline fun build(format: CodeBlockFormatScope.() -> Unit)
		= apply(format).build()
	
	fun build() =
		CodeBlock.of(
			formatBuilder.toString(),
			*argumentList.toTypedArray()
		)
	
	inline fun CodeBlock.Builder.add() = add(build())
	
	fun indent() = add("%>")
	fun dedent() = add("%<")
	fun addLineBreak() = add("\n\n")
	
	fun comment(comment: String) = add("// %L\n", comment)
	fun comment(format: String, vararg arguments: Any?) = add("// ").add(format, arguments).add("\n")
	
	fun startChainCall() = add("\n%>")
	fun endChainCall() = add("%<\n")
	
	inline fun callExtensionFunction(name: String) = callMemberFunction(name)
	
	inline fun callExtensionFunction(name: String, isSingleLine: Boolean = false, parameterList: String, vararg arguments: Any?) =
		callMemberFunction(name, isSingleLine, parameterList, *arguments)
	
	inline fun callExtensionFunction(name: String, isSingleLine: Boolean = false, parameterList: ParameterListScope.() -> Unit) =
		callMemberFunction(name, isSingleLine, parameterList)
	
	fun callMemberFunction(name: String) = add(".%N()", name)
	
	fun callMemberFunction(name: String, isSingleLine: Boolean = false, parameterList: String, vararg arguments: Any?) =
		add(".%N", name).addParameterList(isSingleLine, parameterList, *arguments)
	
	inline fun callMemberFunction(name: String, isSingleLine: Boolean = false, parameterList: ParameterListScope.() -> Unit) =
		add(".%N", name).addParameterList(isSingleLine, parameterList)
	
	fun callFunction(name: String) = add("%N()", name)
	
	fun callFunction(name: String, isSingleLine: Boolean = false, parameterList: String, vararg arguments: Any?) =
		add("%N", name).addParameterList(isSingleLine, parameterList, *arguments)
	
	inline fun callFunction(name: String, isSingleLine: Boolean = false, parameterList: ParameterListScope.() -> Unit) =
		add("%N", name).addParameterList(isSingleLine, parameterList)
	
	fun callConstructor(type: TypeName, parameterList: String, vararg arguments: Any?) =
		add("%T", type).addParameterList(isSingleLine = true, parameterList, *arguments)
	
	fun callConstructor(type: KClass<*>) = add("%T()", type.asClassName())
	
	inline fun callConstructor(type: KClass<*>, isSingleLine: Boolean = false, parameterList: ParameterListScope.() -> Unit) =
		add("%T", type.asClassName()).addParameterList(isSingleLine, parameterList)
	
	inline fun enclose(enclosed: () -> Unit): CodeBlockFormatScope
	{
		contract { callsInPlace(enclosed, InvocationKind.EXACTLY_ONCE) }
		
		return open().also { enclosed() }.close()
	}
	
	fun open() = add("\n{\n").indent()
	fun close() = dedent().add("\n}")
	
	fun addParameterList(isSingleLine: Boolean = false, parameterList: String, vararg arguments: Any?) =
		if (!isSingleLine)
			startParameterList()
				.also { add(format, *arguments) }
				.endParameterList()
		else add("(").add(format, *arguments).add(")")
	
	inline fun addParameterList(isSingleLine: Boolean = false, parameterList: ParameterListScope.() -> Unit): CodeBlockFormatScope
	{
		contract { callsInPlace(parameterList, InvocationKind.EXACTLY_ONCE) }
		
		return if (!isSingleLine)
			startParameterList()
				.also()
				{
					ParameterListScope(isSingleLine).parameterList()
				}.endParameterList()
		else add("(").also { ParameterListScope(isSingleLine).parameterList() }.add(")")
	}
	
	fun startParameterList() = add("(\n").indent()
	fun endParameterList() = add(")").dedent()
	
	inner class ParameterListScope(
		private val isSingleLine: Boolean
	)
	{
		private var hasParameters = false
		
		fun addNamedParameter(name: String, value: Boolean) = addNamedParameter(name, literal, value)
		
		fun addNamedParameter(name: String, format: String) =
			addNamedParameter(name) { add(format) }
		
		fun addNamedParameter(name: String, format: String, vararg arguments: Any?) =
			addNamedParameter(name) { add(format, *arguments) }
		
		inline fun addNamedParameter(name: String, parameter: () -> Unit) =
			addParameter()
			{
				add("%N = ", name)
				parameter()
			}
		
		fun addParameters(values: BooleanArray) = values.forEach { addParameter(it) }
		fun addParameters(values:    ByteArray) = values.forEach { addParameter(it) }
		fun addParameters(values:    CharArray) = values.forEach { addParameter(it) }
		fun addParameters(values:     IntArray) = values.forEach { addParameter(it) }
		fun addParameters(values:    LongArray) = values.forEach { addParameter(it) }
		fun addParameters(values:   ShortArray) = values.forEach { addParameter(it) }
		fun addParameters(values: Array<out Enum<*>>) = values.forEach { addParameter(it) }
		fun addParameters(values: Array<out KClass<*>>) = values.forEach { addParameter(it) }
		
		fun addParameter(value: Boolean) = addParameter(literal, value)
		fun addParameter(value: Number) = addParameter(literal, value)
		// Todo: There isn't an escape for chars in CodeBlock, is there?
		fun addParameter(value: Char) = addParameter(charLiteral, value)
		fun addParameter(value: Enum<*>) =
			addParameter("%T.%N", value::class.asClassName(), value.name)
		fun addParameter(value: KClass<*>) = addParameter("%T::class", value.asClassName())
		
		fun addParameter(format: String) =
			addParameter { add(format) }
		
		fun addParameter(format: String, vararg arguments: Any?) =
			addParameter { add(format, *arguments) }
		
		inline fun addParameter(parameter: () -> Unit): ParameterListScope
		{
			if (hasParameters)
				if (isSingleLine)
					add(", ")
				else
					add(",\n")
			else hasParameters = true
			
			return apply { parameter() }
		}
	}
	
	// Statements
	
	// Todo: Rename to branch?
	inline fun addIf(condition: String, vararg arguments: Any?, isMultiLine: Boolean = false, block: () -> Unit): CodeBlockFormatScope
	{
		add("if (").add(condition, *arguments)
		
		return if (isMultiLine)
			add(")").open().also { block() }.close().add("\n")
		else add(")\n").indent().also { block() }.dedent().add("\n")
	}
	
	inline fun addElse(isMultiLine: Boolean = false, block: () -> Unit) =
		if (isMultiLine)
			add("else ").open().also { block() }.close().add("\n")
		else add("else ").also { block() }.add("\n")
	
	// Local Property
	
	inline fun declareLocalProperty(name: String, isMutable: Boolean = false, type: TypeName? = null, initBlock: () -> Unit): CodeBlockFormatScope
	{
		if (isMutable)
			add("var %N", name)
		else add("val %N", name)
		
		if (type != null)
			add(": %T", type)
		
		return add(" = ").also { initBlock() }
	}
	
	fun declareLocalProperty(name: String, isMutable: Boolean = false, type: TypeName): CodeBlockFormatScope
	{
		if (isMutable)
			add("var %N", name)
		else add("val %N", name)
		
		return add(": %T", type)
	}
	
	// Operators
	
	inline fun assign(name: String, assignment: () -> Unit) =
		add("%N = ", name).apply { assignment() }
	
	fun assign(name: String, value: String) = add("%N = %L", name, value)
	
	inline fun assignIndex(name: String, index: () -> Unit, assignment: () -> Unit) =
		add("%N[", name).apply { index() }.add("] = ").apply { assignment() }
	
	// Todo: Find a better name for this.
	inline fun assignAddition(name: String, assignment: () -> Unit) =
		add("%N += ", name).apply { assignment() }
}

// Type Names

@Suppress("NOTHING_TO_INLINE")
internal inline fun KClass<*>.toTypeName() = asClassName()

@Suppress("NOTHING_TO_INLINE")
internal inline fun KClass<*>.toTypeName(vararg parameters: TypeName): TypeName =
	asClassName().parameterizedBy(*parameters)

// Types

@Suppress("NOTHING_TO_INLINE")
internal inline fun TypeSpec.Builder.implement(interfaceName: TypeName, delegate: String) =
	superinterfaces.put(interfaceName, CodeBlock.of(delegate))

internal inline fun TypeSpec.Builder.setInitializerBlock(
	formatCapacity: Int = 128,
	argumentCapacity: Int = 4,
	format: CodeBlockFormatScope.() -> Unit
) = addInitializerBlock(codeBlock(formatCapacity, argumentCapacity, format))

internal inline fun TypeSpec.Builder.overrideProperty(
	name: String,
	type: TypeName,
	visibility: KVisibility = KVisibility.PUBLIC,
	isAbstract: Boolean = false,
	build: PropertySpec.Builder.() -> Unit = { }
) = declareProperty(name, type, visibility, isAbstract = isAbstract)
	{
		modifiers += KModifier.OVERRIDE
		
		apply(build)
	}

internal inline fun TypeSpec.Builder.declareProperty(
	name: String,
	type: TypeName,
	visibility: KVisibility = KVisibility.PUBLIC,
	isInline: Boolean = false,
	isOpen: Boolean = false,
	isAbstract: Boolean = false,
	build: PropertySpec.Builder.() -> Unit = { }
) = addProperty(createProperty(name, type, visibility, isInline, isOpen, isAbstract, build))

internal inline fun TypeSpec.Builder.overrideFunction(
	name: String,
	visibility: KVisibility = KVisibility.PUBLIC,
	isAbstract: Boolean = false,
	build: FunSpec.Builder.() -> Unit
) = declareFunction(name, visibility, isAbstract = isAbstract)
{
	modifiers += KModifier.OVERRIDE
	
	apply(build)
}

internal inline fun TypeSpec.Builder.declareFunction(
	name: String,
	visibility: KVisibility = KVisibility.PUBLIC,
	isInline: Boolean = false,
	isOpen: Boolean = false,
	isAbstract: Boolean = false,
	build: FunSpec.Builder.() -> Unit = { }
) = addFunction(createFunction(name, visibility, isInline, isOpen, isAbstract, build))

internal inline fun FileSpec.Builder.declareFunction(
	name: String,
	visibility: KVisibility = KVisibility.PUBLIC,
	isInline: Boolean = false,
	build: FunSpec.Builder.() -> Unit = { }
) = addFunction(createFunction(name, visibility, isInline, build = build))

internal inline fun FileSpec.Builder.declareClass(
	name: String,
	visibility: KVisibility = KVisibility.PUBLIC,
	build: TypeSpec.Builder.() -> Unit
) = addType(
		TypeSpec
			.classBuilder(name)
			.apply()
			{
				if (visibility != KVisibility.PUBLIC)
					addModifiers(visibility.modifier)
			}
			.apply(build)
			.build()
	)

// Functions and Properties

internal inline fun createProperty(
	name: String,
	type: TypeName,
	visibility: KVisibility = KVisibility.PUBLIC,
	isInline: Boolean = false,
	isOpen: Boolean = false,
	isAbstract: Boolean = false,
	build: PropertySpec.Builder.() -> Unit = { }
) = PropertySpec
		.builder(name, type)
		.apply()
		{
			if (visibility != KVisibility.PUBLIC)
				modifiers += visibility.modifier
			
			if (isInline) modifiers += KModifier.INLINE
			
			if (isAbstract)
			{
				require(!isOpen) { "Abstract properties cannot be open." }
				
				modifiers += KModifier.ABSTRACT
			}
			
			if (isOpen)
			{
				require(!isAbstract) { "Open properties cannot be abstract." }
				
				modifiers += KModifier.OPEN
			}
		}
		.apply(build)
		.build()

internal inline fun createFunction(
	name: String,
	visibility: KVisibility = KVisibility.PUBLIC,
	isInline: Boolean = false,
	isOpen: Boolean = false,
	isAbstract: Boolean = false,
	build: FunSpec.Builder.() -> Unit = { }
) = FunSpec
		.builder(name)
		.apply()
		{
			if (visibility != KVisibility.PUBLIC)
				modifiers += visibility.modifier
			
			if (isInline) modifiers += KModifier.INLINE
			
			if (isAbstract)
			{
				require(!isOpen) { "Abstract functions cannot be open." }
				
				modifiers += KModifier.ABSTRACT
			}
			
			if (isOpen)
			{
				require(!isAbstract) { "Open functions cannot be abstract." }
				
				modifiers += KModifier.OPEN
			}
		}
		.apply(build)
		.build()

private inline val KVisibility.modifier get() = when(this)
{
	KVisibility.PUBLIC    -> KModifier.PUBLIC
	KVisibility.PROTECTED -> KModifier.PROTECTED
	KVisibility.INTERNAL  -> KModifier.INTERNAL
	KVisibility.PRIVATE   -> KModifier.PRIVATE
}

internal inline fun FunSpec.Builder.declareParameter(name: String, type: TypeName, build: ParameterSpec.Builder.() -> Unit = { }) =
	parameters.add(ParameterSpec.builder(name, type).apply(build).build())

internal inline fun <reified T : Any> FunSpec.Builder.declareParameter(name: String, default: T) =
	parameters.add(
		ParameterSpec.builder(name, default::class.toTypeName())
			.apply { defaultValue(CodeBlock.of(literal, default)) }
			.build()
	)

internal inline fun FunSpec.Builder.setBody(
	formatCapacity: Int = 128,
	argumentCapacity: Int = 4,
	format: CodeBlockFormatScope.() -> Unit
) = addCode(formatCapacity, argumentCapacity, format)

internal inline var FunSpec.Builder.expressionBody: String
	get() =
		error(
			"This property is a short form of setExpressionBody { add(\"...\") " +
			"} for string literals; it has no accessor."
		)
	set(value) { addCode(CodeBlock.of("return $value")) }

internal inline fun FunSpec.Builder.setExpressionBody(
	formatCapacity: Int = 128,
	argumentCapacity: Int = 4,
	format: CodeBlockFormatScope.() -> Unit
) = addCode(formatCapacity, argumentCapacity) { add("return ").format() }

internal inline fun FunSpec.Builder.addCode(
	formatCapacity: Int = 128,
	argumentCapacity: Int = 4,
	format: CodeBlockFormatScope.() -> Unit
) = addCode(codeBlock(formatCapacity, argumentCapacity, format))

// Annotations

@Suppress("NOTHING_TO_INLINE")
internal inline fun FunSpec.Builder.suppress(error: SuppressedError) =
	annotate<Suppress>
	{
		addMember(CodeBlock.of(error.error))
	}

internal inline fun <reified A : Annotation> FunSpec.Builder.annotate(build: AnnotationSpec.Builder.() -> Unit) =
	addAnnotation(AnnotationSpec.builder(A::class.toTypeName()).apply(build).build())

internal enum class SuppressedError(inline val error: String)
{
	UncheckedCast("UNCHECKED_CAST")
}



// Old DSL Attempt Stuff

internal inline fun file(
	packageName: String,
	fileName: String,
	build: FileSpec.Builder.() -> Unit
) = FileSpec.builder(packageName, fileName)
			.initializedWith(build).build()

internal inline fun FileSpec.Builder.`object`(
	name: String,
	build: TypeSpec.Builder.() -> Unit
) = addType(
		TypeSpec.objectBuilder(name)
				.initializedWith(build).build()
	)

internal inline fun FileSpec.Builder.function(
	name: String,
	build: FunSpec.Builder.() -> Unit
) = addFunction(
		FunSpec.builder(name)
			   .initializedWith(build).build()
	)

internal inline fun <reified A : Annotation> FunSpec.Builder.addAnnotation(
	build: AnnotationSpec.Builder.() -> Unit
) = addAnnotation(
		AnnotationSpec.builder(A::class.asClassName())
					  .initializedWith(build).build()
	)

internal inline fun TypeSpec.Builder.property(
	name: String,
	type: TypeName,
	vararg modifiers: KModifier,
	build: PropertySpec.Builder.() -> Unit
) = addProperty(
		PropertySpec.builder(name, type, *modifiers)
					.initializedWith(build).build()
	)

internal inline fun CodeBlock.Builder.property(
	name: String,
	type: TypeName,
	build: PropertySpec.Builder.() -> Unit
) = add(
		CodeBlock.of("%L",
			PropertySpec.builder(name, type)
						.initializedWith(build)
						.build()
		)
	)

internal inline fun TypeSpec.Builder.initializer(
	build: CodeBlock.Builder.() -> Unit
) = addInitializerBlock(block(build))

internal inline fun PropertySpec.Builder.initializer(
	build: CodeBlock.Builder.() -> Unit
) = initializer(block(build))

// Code Blocks

internal inline fun block(
	build: CodeBlock.Builder.() -> Unit
) = CodeBlock.Builder().initializedWith(build).build()

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.whitespace() = add("%W")

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.nextLine() = add("\n")

@Suppress("NOTHING_TO_INLINE")
internal inline fun literal(value: Any?) = CodeBlock.of("%L", value)

@Suppress("NOTHING_TO_INLINE")
internal inline fun escapedString(value: Any?) = CodeBlock.of("%S", value)

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.addLiteral(value: String) = add("%L", value)

@Suppress("NOTHING_TO_INLINE")
internal inline fun type(value: Any?) = CodeBlock.of("%T", value)

@Suppress("NOTHING_TO_INLINE")
internal inline fun enumReference(enumType: TypeName, enum: Enum<*>) =
	CodeBlock.of("%T.%N", enumType, enum.name)

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.addType(type: KClass<*>, vararg parameters: TypeName) =
	addType(type.asClassName().parameterizedBy(*parameters))

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.addType(type: ClassName, vararg parameters: TypeName) =
	addType(type.parameterizedBy(*parameters))

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.addType(value: TypeName) = add("%T", value)

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.open() = add("\n{\n").indent()

@Suppress("NOTHING_TO_INLINE")
internal inline fun CodeBlock.Builder.close() = add("}\n").unindent()

@Suppress("NOTHING_TO_INLINE")
internal class FormatScope(
	val format: StringBuilder,
	val arguments: ArrayList<Any?>
)
{
	inline fun add(value: Any?) = apply()
	{
		format.append(
			if (value is String)
				value
			else value.toString()
		)
	}
	
	inline fun add(value: String, argument: Any?) = apply()
	{
		format.append(value)
		arguments += argument
	}
	
	inline fun literal(value: Any?) = add("%L", value)
	
	inline fun <reified T : Any> addType(vararg parameters: TypeName) =
		addType(T::class, *parameters)
	
	inline fun addType(type: KClass<*>, vararg parameters: TypeName) =
		addType(
			type.asClassName()
				.run()
				{
					if (parameters.isEmpty())
						this
					else parameterizedBy(*parameters)
				}
		)
	
	inline fun addType(value: TypeName) = add("%T", value)
	
	inline fun addLine() = add("\n")
	
	inline fun addString(value: Any?) = add("%S", value)
	
	inline fun addName(value: Any?) = add("%N", value)
}