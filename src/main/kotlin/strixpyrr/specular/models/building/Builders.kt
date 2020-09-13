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
package strixpyrr.specular.models.building

import strixpyrr.specular.models.IAttributeContainer
import strixpyrr.specular.models.IDelegatedProperty
import strixpyrr.specular.models.SimpleProperty
import uy.klutter.core.common.with
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KAnnotatedElement

// "Pure" builder manipulation

/**
 * @since 0.5
 */
@PublishedApi
internal inline fun <B : IBuilder<T>, T, R : T> build(factory: () -> B, block: B.() -> Unit): R
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	@Suppress("UNCHECKED_CAST")
	return factory().apply(block).build() as R;
}

// Attribute Containers

/**
 * @since 0.5
 */
inline fun <L> buildAttributeContainer(block: IAttributeContainerBuilder<L>.() -> Unit): IAttributeContainer<L>
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	return build(::AttributeContainerBuilder, block);
}

/**
 * @since 0.5
 */
inline fun <L> IAttributeContainerBuilder<L>.setAll(
	source: KAnnotatedElement,
	labelSelector: (Annotation) -> L,
	attributeSelector: (Annotation) -> Any?
) = setAll(source, { true }, labelSelector, attributeSelector);

/**
 * @since 0.5
 */
inline fun <L> IAttributeContainerBuilder<L>.setAll(
	source: KAnnotatedElement,
	filter: (Annotation) -> Boolean,
	labelSelector: (Annotation) -> L,
	attributeSelector: (Annotation) -> Any?
) = source.annotations.forEach {
	if (filter(it)) set(labelSelector(it), attributeSelector(it));
}

/**
 * @since 0.5
 */
fun <L> IAttributeContainerBuilder<L>.setAll(map: Map<out L, Any?>)
	= if (this is AttributeContainerBuilder<L>)
		addAll(map)
	else map.forEach { (l, a) -> set(l, a) };

// Attribute Providers

/**
 * @since 0.5
 */
inline fun <B : IAttributeProviderBuilder<L>, L> B.buildAttributes(
	block: IAttributeContainerBuilder<L>.() -> Unit
): B
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	return with { attributeContainer = buildAttributeContainer(block) };
}

/**
 * @since 0.5
 */
inline fun <B : SimpleModelBuilder<*>> B.buildAttributes(
	block: NamedAttributeContainerBuilder.() -> Unit
): B
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	return with { attributeContainer = build(::NamedAttributeContainerBuilder, block) };
}

// Properties

/**
 * @since 0.5
 */
inline fun <T, V, L> buildProperty(
	block: DelegatedPropertyBuilder<T, V, L>.() -> Unit
): IDelegatedProperty<T, V, L>
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	return build(::DelegatedPropertyBuilder, block);
}

/**
 * @since 0.5
 */
inline fun <T, V> buildProperty(
	block: SimplePropertyBuilder<T, V>.() -> Unit
): SimpleProperty<T, V>
{
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	return build(::SimplePropertyBuilder, block);
}