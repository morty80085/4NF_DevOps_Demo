package com.notfound.devopsdemo.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title max length is 120")
        String title
) {
}

