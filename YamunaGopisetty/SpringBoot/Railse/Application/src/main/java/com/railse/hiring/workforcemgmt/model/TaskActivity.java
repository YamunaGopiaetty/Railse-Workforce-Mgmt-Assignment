package com.railse.hiring.workforcemgmt.model;

import lombok.Data;



@Data
public class TaskActivity {
	
	private Long id;
    private Long taskId;
    private String action;     
    private String actor;     
    private Long timestamp; 
}
