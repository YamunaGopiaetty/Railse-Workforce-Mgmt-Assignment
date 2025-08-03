package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskActivityDto {
	
	private Long id;
    private Long taskId;
    private String action;     
    private String actor;      
    private Long timestamp; 
}
