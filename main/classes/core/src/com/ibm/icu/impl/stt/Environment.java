/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt;

import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.util.ULocale;

/**
 * Describes the environment within which structured text strings are processed.
 * It includes:
 * <ul>
 * <li>locale,</li>
 * <li>desired orientation,</li>
 * <li>text mirroring attributes.</li>
 * </ul>
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public class Environment {

    /**
     * Pre-defined <code>Environment</code> instance which uses default locale,
     * non-mirrored environment, and a Left-to-Right presentation component.
     */
    public static final Environment DEFAULT = new Environment(null, false, BidiStructuredProcessor.Orientation.LTR);

    /**
     * The locale of the environment.
     */
    private ULocale locale = null;

    /**
     * Flag specifying that structured text processed under this environment
     * should assume that the GUI is mirrored (globally going from right to
     * left).
     */
    final private boolean mirrored;

    /**
     * Specify the orientation (a.k.a. base direction) of the GUI component in
     * which the <i>full</i> structured text will be displayed.
     */
    final private BidiStructuredProcessor.Orientation orientation;

    /**
     * Cached value that determines if the Bidi processing is needed in this
     * environment.
     */
    private Boolean processingNeeded;

    /**
     * Creates an instance of a structured text environment.
     * 
     * @param locale
     *            of the environment. Might be <code>null</code>, in which case
     *            the default locale is used.
     * @param mirrored
     *            specifies if the environment is mirrored.
     * @param orientation
     *            the orientation of the GUI component, {@link #getOrientation()}
     */
    public Environment(ULocale locale, boolean mirrored, BidiStructuredProcessor.Orientation orientation) {
        this.locale = locale == null ? ULocale.getDefault() : locale;
        this.mirrored = mirrored;
        this.orientation = orientation;
    }

    /**
     * Returns a String representing the language of the environment.
     * 
     * @return language of the environment
     */
    public ULocale getLocale() {
        return locale;
    }

    /**
     * Returns a flag indicating that structured text processed within this
     * environment should assume that the GUI is mirrored (globally going from
     * right to left).
     * 
     * @return <code>true</code> if environment is mirrored
     */
    public boolean getMirrored() {
        return mirrored;
    }

    /**
     * Returns the orientation (a.k.a. base direction) of the GUI component in
     * which the <i>full</i> structured text will be displayed.
     * <p>
     * The orientation value is one of the following:
     * <ul>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#LTR},</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#RTL},</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#CONTEXTUAL},</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#CONTEXTUAL_LTR},</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#CONTEXTUAL_RTL},</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#IGNORE}, or</li>
     * <li>{@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#UNKNOWN}</li>.
     * </ul>
     * </p>
     */
    public BidiStructuredProcessor.Orientation getOrientation() {
        return orientation;
    }

    /**
     * Checks if bidi processing is needed in this environment. The result
     * depends on the operating system (must be supported by this package) and
     * on the language supplied when constructing the instance (it must be a
     * language using a bidirectional script).
     * 
     * @return <code>true</code> if bidi processing is needed in this
     *         environment.
     */
    public boolean isProcessingNeeded() {
        if (processingNeeded == null) {
            String osName = System.getProperty("os.name");
            if (osName != null)
                osName = osName.toLowerCase();
            boolean supportedOS = osName.startsWith("windows")
                    || osName.startsWith("linux") || osName.startsWith("mac");
            if (supportedOS) {
                // Check whether the current language uses a bidi script
                // (Arabic, Hebrew, Farsi or Urdu)
                String language = locale.getLanguage();
                boolean isBidi = "iw".equals(language) || "he".equals(language)
                        || "ar".equals(language) || "fa".equals(language)
                        || "ur".equals(language);
                processingNeeded = new Boolean(isBidi);
            } else {
                processingNeeded = new Boolean(false);
            }
        }
        return processingNeeded.booleanValue();
    }

    /*
     * (non-Javadoc)
     * 
     * Computes the hashCode based on the values supplied when constructing the
     * instance and on the result of {@link #isProcessingNeeded()}.
     * 
     * @return the hash code.
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((locale == null) ? 0 : locale.getLanguage().hashCode());
        result = prime * result + (mirrored ? 1231 : 1237);
        result = prime * result + orientation.hashCode();
        result = prime
                * result
                + ((processingNeeded == null) ? 0 : processingNeeded.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * Compare 2 environment instances and returns true if both instances were
     * constructed with the same arguments.
     * 
     * @return true if the 2 instances can be used interchangeably.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Environment other = (Environment) obj;
        if (locale == null) {
            if (other.locale != null)
                return false;
        } else if (!locale.getLanguage().equals(other.locale.getLanguage()))
            return false;
        if (mirrored != other.mirrored)
            return false;
        if (orientation != other.orientation)
            return false;
        if (processingNeeded == null) {
            if (other.processingNeeded != null)
                return false;
        } else if (!processingNeeded.equals(other.processingNeeded))
            return false;
        return true;
    }

}