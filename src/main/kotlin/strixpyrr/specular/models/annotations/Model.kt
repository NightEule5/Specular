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
package strixpyrr.specular.models.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

/**
 * Marks a class as a Model. This is optional.
 * @param labelType The model level attribute label type. Usually this will be an
 * [Enum] type.
 * @param propertyLabelType The property level attribute label type.
 * @since 0.5
 */
@Target(CLASS, TYPE)
annotation class Model(
	val memberInclusion: MemberInclusion = MemberInclusion.OptOut,
	val labelType: KClass<*> = String::class,
	val propertyLabelType: KClass<*> = String::class
)
{
	enum class MemberInclusion { OptIn, OptOut }
}