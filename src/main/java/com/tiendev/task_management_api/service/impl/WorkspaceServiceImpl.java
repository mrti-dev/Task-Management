package com.tiendev.task_management_api.service.impl;

import com.tiendev.task_management_api.dto.UserResponse;
import com.tiendev.task_management_api.dto.WorkspaceResponse;
import com.tiendev.task_management_api.dto.request.WorkspaceCreateRequest;
import com.tiendev.task_management_api.dto.request.WorkspaceUpdateRequest;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;
import com.tiendev.task_management_api.model.User;
import com.tiendev.task_management_api.model.Workspace;
import com.tiendev.task_management_api.model.WorkspaceMember;
import com.tiendev.task_management_api.model.enums.WorkspaceRole;
import com.tiendev.task_management_api.repository.UserRepository;
import com.tiendev.task_management_api.repository.WorkspaceRepository;
import com.tiendev.task_management_api.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WorkspaceResponse create(WorkspaceCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOwnerId()));

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(workspace);
        ownerMember.setUser(owner);
        ownerMember.setRole(WorkspaceRole.OWNER);
        workspace.getMembers().add(ownerMember);

        workspace = workspaceRepository.save(workspace);
        return toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getById(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        return toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getAll() {
        return workspaceRepository.findAll().stream()
                .map(this::toWorkspaceResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkspaceResponse update(Long id, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));

        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        workspace = workspaceRepository.save(workspace);
        return toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workspace not found with id: " + id);
        }
        workspaceRepository.deleteById(id);
    }

    private WorkspaceResponse toWorkspaceResponse(Workspace workspace) {
        UserResponse ownerResponse = workspace.getMembers().stream()
                .filter(m -> m.getRole() == WorkspaceRole.OWNER)
                .findFirst()
                .map(m -> UserResponse.builder()
                        .id(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .email(m.getUser().getEmail())
                        .avatar(m.getUser().getAvatar())
                        .role(m.getUser().getRole())
                        .build())
                .orElse(null);

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .owner(ownerResponse)
                .memberCount(workspace.getMembers().size())
                .projectCount(workspace.getProjects().size())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
