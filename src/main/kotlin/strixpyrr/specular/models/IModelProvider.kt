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

import strixpyrr.specular.models.annotations.Excluded

/**
 * Exposes a model instance. This is intended to be implemented by target types for
 * direct model creation, but it doesn't have to be used this way.
 * @since 0.5
 */
interface IModelProvider<T, K, L, Lp>
{
	@Excluded
	val model: IModel<T, K, L, Lp>;
}