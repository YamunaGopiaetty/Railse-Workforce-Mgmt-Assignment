package com.railse.hiring.workforcemgmt.service.impl;


import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskManagementServiceImpl implements TaskManagementService{
		
	   private final TaskRepository taskRepository;
	   private final ITaskManagementMapper taskMapper;

	   public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
	       this.taskRepository = taskRepository;
	       this.taskMapper = taskMapper;
	   }


	   @Override
	   public TaskManagementDto findTaskById(Long id) {
	       TaskManagement task = taskRepository.findById(id)
	               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
	       return taskMapper.modelToDto(task);
	   }


	   @Override
	   public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
	       List<TaskManagement> createdTasks = new ArrayList<>();
	       for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
	           TaskManagement newTask = new TaskManagement();
	           newTask.setReferenceId(item.getReferenceId());
	           newTask.setReferenceType(item.getReferenceType());
	           newTask.setTask(item.getTask());
	           newTask.setAssigneeId(item.getAssigneeId());
	           newTask.setPriority(item.getPriority());
	           newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
	           newTask.setStatus(TaskStatus.ASSIGNED);
	           newTask.setDescription("New task created.");
	           createdTasks.add(taskRepository.save(newTask));
	       }
	       return taskMapper.modelListToDtoList(createdTasks);
	   }


	   @Override
	   public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
	       List<TaskManagement> updatedTasks = new ArrayList<>();
	       for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
	           TaskManagement task = taskRepository.findById(item.getTaskId())
	                   .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


	           if (item.getTaskStatus() != null) {
	               task.setStatus(item.getTaskStatus());
	           }
	           if (item.getDescription() != null) {
	               task.setDescription(item.getDescription());
	           }
	           if (item.getPriority() != null) {
	               task.setPriority(item.getPriority());
	           }
	           updatedTasks.add(taskRepository.save(task));
	       }
	       return taskMapper.modelListToDtoList(updatedTasks);
	   }


	   @Override
	   public String assignByReference(AssignByReferenceRequest request) {
	       List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
	       List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());


	       for (Task taskType : applicableTasks) {
	           List<TaskManagement> tasksOfType = existingTasks.stream()
	                   .filter(t -> t.getTask() == taskType )//&& t.getStatus() != TaskStatus.COMPLETED)
	                   .collect(Collectors.toList());
	           
	           boolean reassigned = false;
	           
	          // Bug Fix 1: Task Re-assignment Creates Duplicates
	           
	           for (TaskManagement existingTask : tasksOfType) {
	               if (existingTask.getStatus() == TaskStatus.COMPLETED) {
	                   continue; 
	               }

	               if (!reassigned) {
	                   existingTask.setAssigneeId(request.getAssigneeId());
	                   existingTask.setStatus(TaskStatus.ASSIGNED);
	                   taskRepository.save(existingTask);
	                   reassigned = true;
	               } else {
	                   
	                   existingTask.setStatus(TaskStatus.CANCELLED);
	                   taskRepository.save(existingTask);
	               }
	           }

	           if (!reassigned) {
	              
	               TaskManagement newTask = new TaskManagement();
	               newTask.setReferenceId(request.getReferenceId());
	               newTask.setReferenceType(request.getReferenceType());
	               newTask.setTask(taskType);
	               newTask.setAssigneeId(request.getAssigneeId());
	               newTask.setStatus(TaskStatus.ASSIGNED);
	               taskRepository.save(newTask);
	           }

	           
	       }
	       return "Tasks assigned successfully for reference " + request.getReferenceId();
	   }


	   @Override
	   public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
	       List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());
	       
	       
	       LocalDate startDate = Instant.ofEpochMilli(request.getStartDate())
	               .atZone(ZoneId.systemDefault())
	               .toLocalDate();

	       LocalDate endDate = Instant.ofEpochMilli(request.getEndDate())
	               .atZone(ZoneId.systemDefault())
	               .toLocalDate();

	       //  Bug Fix 2: Cancelled Tasks Clutter the View
	       
	       List<TaskManagement> filteredTasks = tasks.stream()
	               .filter(task -> {
	            	   
	            	   if (task.getStatus() == TaskStatus.CANCELLED) {
	                       return false;
	                   }
	            	   if (task.getTaskDeadlineTime() == null) return false;

	                   LocalDate taskDate = Instant.ofEpochMilli(task.getTaskDeadlineTime())
	                           .atZone(ZoneId.systemDefault())
	                           .toLocalDate();
	                   
	                   //Feature 1: Smart Daily Task View
	                   boolean inDateRange = (!taskDate.isBefore(startDate) && !taskDate.isAfter(endDate));

	                   // Case 2: Task started before startDate but is still active
	                   boolean startedBeforeAndActive = taskDate.isBefore(startDate)
	                           && !(task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED);

	                   return inDateRange || startedBeforeAndActive;
	                   
	               })
	               .collect(Collectors.toList());


	       return taskMapper.modelListToDtoList(filteredTasks);
	   }

	   //Feature 2: Task Priority Management
	   @Override
	   public List<TaskManagementDto> fetchTasksByPriority(Priority priority) {
		   List<TaskManagement> tasks = taskRepository.findAll(); // or use in-memory list
		    List<TaskManagement> filtered = tasks.stream()
		            .filter(task -> priority.equals(task.getPriority()))
		            .collect(Collectors.toList());

		    return taskMapper.modelListToDtoList(filtered);
	   }
	   
	   @Override
	   public TaskManagementDto updateTaskPriority(UpdateTaskPriorityRequest request) {
		// TODO Auto-generated method stub
		   TaskManagement task = taskRepository.findById(request.getTaskId())
			        .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + request.getTaskId()));

			    task.setPriority(request.getNewPriority());

			    // Log this in activity history
			    TaskActivity activity = new TaskActivity();
			    activity.setTaskId(task.getId());
			    activity.setActor(request.getUpdatedBy());
			    activity.setAction("Changed priority to " + request.getNewPriority().name());
			    activity.setTimestamp(System.currentTimeMillis());

			    task.getActivityHistory().add(activity);

			    taskRepository.save(task);
			    return taskMapper.modelToDto(task);
			
	   }
	   
	//Feature 3: Task Comments & Activity History
	   public TaskManagementDto addCommentToTask(Long taskId, String commenter, String commentText) {
		    TaskManagement task = taskRepository.findById(taskId)
		            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
		    
		    TaskComment comment = new TaskComment();
		    comment.setTaskId(taskId);
		    comment.setCommenter(commenter);
		    comment.setComment(commentText);
		    comment.setTimestamp(System.currentTimeMillis());

		    task.getComments().add(comment);

		    
		    TaskActivity activity = new TaskActivity();
		    activity.setTaskId(taskId);
		    activity.setActor(commenter);
		    activity.setAction("Commented on the task");
		    activity.setTimestamp(System.currentTimeMillis());
		    task.getActivityHistory().add(activity);

		    taskRepository.save(task);
		    return taskMapper.modelToDto(task);
		}


	   
	   
}
