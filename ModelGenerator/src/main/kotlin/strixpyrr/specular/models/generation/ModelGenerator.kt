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
import com.squareup.kotlinpoet.WildcardTypeName.Companion.STAR
import strixpyrr.abstrakt.text.empty
import strixpyrr.specular.isBaseOf
import strixpyrr.specular.models.*
import strixpyrr.specular.models.building.DelegatedPropertyBuilder
import strixpyrr.specular.models.generation.api.IFactoryData
import strixpyrr.specular.models.generation.api.IModelData
import strixpyrr.specular.models.generation.api.IModelGenerator
import strixpyrr.specular.models.generation.api.IPropertyData
import strixpyrr.specular.models.generation.internal.className
import strixpyrr.specular.models.generation.internal.coerce
import strixpyrr.specular.models.generation.internal.typeName
import uy.klutter.core.common.with
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility.PRIVATE

object ModelGenerator : IModelGenerator
{
	override fun addTopLevelMembers(fileBuilder: FileSpec.Builder)
	{
		val target = TypeVariableName("T")
		val value = TypeVariableName("V")
		val label = TypeVariableName("L")
		val key = TypeVariableName("K")
		
		fileBuilder with
		{
			declareFunction(
				"createProperty",
				visibility = PRIVATE,
				isInline = true
			)
			{
				typeVariables coerce 3
				typeVariables += target
				typeVariables += value
				typeVariables += label
				
				parameters coerce 2
				declareParameter("property", KProperty1::class.toTypeName(target, value))
				declareParameter("attributes", IAttributeContainer::class.toTypeName(label))
				
				returns(IProperty::class.toTypeName(target, value, label))
				
				setExpressionBody(133, 2)
				{
					callConstructor(DelegatedPropertyBuilder::class)
					callExtensionFunction("apply")
					
					enclose()
					{
						addIf("property is %T", KMutableProperty1::class.toTypeName())
						{
							add("setFromProperty(property)")
						}
						
						addElse { add("setFromProperty(property)") }
						
						add("\n")
						
						add("setAttributeContainer(attributes)")
					}
				}
			}
			
			val mutableMapType = MutableMap::class.toTypeName(key, value)
			val mapType        =        Map::class.toTypeName(key, value)
			
			declareFunction(
				"fix",
				visibility = PRIVATE,
				isInline = true
			)
			{
				typeVariables coerce 2
				typeVariables += key
				typeVariables += value
				
				receiver(mutableMapType)
				
				declareParameter("isNamed", default = false)
				
				returns(mapType)
				
				setExpressionBody(101, 0)
				{
					callFunction("fixTo")
					{
						addIf("isNamed")
						{
							add("TreeMap(String.CASE_INSENSITIVE_ORDER)")
						}
						
						addElse()
						{
							// According to this answer, since the hash functions
							// of integers and enums are perfect, a load factor of
							// 1 should work best. When I'm able to implement case
							// insensitivity for String in a LinkedHashMap, a load
							// factor of 1 works there too (Strings are immutable).
							// https://stackoverflow.com/a/7154181/10258324
							add("LinkedHashMap(size, 1f)")
						}
					}
				}
			}
			
			declareFunction(
				"fixTo",
				visibility = PRIVATE
			)
			{
				typeVariables coerce 2
				typeVariables += key
				typeVariables += value
				
				receiver(mutableMapType)
				
				declareParameter("target", mutableMapType)
				
				// Todo: Will the emit properly as an expression body on a new line?
				expressionBody = "\n%>filterTo(map) { it !in map }.asReadOnly()"
			}
		}
	}
	
	override fun addImports(fileBuilder: FileSpec.Builder)
	{
		fileBuilder.addImport("uy.klutter.core.collections", "asReadOnly")
		fileBuilder.addImport("strixpyrr.specular.models", "toFactoryParameter")
	}
	
	private val emptyListBlock by lazy { CodeBlock.of(literal, "emptyList()") }
	
