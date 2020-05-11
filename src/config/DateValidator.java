package config;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateValidator {

    public static boolean validateDate(String input) {

        boolean validator = false;


        String date_regex = "^(3[01]|[12][0-9]|0[1-9])-(1[0-2]|0[1-9])-[0-9]{4}$";

        Pattern pattern = Pattern.compile(date_regex);
        Matcher matcher = pattern.matcher(input);

        validator = matcher.matches();

        return validator;
    }

    public static boolean timeValidator(String input){
        boolean validator = false;

        String date_regex = "^(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)$";

        Pattern pattern = Pattern.compile(date_regex);
        Matcher matcher = pattern.matcher(input);

        validator = matcher.matches();

        return validator;
    }
}
