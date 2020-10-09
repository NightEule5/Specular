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

import uy.klutter.core.collections.asReadOnly
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

/**
 * @since 0.6
 */
open class DelegatedFactoryOverload<T>(
	override val isPrimary: Boolean,
	override val parameters: Collection<IFactoryParameter>,
	val call: (Array<out Any?>) -> T,
	val callBy: (Map<KParameter, Any?>) -> T
) : IFactoryVariant<T>
{
	constructor(
		isPrimary: Boolean,
		function: KFunction<T>
	) : this(
		isPrimary,
		function,
		function.parameters.map(KParameter::toFactoryParameter).asReadOnly()
	)
	
	constructor(
		isPrimary: Boolean,
		function: KFunction<T>,
		parameters: Collection<IFactoryParameter>
	) : this(
		isPrimary,
		parameters,
		function::call,
		function::callBy
	)
	
	override fun create(parameters: Map<KParameter, Any?>) = callBy(parameters);
	
	override fun create(vararg parameters: Any?) = call(parameters);
}