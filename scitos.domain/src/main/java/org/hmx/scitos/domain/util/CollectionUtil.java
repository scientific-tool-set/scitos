/*
   Copyright (C) 2016 HermeneutiX.org

   This file is part of SciToS.

   SciToS is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   SciToS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with SciToS. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hmx.scitos.domain.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Convenience functions for handling collections.
 */
public final class CollectionUtil {

    /**
     * Check if the given collection contains the given object instance. Thereby avoiding the use of {@link Object#equals(Object) equals(Object)} and
     * {@link Object#hashCode() hashCode()} in the {@link Collection#contains(Object) contains(Object)} implementation of the collection itself.
     *
     * <p>
     * This check is supposed to be faster and can be used with objects, that are missing proper implementations of those methods. Additionally,
     * objects that are deemed {@link Object#equals(Object) equal} but not identical are ignored.
     * </p>
     *
     * @param collection
     *            collection to check for contained instance
     * @param instance
     *            specific object instance to check for
     * @param <T>
     *            type of the object to check for
     * @return if the given collection contains the given instance (not just an equal object)
     */
    public static <T> boolean containsInstance(final Collection<? extends T> collection, final T instance) {
        return -1 != CollectionUtil.indexOfInstance(collection, instance);
    }

    /**
     * Determine the index position of the first occurrence of the given object instance in the given collection. Thereby avoiding the use of
     * {@link Object#equals(Object) equals(Object)} and {@link Object#hashCode() hashCode()} in the {@link java.util.List#indexOf(Object)
     * indexOf(Object)} implementation of the collection/list itself.
     *
     * <p>
     * This check is supposed to be faster and can be used with objects, that are missing proper implementations of those methods. Additionally,
     * objects that are deemed {@link Object#equals(Object) equal} but not identical are ignored.
     * </p>
     *
     * @param collection
     *            collection to check for contained instance
     * @param instance
     *            specific object instance to check for
     * @param <T>
     *            type of the object to check for
     * @return position of the given instance in the collection (not just of an equal object)
     */
    public static <T> int indexOfInstance(final Collection<? extends T> collection, final T instance) {
        if (collection != null && !collection.isEmpty()) {
            int index = 0;
            for (final T collectionElement : collection) {
                if (collectionElement == instance) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * Count the number of occurrences in a given collection.
     *
     * @param <T>
     *            the type to count the occurrences in the given {@code collection} for
     * @param collection
     *            the targeted collection to count repeated instances in
     * @return mapping of contained instances to their respective count of occurrences
     */
    public static <T> Map<T, AtomicInteger> countOccurrences(final Collection<T> collection) {
        final Map<T, AtomicInteger> map = new HashMap<>();
        for (final T instance : collection) {
            final AtomicInteger counter = map.get(instance);
            if (counter == null) {
                map.put(instance, new AtomicInteger(1));
            } else {
                counter.incrementAndGet();
            }
        }
        return map;
    }

    /**
     * Move a single entry (indicated by its key) up or down by one step inside an insert sorted map (e.g. a {@link LinkedHashMap}).
     * 
     * @param <K>
     *            type of the map's keys
     * @param <V>
     *            type of the map's values
     * @param insertSortedMap
     *            map containing the entry to be moved; should be a map implementation preserving the insert order (e.g. a {@link LinkedHashMap})
     * @param entryKey
     *            key of the entry to be moved up/down by one step
     * @param increaseIndexByOne
     *            if the entry's index should be increased by one (i.e. moved down); otherwise decrease the entry's index by one (i.e. moved up)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the given map does not contain the specified key,</li>
     *             <li>if the specified entry is the first in the map and cannot be moved further up, or</li>
     *             <li>if the specified entry is the last in the map and cannot be moved further down</li>
     *             </ul>
     */
    public static <K, V> void moveEntryInInsertSortedMap(final Map<K, V> insertSortedMap, final K entryKey, final boolean increaseIndexByOne) {
        // #1 create a copy of the original key order as list (to make it accessible via index)
        final List<K> keyList = new ArrayList<>(insertSortedMap.keySet());
        // #2 determine the designated entry's current position
        final int index = keyList.indexOf(entryKey);
        // #3 determine the entry's new position
        final int indexToSwitchWith;
        if (increaseIndexByOne) {
            indexToSwitchWith = index + 1;
        } else {
            indexToSwitchWith = index - 1;
        }
        final int totalEntryCount = keyList.size();
        if (index == -1 || indexToSwitchWith == -1 || indexToSwitchWith == totalEntryCount) {
            // the entry cannot be moved as indicated
            throw new IllegalArgumentException();
        }
        // #4 create a copy of the unchanged relation template groups map
        final Map<K, V> groupsCopy = new LinkedHashMap<>(insertSortedMap);
        // #5 remove all mapping from the original relation template groups map, starting at the affected groups' indices
        insertSortedMap.keySet().retainAll(keyList.subList(0, Math.min(index, indexToSwitchWith)));
        final K entryToSwitchWith = keyList.get(indexToSwitchWith);
        // #6 re-insert the two affected groups in their new (inverse) order
        if (increaseIndexByOne) {
            insertSortedMap.put(entryToSwitchWith, groupsCopy.get(entryToSwitchWith));
            insertSortedMap.put(entryKey, groupsCopy.get(entryKey));
        } else {
            insertSortedMap.put(entryKey, groupsCopy.get(entryKey));
            insertSortedMap.put(entryToSwitchWith, groupsCopy.get(entryToSwitchWith));
        }
        // #7 re-insert all groups that are following the affected two relation tempate groups
        final int firstTrailingRetainedIndex = Math.max(index, indexToSwitchWith) + 1;
        if (firstTrailingRetainedIndex < totalEntryCount) {
            // there is at least one more value behind the affected two entries that needs to be re-inserted
            groupsCopy.keySet().retainAll(keyList.subList(firstTrailingRetainedIndex, totalEntryCount));
            insertSortedMap.putAll(groupsCopy);
        }
    }

    /**
     * Create a textual representation of the given collection, separating the individual elements by the provided separator.
     * 
     * @param collection
     *            the collection to represent as text
     * @param separator
     *            the separator to include between elements of the collection
     * @return the colleciton's textual representation (or an empty String if the collection is {@code null} or empty)
     */
    public static String toString(final Collection<?> collection, final String separator) {
        if (collection == null || collection.isEmpty()) {
            // just return an empty String if the collection is null or empty
            return "";
        }
        if (separator == null) {
            // fall back to the collection's toString() method if no custom separator has been specified
            return collection.toString();
        }
        // guess at a meaningful initial size
        final StringBuilder builder = new StringBuilder(collection.size() * (16 + separator.length()));
        boolean addSeparator = false;
        // iterate over the individual elements
        for (final Object collectionElement : collection) {
            if (addSeparator) {
                builder.append(separator);
            } else {
                addSeparator = true;
            }
            // null elements are treated as empty Strings
            if (collectionElement != null) {
                builder.append(collectionElement.toString());
            }
        }
        return builder.toString();
    }
}
