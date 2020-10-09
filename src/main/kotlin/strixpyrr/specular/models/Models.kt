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
@file:JvmName("ModelFactory")
package strixpyrr.specular.models

import strixpyrr.specular.models.factory.DecoratedClassModelFactory
import java.util.LinkedHashMap
import java.util.TreeMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

// Todo: The named attribute containers rely on TreeMaps for case insensitivity. I
//  would rather not use those, but re-implementing LinkedHashMap to make its hash
//  function case-insensitive is an optimization project for another day. Apache's
//  implementation is out of the question, as it creates new, lower-case strings.
//  I want to preserve case and retain some semblance of speed.

// Models //

@PublishedApi
internal val globalModelFactory by lazy(::DecoratedClassModelFactory)

/**
 * Creates a model. This is just syntactic sugar for [DecoratedClassModelFactory].
 * @param T The type of the target.
 * @param K The type of the keys used to access properties.
 * @param L The model-level attribute label type. If you don't know what this is,
 * use [String].
 * @param Lp The property-level attribute label type. If you don't know what this
 * is, use [String].
 * @since 0.5
 */
inline fun <reified T  : Any,
			reified K  : Any,
			reified L  : Any,
			reified Lp : Any> create()
	= globalModelFactory.create<T, K, L, Lp>();

// Attribute Containers //

/**
 * @since 0.5
 */
fun Iterable<Annotation>.toNamedAttributeContainer(
	ignoreCase: Boolean = true
) = toAttributeContainer(
		if (ignoreCase)
			TreeMap(String.CASE_INSENSITIVE_ORDER)
		else LinkedHashMap(count(), 1f)
	) { it.annotationClass.simpleName as String };

/**
 * @since 0.5
 */
inline fun <L, A> Iterable<A>.toAttributeContainer(
	label: (A) -> L
) = createAttributeContainer(associateBy(label));

/**
 * @since 0.5
 */
inline fun <L, A> Iterable<A>.toAttributeContainer(
	destination: MutableMap<L, A>,
	label: (A) -> L
) = createAttributeContainer(associateByTo(destination, label));

/**
 * @since 0.5
 */
fun <L, A> createAttributeContainer(
	attributes: Map<L, A>
) = AttributeContainer(attributes);

/**
 * @since 0.5
 */
@Suppress("UNCHECKED_CAST")
fun <L> getEmptyAttributeContainer()
	= EmptyAttributeContainer as IAttributeContainer<L>;

private object EmptyAttributeContainer : IAttributeContainer<Any?>
{
	override val labels     = emptySet <Any?>();
	override val attributes = emptyList<Any?>();
	
	override fun hasAttribute(label: Any?) = false;
	
	override fun <A> getAttribute(label: Any?)
		= throw NoSuchElementException("This container is empty.");
}

// Factory Variants

fun <T> createFactoryVariant(
	isPrimary: Boolean,
	factory: KFunction<T>
) = DelegatedFactoryOverload(isPrimary, factory)

// Factory Parameters

@Suppress("NOTHING_TO_INLINE")
inline fun KParameter.toFactoryParameter() = FactoryParameter(this);

@Suppress("NOTHING_TO_INLINE")
inline fun KParameter.toFactoryParameter(property: IProperty<*, *, *>) = PropertyLinkedFactoryParameter(this, property);