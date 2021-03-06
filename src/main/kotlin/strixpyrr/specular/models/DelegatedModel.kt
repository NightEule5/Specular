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
package strixpyrr.specular.models

/**
 * @since 0.5
 */
open class DelegatedModel<T, K, L, Lp>(
	protected val propertyMap: Map<K, IDelegatedProperty<T, *, Lp>>,
	override val attributeContainer: IAttributeContainer<L>,
	override val factoryVariants: Collection<IFactoryVariant<T>> = emptyList()
) : IDelegatedModel<T, K, L, Lp>
{
	override val keys       by propertyMap::keys;
	override val properties by propertyMap::values;
	
	@Suppress("LeakingThis")
	override val canCreate = factoryVariants.isNotEmpty();
	
	override fun hasProperty(key: K) = propertyMap.containsKey(key);
	
	@Suppress("UNCHECKED_CAST")
	override fun <V> getProperty(key: K)
		= (propertyMap[key] as? IDelegatedProperty<T, V, Lp>) ?:
		  throw NoSuchElementException(
			  "The key $key does not exist in the property map."
		  );
}