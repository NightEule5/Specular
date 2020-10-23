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

import com.google.auto.service.AutoService
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.ClassKind.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import strixpyrr.specular.isBaseOf
import strixpyrr.specular.models.IModel
import strixpyrr.specular.models.annotations.*
import strixpyrr.specular.models.generation.annotations.StaticModel
import strixpyrr.specular.models.generation.api.GenerationException
import strixpyrr.specular.models.generation.api.IModelGenerator
import strixpyrr.specular.models.generation.internal.*
import uy.klutter.core.common.initializedWith
import uy.klutter.core.common.with
import java.io.OutputStreamWriter
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.text.Charsets.UTF_8

// Todo: This is 512 lines of pure chaos; the IDE is struggling to keep up with its
//  error checking and syntax highlighting. Breaking this up at some point needs to
//  happen. I can't be bothered right now though.

@AutoService(SymbolProcessor::class)
class ModelSymbolProcessor : SymbolProcessor
{
	private lateinit var `package`: String
	
	private lateinit var generator: CodeGenerator
	private lateinit var log: KSPLogger
	
	override fun init(options: Map<String, String>, kotlinVersion: KotlinVersion, codeGenerator: CodeGenerator, logger: KSPLogger)
	{
		`package` = options["SpecularModelPackage"] ?: ""
		
		generator = codeGenerator
		log = logger
	}
	
	override fun process(resolver: Resolver)
	{
		resolver.getSymbolsWithAnnotation<StaticModel>().run()
		{
			if (isEmpty()) return
			
			val extractor = DataExtractor(resolver)
			
			file(`package`, "GeneratedModels")
			{
				indent("\t")
				
				forEach()
				{
					if (it !is KSClassDeclaration)
						return log.error("A static model cannot be generated for a non-class symbol.", it)
					
					if (it.classKind != INTERFACE && it.classKind != CLASS && it.classKind != OBJECT)
						return log.error("Only interfaces, classes, and objects can be handled.", it)
					
					generate(extractor.extract(it))
				}
			}.writeToGenerator()
		}
	}
	
	private fun FileSpec.Builder.generate(data: ModelData)
	{
		val generator = data.generator.objectInstance ?: data.generator.createInstance()
		
		catch<GenerationException>(log::exception)
		{
			`object`(data.name)
			{
				generator.addImports(this@generate)
				
				addSuperinterface(
					IModel::class
						.asClassName()
						.parameterizedBy(
							data.declaration.typeName,
							data.properties.keyType.asClassName(),
							data.attributes.labelType.asClassName(),
							data.properties.labelType.asClassName()
						)
				)
				
				generator.generate(builder = this, data)
				
				generator.addTopLevelMembers(this@generate)
			}
		}
	}
	
	override fun finish() { }
	
	private inline fun <reified A : Annotation> Resolver.getSymbolsWithAnnotation() =
		getSymbolsWithAnnotation(A::class.qualifiedName as String)
	
	private fun FileSpec.writeToGenerator()
	{
		generator.createNewFile(`package`, "GeneratedModels").use()
		{ stream ->
			OutputStreamWriter(stream, UTF_8).use()
			{ writer ->
				writeTo(writer)
			}
		}
	}
	
