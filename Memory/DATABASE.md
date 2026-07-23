# Database

## Công nghệ

- **MySQL 8** qua Docker (hoặc local).
- **Hibernate 6** (Spring Data JPA) — `ddl-auto=update`.
- Database name: `taskmanagement`.

## Entity diagrams

### Quan hệ

```
User (1) ────< (N) WorkspaceMember (N) >──── (1) Workspace
                                                      │ 1
                                                      │
                                                      N
                                                   Project
                                                      │ 1
                                                      │
                                                      N
                                                     Task ────< (N) Comment
                                                      │ N
                                                      │
                                                   User (assignee)
```

### Summary

| Bảng | PK | FK |
|------|----|-----|
| `users` | id | — |
| `workspaces` | id | — |
| `workspace_members` | id | workspace_id, user_id |
| `projects` | id | workspace_id |
| `tasks` | id | project_id, assignee_id |
| `comments` | id | task_id, user_id |

## Bảng: `users`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| avatar | VARCHAR(500) | nullable |
| role | ENUM(ADMIN, USER) | NOT NULL |
| created_at | DATETIME | NOT NULL, updatable=false |
| updated_at | DATETIME | nullable |

## Bảng: `workspaces`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| name | VARCHAR(100) | NOT NULL |
| description | VARCHAR(500) | nullable |
| created_at | DATETIME | NOT NULL, updatable=false |
| updated_at | DATETIME | nullable |

## Bảng: `workspace_members`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| workspace_id | BIGINT | NOT NULL, FK → workspaces.id, UNIQUE(workspace_id, user_id) |
| user_id | BIGINT | NOT NULL, FK → users.id, UNIQUE(workspace_id, user_id) |
| role | ENUM(OWNER, MEMBER) | NOT NULL |
| joined_at | DATETIME | NOT NULL, updatable=false |

**Unique constraint**: `(workspace_id, user_id)` — một user chỉ có một membership trong một workspace.

## Bảng: `projects`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| workspace_id | BIGINT | NOT NULL, FK → workspaces.id |
| name | VARCHAR(100) | NOT NULL |
| description | VARCHAR(1000) | nullable |
| status | ENUM(PLANNING, ACTIVE, COMPLETED, ARCHIVED) | NOT NULL |
| start_date | DATE | nullable |
| end_date | DATE | nullable |
| created_at | DATETIME | NOT NULL, updatable=false |
| updated_at | DATETIME | nullable |

## Bảng: `tasks`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| project_id | BIGINT | NOT NULL, FK → projects.id |
| title | VARCHAR(200) | NOT NULL |
| description | VARCHAR(1000) | nullable |
| priority | ENUM(LOW, MEDIUM, HIGH, URGENT) | NOT NULL |
| status | ENUM(TODO, IN_PROGRESS, REVIEW, DONE, CANCELLED) | NOT NULL |
| assignee_id | BIGINT | FK → users.id, **nullable** (Unassigned) |
| deadline | DATETIME | nullable |
| created_at | DATETIME | NOT NULL, updatable=false |
| updated_at | DATETIME | nullable |

## Bảng: `comments`

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT (auto_increment) | PK |
| task_id | BIGINT | NOT NULL, FK → tasks.id |
| user_id | BIGINT | NOT NULL, FK → users.id |
| content | VARCHAR(1000) | NOT NULL |
| created_at | DATETIME | NOT NULL, updatable=false |

## Quy tắc đặt tên

- Bảng: số nhiều, snake_case (`users`, `workspace_members`, `tasks`).
- Cột: snake_case (`created_at`, `workspace_id`, `file_name`).
- Enum lưu dạng STRING (tên enum) trong database.

## Chuẩn BCNF

Tất cả 6 bảng đều đạt chuẩn BCNF:
- Không có thuộc tính lặp.
- Không có phụ thuộc bắc cầu.
- Mọi thuộc tính không khóa phụ thuộc hoàn toàn vào khóa chính (id).
- Không lưu dữ liệu suy diễn (derived data).
