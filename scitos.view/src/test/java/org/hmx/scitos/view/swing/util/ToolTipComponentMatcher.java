package org.hmx.scitos.view.swing.util;

import javax.swing.JComponent;

import org.assertj.swing.core.ComponentMatcher;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.dependency.jsr305.Nonnull;

/**
 * {@link ComponentMatcher} that matches an AWT or Swing {@code Component} by type and its tool tip.
 * 
 * @param <T>
 *            the type of {@code Component} supported by this matcher.
 */
public class ToolTipComponentMatcher<T extends JComponent> extends GenericTypeMatcher<T> {

    private final String expectedToolTip;

    /**
     * Creates a new {@link ToolTipComponentMatcher}. The {@code Component} to match does not have to be showing.
     * 
     * @param supportedType
     *            the type supported by this matcher.
     * @param toolTipText
     *            the matching component's expected tool tip text.
     * @throws NullPointerException
     *             if the given type is {@code null}.
     */
    public ToolTipComponentMatcher(final @Nonnull Class<T> supportedType, final String toolTipText) {
        this(supportedType, toolTipText, false);
    }

    /**
     * Creates a new {@link ToolTipComponentMatcher}.
     * 
     * @param supportedType
     *            the type supported by this matcher.
     * @param toolTipText
     *            the matching component's expected tool tip text.
     * @param requireShowing
     *            indicates if the {@code Component} to match should be showing or not.
     * @throws NullPointerException
     *             if the given type is {@code null}.
     */
    public ToolTipComponentMatcher(final @Nonnull Class<T> supportedType, final String toolTipText, final boolean requireShowing) {
        super(supportedType, requireShowing);
        this.expectedToolTip = toolTipText;
    }

    @Override
    protected boolean isMatching(T component) {
        final String actualToolTip = component.getToolTipText();
        return this.expectedToolTip == null ? actualToolTip == null : this.expectedToolTip.equals(actualToolTip);
    }
}
