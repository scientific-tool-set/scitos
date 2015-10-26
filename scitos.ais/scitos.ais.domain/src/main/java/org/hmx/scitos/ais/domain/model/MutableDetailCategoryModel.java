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

package org.hmx.scitos.ais.domain.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Implementation of a mutable detail category model for handling it outside of an actual project, in order to be able to modify or display it in its
 * hierarchical structure.
 */
public class MutableDetailCategoryModel implements IDetailCategoryProvider {

    /** The contained detail categories mapped by their code value. */
    private final Map<String, DetailCategory> categoryByCode;

    /** Main constructor: initializes an empty category model. */
    public MutableDetailCategoryModel() {
        this.categoryByCode = new LinkedHashMap<String, DetailCategory>();
    }

    /**
     * Add the given detail category to this detail category model.
     *
     * @param category
     *            detail category to add
     * @return self reference
     */
    public MutableDetailCategoryModel add(final DetailCategory category) {
        this.categoryByCode.put(category.getCode(), category);
        return this;
    }

    /**
     * Add the given detail categories to this detail category model.
     *
     * @param categories
     *            detail categories to add
     * @return self reference
     */
    public MutableDetailCategoryModel addAll(final List<DetailCategory> categories) {
        for (final DetailCategory singleCategory : categories) {
            this.add(singleCategory);
        }
        return this;
    }

    /**
     * Replace all current detail categories with the given ones.
     *
     * @param categories
     *            detail categories to set
     * @return self reference
     */
    public MutableDetailCategoryModel reset(final List<DetailCategory> categories) {
        this.categoryByCode.clear();
        this.addAll(categories);
        return this;
    }

    /**
     * Getter for the list of categories.
     *
     * @return the categories in this model
     */
    @Override
    public List<DetailCategory> provide() {
        return new ArrayList<DetailCategory>(this.categoryByCode.values());
    }

    @Override
    public List<DetailCategory> provideSelectables() {
        final List<DetailCategory> selectables = new LinkedList<DetailCategory>();
        for (final DetailCategory singleCategory : this.categoryByCode.values()) {
            if (singleCategory.isSelectable()) {
                selectables.add(singleCategory);
            }
        }
        return selectables;
    }

    /**
     * Getter for the detail categories, that have a {@code null} parent category.
     *
     * @return the root categories without any super ordinated category
     */
    public List<DetailCategory> getRootCategories() {
        return this.getChildCategories(null);
    }

    /**
     * Getter for the detail categories, that are children of the given detail category.
     *
     * @param parent
     *            detail category to collect the children categories for (can be {@code null} to get the list of root categories)
     * @return detail category that have the given one as their parent
     */
    public List<DetailCategory> getChildCategories(final DetailCategory parent) {
        final List<DetailCategory> children = new LinkedList<DetailCategory>();
        for (final DetailCategory singleCategory : this.categoryByCode.values()) {
            if (ComparisonUtil.isNullAwareEqual(singleCategory.getParent(), parent)) {
                children.add(singleCategory);
            }
        }
        return children;
    }

    /**
     * Getter for a single detail category, that is represented by the given unique code.
     *
     * @param detailCode
     *            code to get the associated detail category for
     * @return the detail category represented by the given unique code
     */
    public DetailCategory getDetailByCode(final String detailCode) {
        return this.categoryByCode.get(detailCode);
    }

    @Override
    public int hashCode() {
        return 29 + 13 * this.categoryByCode.size();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof MutableDetailCategoryModel)) {
            return false;
        }
        final MutableDetailCategoryModel otherModel = (MutableDetailCategoryModel) otherObject;
        if (this.categoryByCode.size() != otherModel.categoryByCode.size()) {
            return false;
        }
        for (final Entry<String, DetailCategory> categoryEntry : this.categoryByCode.entrySet()) {
            if (!categoryEntry.getValue().equals(otherModel.categoryByCode.get(categoryEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }
}
