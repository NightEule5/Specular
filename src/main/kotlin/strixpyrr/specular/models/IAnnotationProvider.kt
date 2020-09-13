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

import kotlin.reflect.KClass

@Deprecated("")
interface IAnnotationProvider<L> : IAttributeProvider<L>
{
	override val attributeContainer: IAnnotationContainer<L>;
	
	fun hasAnnotation(annotation: KClass<out Annotation>)
		= attributeContainer.hasAnnotation(annotation);
	
	fun <A : Annotation> getAnnotation(annotation: KClass<A>)
		= attributeContainer.getAnnotation(annotation);
	
	companion object
	{
		inline fun <reified A : Annotation> IAnnotationProvider<*>.hasAnnotation() = hasAnnotation(A::class);
		inline fun <reified A : Annotation> IAnnotationProvider<*>.getAnnotation() = getAnnotation(A::class);
	}
}