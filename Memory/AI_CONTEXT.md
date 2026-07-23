# AI Context

## Trạng thái dự án

Dự án Spring Boot REST API đang ở giai đoạn phát triển Phase 1. CRUD cơ bản cho 6 entity đã hoàn thành. Authentication/Authorization chưa triển khai. Dự án đã exclude SecurityAutoConfiguration để chạy tạm.

## Packages

| Package | Chức năng |
|---------|-----------|
| `config` | `SecurityConfig` — Bean `PasswordEncoder` (BCrypt) |
| `controller` | REST endpoints: User, Workspace, WorkspaceMember, Project, Task, Comment |
| `dto` | `UserResponse`, `WorkspaceResponse`, `WorkspaceMemberResponse`, `ProjectResponse`, `TaskResponse`, `CommentResponse` |
| `dto/request` | Create + Update request DTOs cho 6 entity |
| `exception` | `ResourceNotFoundException`, `ResourceAlreadyExistsException` |
| `helper` | `ApiResponse<T>` (success, created, error), `GlobalExceptionHandle` (RestControllerAdvice) |
| `model` | 6 entities: `User`, `Workspace`, `WorkspaceMember`, `Project`, `Task`, `Comment` |
| `model/enums` | `Role`, `WorkspaceRole`, `TaskStatus`, `TaskPriority`, `ProjectStatus` |
| `repository` | 6 JpaRepository interfaces với query methods cần thiết |
| `service` | Interface + Impl cho 6 entity, CRUD đầy đủ |

## Quyết định thiết kế

1. **Không dùng BaseEntity chung** — mỗi entity tự quản lý createdAt/updatedAt qua `@PrePersist`/`@PreUpdate`.
2. **WorkspaceMember thay vì ManyToMany** — linh hoạt hơn, có thể mở rộng role, lưu joinedAt. Dùng `WorkspaceRole` enum riêng (OWNER/MEMBER), tách biệt với `Role` (ADMIN/USER) của hệ thống.
3. **Không dùng MapStruct hay ModelMapper** — mapping thủ công trong service impl để tránh dependency phức tạp.
4. **DTO thay vì Entity trong response** — tránh infinite recursion JSON, kiểm soát dữ liệu trả về.
5. **BCrypt cho password** — dùng `PasswordEncoder` bean, không lưu plain text.
6. **Không dùng @JsonIgnore** — giải quyết vòng lặp bằng DTO, không dùng annotation Jackson trên entity.
7. **Constructor Injection** — field `private final` + `@RequiredArgsConstructor`.

## Luồng dữ liệu

```
Controller → Service (Interface → Impl) → Repository → Entity → Database
                ↑
           PasswordEncoder (encode khi create/update password)
```

## Quy tắc phải tuân thủ

- Không ghi đè file tồn tại.
- Không thêm dependency nếu không được yêu cầu.
- Mọi response dùng `ResponseEntity<ApiResponse<T>>`.
- Kiểm tra unique (username, email) trước khi create/update.
- Mã hóa password BCrypt khi create và update password.
- Không lộ password trong response DTO.
- Xóa file thừa khi thay đổi cấu trúc.

## TODO & FIXME

- [ ] **TODO**: Triển khai JWT Authentication (login/register, token).
- [ ] **TODO**: Triển khai Spring Security filter chain.
- [ ] **TODO**: Phân quyền theo workspace role (OWNER vs MEMBER).
- [ ] **TODO**: Pagination + sorting cho GET all endpoints.
- [ ] **TODO**: Swagger/OpenAPI config.
- [ ] **TODO**: Unit tests.
- [ ] **FIXME**: `GlobalExceptionHandle.handleNotFounf` — tên method sai chính tả (nên là `handleNotFound`).
- [ ] **FIXME**: `SecurityConfig` — method `passwordEncoder()` thiếu access modifier `public`.

## Vấn đề đã giải quyết

1. **WorkspaceMember dùng sai enum**: Đã sửa từ `Role` (ADMIN/USER) → `WorkspaceRole` (OWNER/MEMBER).
2. **WorkspaceCreateRequest.ownerId**: `@NotBlank` trên Long → sửa thành `@NotNull`.
3. **@SpringBootApplication scan sai**: Còn scan package "Controller" cũ đã xóa → sửa thành `@SpringBootApplication` + `exclude`.
4. **Xóa Attachment entity**: Do user không còn nhu cầu.
5. **Xóa avatar field và thêm lại**: Theo yêu cầu của user.
