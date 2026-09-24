package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * An exact decimal value, parsed from a JSON number's literal source text and
 * compared using digit-string arithmetic rather than floating point.
 * <p>
 * JSON numbers are IEEE-754 doubles in JavaScript; two distinct decimal
 * literals can be indistinguishable once parsed ({@code 9007199254740993} and
 * {@code 9007199254740992} both become the same double), and floating
 * division introduces error that a tolerance-based comparison can mistake for
 * an exact multiple ({@code 0.30000000000000004 / 0.1} is not an integer as a
 * double, even though {@code 0.3} is an exact multiple of {@code 0.1}).
 * {@code ExactDecimal} avoids both problems by never converting its digits to
 * a native number: {@link #parse(String)} keeps the sign, significant digits,
 * and decimal scale exactly as written, {@link #compareTo(ExactDecimal)}
 * compares digit strings after aligning scale, and
 * {@link #isIntegralMultipleOf(ExactDecimal)} performs schoolbook long
 * division on digit strings to test divisibility exactly.
 * <p>
 * {@code java.math.BigDecimal} is not used here (or anywhere reachable from
 * the TypeScript build): it has no JSweet/TypeScript equivalent. This class's
 * arithmetic is hand-written for exactly that reason, and must remain
 * transpilable.
 */
public final class ExactDecimal implements Comparable<ExactDecimal> {

    /** Not negative; the unscaled significant digits, no leading zeros except a bare {@code "0"}. */
    private final String digits;
    /** The number of {@link #digits} that fall after the decimal point. Never negative. */
    private final int scale;
    /** True if this value is strictly negative. Always false when {@link #digits} is {@code "0"}. */
    private final boolean negative;

    private ExactDecimal(String digits, int scale, boolean negative) {
        this.digits = digits;
        this.scale = scale;
        this.negative = negative;
    }

    /**
     * Parses a JSON number literal (optionally signed, optionally with a
     * fractional part and/or an exponent) into an exact decimal value.
     *
     * @param token the literal source text, e.g. {@code "-0.3"}, {@code "1e2"}, {@code "9007199254740993"}
     * @throws IllegalArgumentException if {@code token} is not a valid JSON number literal
     */
    public static ExactDecimal parse(String token) {
        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token must not be null or empty");
        }
        String text = token.trim();
        int index = 0;
        boolean negative = false;
        if (index < text.length() && (text.charAt(index) == '-' || text.charAt(index) == '+')) {
            negative = text.charAt(index) == '-';
            index++;
        }

        int mantissaStart = index;
        int exponentMarker = -1;
        for (int i = index; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 'e' || c == 'E') {
                exponentMarker = i;
                break;
            }
        }
        String mantissa = exponentMarker < 0 ? text.substring(mantissaStart) : text.substring(mantissaStart, exponentMarker);
        int exponent = 0;
        if (exponentMarker >= 0) {
            exponent = parseSignedInt(text.substring(exponentMarker + 1));
        }

        int dot = mantissa.indexOf('.');
        String integerPart;
        String fractionalPart;
        if (dot < 0) {
            integerPart = mantissa;
            fractionalPart = "";
        } else {
            integerPart = mantissa.substring(0, dot);
            fractionalPart = mantissa.substring(dot + 1);
        }
        if (integerPart.length() == 0 && fractionalPart.length() == 0) {
            throw new IllegalArgumentException("Not a valid number literal: " + token);
        }
        if (integerPart.length() == 0) {
            integerPart = "0";
        }
        String digits = integerPart + fractionalPart;
        for (int i = 0; i < digits.length(); i++) {
            char c = digits.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Not a valid number literal: " + token);
            }
        }
        int scale = fractionalPart.length() - exponent;

        if (scale < 0) {
            digits = digits + zeros(-scale);
            scale = 0;
        }
        digits = stripLeadingZeros(digits);
        while (scale > 0 && digits.length() > 1 && digits.charAt(digits.length() - 1) == '0') {
            digits = digits.substring(0, digits.length() - 1);
            scale--;
        }
        if ("0".equals(digits)) {
            negative = false;
            scale = 0;
        }
        return new ExactDecimal(digits, scale, negative);
    }

    private static int parseSignedInt(String text) {
        boolean negative = false;
        int index = 0;
        if (text.length() > 0 && (text.charAt(0) == '-' || text.charAt(0) == '+')) {
            negative = text.charAt(0) == '-';
            index = 1;
        }
        int value = 0;
        for (int i = index; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Not a valid exponent: " + text);
            }
            value = value * 10 + (c - '0');
        }
        return negative ? -value : value;
    }

    /** True if this value is exactly zero. */
    public boolean isZero() {
        return "0".equals(digits);
    }

    /** True if this value is strictly negative. */
    public boolean isNegative() {
        return negative;
    }

    @Override
    public int compareTo(ExactDecimal other) {
        if (this.negative != other.negative) {
            return this.negative ? -1 : 1;
        }
        String[] aligned = align(this, other);
        int magnitude = compareMagnitude(aligned[0], aligned[1]);
        return this.negative ? -magnitude : magnitude;
    }

    /**
     * True if this value is an exact integer multiple of {@code other} -- that
     * is, {@code this / other} is an integer, computed by long division on
     * digit strings rather than floating point.
     *
     * @throws IllegalArgumentException if {@code other} is zero
     */
    public boolean isIntegralMultipleOf(ExactDecimal other) {
        if (other.isZero()) {
            throw new IllegalArgumentException("multipleOf must not be zero");
        }
        String[] aligned = align(this, other);
        String remainder = mod(aligned[0], aligned[1]);
        return "0".equals(remainder);
    }

    /** This value's original sign, significant digits, and scale, as a canonical decimal string (no exponent). */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (negative) {
            result.append('-');
        }
        if (scale == 0) {
            result.append(digits);
            return result.toString();
        }
        String padded = digits.length() > scale ? digits : zeros(scale - digits.length() + 1) + digits;
        int splitAt = padded.length() - scale;
        result.append(padded, 0, splitAt);
        result.append('.');
        result.append(padded, splitAt, padded.length());
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExactDecimal)) {
            return false;
        }
        return this.compareTo((ExactDecimal) other) == 0;
    }

    @Override
    public int hashCode() {
        String canonical = (negative ? "-" : "") + digits + ":" + scale;
        return canonical.hashCode();
    }

    // -- digit-string arithmetic; all inputs/outputs here are unsigned integer digit strings --

    private static String[] align(ExactDecimal a, ExactDecimal b) {
        int maxScale = a.scale > b.scale ? a.scale : b.scale;
        String da = a.digits + zeros(maxScale - a.scale);
        String db = b.digits + zeros(maxScale - b.scale);
        return new String[] { stripLeadingZeros(da), stripLeadingZeros(db) };
    }

    private static int compareMagnitude(String a, String b) {
        if (a.length() != b.length()) {
            return a.length() < b.length() ? -1 : 1;
        }
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.charAt(i) < b.charAt(i) ? -1 : 1;
            }
        }
        return 0;
    }

    private static String mod(String dividend, String divisor) {
        String remainder = "0";
        for (int i = 0; i < dividend.length(); i++) {
            remainder = stripLeadingZeros(remainder + dividend.charAt(i));
            while (compareMagnitude(remainder, divisor) >= 0) {
                remainder = subtract(remainder, divisor);
            }
        }
        return remainder;
    }

    private static String subtract(String a, String b) {
        String paddedB = zeros(a.length() - b.length()) + b;
        char[] result = new char[a.length()];
        int borrow = 0;
        for (int i = a.length() - 1; i >= 0; i--) {
            int digitA = a.charAt(i) - '0';
            int digitB = paddedB.charAt(i) - '0';
            int diff = digitA - digitB - borrow;
            if (diff < 0) {
                diff += 10;
                borrow = 1;
            } else {
                borrow = 0;
            }
            result[i] = (char) ('0' + diff);
        }
        return stripLeadingZeros(new String(result));
    }

    private static String stripLeadingZeros(String digits) {
        int i = 0;
        while (i < digits.length() - 1 && digits.charAt(i) == '0') {
            i++;
        }
        return digits.substring(i);
    }

    private static String zeros(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append('0');
        }
        return result.toString();
    }
}
