package org.hmx.scitos.core;

import org.hmx.scitos.core.i18n.Message;

/** Descriptor for an available export option. */
public class ExportOption {

    /** File type associated with an export option. */
    public enum TargetFileType {
        /** Export file type: Web Page (*.html). */
        HTML(".html"),
        /** Export file type: Spreadsheet (Open Document Standard - *.ods) */
        ODS(".ods");

        /** The file extension of an exported file. */
        private final String extension;

        /**
         * Constructor.
         *
         * @param extension
         *            the actual target file extension
         */
        private TargetFileType(final String extension) {
            this.extension = extension;
        }

        /**
         * Getter for the actual target file extension.
         *
         * @return file extension (e.g. ".html")
         */
        public String getExtension() {
            return this.extension;
        }
    }

    /** The text of the export menu entry. */
    private final Message menuEntry;
    /** The exported file's type. */
    private final TargetFileType targetFileType;
    /** The location of the XSLT stylesheet on the classpath. */
    private final String stylesheetPath;

    /**
     * Main constructor.
     *
     * @param menuEntry
     *            the associated export menu entry's text
     * @param targetFileType
     *            the type of the exported file
     * @param stylesheetPath
     *            the location of the XSLT stylesheet on the classpath
     */
    public ExportOption(final Message menuEntry, final TargetFileType targetFileType, final String stylesheetPath) {
        super();
        this.menuEntry = menuEntry;
        this.targetFileType = targetFileType;
        this.stylesheetPath = stylesheetPath;
    }

    /**
     * Getter for the associated export menu entry's text.
     *
     * @return the text of the export menu entry
     */
    public Message getMenuEntry() {
        return this.menuEntry;
    }

    /**
     * Getter for the exported file's type.
     *
     * @return the target file's type
     */
    public TargetFileType getTargetFileType() {
        return this.targetFileType;
    }

    /**
     * Getter for the location of the XSLT stylesheet on the classpath.
     *
     * @return the stylesheet's path
     */
    public String getStylesheetPath() {
        return this.stylesheetPath;
    }
}
