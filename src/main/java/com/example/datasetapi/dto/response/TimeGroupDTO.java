package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class TimeGroupDTO {
    private long timeGroupId;
    private int day;
    private int month;
    private int year;
}
