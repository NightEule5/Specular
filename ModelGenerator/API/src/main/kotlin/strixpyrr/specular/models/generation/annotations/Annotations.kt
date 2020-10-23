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
package strixpyrr.specular.models.generation.annotations

import strixpyrr.specular.models.generation.api.IModelGenerator
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * Marks a type for static model generation.
 * @param modelName The name of the model, or an empty [String] to use the class
 * name with a `Model` suffix.
 * @param keyType The key type of the generated model.
 * @param labelType The label type of the generated model.
 * @param propertyLabelType The property label type of the generated model.
 */
@MustBeDocumented
@Retention(SOURCE)
@Target(CLASS)
annotation class StaticModel(
	val modelName: String = "",
	val types: Types = Types(),
	val generator: KClass<out IModelGenerator> = IModelGenerator::class
)
{
	@Target
	annotation class Types(
		val key: KClass<*> = String::class,
		val modelLabel: KClass<*> = String::class,
		val propertyLabel: KClass<*> = String::class
	)
}

/**
 * Exposes an abstract or sealed class as a model fragment, a model implementation
 * with incomplete functionality.
 * @param generator The generator implementation to use.
 */
@Retention(SOURCE)
@Target(CLASS)
annotation class ModelFragment(val generator: KClass<out IModelGenerator> = IModelGenerator::class)