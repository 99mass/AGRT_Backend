package com.unchk.AGRT_Backend.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private List<String> details;

    public ErrorResponse(String message, List<String> details) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.details = details;
    }
}

