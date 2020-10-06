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
package strixpyrr.specular

import kotlin.reflect.KFunction

/**
 * Finds a parameterless function. Optionally, more [strict] filtering can exclude
 * functions with optional parameters.
 * @since 0.6
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <F : KFunction<*>> Iterable<F>.findParameterless(strict: Boolean = false) =
	if (strict)
		find { it.parameters.isEmpty() }
	else find { !it.hasParameters }

/**
 * Finds a parameterless function. Optionally, more [strict] filtering can exclude
 * functions with optional parameters.
 * @since 0.6
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <F : KFunction<*>> Sequence<F>.findParameterless(strict: Boolean = false) =
	if (strict)
		find { it.parameters.isEmpty() }
	else find { !it.hasParameters }

/**
 * Determines whether a function has parameters, where optional parameters are not
 * counted.
 * @since 0.6
 */
val KFunction<*>.hasParameters get() = !parameters.all { it.isOptional }
