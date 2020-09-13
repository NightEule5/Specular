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

import strixpyrr.specular.models.DelegatedModel
import strixpyrr.specular.models.IDelegatedModel
import strixpyrr.specular.models.IDelegatedProperty
import strixpyrr.specular.models.internal.fix
import uy.klutter.core.common.with
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @since 0.5
 */
open class DelegatedModelBuilder<T, K, L, Lp> protected constructor(
	protected val storage: MutableMap<K, IDelegatedProperty<T, *, Lp>>
) : IModelBuilder<T, K, L, Lp>, AttributeProviderBuilder<L>()
{
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
	
	override fun build(): IDelegatedModel<T, K, L, Lp> = DelegatedModel(finalizeStorage(), attributeContainer);
	
	protected open fun finalizeStorage() = storage.fix();
	
	companion object
	{
		@JvmStatic
		protected fun <K, V> storageMap(capacity: Int, keepInsertionOrder: Boolean)
			= if (keepInsertionOrder) LinkedHashMap<K, V>(capacity) else HashMap<K, V>(capacity);
	}
}