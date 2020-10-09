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
package strixpyrr.specular.models.internal

import java.io.Serializable
import java.util.TreeMap

// Read Only Maps

internal fun <K, V> Map<K, V>.fix(): Map<K, V> = ReadOnlyHashMap(LinkedHashMap(this));

internal fun <V> Map<String, V>.fixAsCaseInsensitive(): Map<String, V>
{
	val map = TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER);
	
	map.putAll(this);
	
	return ReadOnlyTreeMap(map);
}

private class ReadOnlyHashMap<K, V>(private val map: HashMap<K, V>) : Map<K, V> by map, Serializable by map
private class ReadOnlyTreeMap<K, V>(private val map: TreeMap<K, V>) : Map<K, V> by map, Serializable by map

// Sort

internal fun <K, V : Comparable<V>> MutableMap<K, V>.sortDescendingByValue()
{
	val list = toDescendingSortedListByValue();
	
	// Clear the map to remove any structured ordering that was already there.
	clear();
	
	for ((key, value) in list)
		put(key, value);
}

// This method name is ridiculous lol
internal fun <K, V : Comparable<V>> Map<K, V>.toDescendingSortedListByValue(): MutableList<Map.Entry<K, V>>
{
	// toSortedMap uses a TreeMap (could be slow) and can't sorting by value or in
	// descending order, so that's out. Sequence sorting converts to a list, which
	// is problematic since sequences don't know the original size. Sorting toList
	// converts to an array first, so that's out as well. The least expensive way,
	// that I know of at least, is to sort mutable lists or arrays directly. I'll
	// opt for the former.
	
	return entries.toMutableList().apply { sortByDescending(Map.Entry<K, V>::value) };
}

// Contextual "All"

@JvmName("allWithContext")
internal inline fun <T> Iterable<T>.all(block: (prev: T, curr: T) -> Boolean): Boolean
{
	if (count() < 2) return true;
	
	val values = iterator();
	
	var previous = values.next();
	
	for (value in values)
		if (!block(previous, value))
			return false;
		else previous = value;
	
	return true;
}

// Indexed "All"

@JvmName("allIndexed")
internal inline fun <T> Iterable<T>.all(block: (Int, T) -> Boolean): Boolean
{
	var i = 0
	
	return all { v -> block(i++, v) }
}