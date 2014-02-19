/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt;

import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Provides advanced methods for processing bidirectional text with a specific
 * structure to ensure proper presentation. For a general introduction to
 * structured text, see the explanations in {@link Processor}.
 * <p>
 * This interface provides an API for users who need finer control on structured
 * text handling. In particular, the user should use the methods in this
 * interface in the following cases:
 * </p>
 * <ul>
 * <li>The structured type handler to use is not one of those predefined in
 * {@link TypeHandlerFactory}.</li>
 * <li>A non-default {@link Environment environment} needs to be specified.</li>
 * <li>A call to a method processing a piece of text may create a state which
 * affects the processing of the next piece of text.</li>
 * <li>The user needs to manage the offsets where directional formatting
 * characters are inserted in the text.</li>
 * </ul>
 * <p>
 * Note that basic functions are provided in the {@link Processor} class.
 * </p>
 * <p>
 * To access the more sophisticated methods for processing structured text, the
 * user should proceed as follows:
 * </p>
 * <ul>
 * <li>The user has to obtain an appropriate <code>Expert</code> instance using
 * the methods in {@link ExpertFactory}.</li>
 * <li>The user may then invoke the more sophisticated methods provided by
 * <code>Expert</code> for processing the structured text.</li>
 * <li>If those methods should work in an environment different from the
 * default, the user may specify properties of the environment using the class
 * <@link Environment}.
 * <p>
 * This should be done before obtaining a <code>Expert</code> instance, and the
 * specific environment must be specified when invoking
 * <code>ExpertFactory</code> methods. This will associate a type handler with
 * the given environment and provide a customized <code>Expert</code> instance
 * to the user.
 * </p>
 * </li>
 * </ul>
 * <p>
 * Identifiers for several common handlers are included in
 * {@link TypeHandlerFactory}. For handlers supplied independently, a handler
 * instance can be obtained by instantiating a private handler.
 * </p>
 * <p>
 * Most of the methods in this interface have a <code>text</code> argument which
 * may be just a part of a larger body of text. When it is the case that the
 * text is submitted in parts with repeated calls, there may be a need to pass
 * information from one invocation to the next one. For instance, one invocation
 * may detect that a comment or a literal has been started but has not been
 * completed. In such cases, the state must be managed by a <code>Expert</code>
 * instance obtained with the {@link ExpertFactory#getStatefulExpert} method.
 * </p>
 * <p>
 * The <code>state</code> returned after processing a string can be retrieved,
 * set and reset using the {@link #getState()}, {@link #setState(Object)} and
 * {@link #clearState()} methods.
 * </p>
 * <p>
 * When submitting the initial part of a text, the state should be reset if it
 * is not the first processing call for this <code>Expert</code> instance.
 * </p>
 * <p>
 * Values returned by {@link #getState()} are opaque objects whose meaning is
 * internal to the relevant structured type handler. These values can only be
 * used in {@link #setState(Object)} calls to restore a state previously
 * obtained after processing a given part of a text before processing the next
 * part of the text.
 * </p>
 * <p>
 * Note that if the user does not modify the state, the state returned by a
 * given processing call is automatically passed as initial state to the next
 * processing call, provided that the expert is a stateful one.
 * </p>
 * <p>
 * <b>Code Samples</b>
 * </p>
 * <p>
 * The following code shows how to transform a certain type of structured text
 * (directory and file paths) in order to obtain the <i>full</i> text
 * corresponding to the given <i>lean</i> text.
 * 
 * <pre>
 * Expert expert = ExpertFactory.getExpert(TypeHandlerFactory.FILE);
 * String leanText = &quot;D:\\\u05d0\u05d1\\\u05d2\\\u05d3.ext&quot;;
 * String fullText = expert.leanToFullText(leanText);
 * System.out.println(&quot;full text = &quot; + fullText);
 * </pre>
 * 
 * </p>
 * <p>
 * The following code shows how to transform successive lines of Java code in
 * order to obtain the <i>full</i> text corresponding to the <i>lean</i> text of
 * each line.
 * 
 * <pre>
 *    Expert expert = ExpertFactory.getStatefulExpert(TypeHandlerFactory.JAVA);
 *    String leanText = "int i = 3; // first Java statement";
 *    String fullText = expert.leanToFullText(leanText);
 *    System.out.println("full text = " + fullText);
 *    leanText = "i += 4; // next Java statement";
 *    fullText = expert.leanToFullText(leanText,);
 *    System.out.println("full text = " + fullText);
 * </pre>
 * 
 * </p>
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 * 
 */
public interface Expert {

    static final char LRM = '\u200e';
    static final char RLM = '\u200f';
    static final char LRE = '\u202a';
    static final char RLE = '\u202b';
    static final char PDF = '\u202c';

    /**
     * Obtains the structured type handler associated with this
     * <code>Expert</code> instance.
     * 
     * @return the type handler instance.
     */
    public TypeHandler getTypeHandler();

    /**
     * Obtains the environment associated with this <code>Expert</code>
     * instance.
     * 
     * @return the environment instance.
     */
    public Environment getEnvironment();

    /**
     * Adds directional formatting characters to a structured text to ensure
     * correct presentation.
     * 
     * @param text
     *            is the structured text string
     * 
     * @return the structured text with directional formatting characters added
     *         to ensure correct presentation.
     */
    public String leanToFullText(String text);

    /**
     * Given a <i>lean</i> string, computes the positions of each of its
     * characters within the corresponding <i>full</i> string.
     * 
     * @param text
     *            is the structured text string.
     * 
     * @return an array of integers with one element for each of the characters
     *         in the <code>text</code> argument, equal to the offset of the
     *         corresponding character in the <i>full</i> string.
     */
    public int[] leanToFullMap(String text);

    /**
     * Given a <i>lean</i> string, computes the offsets of characters before
     * which directional formatting characters must be added in order to ensure
     * correct presentation.
     * <p>
     * Only LRMs (for a string with LTR base direction) and RLMs (for a string
     * with RTL base direction) are considered. Leading and trailing LRE, RLE
     * and PDF which might be prefixed or suffixed depending on the
     * {@link Environment#getOrientation orientation} of the GUI component used
     * for display are not reflected in this method.
     * </p>
     * 
     * @param text
     *            is the structured text string
     * 
     * @return an array of offsets to the characters in the <code>text</code>
     *         argument before which directional marks must be added to ensure
     *         correct presentation. The offsets are sorted in ascending order.
     */
    public int[] leanBidiCharOffsets(String text);

    /**
     * Removes directional formatting characters which were added to a
     * structured text string to ensure correct presentation.
     * 
     * @param text
     *            is the structured text string including directional formatting
     *            characters.
     * 
     * @return the structured text string without directional formatting
     *         characters which might have been added by processing it with
     *         {@link #leanToFullText}.
     * 
     */
    public String fullToLeanText(String text);

    /**
     * Given a <i>full</i> string, computes the positions of each of its
     * characters within the corresponding <i>lean</i> string.
     * 
     * @param text
     *            is the structured text string including directional formatting
     *            characters.
     * 
     * @return an array of integers with one element for each of the characters
     *         in the <code>text</code> argument, equal to the offset of the
     *         corresponding character in the <i>lean</i> string. If there is no
     *         corresponding character in the <i>lean</i> string (because the
     *         specified character is a directional formatting character added
     *         when invoking {@link #leanToFullText}), the value returned for
     *         this character is -1.
     */
    public int[] fullToLeanMap(String text);

    /**
     * Given a <i>full</i> string, returns the offsets of characters which are
     * directional formatting characters that have been added in order to ensure
     * correct presentation.
     * <p>
     * LRMs (for a string with LTR base direction), RLMs (for a string with RTL
     * base direction) are considered as well as leading and trailing LRE, RLE
     * and PDF which might be prefixed or suffixed depending on the
     * {@link Environment#getOrientation orientation} of the GUI component used
     * for display.
     * </p>
     * 
     * @param text
     *            is the structured text string including directional formatting
     *            characters
     * 
     * @return an array of offsets to the characters in the <code>text</code>
     *         argument which are directional formatting characters added to
     *         ensure correct presentation. The offsets are sorted in ascending
     *         order.
     */
    public int[] fullBidiCharOffsets(String text);

    /**
     * Adds directional marks to the given text before the characters specified
     * in the given array of offsets. It can be used to add a prefix and/or a
     * suffix of directional formatting characters.
     * <p>
     * The directional marks will be LRMs for structured text strings with LTR
     * base direction and RLMs for strings with RTL base direction.
     * </p>
     * <p>
     * If necessary, leading and trailing directional formatting characters
     * (LRE, RLE and PDF) can be added depending on the value of the
     * <code>affix</code> argument.
     * </p>
     * <ul>
     * <li>A value of 1 means that one LRM or RLM must be prefixed, depending on
     * the direction. This is useful when the GUI component presenting this text
     * has a contextual orientation.</li>
     * <li>A value of 2 means that LRE+LRM or RLE+RLM must be prefixed,
     * depending on the direction, and LRM+PDF or RLM+PDF must be suffixed,
     * depending on the direction. This is useful if the GUI component
     * presenting this text needs to have the text orientation explicitly
     * specified.</li>
     * <li>A value of 0 means that no prefix or suffix are needed.</li>
     * </ul>
     * 
     * @see Expert#leanBidiCharOffsets(String)
     * 
     * @param text
     *            the structured text string
     * @param offsets
     *            an array of offsets to characters in <code>text</code> before
     *            which an LRM or RLM will be inserted. The array must be sorted
     *            in ascending order without duplicates. This argument may be
     *            <code>null</code> if there are no marks to add.
     * @param direction
     *            the base direction of the structured text. It must be one of
     *            the values {@link #DIR_LTR}, or {@link #DIR_RTL}.
     * @param affixLength
     *            specifies the length of prefix and suffix which should be
     *            added to the result.<br>
     *            0 means no prefix or suffix<br>
     *            1 means one LRM or RLM as prefix and no suffix<br>
     *            2 means 2 characters in both prefix and suffix.
     * 
     * @return a string corresponding to the source <code>text</code> with
     *         directional marks (LRMs or RLMs) added at the specified offsets,
     *         and directional formatting characters (LRE, RLE, PDF) added as
     *         prefix and suffix if so required.
     */
    public String insertMarks(String text, int[] offsets, BidiStructuredProcessor.Orientation direction,
            int affixLength);

    /**
     * Get the base direction of a structured text. This base direction may
     * depend on whether the text contains Arabic or Hebrew words. If the text
     * contains both, the first Arabic or Hebrew letter in the text determines
     * which is the governing script.
     * 
     * @param text
     *            is the structured text string.
     * 
     * @return the base direction of the structured text, {@link #DIR_LTR} or
     *         {@link #DIR_RTL}
     */
    public BidiStructuredProcessor.Orientation getTextDirection(String text);

}