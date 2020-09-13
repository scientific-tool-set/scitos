package org.hmx.scitos.view.swing.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.hmx.scitos.domain.util.CollectionUtil;

/**
 * {@link GenericTypeMatcher} that finds the n-th instance of an AWT or Swing {@code Component}.
 *
 * @param <T>
 *            the type of {@code Component} supported by this matcher.
 */
public class OrdinalComponentMatcher<T extends Component> extends GenericTypeMatcher<T> {

    /** Indicator for the number of the component to match. */
    private final int targetOrdinal;
    /** Already encountered components of the supported type in order of appearance. */
    private final List<T> checkedComponents;

    /**
     * Creates a new {@link OrdinalComponentMatcher}. The {@code Component} to match does not have to be showing.
     *
     * @param supportedType
     *            the type supported by this matcher.
     * @param ordinal
     *            the targeted index of the component to match.
     * @throws NullPointerException
     *             if the given type is {@code null}.
     */
    public OrdinalComponentMatcher(final @Nonnull Class<T> supportedType, final int ordinal) {
        this(supportedType, ordinal, false);
    }

    /**
     * Creates a new {@link OrdinalComponentMatcher}.
     *
     * @param supportedType
     *            the type supported by this matcher.
     * @param ordinal
     *            the targeted index of the component to match.
     * @param requireShowing
     *            indicates if the {@code Component} to match should be showing or not.
     * @throws NullPointerException
     *             if the given type is {@code null}.
     */
    public OrdinalComponentMatcher(final @Nonnull Class<T> supportedType, final int ordinal, final boolean requireShowing) {
        super(supportedType, requireShowing);
        this.targetOrdinal = ordinal;
        // avoid counting the same component multiple times if the matcher is being called repeatedly but preserve component order
        this.checkedComponents = new ArrayList<>(ordinal + 1);
    }

    @Override
    protected boolean isMatching(final @Nonnull T component) {
        final boolean result;
        if (this.checkedComponents.size() > this.targetOrdinal) {
            // the designated number of components has been reached, check if the given component is at the target position
            result = this.checkedComponents.get(this.targetOrdinal) == component;
            // no need to add the component to the internal list
        } else if (CollectionUtil.containsInstance(this.checkedComponents, component)) {
            // the designated number of components has not been reached yet but the given component is already in the internal list
            result = false;
        } else {
            // the designated number of components has not been reached yet, add the (newly encountered) given component to the internal list
            this.checkedComponents.add(component);
            // the component could be at the target position now
            result = this.checkedComponents.size() > this.targetOrdinal;
        }
        return result;
    }

    @Override
    public void reset(final boolean matchFound) {
        super.reset(matchFound);
        this.checkedComponents.clear();
    }
}