	override fun generate(builder: TypeSpec.Builder, data: IModelData)
	{
		val rawTargetTypeName = data.declaration.className
		val targetTypeName = data.declaration.typeName
		
		val keyTypeName = data.properties.keyType.asClassName()
		
		val labelTypeName = data.attributes.labelType.toTypeName()
		
		val propertyLabelTypeName = data.properties.labelType.asClassName()
		
		val propertyTypeName =
			IProperty::class.toTypeName(
				targetTypeName,
				STAR,
				propertyLabelTypeName
			)
		
		val factoryVariantTypeName =
			IFactoryVariant::class
				.toTypeName(
					targetTypeName
				)
		
		builder with
		{
			declareProperty(
				"storage",
				Map::class
					.toTypeName(
						keyTypeName,
						propertyTypeName
					),
				visibility = PRIVATE
			)
			
			overrideProperty(
				"factoryVariants",
				List::class
					.toTypeName(
						factoryVariantTypeName
					)
			)
			{
				if (data.factories.isEmpty())
					initializer(emptyListBlock)
			}
			
			overrideProperty("keys", Set::class.toTypeName(keyTypeName))
			
			overrideProperty(
				"values",
				Collection::class
					.toTypeName(propertyTypeName)
			)
			{
				if (data.factories.isEmpty())
					initializer(CodeBlock.of(literal, "true"))
			}
			
			overrideProperty("canCreate", Boolean::class.toTypeName())
			
			overrideProperty(
				"attributeContainer",
				IAttributeContainer::class
					.toTypeName(labelTypeName)
			)
			{
				if (data.attributes.attributes.isEmpty())
					initializer(emptyListBlock)
			}
			
			setInitializerBlock(512, 32)
			{
				// Properties
				
				data.properties.run()
				{
					declareLocalProperty("map")
					{
						callConstructor(
							LinkedHashMap::class
								.toTypeName(keyTypeName, propertyTypeName),
							parameterList = literal, properties.size
						)
					}.addLineBreak()
					
					declareLocalProperty(
						"attributes",
						isMutable = true,
						AttributeContainer::class
							.toTypeName(
								propertyLabelTypeName,
								ANY.asNullable()
							)
					).addLineBreak()
					
					// Terrible name, I know. Feel free to berate me for it.
					val keyTypeValue = KeyType.get(keyType)
					
					when (keyTypeValue)
					{
						KeyType.Enum   -> generate(properties, rawTargetTypeName, labelType)
						{
							add("%T.%N", keyTypeName, (it as Enum<*>).name)
						}
						KeyType.Sealed -> TODO("Sealed classes are not supported yet.")
						KeyType.Name   -> generate(properties, rawTargetTypeName, labelType)
						{
							add(escapedString, it)
						}
						KeyType.Index  -> generate(properties, rawTargetTypeName, labelType)
						{
							add(literal, it)
						}
					}
					
					// Assign the final map to storage.
					
					assign("storage")
					{
						callFunction(
							"fix",
							isSingleLine = true,
							if (keyTypeValue == KeyType.Name)
								"isNamed = true"
							else String.empty
						)
					}.addLineBreak()
					
					assign("keys", "storage.keys.asReadOnly()")
					assign("value", "storage.values.asReadOnly()")
				}
				
				// Attributes
				
				data.attributes.run()
				{
					if (attributes.isEmpty()) return@run
					
					comment("Attributes")
					
					assign("attributeContainer")
					{
						callConstructor(AttributeContainer::class)
						{
							callConstructor(LinkedHashMap::class, isSingleLine = true)
							{
								addParameter("%L", attributes.size)
							}
							
							callExtensionFunction("apply").enclose()
							{
								generate(attributes, KeyType.get(labelType))
							}
						}
					}.addLineBreak()
				}
				
				// Factory Variants
				
				data.factories.run()
				{
					if (isEmpty()) return@run
					
					comment("Factory Variants")
					
					declareLocalProperty("factories")
					{
						callConstructor(
							ArrayList::class
								.toTypeName(factoryVariantTypeName),
							literal,
							size
						)
					}.addLineBreak()
					
					// Todo: Is there any way to avoid reflection in the generated
					//  model here?
					
					val primary = find(IFactoryData::isPrimary)
					
					if (primary != null)
					{
						assignAddition("factories")
						{
							add("%T::class.primaryConstructor!!", rawTargetTypeName)
								.generateFactory(primary, rawTargetTypeName)
						}
					}
					
					declareLocalProperty("constructors")
					{
						add("%T::class.constructors", rawTargetTypeName)
					}
					
					if (data.factories.any { !it.isConstructor })
						declareLocalProperty("staticFunctions")
						{
							add("%T::class.staticFunctions", rawTargetTypeName)
						}.addLineBreak()
					
					data.factories.forEach()
					{ factory ->
						if (factory == primary) return@forEach
						
						assignAddition("factories")
						{
							if (factory.isConstructor)
								add("constructors[%L]", factory.index)
									.generateFactory(factory, rawTargetTypeName)
							else
								add("staticFunctions[%L]", factory.index)
									.generateFactory(factory, rawTargetTypeName)
						}.addLineBreak()
					}
					
					assign("factoryVariants", "factories.asReadOnly()")
					assign("canCreate", "true")
				}
				
			}
			
			// Functions
			
			overrideFunction("hasProperty")
			{
				declareParameter("key", keyTypeName)
				
				expressionBody = "key in storage"
			}
			
			overrideFunction("getProperty")
			{
				suppress(SuppressedError.UncheckedCast)
				
				declareParameter("key", keyTypeName)
				
				setExpressionBody(101, 1)
				{
					add("\n").indent()
					
					add("storage.getOrElse(key)").enclose()
					{
						add("throw ").callConstructor(NoSuchElementException::class)
						{
							addParameter("\"The key \$key does not exist in storage.\"")
						}
					}.add("\n")
					
					dedent()
				}
			}
		}
	}
	
