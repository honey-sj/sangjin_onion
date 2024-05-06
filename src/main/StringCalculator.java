package main;

import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringCalculator {

        public int add(String text) {

            if (isBlank(text)) {
                return 0;
            }

            return sum(toInts(split(text)));
        }

        private boolean isBlank(String text) {
            return text == null || text.isEmpty();
        }

        private String[] split(String text) {
            Matcher m = Pattern.compile("//(.)\n(.*)").matcher(text);
            if (m.find()) {
                String customDelimiter = m.group(1);
                return m.group(2).split(customDelimiter);
            }

            return text.split(",|:");
        }

        private int[] toInts(String[] text) {
            int[] numbers = new int[text.length];
            for(int i = 0 ; i < text.length ; i++){
                numbers[i]  = checkMinus(text[i]);
            }
            return numbers;

        }

        private int checkMinus(String text) {
            int number = Integer.parseInt(text);
            if (number < 0) {
                throw new RuntimeException();
            }
            return number;
        }

        private int sum(int[] text) {
            int sumNum = 0;
            for(int i : text){
                sumNum += i;
            }
            return sumNum;
        }
}