	private inner class DataExtractor(
		private val resolver: Resolver
	)
	{
		private val transformer = AnnotationTransformer()
		
		fun extract(declaration: KSClassDeclaration) =
			ModelData(declaration) initializedWith
			{
				val annotations = declaration.annotations
				
				val metadata = ModelMetadata(declaration, annotations.findAnnotation<StaticModel>())
				
				name = metadata.name
				generator = metadata.generator
				
				attributes.labelType = metadata.modelLabelType
				attributes.extractAttributes(annotations)
				
				// Properties
				
				val propertyDeclarations = declaration.getAllProperties()
				
				properties.run()
				{
					keyType = metadata.keyType
					labelType = metadata.propertyLabelType
					
					val optIn = (annotations
						.findAnnotationOrNull<Model>()
						?.arguments?.firstOrNull()
						?.value as Model.MemberInclusion?) == Model.MemberInclusion.OptIn
					
					val inclusion =
						if (optIn)
							ExcludedByDefault()
						else IncludedByDefault()
					
					val data = propertyDeclarations.run()
					{
						val map = LinkedHashMap<Any?, PropertyData>(size)
						
						forEach()
						{
							if (it.getVisibility() == Visibility.PUBLIC)
							{
								val propertyAnnotations = it.annotations
								
								if (inclusion.canInclude(propertyAnnotations))
								{
									val propertyMetadata = propertyAnnotations.findAnnotation<Property>().arguments
									
									val name = propertyMetadata.get("name", 0, it.simpleName::asString)
									val index = propertyMetadata.get("index", 1) ?: -1
									val tags = propertyMetadata.get("tags", 2) ?: emptyArray<String>()
									
									val key = it.extractPropertyKey(
										keyType,
										if (name.isBlank())
											it.simpleName.asString()
										else name,
										index,
										tags
									)
									
									val value = it.extractProperty(labelType, propertyAnnotations, this)
									
									map[key] = value
								}
							}
						}
						
						map
					}
					
					properties.putAll(data)
				}
				
				// Factories
				
				if (declaration.classKind == OBJECT) return@initializedWith
				
				val primaryConstructor = declaration.primaryConstructor
				
				if (primaryConstructor != null && primaryConstructor.isFactoryIncluded())
					factories += primaryConstructor.extractFactory(propertyDeclarations, index = -1, isPrimary = true)
				
				declaration.getConstructors().forEachIndexed()
				{ i, it ->
					if (it != primaryConstructor && it.isPublic() && it.isFactoryIncluded())
						factories += it.extractFactory(propertyDeclarations, i)
				}
				
				// Todo: Will static functions be here or are they in a companion
				//  object declaration?
				declaration.getDeclaredFunctions().forEachIndexed()
				{ i, it ->
					// Todo: Do these conditions also apply for constructors?
					if (it.isPublic() && it.functionKind == FunctionKind.STATIC)
						if (it.hasAnnotation<Factory>())
							if (declaration.asStarProjectedType().isAssignableFrom(it.returnType!!.resolve()))
								factories += it.extractFactory(propertyDeclarations, i, isConstructor = false)
				}
			}
		
		private fun KSPropertyDeclaration.extractPropertyKey(keyType: KClass<*>, name: String, index: Int, tags: Array<String>): Any?
		{
			// Todo: Support sealed class keys.
			if (keyType.isSealed)
				throw UnsupportedOperationException(
					"Sealed classes are not supported as keys yet."
				)
			
			// Todo: Support enum keys.
			if (keyType.java.isEnum)
				throw UnsupportedOperationException(
					"Enums are not supported as keys yet."
				);
			
			if (keyType.isBaseOf<String>())
				return name
			
			if (keyType.isBaseOf<Int>())
				if (index > -1)
					return index
				else log.error(
					"Integers are requested as the key type, but the index of t" +
					"his property is negative (disabled).",
					this
				)
			
			return log.error(
				"No key could be found for the property. The specified key type" +
				", $keyType, is unknown."
			)
		}
		
		private fun KSPropertyDeclaration.extractProperty(labelType: KClass<*>, annotations: List<KSAnnotation>, properties: List<KSPropertyDeclaration>) =
			PropertyData(this, simpleName.asString(), this.type.resolve(), !isMutable) with
			{
				transformer.transform(annotations, labelType, attributes)
				
				if (Modifier.LATEINIT in modifiers)
				{
					val initPropertyName = "has$name"
					
					val initProperty = properties.find()
					{
						it.simpleName.asString() == initPropertyName
					}
					
					initializationProperty = initProperty
					log.error(
						"The property is not lateinit and no init property was " +
						"found. Model generation will fail.",
						this@extractProperty
					)
				}
				else isAlwaysInitialized = true
			}
		
		private fun KSFunctionDeclaration.isFactoryIncluded() = !hasAnnotation<ExcludedFactory>()
		
		private fun KSFunctionDeclaration.extractFactory(
			properties: List<KSPropertyDeclaration>,
			index: Int,
			isConstructor: Boolean = true,
			isPrimary: Boolean = false
		): FactoryData
		{
			val parameters = parameters.mapTo(ArrayList(parameters.size))
			{
				val propertyName = it.annotations
					.findAnnotationOrNull<Parameter>()
					?.arguments
					?.get("propertyName", 0)
					{
						(it.name as KSName).asString()
					}
				
				val property = properties.find()
				{
					p -> p.simpleName.asString() == propertyName
				}
				
				FactoryParameterData(it, property)
			}
			
			return FactoryData(this, index, isConstructor, isPrimary, parameters)
		}
		
		// Attributes
		
		private fun ModelAttributeData.extractAttributes(
			annotations: List<KSAnnotation>
		) = transformer.transform(annotations, labelType, attributes)
		
		private inline fun <reified A : Annotation> List<KSAnnotation>.findAnnotation(): KSAnnotation
		{
			val name = A::class.simpleName as String
			
			return first { it.nameEquals(name, A::class::qualifiedName) }
		}
		
		private inline fun <reified A : Annotation> List<KSAnnotation>.findAnnotationOrNull(): KSAnnotation?
		{
			val name = A::class.simpleName as String
			
			return find { it.nameEquals(name, A::class::qualifiedName) }
		}
		
		private inline fun <reified A : Annotation> KSAnnotated.hasAnnotation(): Boolean
		{
			val name = A::class.simpleName as String
			
			return annotations.any { it.nameEquals(name, A::class::qualifiedName) }
		}
		
		private inline fun KSAnnotation.nameEquals(name: String, qualifiedName: () -> String?) =
			shortName.asString() == name &&
			annotationType.resolve()
						  .declaration
						  .qualifiedName?.asString() == qualifiedName()
		
		// Look familiar?
		private inner class AnnotationTransformer
		{
			private val attributeLookup = HashMap<KSTypeReference, KSAnnotation.() -> Any?>(16)
			private val labelLookup = HashMap<KSTypeReference, Any>(16)
			
			fun isAttribute(annotationType: KSTypeReference) =
				annotationType in attributeLookup ||
				annotationType.hasAnnotation<AttributeAnnotation>()
			
			fun transform(
				annotations: List<KSAnnotation>,
				labelType: KClass<*>,
				data: MutableMap<Any?, Any?>
			)
			{
				for (annotation in annotations)
				{
					val type = annotation.annotationType
					
					if (isAttribute(type))
					{
						val label = getLabel(type, annotation.shortName.asString(), labelType)
						
						data[label] = annotation.getValue(type, type.resolve().declaration as KSClassDeclaration)
					}
				}
			}
			
			private fun getLabel(type: KSTypeReference, name: String, labelType: KClass<*>): Any?
			{
				return labelLookup.getOrPut(type)
				{
					val kspLabelType =
						resolver.getClassDeclarationByName(labelType.qualifiedName as String)
					
					if (kspLabelType != null)
					{
						if (kspLabelType.asStarProjectedType().isAssignableFrom(resolver.builtIns.stringType))
							return@getOrPut name
						
						if (kspLabelType.classKind == ENUM_CLASS)
						{
							// Todo: Figure out how to get an enum class's fields,
							//  find the one with the AttributeLabel annotation, and
							//  check if the value of AttributeLabel matches the label
							//  type. If that's true, return the enum.
							throw UnsupportedOperationException("Enum labels are not supported yet.")
						}
						
						if (Modifier.SEALED in kspLabelType.modifiers)
							throw UnsupportedOperationException("Sealed class labels are not supported yet.")
					}
					
					error("The specified label type $labelType could not be found.")
				}
			}
			
			// Todo: Restrict what can be used as a value. For example, another
			//  annotation shouldn't be used as a value because they cannot be
			//  instantiated.
			private fun KSAnnotation.getValue(type: KSTypeReference, declaration: KSClassDeclaration): Any?
			{
				val get = attributeLookup.getOrPut(type)
				{
					declaration.getDeclaredProperties().run()
					{
						if (isEmpty()) return@getOrPut { true }
						
						var index = indexOfFirst { it.hasAnnotation<AttributeValue>() }
						
						if (index == -1)
							index = indexOfFirst { it.simpleName.asString() == "value" }
						else
						{
							log.error(
								"The label annotation has no value parameter. " +
								"Generation will fail.",
								this@getValue
							)
						}
						
						{ arguments[index] }
					}
				}
				
				return get()
			}
		}
		
		inner class ExcludedByDefault : PropertyInclusionStrategy()
		{
			override fun canInclude(annotations: List<KSAnnotation>): Boolean
			{
				for (annotation in annotations)
				{
					if (annotation.nameEquals("Included", ::includedAnnotationName)) return true
					if (annotation.nameEquals("Property", ::propertyAnnotationName)) return true
					
					val type = annotation.annotationType;
					
					if (hasEncountered(type)) return true
					
					if (type.hasAnnotation<InclusionAnnotation>())
					{
						encounter(type)
						return true
					}
				}
				
				return false
			}
		}
		
		inner class IncludedByDefault : PropertyInclusionStrategy()
		{
			override fun canInclude(annotations: List<KSAnnotation>): Boolean
			{
				for (annotation in annotations)
				{
					if (annotation.nameEquals("Excluded", ::excludedAnnotationName)) return true
					
					val type = annotation.annotationType;
					
					if (hasEncountered(type)) return true
					
					if (type.hasAnnotation<ExclusionAnnotation>())
					{
						encounter(type)
						return true
					}
				}
				
				return false
			}
		}
	}
	
