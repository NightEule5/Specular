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

import strixpyrr.abstrakt.collections.addFirst
import strixpyrr.specular.models.*
import strixpyrr.specular.models.internal.fix
import uy.klutter.core.collections.asReadOnly
import uy.klutter.core.common.verifiedWith
import uy.klutter.core.common.with
import uy.klutter.core.common.withNotNull
import java.util.LinkedList
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.primaryConstructor

/**
 * @since 0.5
 */
open class DelegatedModelBuilder<T : Any, K : Any, L, Lp> protected constructor(
	protected val storage: MutableMap<K, IDelegatedProperty<T, *, Lp>>,
	protected val factories: MutableList<IFactoryVariant<T>> = LinkedList()
) : IModelBuilder<T, K, L, Lp>, AttributeProviderBuilder<L>()
{
	val properties get() = storage.asReadOnly()
	
	/**
	 * @param keepInsertionOrder Whether the storage map should preserve insertion
	 * order. If left `true`, the storage map will be a [LinkedHashMap] instead of
	 * a normal [HashMap]. This tradeoff has the cost of a higher memory footprint.
	 */
	constructor(
		initialCapacity: Int = 8,
		keepInsertionOrder: Boolean = true
	) : this(storageMap(initialCapacity, keepInsertionOrder))
	
	inline fun <V> addProperty(
		key: K,
		block: DelegatedPropertyBuilder<T, V, Lp>.() -> Unit
	): DelegatedModelBuilder<T, K, L, Lp>
	{
		contract {
			callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		}
		
		return addProperty(key, buildProperty(block));
	}
	
	open fun <V> addProperty(key: K, propertyBuilder: DelegatedPropertyBuilder<T, V, Lp>)
		= addProperty(key, propertyBuilder.build());
	
	open fun <V> addProperty(key: K, property: IDelegatedProperty<T, V, Lp>)
		= with { storage[key] = property };
	
	open fun addFactoryOverloads(type: KClass<T>) = addFactoryOverloads(type) { true }
	
	inline fun addFactoryOverloads(
		type: KClass<T>,
		inclusion: (KFunction<T>) -> Boolean
	) = addFactoryOverloads(type, inclusion, KParameter::toFactoryParameter)
	
	inline fun addFactoryOverloads(
		type: KClass<T>,
		inclusion: (KFunction<T>) -> Boolean,
		parameterMap: (KParameter) -> IFactoryParameter
	)
	{
		type.primaryConstructor withNotNull
		{
			if (inclusion(this))
				addFactoryOverload(this, isPrimary = true, parameterMap)
		}
		
		for (constructor in type.constructors)
		{
			if (constructor.visibility == KVisibility.PUBLIC && inclusion(constructor))
				addFactoryOverload(constructor, isPrimary = false, parameterMap)
		}
	}
	
	open fun addFactoryOverload(factory: KFunction<T>, isPrimary: Boolean = false) =
		addFactoryOverload(factory, isPrimary, KParameter::toFactoryParameter)
	
	inline fun addFactoryOverload(factory: KFunction<T>, isPrimary: Boolean, parameterMap: (KParameter) -> IFactoryParameter) =
		addFactoryOverload(DelegatedFactoryOverload(isPrimary, factory, factory.mapParameters(parameterMap)))
	
	open fun addFactoryOverload(overload: DelegatedFactoryOverload<T>)
	{
		if (overload !in factories)
			if (overload.isPrimary)
				factories.addFirst(overload)
			else factories += overload
	}
	
	@PublishedApi
	internal inline fun KFunction<T>.mapParameters(map: (KParameter) -> IFactoryParameter) =
		parameters.map(map).asReadOnly()
	
	override fun build(): IDelegatedModel<T, K, L, Lp> =
		DelegatedModel(
			finalizeStorage(),
			attributeContainer,
			if (factories.isEmpty())
				emptyList()
			else
				ArrayList(
					factories verifiedWith ::verifyFactoryOverloads
				).asReadOnly()
		);
	
	protected open fun finalizeStorage() = storage.fix();
	
	protected fun verifyFactoryOverloads(overloads: MutableList<IFactoryVariant<T>>)
	{
		var hasPrimary = false
		
		overloads.forEachIndexed()
		{ i, factory ->
			if (factory.isPrimary)
			{
				if (hasPrimary)
					// Todo: Replace this with a more informative Exception.
					throw Exception(
						"Only one factory overload may be primary, but a second" +
						" was found at index $i."
					)
				
				hasPrimary = true
			}
		}
	}
	
	companion object
	{
		@JvmStatic
		protected fun <K, V> storageMap(capacity: Int, keepInsertionOrder: Boolean)
			= if (keepInsertionOrder) LinkedHashMap<K, V>(capacity) else HashMap<K, V>(capacity);
	}
}