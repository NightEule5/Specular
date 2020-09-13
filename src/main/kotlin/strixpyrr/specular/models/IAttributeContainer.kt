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
interface IAttributeContainer<L>
{
	val labels: Set<L>;
	val attributes: Collection<Any?>;
	
	/**
	 * Determines whether an attribute exists under the specified [label].
	 */
	fun hasAttribute(label: L): Boolean;
	
	/**
	 * Get the attribute, if any, with the specified [label].
	 * @return The attribute found, casted to [A].
	 * @throws NoSuchElementException If the specified [label] isn't found.
	 */
	fun <A> getAttribute(label: L): A;
}