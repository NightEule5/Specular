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
package strixpyrr.specular.models.factory

import strixpyrr.specular.isBaseOf
import strixpyrr.specular.isSubtypeOf
import strixpyrr.specular.models.annotations.*
import strixpyrr.specular.models.building.DelegatedModelBuilder
import strixpyrr.specular.models.building.buildAttributes
import strixpyrr.specular.models.factory.internal.isEnum
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

/**
 * @since 0.5
 */
open class DecoratedClassModelFactory : IClassModelFactory
{
	var transformer = AnnotationTransformer();
	
	inline fun <reified T : Any, reified K : Any, reified L : Any, reified Lp : Any> create()
		= create(T::class, K::class, L::class, Lp::class);
	
	override fun <T : Any, K : Any, L : Any, Lp : Any> create(
		targetClass: KClass<T>,
		keyClass: KClass<K>,
		labelClass: KClass<L>,
		propertyLabelClass: KClass<Lp>
	) = Context<T, K, L, Lp>(
		targetClass,
		keyClass,
		labelClass,
		propertyLabelClass
	).apply { create() }.builder.build();
	
	protected open fun <T : Any, K : Any, L : Any, Lp : Any> Context<T, K, L, Lp>.create()
	{
		builder.buildAttributes {
			transformer.transform(target, labelType, this)
		}
		
		val properties = target.memberProperties;
		val mutable = properties
							.asSequence()
							.filter { it.visibility == KVisibility.PUBLIC }
							.filter { it is KMutableProperty1 };
		
		for (property in mutable)
		{
			val annotations = property.annotations;
			
			if (inclusion.canInclude(annotations))
			{
				val metadata = property.findAnnotation<Property>();
				val name = if (metadata == null || metadata.name.isBlank())
							   property.name
						   else metadata.name;
				val key = getKey(property, name, metadata);
				
				builder.addProperty<Any?>(key) {
					buildAttributes {
						transformer.transform(annotations, propertyLabelType, this);
					}
					
					@Suppress("UNCHECKED_CAST") // Todo: Will this fail?
					setFromProperty(property as KMutableProperty1<T, Any?>);
					
					// Initialization
					
					val initPropertyName = "has$name";
					var initProperty: KProperty1<T, Boolean>? = null;
					
					for (p in properties)
					{
						if (p.name.equals(initPropertyName, true))
						{
							if (p.returnType.isSubtypeOf<Boolean>())
								@Suppress("UNCHECKED_CAST")
								initProperty = p as KProperty1<T, Boolean>;
							
							break;
						}
					}
					
					if (initProperty != null)
						setInitialization(initProperty::get);
					else if (property.isLateinit && valueType.isMarkedNullable)
						setInitialization { property.get(this) != null };
				}
			}
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	protected open fun <T : Any, K : Any, L : Any, Lp : Any> Context<T, K, L, Lp>.getKey(
		property: KProperty1<T, *>,
		name: String,
		metadata: Property?
	): K
	{
		if (keyType.isSealed) TODO("Sealed classes as keys aren't supported yet. Use enums for the time being.")
		
		if (keyType.isEnum)
		{
			val constants = keyType.java.enumConstants;
			
			if (metadata == null)
			{
				for (constant in constants)
					if ((constant as Enum<*>).name.equals(name, ignoreCase = true))
						return constant;
			}
			else
			{
				for (constant in constants)
					for (tag in metadata.tags)
						if ((constant as Enum<*>).name.equals(tag, ignoreCase = true))
							return constant;
				
				for (constant in constants)
					if ((constant as Enum<*>).name.equals(name, ignoreCase = true))
						return constant;
				
				for (constant in constants)
					if ((constant as Enum<*>).ordinal == metadata.index)
						return constant;
			}
			
			throw Exception(
				"An enum constant in $keyType could not be found as a key for a" +
				" property: $property."
			);
		}
		
		if (keyType.isBaseOf<String>()) return name as K;
		
		if (keyType.isBaseOf<Int>() && metadata != null)
			return metadata.index as K;
		
		throw Exception(
			"No key could be found for a property: $property. The specified key" +
			" type, $keyType, is unknown."
		)
	}
	
	// Todo: Hmm, this got out of hand fast. Tame it down a bit maybe?
	protected class Context<T : Any, K : Any, L : Any, Lp : Any>(val target: KClass<T>, val keyType: KClass<K>, val labelType: KClass<L>, val propertyLabelType: KClass<Lp>)
	{
		val builder = DelegatedModelBuilder<T, K, L, Lp>(initialCapacity = 16);
		val isOptIn: Boolean;
		
		init
		{
			val metadata = target.findAnnotation<Model>();
			val inclusion = metadata?.memberInclusion ?: Model.MemberInclusion.OptOut;
			
			isOptIn = inclusion == Model.MemberInclusion.OptIn;
		}
		
		val inclusion by lazy { PropertyInclusionStrategy.get(isOptIn) };
		
		abstract class PropertyInclusionStrategy
		{
			companion object
			{
				fun get(isOptIn: Boolean) =
					if (isOptIn)
						ExcludedByDefault()
					else IncludedByDefault();
			}
			
			// Stores annotations that have been established to indicate inclusion
			// or exclusion, so their annotations don't need to be searched again.
			private val encountered = ArrayList<KClass<out Annotation>>(8);
			
			protected fun hasEncountered(type: KClass<out Annotation>)
				= encountered.contains(type);
			
			protected fun encounter(type: KClass<out Annotation>)
				= encountered.add(type);
			
			abstract fun canInclude(annotations: List<Annotation>): Boolean;
			
			// A property is excluded unless explicitly Included. The Excluded
			// annotation is ignored.
			private class ExcludedByDefault : PropertyInclusionStrategy()
			{
				override fun canInclude(annotations: List<Annotation>): Boolean
				{
					for (annotation in annotations)
					{
						if (annotation is Included || annotation is Property) return true;
						
						val type = annotation.annotationClass;
						
						if (hasEncountered(type)) return true;
						
						if (type.hasAnnotation<InclusionAnnotation>())
						{
							encounter(type);
							return true;
						}
					}
					
					return false;
				}
			}
			
			// A property is included unless explicitly Excluded. The Included
			// annotation is ignored.
			private class IncludedByDefault : PropertyInclusionStrategy()
			{
				override fun canInclude(annotations: List<Annotation>): Boolean
				{
					for (annotation in annotations)
					{
						if (annotation is Excluded) return false;
						
						val type = annotation.annotationClass;
						
						if (hasEncountered(type)) return false;
						
						if (type.hasAnnotation<ExclusionAnnotation>())
						{
							encounter(type);
							return false;
						}
					}
					
					return true;
				}
			}
		}
	}
}