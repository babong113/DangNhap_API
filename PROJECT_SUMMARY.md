# Project Summary

Tai lieu nay tom tat cac thanh phan chinh trong do an `DangNhap API`.

## Tong Quan

`DangNhap API` la backend Spring Boot cho xac thuc va phan quyen nguoi dung.

Chuc nang chinh:

- Dang ky tai khoan.
- Dang nhap bang email/password.
- Xac thuc request bang JWT access token.
- Lam moi access token bang refresh token.
- Logout mot thiet bi.
- Logout tat ca thiet bi.
- Lay thong tin user hien tai bang `/api/auth/me`.
- Quen mat khau va dat lai mat khau.
- Phan quyen bang RBAC: role va permission.
- Quan ly schema PostgreSQL bang Flyway.

Cong nghe:

- Java 21.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- JJWT.
- Lombok.
- Maven wrapper.

## Cau Truc Thu Muc

```text
src/main/java/com/bteam/platform/
  PlatformApplication.java
  core/
    auth/
      controller/      AuthController
      dto/             Request/response DTO
      model/           Account, RefreshToken, AccountStatus
      port/            AccountStore, RefreshTokenStore, RolePolicy, MailSender
      service/         AuthService, JwtService
    common/
      exception/       GlobalExceptionHandler va exception rieng
      response/        ApiResponse
    security/          SecurityConfig, JwtAuthenticationFilter, CustomUserDetailsService
  adapter/
    mail/              SmtpMailSender
    persistence/jpa/   JPA store, entity, repository
src/main/resources/
  application.properties
  application-postgres.properties
  db/migration/postgres/
```

## Database

Database chinh: PostgreSQL.

Migration:

- `V1__create_auth_rbac_tables.sql`
  - Tao `users`, `roles`, `permissions`, `user_roles`, `role_permissions`.
- `V2__migrate_user_role_to_user_roles.sql`
  - Migrate cot `users.role` cu sang bang `user_roles` neu co.
- `V3__create_refresh_tokens.sql`
  - Tao bang `refresh_tokens`.
- `V4__ensure_refresh_tokens_schema.sql`
  - Dam bao bang `refresh_tokens` co du cot/index can thiet.

Bang chinh:

- `users`: thong tin tai khoan, password hash, status, reset password token.
- `roles`: danh sach role.
- `permissions`: danh sach permission.
- `user_roles`: lien ket user-role.
- `role_permissions`: lien ket role-permission.
- `refresh_tokens`: luu refresh token hash, expiry, revoke status va thong tin rotate.

Role mac dinh:

- `TUTOR`
- `STUDENT`
- `PARENT`
- `ADMIN`

## Cau Hinh Backend

Tao file `.env` tai thu muc goc:

```properties
APP_PROFILE=postgres
SERVER_PORT=7000

DB_URL=jdbc:postgresql://localhost:5432/dangnhap_auth_api
DB_USERNAME=postgres
DB_PASSWORD=123456

JWT_SECRET=replace_with_at_least_32_characters_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=2592000000

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

AUTH_DEFAULT_ROLE=STUDENT
AUTH_ALLOWED_ROLES=TUTOR,STUDENT,PARENT,ADMIN

FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false

MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Ghi chu:

- `JWT_ACCESS_TOKEN_EXPIRATION=900000`: access token song 15 phut.
- `JWT_REFRESH_TOKEN_EXPIRATION=2592000000`: refresh token song 30 ngay.
- `JWT_SECRET` phai co it nhat 32 bytes.
- `CORS_ALLOWED_ORIGINS` nen khai bao dung domain frontend.

## Chay Project

Chay API:

```powershell
.\mvnw.cmd spring-boot:run
```

Chay test:

```powershell
.\mvnw.cmd clean test
```

Base URL mac dinh:

```text
http://localhost:7000
```

## JWT Va Bao Mat

Da co:

- Access token co `iat`, `exp`, `jti`.
- Access token mac dinh 15 phut.
- Refresh token mac dinh 30 ngay.
- Refresh token raw chi tra ve client.
- Database chi luu SHA-256 hash cua refresh token.
- Refresh token rotate moi lan refresh.
- Detect reuse refresh token da revoke.
- Logout revoke refresh token.
- Logout all revoke tat ca refresh token active cua user.
- User inactive/disabled khong duoc authenticate.

Chua lam:

- Chua blacklist access token theo `jti`.
- Khi logout, access token cu van co the dung toi khi het han.
- Vi access token song ngan 15 phut, trang thai nay chap nhan duoc cho hien tai.

## Response Chung

Response thanh cong/thong thuong:

```json
{
  "success": true,
  "message": "Thong diep",
  "data": {}
}
```

Validation error:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": [
    {
      "field": "email",
      "message": "Email khong hop le"
    }
  ]
}
```

## API Chinh

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/refresh-token`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

Chi tiet cach frontend ket noi nam trong:

```text
FRONTEND_CONNECTION_GUIDE.md
```

## Lenh Kiem Tra DB

Kiem tra refresh token:

```sql
select id, user_id, token_hash, expires_at, revoked_at, replaced_by_token_id
from refresh_tokens
order by id desc;
```

DB chi nen co `token_hash`, khong nen co raw refresh token.
