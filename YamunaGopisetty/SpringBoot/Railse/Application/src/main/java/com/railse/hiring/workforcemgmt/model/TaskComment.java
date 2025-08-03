package com.railse.hiring.workforcemgmt.model;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class TaskComment {
	
	private Long id;
    private Long taskId;
    private String commenter; 
    private String comment;
    private Long timestamp;
}
