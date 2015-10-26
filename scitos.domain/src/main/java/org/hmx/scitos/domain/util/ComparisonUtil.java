/*
   Copyright (C) 2015 HermeneutiX.org

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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collection of simple comparator functions.
 */
public final class ComparisonUtil {

    /**
     * Check if either both objects are {@code null} or one {@link Object#equals(Object) equals} the other.
     *
     * @param targetOne
     *            one object to check for equality
     * @param targetTwo
     *            other object to check for equality
     * @return if both are {@code null} or {@link Object#equals(Object) equal} to each other
     */
    public static boolean isNullAwareEqual(final Object targetOne, final Object targetTwo) {
        final boolean checkResult;
        if (targetOne == null) {
            checkResult = targetTwo == null;
        } else {
            checkResult = targetOne.equals(targetTwo);
        }
        return checkResult;
    }

    /**
     * Check if either both objects are {@code null} or {@link String#isEmpty() empty}. Otherwise check if one {@link String#equals(Object) equals}
     * the other.
     *
     * @param targetOne
     *            one {@code String} to check for equality
     * @param targetTwo
     *            other {@code String} to check for equality
     * @return if both are {@code null}/{@link String#isEmpty() empty} or {@link String#equals(Object) equal} to each other
     */
    public static boolean isNullOrEmptyAwareEqual(final String targetOne, final String targetTwo) {
        final boolean checkResult;
        if (targetOne == null || targetOne.isEmpty()) {
            checkResult = targetTwo == null || targetTwo.isEmpty();
        } else {
            checkResult = targetOne.equals(targetTwo);
        }
        return checkResult;
    }

    /**
     * Check if either both objects are {@code null} or {@link List#isEmpty() empty}. Otherwise check if one {@link List#equals(Object) equals} the
     * other.
     *
     * @param targetOne
     *            one list to check for equality
     * @param targetTwo
     *            other list to check for equality
     * @return if both are {@code null}/{@link List#isEmpty() empty} or {@link List#equals(Object) equal} to each other
     */
    public static boolean isNullOrEmptyAwareEqual(final List<?> targetOne, final List<?> targetTwo) {
        final boolean checkResult;
        if (targetOne == null || targetOne.isEmpty()) {
            checkResult = targetTwo == null || targetTwo.isEmpty();
        } else {
            checkResult = targetOne.equals(targetTwo);
        }
        return checkResult;
    }

    /**
     * Compares the first object with the second object for order. Returns a negative integer, zero, or a positive integer as the first object is less
     * than, equal to, or greater than the second object.
     *
     * <p>
     * If both objects are null, they are deemed equal (returns zero). If only the first object is null, {@code -1} is returned. If only the second
     * object is null, {@code 1} is returned. This ensures that {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))} for all {@code x} and {@code y},
     * as defined by {@link Comparable#compareTo(Object)}.
     * </p>
     *
     * @param targetOne
     *            first object to compare for order
     * @param targetTwo
     *            second object to compare for order
     * @param <T>
     *            type of the objects to compare
     * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or greater than the second object.
     */
    public static <T extends Comparable<T>> int compareNullAware(final T targetOne, final T targetTwo) {
        final int result;
        if (targetOne == targetTwo) {
            result = 0;
        } else if (targetOne == null) {
            result = -1;
        } else if (targetTwo == null) {
            result = 1;
        } else {
            result = targetOne.compareTo(targetTwo);
        }
        return result;
    }

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
        return -1 != ComparisonUtil.indexOfInstance(collection, instance);
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
        final Map<T, AtomicInteger> map = new HashMap<T, AtomicInteger>();
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
}
