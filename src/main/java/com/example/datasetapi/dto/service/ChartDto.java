package com.example.datasetapi.dto.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartDto {

    private String title;
    private String type;
    private String x;
    private String y;
    private String insight;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getX() { return x; }
    public void setX(String x) { this.x = x; }

    public String getY() { return y; }
    public void setY(String y) { this.y = y; }

    public String getInsight() { return insight; }
    public void setInsight(String insight) { this.insight = insight; }
}
