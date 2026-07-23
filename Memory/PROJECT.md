# Task Management API

## Tổng quan

API quản lý công việc (Task Management) cho phép người dùng tạo workspace, quản lý dự án, phân công công việc và trao đổi qua comments. Hỗ trợ phân quyền theo workspace (OWNER/MEMBER).

## Mục tiêu

- Xây dựng RESTful API cho hệ thống quản lý công việc.
- Quản lý workspace, project, task, comment.
- Phân quyền theo workspace (OWNER/MEMBER).
- Thiết kế CSDL chuẩn BCNF, dễ mở rộng.

## Kiến trúc

- **Layered Architecture** kết hợp **Clean Architecture**.
- Package theo module: `model` → `repository` → `service` → `controller`.

## Công nghệ

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java 25 |
| Framework | Spring Boot 3.5.6 |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | MySQL 8 |
| Build tool | Maven |
| Validation | Jakarta Validation |
| Security | Spring Security (chỉ PasswordEncoder, chưa Auth/JWT) |
| Lombok | @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder, @RequiredArgsConstructor |
| API Spec | Springdoc OpenAPI (chưa cấu hình Swagger) |

## Cấu trúc package

```
com.tiendev.task_management_api/
├── config/              # Cấu hình ứng dụng (SecurityConfig)
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs (thực tế để trong dto/)
├── exception/           # Custom exceptions
├── helper/              # ApiResponse, GlobalExceptionHandler
├── model/               # JPA Entities
│   └── enums/           # Enums
├── repository/          # Spring Data JPA Repositories
├── service/             # Business Logic Interfaces
│   └── impl/            # Implementations
└── TaskManagementApiApplication.java
```

## Quy ước coding

- **Đặt tên**: camelCase (Java), snake_case (database).
- **Entity**: `@Entity`, `@Table(name = "tên_số_nhiều")`, kế thừa không có BaseEntity chung.
- **Repository**: extends `JpaRepository<Entity, Long>`.
- **Service**: Interface + Impl, `@Transactional` ở Impl.
- **Controller**: `@RestController`, `@RequiredArgsConstructor`, Constructor Injection.
- **DTO**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`, không trả Entity trực tiếp.
- **Exception**: dùng `orElseThrow()` với custom exception.
- **Field Injection**: Không dùng. Chỉ dùng Constructor Injection.

## Tính năng đã hoàn thành

- [x] CRUD User (có mã hóa password BCrypt).
- [x] CRUD Workspace (tự động tạo OWNER khi tạo workspace).
- [x] CRUD WorkspaceMember (kiểm tra trùng membership).
- [x] CRUD Project.
- [x] CRUD Task (assignee có thể null = Unassigned).
- [x] CRUD Comment.
- [x] Mã hóa password với BCrypt.
- [x] Global exception handling + validation.
- [x] ApiResponse format thống nhất.

## Tính năng đang phát triển

- [ ] Authentication / Login (JWT).
- [ ] Authorization (phân quyền theo workspace).
- [ ] Spring Security filter chain.
- [ ] Swagger/OpenAPI documentation.
- [ ] Pagination, sorting, filtering.
- [ ] File upload (attachment).
- [ ] Unit test.

## Roadmap

1. **Phase 1** (hiện tại): CRUD cơ bản, quan hệ dữ liệu, mã hóa password.
2. **Phase 2**: Authentication (JWT) + Authorization (workspace roles).
3. **Phase 3**: Nâng cao (pagination, search, file upload, notification).
4. **Phase 4**: Testing, CI/CD, deployment.

## Quy tắc dành cho AI

- Không ghi đè file đã tồn tại trừ khi có lệnh.
- Không thêm dependency mới nếu không được yêu cầu.
- Luôn kiểm tra file hiện có trước khi tạo mới.
- Tuân thủ cấu trúc package và quy ước coding hiện tại.
- DTO để trong `dto/request/` và `dto/`, không dùng Mapper.
- Sử dụng `ApiResponse` trong `helper` package cho mọi API response.
