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

package org.hmx.scitos.ais.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.domain.IMultiObjectModel;

/**
 * Representation of a single project (i.e. study) containing (i.e. conducting) a number of {@link Interview autobiographical interviews} and scoring
 * them.
 */
public final class AisProject implements IMultiObjectModel<AisProject, Interview>, IDetailCategoryProvider {

    /** The short descriptive label or title. */
    private String label;
    /** The detail categories that can be used for scoring tokens in interviews. */
    private final List<DetailCategory> categories;
    /** The contained interviews. */
    private final List<Interview> interviews = new LinkedList<Interview>();

    /**
     * Main constructor.
     *
     * @param label
     *            the short label representing the project's content (or name of the file it was loaded from)
     * @param categories
     *            the detail categories available to apply to text tokens
     */
    public AisProject(final String label, final List<DetailCategory> categories) {
        this.setLabel(label);
        this.categories = new ArrayList<DetailCategory>(categories);
    }

    /**
     * Getter for the short label representing the project's content.
     *
     * @return project label/title
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for the short label representing the project's content.
     *
     * @param value
     *            project label/title
     * @return self reference
     */
    public AisProject setLabel(final String value) {
        this.label = value;
        return this;
    }

    /**
     * Getter for the contained interviews.
     *
     * @return contained interviews
     */
    public List<Interview> getInterviews() {
        return Collections.unmodifiableList(this.interviews);
    }

    /**
     * Setter for the contained interviews.
     *
     * @param value
     *            contained interviews
     * @return self reference
     */
    public AisProject setInterviews(final List<Interview> value) {
        this.interviews.clear();
        if (value != null) {
            this.interviews.addAll(value);
        }
        return this;
    }

    /**
     * Setter for the contained detail category model.
     *
     * @param value
     *            contained detail category model
     * @return self reference
     */
    public AisProject setCategories(final List<DetailCategory> value) {
        this.categories.clear();
        this.categories.addAll(value);
        return this;
    }

    @Override
    public String getGroupKey(final Object interview) {
        return ((Interview) interview).getParticipantId();
    }

    @Override
    public Map<String, List<Interview>> getSubModelObjects() {
        final Map<String, List<Interview>> subModelMap = new HashMap<String, List<Interview>>();
        final List<Interview> sortedInterviews = new ArrayList<Interview>(this.interviews);
        Collections.sort(sortedInterviews);
        for (final Interview singleInterview : sortedInterviews) {
            final String groupKey = this.getGroupKey(singleInterview);
            final List<Interview> groupedInterviews;
            if (subModelMap.containsKey(groupKey)) {
                groupedInterviews = subModelMap.get(groupKey);
            } else {
                groupedInterviews = new LinkedList<Interview>();
                subModelMap.put(groupKey, groupedInterviews);
            }
            groupedInterviews.add(singleInterview);
        }
        return subModelMap;
    }

    @Override
    public List<DetailCategory> provide() {
        return Collections.unmodifiableList(this.categories);
    }

    @Override
    public List<DetailCategory> provideSelectables() {
        final List<DetailCategory> selectables = new LinkedList<DetailCategory>();
        for (final DetailCategory singleCategory : this.categories) {
            if (singleCategory.isSelectable()) {
                selectables.add(singleCategory);
            }
        }
        return selectables;
    }

    @Override
    public AisProject clone() {
        final List<Interview> clonedInterviews = new LinkedList<Interview>();
        for (final Interview singleInterview : this.getInterviews()) {
            clonedInterviews.add(singleInterview.clone());
        }
        // String and DetailCategory are immutable and don't need to be cloned
        return new AisProject(this.getLabel(), this.provide()).setInterviews(clonedInterviews);
    }

    @Override
    public int hashCode() {
        return (13 + this.categories.size()) * this.interviews.size() * 7;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof AisProject)) {
            return false;
        }
        final AisProject otherProject = (AisProject) otherObject;
        return this.categories.equals(otherProject.categories) && this.interviews.equals(otherProject.interviews);
    }
}
