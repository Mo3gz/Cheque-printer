package org.chequePrinter.util;

public class ArabicNumberToWords {

    private static final String[] ones = {
            "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
            "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    };

    private static final String[] tens = {
            "", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
    };

    private static final String[] hundreds = {
            "", "مائة", "مئتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    };

    public static String convert(double number) {
        if (number == 0) {
            return "صفر";
        }

        long longPart = (long) number;
        String result = convert(longPart);

        long decimalPart = Math.round((number - longPart) * 100);
        if (decimalPart > 0) {
            // For decimal part, we generally use 'قرش' (piaster) or 'هللة' (halala)
            // The current implementation uses 'قرش' which is common for currency.
            // No complex gender/plural rules applied here for simplicity, as it's a fractional part.
            result += " و " + convert(decimalPart) + " قرش";
        }

        return result;
    }

    public static String convert(long number) {
        if (number == 0) {
            return "صفر";
        }

        String result = "";

        if (number < 20) {
            result = ones[(int) number];
        } else if (number < 100) {
            result = tens[(int) (number / 10)];
            if (number % 10 != 0) {
                result = ones[(int) (number % 10)] + " و " + result;
            }
        } else if (number < 1000) {
            result = hundreds[(int) (number / 100)];
            if (number % 100 != 0) {
                result += " و " + convert(number % 100);
            }
        } else if (number < 1_000_000) {
            long thousands = number / 1000;
            String thousandWord;
            if (thousands == 1) {
                thousandWord = "ألف";
            } else if (thousands == 2) {
                thousandWord = "ألفان";
            } else if (thousands >= 3 && thousands <= 10) {
                thousandWord = convert(thousands) + " آلاف"; // e.g., ثلاثة آلاف
            } else {
                thousandWord = convert(thousands) + " ألف"; // e.g., عشرون ألف, مائة ألف
            }
            result = thousandWord;
            if (number % 1000 != 0) {
                result += " و " + convert(number % 1000);
            }
        } else if (number < 1_000_000_000) {
            long millions = number / 1_000_000;
            String millionWord;
            if (millions == 1) {
                millionWord = "مليون";
            } else if (millions == 2) {
                millionWord = "مليونان";
            } else if (millions >= 3 && millions <= 10) {
                millionWord = convert(millions) + " ملايين";
            } else {
                millionWord = convert(millions) + " مليون";
            }
            result = millionWord;
            if (number % 1_000_000 != 0) {
                result += " و " + convert(number % 1_000_000);
            }
        } else if (number < 1_000_000_000_000L) { // Using L for long literal
            long billions = number / 1_000_000_000L;
            String billionWord;
            if (billions == 1) {
                billionWord = "مليار";
            } else if (billions == 2) {
                billionWord = "ملياران";
            } else if (billions >= 3 && billions <= 10) {
                billionWord = convert(billions) + " مليارات";
            } else {
                billionWord = convert(billions) + " مليار";
            }
            result = billionWord;
            if (number % 1_000_000_000L != 0) {
                result += " و " + convert(number % 1_000_000_000L);
            }
        } else {
            // For numbers larger than billions, we can extend this pattern
            // For simplicity, we'll just return a generic message or handle up to trillions if needed.
            // This implementation stops at billions for now.
            return "عدد كبير جدا";
        }

        return result.trim();
    }
}

