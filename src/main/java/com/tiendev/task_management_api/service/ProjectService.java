package com.tiendev.task_management_api.service;

import com.tiendev.task_management_api.dto.ProjectResponse;
import com.tiendev.task_management_api.dto.request.ProjectCreateRequest;
import com.tiendev.task_management_api.dto.request.ProjectUpdateRequest;

import java.util.List;

public interface ProjectService {
    ProjectResponse create(ProjectCreateRequest request);
    ProjectResponse getById(Long id);
    List<ProjectResponse> getAll();
    ProjectResponse update(Long id, ProjectUpdateRequest request);
    void delete(Long id);
}
