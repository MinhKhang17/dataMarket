package com.example.datasetapi.dto.response;

import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API Response Wrapper")
public class ApiResponse {

    private boolean success;
    private String message;

    @Schema(description = "Actual data object",
            anyOf = {WithdrawResponse.class, ProcessWithdrawRequest.class})
    private Object data;
}

