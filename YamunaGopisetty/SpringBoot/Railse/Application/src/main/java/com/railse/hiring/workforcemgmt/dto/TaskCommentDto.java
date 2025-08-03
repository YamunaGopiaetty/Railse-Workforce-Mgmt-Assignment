package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskCommentDto {
	
	private Long id;
    private Long taskId;
    private String commenter; 
    private String comment;
    private Long timestamp;
}
