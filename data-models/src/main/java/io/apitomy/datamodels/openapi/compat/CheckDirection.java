package io.apitomy.datamodels.openapi.compat;

/**
 * Which replacement direction a compatibility check evaluates.
 */
public enum CheckDirection {

    /** Can the updated document replace the original for existing consumers? */
    BACKWARD,

    /** Can the original document replace the updated document for its consumers? */
    FORWARD
}
