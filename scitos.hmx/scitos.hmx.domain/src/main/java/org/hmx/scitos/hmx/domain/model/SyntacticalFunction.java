package org.hmx.scitos.hmx.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;

/**
 * Representation of an associated function in the syntactical analysis. Either for a {@link ClauseItem} in its containing {@link Proposition}, or for
 * a subordinated (i.e. indented) {@link Proposition} to its {@code parent} {@link Proposition}.
 */
public class SyntacticalFunction implements Serializable {

    /** Short identifier that is used as the textual representation of this function in the syntactical analysis. */
    private final String code;
    /** Full identifier for this function, displayed for selection in the syntactical analysis. */
    private final String name;
    /** More elaborate description what this syntactical function represents. */
    private final String description;
    /** If an element with this syntactical function should be underlined. */
    private final boolean underlined;
    /** Functions belonging to this function group – or an empty list if this a selectable function. */
    private final List<SyntacticalFunction> subFunctions;

    /**
     * Constructor: setting all fields to the given values to achieve immutability.
     *
     * @param code
     *            the short identifier used in the syntactical analysis
     * @param name
     *            the full identifier displayed when selecting this function in the syntactical analysis
     * @param underlined
     *            if an element with this syntactical function should be underlined
     * @param description
     *            the description of this function's meaning
     * @param subFunctions
     *            the functions belonging to this function group – {@code null} or an empty list indicate that this a selectable function (i.e. not a
     *            group)
     */
    public SyntacticalFunction(final String code, final String name, final boolean underlined, final String description,
            final List<SyntacticalFunction> subFunctions) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.underlined = underlined;
        if (subFunctions == null || subFunctions.isEmpty()) {
            this.subFunctions = Collections.emptyList();
        } else {
            this.subFunctions = Collections.unmodifiableList(new ArrayList<SyntacticalFunction>(subFunctions));
        }
    }

    /**
     * Getter for the short identifier that is used as the textual representation of this function in the syntactical analysis.
     *
     * @return the short identifier
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Getter for the full identifier that is used when displaying available functions in the syntactical analysis..
     *
     * @return the full identifier
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the more elaborate description what this syntactical function represents.
     *
     * @return the description of this function's meaning
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Getter for the flag indicating if an element with this syntactical function should be underlined..
     *
     * @return if an associated element should be underlined
     */
    public boolean isUnderlined() {
        return this.underlined;
    }

    /**
     * Getter for the functions belonging to this function group. This returns an empty list, if this a selectable function and not a group.
     *
     * @return the subordinated functions
     * @see #isSelectable()
     */
    public List<SyntacticalFunction> getSubFunctions() {
        return this.subFunctions;
    }

    /**
     * Determine if this function can be directly assigned to an element implementing the {@link ICanHaveSyntacticalFunction} interface.
     *
     * @return if this is a selectable function and not a group with contained sub functions
     * @see #getSubFunctions()
     */
    public boolean isSelectable() {
        return this.subFunctions.isEmpty();
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof SyntacticalFunction)) {
            return false;
        }
        final SyntacticalFunction otherFunction = (SyntacticalFunction) otherObj;
        return this.code.equals(otherFunction.code) && this.name.equals(otherFunction.name) && this.underlined == otherFunction.underlined
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.description, otherFunction.description)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.subFunctions, otherFunction.subFunctions);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("code: '").append(this.code).append("' - name: '").append(this.name);
        builder.append("' - description: '").append(this.description).append("' - underline: ").append(this.underlined);
        builder.append(" - subFunctions: ").append(this.subFunctions == null ? 0 : this.subFunctions.size());
        return builder.toString();
    }
}
