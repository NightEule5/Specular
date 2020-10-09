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

import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Marks a constructor or static function as a factory. Along with [Parameter],
 * this directs how models will create instances of the target. This currently has
 * no effect on constructors, as they're included by default. Constructors can be
 * opted out by [ExcludedFactory].
 * @since 0.6
 */
@MustBeDocumented
@Target(CONSTRUCTOR, FUNCTION)
annotation class Factory