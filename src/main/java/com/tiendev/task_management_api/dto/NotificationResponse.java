package com.tiendev.task_management_api.dto;

import com.tiendev.task_management_api.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private boolean isRead;
    private UserResponse receiver;
    private UserResponse sender;
    private Long taskId;
    private Long projectId;
    private Long workspaceId;
    private String link;
    private LocalDateTime createdAt;
}
