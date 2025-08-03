package com.railse.hiring.workforcemgmt.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.railse.hiring.workforcemgmt.dto.TaskActivityDto;
import com.railse.hiring.workforcemgmt.dto.TaskCommentDto;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;

public class TaskMapperImpl implements ITaskManagementMapper {

	@Override
	public TaskManagementDto modelToDto(TaskManagement model) {
		TaskManagementDto dto = new TaskManagementDto();
	    dto.setId(model.getId());
	    dto.setDescription(model.getDescription());
	    dto.setStatus(model.getStatus());
	    dto.setPriority(model.getPriority());  // <-- Add this line
	    // other mappings...
	    return dto;
	}

	@Override
	public TaskManagement dtoToModel(TaskManagementDto dto) {
		TaskManagement task = new TaskManagement();
	    task.setId(dto.getId());
	    task.setDescription(dto.getDescription());
	    task.setStatus(dto.getStatus());
	    task.setPriority(dto.getPriority());  // <-- And this one too
	    // other mappings...
	    return task;
	}

	@Override
	public List<TaskManagementDto> modelListToDtoList(List<TaskManagement> models) {
		// TODO Auto-generated method stub
		return models.stream()
                .map(this::modelToDto)
                .collect(Collectors.toList());
	}

	public TaskCommentDto toDto(TaskComment comment) {
        TaskCommentDto dto = new TaskCommentDto();
        dto.setComment(comment.getComment());
        dto.setCommenter(comment.getCommenter());
        dto.setTimestamp(comment.getTimestamp());
        return dto;
    }

    public TaskActivityDto toDto(TaskActivity activity) {
        TaskActivityDto dto = new TaskActivityDto();
        dto.setActor(activity.getActor());
        dto.setAction(activity.getAction());
        dto.setTimestamp(activity.getTimestamp());
        return dto;
    }
}
