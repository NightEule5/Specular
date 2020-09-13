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
class SimpleProperty<T, V>(
	get: T.() -> V,
	set: T.(V) -> Unit,
	isInitialized: T.() -> Boolean,
	type: KType,
	attributes: IAttributeContainer<String> = getEmptyAttributeContainer()
) : DelegatedProperty<T, V, String>(get, set, isInitialized, type, attributes)