	private class ModelMetadata(declaration: KSClassDeclaration, annotation: KSAnnotation)
	{
		val name: String
		val generator: KClass<out IModelGenerator>
		val keyType: KClass<*>
		val modelLabelType: KClass<*>
		val propertyLabelType: KClass<*>
		
		init
		{
			val values = annotation.arguments
			
			name      = values.get(StaticModel::modelName.name, 0) { declaration.defaultName }
			generator = values.get(StaticModel::generator.name, 2) { ModelGenerator::class }
			
			// Todo: This probably doesn't work. If that's the case we're fucked.
			//  The only way to fix this would be to break out Types back into
			//  parameters, which clutters the annotation. There's no way to get
			//  access to another KSAnnotation from KSValueArgument that I know of.
			val types = values.get<StaticModel.Types>(StaticModel::types.name, 1)
			
			keyType = types?.key ?: String::class
			modelLabelType = types?.modelLabel ?: String::class
			propertyLabelType = types?.propertyLabel ?: String::class
		}
		
		private inline val KSClassDeclaration.defaultName get() = "${simpleName.asString()}Model"
	}
	
	companion object
	{
		private inline fun <V> List<KSValueArgument>.get(name: String, index: Int, default: () -> V): V
		{
			contract {
				callsInPlace(default, InvocationKind.AT_MOST_ONCE)
			}
			
			return get(name, index) ?: default()
		}
		
		@Suppress("UNCHECKED_CAST")
		private fun <V> List<KSValueArgument>.get(name: String, index: Int) =
			getArgument(name, index)?.value as V?
		
		private fun List<KSValueArgument>.getArgument(name: String, index: Int) =
			find { it.name?.asString() == name } ?: getOrNull(index)
	}
}

private abstract class PropertyInclusionStrategy
{
	// Stores annotations that have been established to indicate inclusion
	// or exclusion, so their annotations don't need to be searched again.
	private val encountered = ArrayList<KSTypeReference>(8)
	
	protected fun hasEncountered(type: KSTypeReference) = type in encountered
	
	protected fun encounter(type: KSTypeReference) = encountered.add(type)
	
	abstract fun canInclude(annotations: List<KSAnnotation>): Boolean
	
	protected val includedAnnotationName = Included::class.qualifiedName
	protected val excludedAnnotationName = Excluded::class.qualifiedName
	protected val propertyAnnotationName = Property::class.qualifiedName
}