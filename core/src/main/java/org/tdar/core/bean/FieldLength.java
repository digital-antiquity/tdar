package org.tdar.core.bean;

/**
 * Field Length labels.  Please use labels (as opposed to integer literals)
 * when defining ORM fields and validation rules.  This practice makes
 * it easier to track usages and easier to perform refactoring operations.
 */
public interface FieldLength {

    int FIELD_LENGTH_5 = 5;
    int FIELD_LENGTH_25 = 25;
    int FIELD_LENGTH_32 = 32;
    int FIELD_LENGTH_50 = 50;
    int FIELD_LENGTH_100 = 100;
    int FIELD_LENGTH_128 = 128;
    int FIELD_LENGTH_254 = 254;
    int FIELD_LENGTH_255 = 255;
    int FIELD_LENGTH_500 = 500;
    int FIELD_LENGTH_512 = 512;
    int FIELD_LENGTH_1024 = 1_024;
    int FIELD_LENGTH_2048 = 2_048;
    int FIELD_LENGTH_5000 = 5_000;
    int FIELD_LENGTH_8196 = 8_192;
}
