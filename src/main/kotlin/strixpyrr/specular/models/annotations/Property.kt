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

import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Includes a property in the model (same effect as [Included]). Optional metadata
 * can also be provided, such as a [name], [index], or [tags].
 * @param name The name of the property, or empty to use the property's actual name.
 * @param index An positive index positioning the property in relation to others in
 * the model. This can be used as a key. To disable this, set it negative.
 * @param tags The names of enum fields that, when used as keys, could point to the
 * property.
 * @since 0.5
 */
@InclusionAnnotation
@MustBeDocumented
@Target(PROPERTY)
annotation class Property(val name: String = "", val index: Int = -1, val tags: Array<String> = [])