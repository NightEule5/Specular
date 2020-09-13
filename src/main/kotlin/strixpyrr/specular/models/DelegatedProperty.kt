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

import kotlin.reflect.KType

/**
 * @since 0.5
 */
open class DelegatedProperty<T, V, L>(
	override val get: T.() -> V,
	override val set: T.(V) -> Unit,
	val isInitialized: T.() -> Boolean,
	override val type: KType,
	attributes: IAttributeContainer<L>
) : IDelegatedProperty<T, V, L>
{
	override val attributeContainer = attributes;
	override val T.hasValue get() = isInitialized();
}