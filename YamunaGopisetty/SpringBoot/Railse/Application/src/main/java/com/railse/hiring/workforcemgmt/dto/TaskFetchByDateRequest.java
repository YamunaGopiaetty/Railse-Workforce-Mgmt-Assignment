package com.railse.hiring.workforcemgmt.dto;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;

import java.util.List;


@Data
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public class TaskFetchByDateRequest {
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	 private Long startDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	   private Long endDate;
	   private List<Long> assigneeIds;

}
