package com.tiendev.task_management_api.service;

import com.tiendev.task_management_api.dto.TaskResponse;
import com.tiendev.task_management_api.dto.request.TaskCreateRequest;
import com.tiendev.task_management_api.dto.request.TaskUpdateRequest;

import java.util.List;

public interface TaskService {
    TaskResponse create(TaskCreateRequest request);
    TaskResponse getById(Long id);
    List<TaskResponse> getAll();
    TaskResponse update(Long id, TaskUpdateRequest request);
    void delete(Long id);
}