	private fun CodeBlockFormatScope.generateFactory(
		factory: IFactoryData,
		target: TypeName
	)
	{
		callExtensionFunction("run")
		
		enclose()
		{
			if (factory.parameters.isNotEmpty())
				declareLocalProperty("parameters")
				{
					add("parameters")
				}.addLineBreak()
			
			callConstructor(DelegatedFactoryOverload::class)
			{
				addNamedParameter("isPrimary", factory.isPrimary)
				
				addNamedParameter("function", "this")
				
				addNamedParameter("parameters")
				{
					if (factory.parameters.isEmpty())
						callFunction("emptyList")
					else
					{
						callFunction("arrayListOf")
						{
							factory.parameters.forEachIndexed()
							{ i, parameter ->
								addParameter()
								{
									add("parameters[%L]", i)
									
									val property = parameter.linkedProperty
									
									if (property == null)
										callMemberFunction("toFactoryParameter")
									else callMemberFunction(
										"toFactoryParameter",
										isSingleLine = true,
										"%T::%N",
										target,
										property.simpleName.asString()
									)
								}
							}
							
						}.callExtensionFunction("asReadOnly")
					}
				}
			}
		}
	}
	
	private inline fun CodeBlockFormatScope.generate(
		properties: Map<Any?, IPropertyData>,
		target: TypeName,
		labelType: KClass<*>,
		formatKey: (Any?) -> Unit
	) = properties.forEach()
	{ (key, property) ->
		generate(property, target, labelType) { formatKey(key) }
	}
	
	private inline fun CodeBlockFormatScope.generate(
		property: IPropertyData,
		target: TypeName,
		labelType: KClass<*>,
		formatKey: () -> Unit
	)
	{
		val type = KeyType.get(labelType)
		
		// Add a section comment to make the model easier to read.
		comment(property.name).addLineBreak()
		
		assign("attributes")
		{
			callConstructor(AttributeContainer::class)
			{
				callConstructor(LinkedHashMap::class, isSingleLine = true)
				{
					addParameter("%L", property.attributes.size)
				}
				
				callExtensionFunction("apply").enclose()
				{
					generate(property.attributes, type)
				}
			}
		}
		
		assignIndex("map", index = formatKey)
		{
			callFunction(
				"createProperty",
				isSingleLine = true,
				"%T::%N, attributes",
				target,
				property.name
			).addLineBreak()
		}
	}
	
	private enum class KeyType
	{
		Enum, Sealed, Name, Index;
		
		companion object
		{
			@Suppress("NOTHING_TO_INLINE")
			inline fun get(type: KClass<*>) = when
			{
				type.java.isEnum        -> Enum
				type.isSealed           -> Sealed
				type.isBaseOf<String>() -> Name
				else                    -> Index
			}
		}
	}
	
	private fun CodeBlockFormatScope.generate(
		attributes: Map<Any?, Any?>,
		labelType: KeyType
	)
	{
		when (labelType)
		{
			KeyType.Enum   -> generateAttributes(attributes) { addParameter(it as Enum<*>) }
			KeyType.Sealed -> TODO("Sealed classes are not supported yet.")
			KeyType.Name   -> generateAttributes(attributes) { addParameter(it as String) }
			KeyType.Index  -> generateAttributes(attributes) { addParameter(it as Number) }
		}
	}
	
	private inline fun CodeBlockFormatScope.generateAttributes(
		attributes: Map<Any?, Any?>,
		formatLabel: CodeBlockFormatScope.ParameterListScope.(Any?) -> Unit
	)
	{
		attributes.forEach()
		{ (label, value) ->
			callMemberFunction("put", isSingleLine = true)
			{
				formatLabel(label)
				
				when (value)
				{
					is Boolean      -> addParameter(literal, value)
					is Number       -> addParameter(literal, value)
					is Char         -> addParameter(value)
					is String       -> addParameter(escapedString, value)
					is Enum<*>      -> addParameter(value)
					is KClass<*>    -> addParameter(value)
					is BooleanArray -> addParameter()
					{
						callFunction("booleanArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is ByteArray    -> addParameter()
					{
						callFunction("byteArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is CharArray    -> addParameter()
					{
						callFunction("charArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is IntArray     -> addParameter()
					{
						callFunction("intArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is LongArray    -> addParameter()
					{
						callFunction("longArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is ShortArray   -> addParameter()
					{
						callFunction("shortArrayOf", isSingleLine = true)
						{
							addParameters(value)
						}
					}
					is Array<*>     ->
					{
						callFunction("arrayOf", isSingleLine = true)
						{
							@Suppress("UNCHECKED_CAST")
							when
							{
								value.isArrayOf<Enum<*>>()   -> addParameters(value as Array<out Enum<*>>)
								value.isArrayOf<KClass<*>>() -> addParameters(value as Array<out KClass<*>>)
								else                         -> error("Unsupported attribute value")
							}
						}
					}
					else            -> error("Unsupported attribute value")
				}
			}.add("\n")
		}
	}
}