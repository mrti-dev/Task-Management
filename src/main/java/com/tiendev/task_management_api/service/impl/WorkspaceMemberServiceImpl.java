package com.tiendev.task_management_api.service.impl;

import com.tiendev.task_management_api.dto.UserResponse;
import com.tiendev.task_management_api.dto.WorkspaceMemberResponse;
import com.tiendev.task_management_api.dto.request.WorkspaceMemberCreateRequest;
import com.tiendev.task_management_api.dto.request.WorkspaceMemberUpdateRequest;
import com.tiendev.task_management_api.exception.ResourceAlreadyExistsException;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;
import com.tiendev.task_management_api.model.User;
import com.tiendev.task_management_api.model.Workspace;
import com.tiendev.task_management_api.model.WorkspaceMember;
import com.tiendev.task_management_api.repository.UserRepository;
import com.tiendev.task_management_api.repository.WorkspaceMemberRepository;
import com.tiendev.task_management_api.repository.WorkspaceRepository;
import com.tiendev.task_management_api.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WorkspaceMemberResponse create(WorkspaceMemberCreateRequest request) {
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(request.getWorkspaceId(), request.getUserId())) {
            throw new ResourceAlreadyExistsException("User is already a member of this workspace");
        }

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + request.getWorkspaceId()));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(request.getRole());

        member = workspaceMemberRepository.save(member);
        return toWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceMemberResponse getById(Long id) {
        WorkspaceMember member = workspaceMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkspaceMember not found with id: " + id));
        return toWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getAll() {
        return workspaceMemberRepository.findAll().stream()
                .map(this::toWorkspaceMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse update(Long id, WorkspaceMemberUpdateRequest request) {
        WorkspaceMember member = workspaceMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkspaceMember not found with id: " + id));

        member.setRole(request.getRole());
        member = workspaceMemberRepository.save(member);
        return toWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!workspaceMemberRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkspaceMember not found with id: " + id);
        }
        workspaceMemberRepository.deleteById(id);
    }

    private WorkspaceMemberResponse toWorkspaceMemberResponse(WorkspaceMember member) {
        UserResponse userResponse = UserResponse.builder()
                .id(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .avatar(member.getUser().getAvatar())
                .role(member.getUser().getRole())
                .build();

        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .user(userResponse)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
