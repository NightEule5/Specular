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

import strixpyrr.specular.isDerivedFrom
import strixpyrr.specular.models.annotations.AttributeAnnotation
import strixpyrr.specular.models.annotations.AttributeLabel
import strixpyrr.specular.models.annotations.AttributeValue
import strixpyrr.specular.models.building.IAttributeContainerBuilder
import strixpyrr.specular.models.factory.AnnotationTransformer.AttributeProvider
import strixpyrr.specular.models.factory.internal.enumFields
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/**
 * Transforms a [Annotation] into an [AttributeProvider], which returns attributes
 * from [Annotation]s to be manipulated by a model.
 * @since 0.5
 */
open class AnnotationTransformer
{
	private val attributeLookup = HashMap<KClass<*>, Annotation.() -> Any?>(16);
	
	// Ok, the "in" syntax is epic.
	fun isAttribute(annotationType: KClass<out Annotation>) =
		annotationType in attributeLookup ||
		annotationType
			.hasAnnotation<AttributeAnnotation>();
	
	open fun <L : Any> transform(
		annotated: KAnnotatedElement,
		labelType: KClass<L>,
		builder: IAttributeContainerBuilder<L>
	) = transform(annotated.annotations, labelType, builder);
	
	@Suppress("UNCHECKED_CAST")
	open fun <L : Any> transform(
		annotations: List<Annotation>,
		labelType: KClass<L>,
		builder: IAttributeContainerBuilder<L>
	)
	{
		for (annotation in annotations)
		{
			val type = annotation.annotationClass;
			
			if (isAttribute(type))
			{
				val label = getLabel(type, labelType) ?: throw Exception("A suitable label could not be found.");
				
				builder[label] = annotation.getValue(type);
			}
		}
	}
	
	protected open fun <L : Any> getLabel(type: KClass<out Annotation>, labelType: KClass<L>): L?
	{
		val name = labelType.safeCast(type.simpleName);
		
		if (name != null) return name;
		
		if (labelType.isDerivedFrom<Enum<*>>())
		{
			for (enum in labelType.enumFields)
			{
				val callbackType = enum.findAnnotation<AttributeLabel>()?.attribute;
				
				// If the annotation points back to the provided annotation type...
				if (type == callbackType) return labelType.cast(enum);
			}
		}
		
		if (labelType.isSealed)
		{
			TODO("Sealed classes are not supported as labels yet.")
		}
		
		return null;
	}
	
	@Suppress("UNCHECKED_CAST")
	protected open fun Annotation.getValue(type: KClass<out Annotation>): Any?
	{
		val getFromLookup = attributeLookup[type];
		
		if (getFromLookup != null) return getFromLookup();
		
		val get = (getValueProperty(type)?.getter ?: { true }) as Annotation.() -> Any?;
		
		attributeLookup[type] = get;
		
		return get();
	}
	
	protected open fun getValueProperty(type: KClass<out Annotation>): KProperty1<out Annotation, *>?
	{
		val parameters = type.memberProperties;
		
		for (parameter in parameters)
			if (parameter.hasAnnotation<AttributeValue>())
				return parameter;
		
		return parameters.find { it.name == "value" };
	}
	
	// TODO: Remove below this line.
	
	inline fun <reified A : Annotation, L> transform(label: L) = transform(A::class, label);
	
	open fun <A : Annotation, L> transform(annotationType: KClass<A>, label: L): IAttributeProvider<A, L>
	{
		val properties = annotationType.memberProperties;
		val property = properties.find {
						   it.hasAnnotation<AttributeValue>()
					   } ?: properties.find {
								it.name == "value"
							};
		
		return if (property != null)
			transform(annotationType, label, property::get);
		else transform(annotationType, label) { true };
	}
	
	inline fun <reified A : Annotation, L> transform(
		label: L,
		noinline value: A.() -> Any?
	) = transform(A::class, label, value);
	
	open fun <A : Annotation, L> transform(
		annotationType: KClass<A>,
		label: L,
		value: A.() -> Any?
	): IAttributeProvider<A, L>
		= AttributeProvider(annotationType, label, value);
	
	protected class AttributeProvider<A : Annotation, L>(
		override val type: KClass<A>,
		override val label: L,
		private val value: A.() -> Any?
	): IAttributeProvider<A, L>
	{
		override fun getValue(annotation: A) = annotation.value();
	}
}

/**
 * @since 0.5
 */
interface IAttributeProvider<A : Annotation, L>
{
	val type: KClass<A>;
	val label: L;
	
	fun getValue(annotation: A): Any?;
}