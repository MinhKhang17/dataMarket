package com.example.datasetapi.dto.response;

import com.example.datasetapi.dto.service.AlertDto;
import com.example.datasetapi.dto.service.ChartDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsSummaryDto {
    private List<ChartDto> charts;
    private List<AlertDto> alerts;
    private String notes;

    public List<ChartDto> getCharts() { return charts; }
    public void setCharts(List<ChartDto> charts) { this.charts = charts; }

    public List<AlertDto> getAlerts() { return alerts; }
    public void setAlerts(List<AlertDto> alerts) { this.alerts = alerts; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
