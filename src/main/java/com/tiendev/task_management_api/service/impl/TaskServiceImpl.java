package com.tiendev.task_management_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendev.task_management_api.dto.TaskResponse;
import com.tiendev.task_management_api.dto.UserResponse;
import com.tiendev.task_management_api.dto.request.TaskCreateRequest;
import com.tiendev.task_management_api.dto.request.TaskUpdateRequest;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;
import com.tiendev.task_management_api.model.Project;
import com.tiendev.task_management_api.model.Task;
import com.tiendev.task_management_api.model.User;
import com.tiendev.task_management_api.model.enums.TaskStatus;
import com.tiendev.task_management_api.repository.ProjectRepository;
import com.tiendev.task_management_api.repository.TaskRepository;
import com.tiendev.task_management_api.repository.UserRepository;
import com.tiendev.task_management_api.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public TaskResponse create(TaskCreateRequest request) {
		Project project = projectRepository.findById(request.getProjectId()).orElseThrow(
				() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

		User assignee = null;
		if (request.getAssigneeId() != null) {
			assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(
					() -> new ResourceNotFoundException("User not found with id: " + request.getAssigneeId()));
		}

		Task task = new Task();
		task.setProject(project);
		task.setTitle(request.getTitle());
		task.setDescription(request.getDescription());
		task.setPriority(request.getPriority());
		task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
		task.setAssignee(assignee);
		task.setDeadline(request.getDeadline());

		task = taskRepository.save(task);
		return toTaskResponse(task);
	}

	@Override
	@Transactional(readOnly = true)
	public TaskResponse getById(Long id) {
		Task task = taskRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
		return toTaskResponse(task);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TaskResponse> getAll() {
		return taskRepository.findAll().stream().map(this::toTaskResponse).toList();
	}

	@Override
	@Transactional
	public TaskResponse update(Long id, TaskUpdateRequest request) {
		Task task = taskRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

		if (request.getTitle() != null) {
			task.setTitle(request.getTitle());
		}
		if (request.getDescription() != null) {
			task.setDescription(request.getDescription());
		}
		if (request.getPriority() != null) {
			task.setPriority(request.getPriority());
		}
		if (request.getStatus() != null) {
			task.setStatus(request.getStatus());
		}
		if (request.getAssigneeId() != null) {
			User assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(
					() -> new ResourceNotFoundException("User not found with id: " + request.getAssigneeId()));
			task.setAssignee(assignee);
		}
		if (request.getDeadline() != null) {
			task.setDeadline(request.getDeadline());
		}

		task = taskRepository.save(task);
		return toTaskResponse(task);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		if (!taskRepository.existsById(id)) {
			throw new ResourceNotFoundException("Task not found with id: " + id);
		}
		taskRepository.deleteById(id);
	}

	private TaskResponse toTaskResponse(Task task) {
		UserResponse assigneeResponse = null;
		if (task.getAssignee() != null) {
			assigneeResponse = UserResponse.builder().id(task.getAssignee().getId())
					.username(task.getAssignee().getUsername()).email(task.getAssignee().getEmail())
					.avatar(task.getAssignee().getAvatar())
					.role(task.getAssignee().getRole()).build();
		}

		return TaskResponse.builder().id(task.getId()).projectId(task.getProject().getId())
				.projectName(task.getProject().getName()).title(task.getTitle()).description(task.getDescription())
				.priority(task.getPriority()).status(task.getStatus()).assignee(assigneeResponse)
				.deadline(task.getDeadline()).commentCount(task.getComments().size()).createdAt(task.getCreatedAt())
				.updatedAt(task.getUpdatedAt()).build();
	}
}
