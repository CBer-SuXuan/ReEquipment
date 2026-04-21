package org.reuac.reequipment.model;

public class NumberFormat {
    private static final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] romanLiterals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String toRoman(int number) {
        if (number <= 0 || number > 3999) { // 罗马数字通常不表示 0 或负数，以及大于 3999 的数
            return String.valueOf(number); // 返回原始数字作为 fallback
        }

        StringBuilder roman = new StringBuilder();
        int i = 0;
        while (number > 0) {
            if (number >= values[i]) {
                roman.append(romanLiterals[i]);
                number -= values[i];
            } else {
                i++;
            }
        }
        return roman.toString();
    }
}