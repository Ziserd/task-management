package com.denizer.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCommentRequestDto {

    @NotBlank
    @Size(max = 1000)
    private String content;
}