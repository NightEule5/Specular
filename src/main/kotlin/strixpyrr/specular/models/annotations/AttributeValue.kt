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

import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * For annotations with the [AttributeAnnotation], this marks a parameter as an
 * attribute value. This is optional: if the `value()` parameter is present, it
 * will be used; otherwise, the attribute will be a [Boolean] (ie always `true`
 * and just existing to convey presence).
 * @since 0.5
 */
@Target(VALUE_PARAMETER)
annotation class AttributeValue