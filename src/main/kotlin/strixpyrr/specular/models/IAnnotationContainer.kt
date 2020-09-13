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

import kotlin.DeprecationLevel.WARNING
import kotlin.reflect.KClass

@Deprecated(
	"Marked for removal in favor of direct use of IAttributeContainer: store and" +
	" retrieve the values within the annotations, not the annotations themselves.",
	ReplaceWith("IAttributeContainer"), level = WARNING
)
interface IAnnotationContainer<L> : IAttributeContainer<L>
{
	/**
	 * Determines whether an annotation is present by its class, bypassing labels.
	 */
	fun hasAnnotation(annotation: KClass<out Annotation>)
		= attributes.any(annotation::isInstance);
	
	/**
	 * Searches for an annotation by its class, bypassing labels.
	 */
	fun <A : Annotation> getAnnotation(annotation: KClass<A>)
		= attributes.find(annotation::isInstance);
	
	companion object
	{
		inline fun <reified A : Annotation> IAnnotationContainer<*>.hasAnnotation()
			= hasAnnotation(A::class);
		
		inline fun <reified A : Annotation> IAnnotationContainer<*>.getAnnotation()
			= getAnnotation(A::class);
	}
}