# API Guide

Base URL: `http://localhost:8080/api`

Response format chuẩn:
```json
{
  "status": "success",
  "message": "Call API success.",
  "data": { ... },
  "errorCode": null,
  "timestamp": "2026-07-23T12:00:00"
}
```

---

## User

### POST `/api/users` — Tạo user

**Request body** (`UserCreateRequest`):
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "avatar": "https://example.com/avatar.png",
  "role": "USER"
}
```

Validation:
- `username`: @NotBlank, @Size(min=3, max=50)
- `email`: @NotBlank, @Email
- `password`: @NotBlank, @Size(min=8)

**Response** `201 Created`:
```json
{
  "status": "success",
  "message": "Created successfully",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "avatar": "https://example.com/avatar.png",
    "role": "USER",
    "createdAt": "2026-07-23T12:00:00",
    "updatedAt": "2026-07-23T12:00:00"
  }
}
```

**Lỗi**: `400` — Username/Email already exists.

### GET `/api/users` — Danh sách users

**Response** `200 OK`: `ApiResponse<List<UserResponse>>`

### GET `/api/users/{id}` — User theo ID

**Response** `200 OK`: `ApiResponse<UserResponse>`

**Lỗi**: `400` — User not found.

### PUT `/api/users/{id}` — Cập nhật user

**Request body** (`UserUpdateRequest`):
```json
{
  "username": "newusername",
  "email": "new@example.com",
  "password": "newpassword123",
  "avatar": "https://example.com/new-avatar.png",
  "role": "ADMIN"
}
```

Tất cả field đều optional (nullable).

**Response** `200 OK`: `ApiResponse<UserResponse>`

**Lỗi**: `400` — Username/Email already exists (nếu thay đổi và bị trùng).

### DELETE `/api/users/{id}` — Xóa user

**Response** `200 OK`: `ApiResponse` (data null, message "User deleted successfully")

---

## Workspace

### POST `/api/workspaces` — Tạo workspace

**Request body** (`WorkspaceCreateRequest`):
```json
{
  "name": "My Workspace",
  "description": "Description",
  "ownerId": 1
}
```

Tự động thêm user (ownerId) vào danh sách member với role OWNER.

**Response** `201 Created`: `ApiResponse<WorkspaceResponse>` (gồm owner, memberCount, projectCount)

### GET `/api/workspaces` — Danh sách workspaces

### GET `/api/workspaces/{id}` — Workspace theo ID

### PUT `/api/workspaces/{id}` — Cập nhật

### DELETE `/api/workspaces/{id}` — Xóa

---

## WorkspaceMember

### POST `/api/workspace-members` — Thêm member

**Request body** (`WorkspaceMemberCreateRequest`):
```json
{
  "workspaceId": 1,
  "userId": 2,
  "role": "MEMBER"
}
```

Validation:
- `workspaceId`: @NotNull
- `userId`: @NotNull
- `role`: @NotNull

**Lỗi**: `400` — User is already a member of this workspace.

### GET `/api/workspace-members` — Danh sách

### GET `/api/workspace-members/{id}` — Theo ID

### PUT `/api/workspace-members/{id}` — Cập nhật role

**Request body** (`WorkspaceMemberUpdateRequest`):
```json
{
  "role": "OWNER"
}
```

### DELETE `/api/workspace-members/{id}` — Xóa member

---

## Project

### POST `/api/projects` — Tạo project

**Request body** (`ProjectCreateRequest`):
```json
{
  "workspaceId": 1,
  "name": "Project Alpha",
  "description": "Description",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31"
}
```

Status mặc định: `PLANNING`.

### GET `/api/projects` — Danh sách

### GET `/api/projects/{id}` — Theo ID

### PUT / DELETE — CRUD cơ bản

---

## Task

### POST `/api/tasks` — Tạo task

**Request body** (`TaskCreateRequest`):
```json
{
  "projectId": 1,
  "title": "Implement login",
  "description": "Description",
  "priority": "HIGH",
  "status": "TODO",
  "assigneeId": 1,
  "deadline": "2026-08-01T12:00:00"
}
```

- `assigneeId` có thể null (Unassigned).
- Status mặc định: `TODO`.

### GET `/api/tasks` — Danh sách

### GET `/api/tasks/{id}` — Theo ID (kèm assignee, commentCount)

### PUT / DELETE — CRUD cơ bản

---

## Comment

### POST `/api/comments` — Tạo comment

**Request body** (`CommentCreateRequest`):
```json
{
  "taskId": 1,
  "userId": 1,
  "content": "This is a comment"
}
```

Validation:
- `content`: @NotBlank, @Size(max=1000)

### GET `/api/comments` — Danh sách

### GET / PUT / DELETE — CRUD cơ bản

---

## Error codes

| HTTP Status | Điều kiện |
|-------------|-----------|
| 200 | Thành công |
| 201 | Tạo mới thành công |
| 400 | Validation lỗi / Resource đã tồn tại / Resource không tìm thấy / Lỗi không xác định |
| 400 + VALIDATION_ERROR | `MethodArgumentNotValidException` — danh sách field lỗi |
| 500 | Internal server error (hiện tại cũng trả về 400 do xử lý chung) |

## Authentication

Chưa triển khai. Tất cả endpoints đều public.
