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

import strixpyrr.specular.models.AttributeContainer
import strixpyrr.specular.models.IAttributeContainer
import strixpyrr.specular.models.internal.fix

/**
 * @since 0.5
 */
open class AttributeContainerBuilder<L> protected constructor(
	protected val storage: MutableMap<L, Any?>
) : IAttributeContainerBuilder<L>
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
	
	override fun contains(label: L) = storage.containsKey(label);
	
	override fun get(label: L)
		= storage[label] ?: throw NoSuchElementException(
			"An attribute with the label $label could not be found."
		);
	
	override fun set(label: L, attribute: Any?)
	{
		storage[label] = attribute;
	}
	
	override fun remove(label: L)
	{
		storage.remove(label);
	}
	
	override fun clear() = storage.clear();
	
	fun addAll(map: Map<out L, Any?>)
		= storage.putAll(map);
	
	override fun build(): IAttributeContainer<L>
		= AttributeContainer(finalizeStorage());
	
	/**
	 * Finalizes the storage map, which usually means matching its capacity to its
	 * size exactly. The default implementation also fixes its iteration order via
	 * a [LinkedHashMap], which is made read-only by a wrapper class. If [storage]
	 * is already a [LinkedHashMap], this order will not change.
	 */
	protected open fun finalizeStorage() = storage.fix();
	
	companion object
	{
		@JvmStatic
		protected fun <K, V> storageMap(capacity: Int, keepInsertionOrder: Boolean)
			= if (keepInsertionOrder) LinkedHashMap<K, V>(capacity) else HashMap<K, V>(capacity);
	}
}