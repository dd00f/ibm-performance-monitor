package com.ibm.logger.trace;

/**
 * Mode that can be used to print a parameter.
 */
public enum PrintMode {

    /**
     * Full parameter value printing using toString.
     */
    FULL,

    /**
     * The parameter shouldn't be displayed at all.
     */
    NO_DISPLAY,

    /**
     * The parameter value should be masked. Used to hide secure values.
     */
    MASK,

    /**
     * Display the hash value of the parameter. Used to avoid printing large strings.
     */
    HASH

}
