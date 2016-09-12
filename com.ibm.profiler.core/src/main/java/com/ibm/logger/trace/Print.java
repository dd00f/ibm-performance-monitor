package com.ibm.logger.trace;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to flag how parameters should be printed during trace logs.
 * When flagged on a method, it's used to determine if the return value should be printed.
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface Print {

    /**
     * 
     * @return The printing mode associated with the parameter.
     */
    PrintMode value();

}
