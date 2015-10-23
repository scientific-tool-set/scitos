package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.components.SemAnalysisPanel;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.swing.ContextMenuPopupBuilder;
import org.hmx.scitos.view.swing.components.ScaledTextField;

/**
 * extension of the {@link AbstractProposition} with a non-editable {@link JTextField} for the origin text in the top right position
 */
public final class SemProposition extends AbstractProposition implements IConnectable<Proposition> {

    /**
     * super ordinated semantical analysis panel
     */
    private final SemAnalysisPanel semArea;
    /**
     * single field containing the origin text of all clause items
     */
    private final JTextField originText = new ScaledTextField();

    /**
     * creates a new {@link SemProposition} on the specified {@link SemAnalysisPanel} representing the designated {@link Proposition} with the defined
     * language and {@link Font} of the origin text to display
     *
     * @param semPanel
     *            semantical analysis view to be contained in
     * @param represented
     *            model {@link Proposition} to display
     */
    public SemProposition(final IPericopeView viewReference, final SemAnalysisPanel semPanel, final Proposition represented) {
        super(viewReference.getModelHandler(), represented);
        this.semArea = semPanel;
        this.init();
        // indent partAfterArrow
        if (represented.getPartBeforeArrow() != null) {
            this.getIndentationArea().setPreferredSize(AbstractProposition.createIndentation(1));
        }
        if (represented.getSuperOrdinatedRelation() != null) {
            this.setCheckBoxVisible(false);
        }
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                viewReference.handleSelectedCommentable(SemProposition.this);
                this.mouseReleased(event);
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    final ContextMenuBuilder contextMenu = ContextMenuFactory.createSemPropositionPopup(viewReference, represented);
                    ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
    }

    /**
     * initializes the explicit semantical functionality by adding a listener to the translation field, activating the comment listener and inserting
     * the {@link JTextField} for the origin text with the specified {@link Font}
     */
    private void init() {
        this.getTranslationField().addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent event) {
                // only transfer if necessary
                final String translationText = SemProposition.this.getTranslationField().getText();
                final String storedTranslation = SemProposition.this.getRepresented().getSemTranslation();
                if (((translationText != null && !translationText.isEmpty()) || (storedTranslation != null && storedTranslation.isEmpty()))
                        && ((translationText == null) || (!translationText.equals(storedTranslation)))) {
                    SemProposition.this.getModelHandler().setSemTranslation(SemProposition.this.getRepresented(), translationText);
                }
            }
        });
        this.originText.setFont(this.getModelHandler().getModel().getFont());
        this.originText.setEditable(false);
        this.originText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        this.refreshOriginText();
        this.getItemArea().add(this.originText);
        this.refreshComment();
    }

    @Override
    public int getDepth() {
        // propositions are always on the lowest level
        return 0;
    }

    /**
     * @return displayed origin text
     */
    public String getOriginText() {
        return this.originText.getText();
    }

    /**
     * set the displayed origin text regarding to the origin text stored in the represented {@link Proposition}s {@link ClauseItem}s
     */
    public void refreshOriginText() {
        final StringBuffer text = new StringBuffer(" ");
        for (final ClauseItem singleItem : this.getRepresented()) {
            text.append(singleItem.getOriginText() + ' ');
        }
        this.originText.setText(text.toString());
        this.originText.setSize(this.originText.getPreferredSize());
    }

    @Override
    public double getConnectY() {
        return 0.5 + ComparisonUtil.indexOfInstance(this.semArea.getPropositionList(), this);
    }

    /**
     * sets the semantical translation text regarding the stored translation in the represented {@link Proposition}
     *
     * @see AbstractProposition#refreshTranslation()
     */
    @Override
    public void refreshTranslation() {
        this.getTranslationField().setText(this.getRepresented().getSemTranslation());
        super.refreshTranslation();
    }

    /**
     * resets the tool tip info containing the comment text regarding its value in the represented {@link Proposition}
     */
    public void refreshComment() {
        final String comment = this.getRepresented().getComment();
        if (comment == null || comment.isEmpty()) {
            this.setToolTipText(null);
        } else {
            this.setToolTipText(comment);
        }
    }

    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        this.originText.addMouseListener(listener);
        super.addMouseListener(listener);
    }

    @Override
    public synchronized void setToolTipText(final String toolTip) {
        this.originText.setToolTipText(toolTip);
        super.setToolTipText(toolTip);
    }
}
