package com.tiendev.task_management_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendev.task_management_api.dto.CommentResponse;
import com.tiendev.task_management_api.dto.UserResponse;
import com.tiendev.task_management_api.dto.request.CommentCreateRequest;
import com.tiendev.task_management_api.dto.request.CommentUpdateRequest;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;
import com.tiendev.task_management_api.model.Comment;
import com.tiendev.task_management_api.model.Task;
import com.tiendev.task_management_api.model.User;
import com.tiendev.task_management_api.repository.CommentRepository;
import com.tiendev.task_management_api.repository.TaskRepository;
import com.tiendev.task_management_api.repository.UserRepository;
import com.tiendev.task_management_api.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public CommentResponse create(CommentCreateRequest request) {
		Task task = taskRepository.findById(request.getTaskId())
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

		Comment comment = new Comment();
		comment.setTask(task);
		comment.setUser(user);
		comment.setContent(request.getContent());

		comment = commentRepository.save(comment);
		return toCommentResponse(comment);
	}

	@Override
	@Transactional(readOnly = true)
	public CommentResponse getById(Long id) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
		return toCommentResponse(comment);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommentResponse> getAll() {
		return commentRepository.findAll().stream().map(this::toCommentResponse).toList();
	}

	@Override
	@Transactional
	public CommentResponse update(Long id, CommentUpdateRequest request) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

		comment.setContent(request.getContent());
		comment = commentRepository.save(comment);
		return toCommentResponse(comment);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		if (!commentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Comment not found with id: " + id);
		}
		commentRepository.deleteById(id);
	}

	private CommentResponse toCommentResponse(Comment comment) {
		UserResponse userResponse = UserResponse.builder().id(comment.getUser().getId())
				.username(comment.getUser().getUsername()).email(comment.getUser().getEmail())
				.avatar(comment.getUser().getAvatar())
				.role(comment.getUser().getRole()).build();

		return CommentResponse.builder().id(comment.getId()).taskId(comment.getTask().getId()).user(userResponse)
				.content(comment.getContent()).createdAt(comment.getCreatedAt()).build();
	}
}
