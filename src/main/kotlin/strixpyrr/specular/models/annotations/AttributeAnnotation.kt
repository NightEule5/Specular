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

import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS

/**
 * Marks an annotation as an attribute. The value, marked by [AttributeValue], and
 * the label will be used to apply this annotation as an attribute to the model or
 * property it was applied to. The provider an [Enum] label, apply [AttributeLabel]
 * to a static property in the annotation.
 */
@MustBeDocumented
@Target(ANNOTATION_CLASS)
annotation class AttributeAnnotation