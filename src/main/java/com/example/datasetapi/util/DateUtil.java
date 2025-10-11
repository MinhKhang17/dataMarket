package com.example.datasetapi.util;

import java.time.LocalDate;

public class DateUtil {
    public static LocalDate parseToLocalDate(String localDate) {
        return LocalDate.parse(localDate);
    }
}
