package org.tdar.utils;

public class MathUtils {

    public static final long ONE_MB = 1048576L;

    public static long divideByRoundUp(Number number1, Number number2) {
        return (long) Math.ceil(divideBy(number1, number2));
    }

    public static long divideByRoundDown(Number number1, Number number2) {
        return (long) Math.floor(divideBy(number1, number2));
    }

    public static double divideBy(Number number1, Number number2) {
        double n1 = 0;
        double n2 = 0;
        if (number1 != null) {
            n1 = number1.doubleValue();
        }
        if (number2 != null) {
            n2 = number2.doubleValue();
        }
        return n1 / n2;
    }

}
