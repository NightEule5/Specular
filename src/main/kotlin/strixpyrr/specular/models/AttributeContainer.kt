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

// Todo: Rename "labels" to "tags"?

/**
 * @since 0.5
 */
@Suppress("LeakingThis")
open class AttributeContainer<L, A>(
	protected val attributeMap: Map<L, A>
) : IAttributeContainer<L>
{
	// The "leaking this in constructor" shouldn't be an issue, as long as the map
	// is initialized before these are called.
	// Also, calling the map's key and value properties doesn't create new sets as
	// I originally thought, so the performance here is actually fine.
	@Suppress("LeakingThis") override val labels     by attributeMap::keys  ;
	@Suppress("LeakingThis") override val attributes by attributeMap::values;
	
	override fun hasAttribute(label: L) = attributeMap.containsKey(label);
	
	@Suppress("UNCHECKED_CAST")
	override fun <A> getAttribute(label: L)
		= attributeMap[label] as A? ?: throw NoSuchElementException(
			"No attribute with the specified label exists: $label."
		);
}