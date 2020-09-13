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
package strixpyrr.specular.models.factory

import strixpyrr.specular.models.IDelegatedModel
import strixpyrr.specular.models.IModel
import kotlin.reflect.KClass

/**
 * Produces a [IModel] from information provided in a target class.
 * @since 0.5
 */
interface IClassModelFactory
{
	fun <T : Any, K : Any, L : Any, Lp : Any> create(
		targetClass: KClass<T>,
		keyClass: KClass<K>,
		labelClass: KClass<L>,
		propertyLabelClass: KClass<Lp>
	): IDelegatedModel<T, K, L, Lp>;
}