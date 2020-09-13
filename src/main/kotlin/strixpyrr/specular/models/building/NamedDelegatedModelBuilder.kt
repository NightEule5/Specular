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

import strixpyrr.specular.models.IDelegatedProperty
import strixpyrr.specular.models.internal.fixAsCaseInsensitive

/**
 * @since 0.5
 */
open class NamedDelegatedModelBuilder<T, L, Lp> protected constructor(
	storage: MutableMap<String, IDelegatedProperty<T, *, Lp>>
) : DelegatedModelBuilder<T, String, L, Lp>(storage)
{
	// Todo: When a case-insensitive map is implemented, change it here too.
	
	/**
	 * Gets or sets whether the model's keys are case sensitive. If set to `false`,
	 * the storage is finalized to a [java.util.TreeMap] instead. Keep in mind this
	 * would essentially ignore insertion order.
	 */
	var isCaseSensitive = false;
	
	/**
	 * @param keepInsertionOrder Whether the storage map should preserve insertion
	 * order. If left `true`, the storage map will be a [LinkedHashMap] instead of
	 * a normal [HashMap]. This tradeoff has the cost of a higher memory footprint.
	 */
	constructor(
		initialCapacity: Int = 8,
		keepInsertionOrder: Boolean = true
	) : this(storageMap(initialCapacity, keepInsertionOrder))
	
	override fun finalizeStorage()
		= if (isCaseSensitive)
			super.finalizeStorage()
		else storage.fixAsCaseInsensitive();
}