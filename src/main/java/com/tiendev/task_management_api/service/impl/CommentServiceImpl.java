package com.tiendev.task_management_api.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendev.task_management_api.dto.CommentResponse;
import com.tiendev.task_management_api.dto.UserResponse;
import com.tiendev.task_management_api.dto.request.CommentCreateRequest;
import com.tiendev.task_management_api.dto.request.CommentUpdateRequest;
import com.tiendev.task_management_api.exception.InvalidOperationException;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;
import com.tiendev.task_management_api.model.Comment;
import com.tiendev.task_management_api.model.Task;
import com.tiendev.task_management_api.model.User;
import com.tiendev.task_management_api.repository.CommentRepository;
import com.tiendev.task_management_api.repository.TaskRepository;
import com.tiendev.task_management_api.repository.UserRepository;
import com.tiendev.task_management_api.repository.WorkspaceMemberRepository;
import com.tiendev.task_management_api.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	@Override
	@Transactional
	public CommentResponse create(CommentCreateRequest request) {
		Task task = taskRepository.findById(request.getTaskId())
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
		validateParentChain(task);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Long currentUserId = Long.valueOf(auth.getName());
		User user = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

		Long workspaceId = task.getProject().getWorkspace().getId();
		if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
			throw new InvalidOperationException("User is not a member of this workspace.");
		}

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
		validateParentChain(comment.getTask());
		validateMembership(comment.getTask().getProject().getWorkspace().getId());
		return toCommentResponse(comment);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommentResponse> getAll() {
		Set<Long> memberWorkspaceIds = getMemberWorkspaceIds();
		return commentRepository.findAll().stream()
				.filter(c -> c.getTask().isDeleted()
						&& c.getTask().getProject().isDeleted()
						&& c.getTask().getProject().getWorkspace().isDeleted()
						&& memberWorkspaceIds.contains(c.getTask().getProject().getWorkspace().getId()))
				.map(this::toCommentResponse)
				.toList();
	}

	@Override
	@Transactional
	public CommentResponse update(Long id, CommentUpdateRequest request) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
		validateParentChain(comment.getTask());
		validateCommentAuthor(comment);

		comment.setContent(request.getContent());
		comment = commentRepository.save(comment);
		return toCommentResponse(comment);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
		validateParentChain(comment.getTask());
		validateCommentAuthor(comment);
		commentRepository.delete(comment);
	}

	private void validateCommentAuthor(Comment comment) {
		Long currentUserId = getCurrentUserId();
		if (!comment.getUser().getId().equals(currentUserId)) {
			throw new InvalidOperationException("You can only edit or delete your own comments.");
		}
	}

	private Long getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return Long.valueOf(auth.getName());
	}

	private Set<Long> getMemberWorkspaceIds() {
		Long currentUserId = getCurrentUserId();
		return workspaceMemberRepository.findByUserId(currentUserId).stream()
				.map(m -> m.getWorkspace().getId())
				.collect(Collectors.toSet());
	}

	private void validateMembership(Long workspaceId) {
		Long currentUserId = getCurrentUserId();
		if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, currentUserId)) {
			throw new ResourceNotFoundException("Comment not found");
		}
	}

	private void validateParentChain(Task task) {
		if (!task.isDeleted() || !task.getProject().isDeleted() || !task.getProject().getWorkspace().isDeleted()) {
			throw new InvalidOperationException("Cannot perform operation: the associated task or its parent entities are deleted.");
		}
	}

	private CommentResponse toCommentResponse(Comment comment) {
		UserResponse userResponse = UserResponse.builder().id(comment.getUser().getId())
				.username(comment.getUser().getUsername()).email(comment.getUser().getEmail())
				.avatar(comment.getUser().getAvatar()).role(comment.getUser().getRole()).build();

		return CommentResponse.builder().id(comment.getId()).taskId(comment.getTask().getId()).user(userResponse)
				.content(comment.getContent()).createdAt(comment.getCreatedAt()).build();
	}
}
