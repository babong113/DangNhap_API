# DangNhap API

API dang ky/dang nhap bang Spring Boot, Spring Security, JWT, refresh token, RBAC va Flyway cho PostgreSQL.

## Tinh Nang Chinh

- Dang ky, dang nhap, quen mat khau, dat lai mat khau.
- Access token JWT ngan han.
- Refresh token dai han, luu trong DB bang SHA-256 hash.
- Rotate refresh token moi lan refresh.
- Phat hien refresh token da revoke bi dung lai va revoke cac refresh token active cua user.
- Logout mot thiet bi va logout tat ca thiet bi.
- Endpoint `/api/auth/me` de kiem tra access token va lay thong tin user hien tai.
- RBAC voi `roles`, `permissions`, `user_roles`, `role_permissions`.
- PostgreSQL migration bang Flyway.
- Maven wrapper cuc bo de chay build/test.

## Yeu Cau

- Java 21 tro len.
- PostgreSQL.
- SMTP Gmail neu dung chuc nang quen mat khau.

## Cau Hinh `.env`

Tao file `.env` trong thu muc goc:

```text
D:\LogGin\DangNhap_API\.env
```

Vi du:

```properties
APP_PROFILE=postgres
SERVER_PORT=7000

DB_URL=jdbc:postgresql://localhost:5432/dangnhap_auth_api
DB_USERNAME=postgres
DB_PASSWORD=123456

JWT_SECRET=replace_with_at_least_32_characters_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=2592000000

CORS_ALLOWED_ORIGINS=*

AUTH_DEFAULT_ROLE=STUDENT
AUTH_ALLOWED_ROLES=TUTOR,STUDENT,PARENT,ADMIN

FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false

MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Ghi chu:

- `JWT_ACCESS_TOKEN_EXPIRATION=900000` la 15 phut.
- `JWT_REFRESH_TOKEN_EXPIRATION=2592000000` la 30 ngay.
- `JWT_SECRET` phai co it nhat 32 bytes.
- `JWT_EXPIRATION` cu van duoc support fallback, nhung nen dung `JWT_ACCESS_TOKEN_EXPIRATION`.

## Chay API

```powershell
cd D:\LogGin\DangNhap_API
.\mvnw.cmd spring-boot:run
```

API chay tai:

```text
http://localhost:7000
```

## Chay Test

```powershell
.\mvnw.cmd clean test
```

Ket qua test gan nhat:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Flyway PostgreSQL

Migration nam o:

```text
src/main/resources/db/migration/postgres/
```

Danh sach migration:

- `V1__create_auth_rbac_tables.sql`: tao bang auth/RBAC co ban.
- `V2__migrate_user_role_to_user_roles.sql`: migrate cot role cu sang bang `user_roles`.
- `V3__create_refresh_tokens.sql`: tao bang `refresh_tokens`.
- `V4__ensure_refresh_tokens_schema.sql`: dam bao bang `refresh_tokens` co du cot/index can thiet cho DB da tao thu cong truoc do.

Bang `refresh_tokens` luu:

- `token_hash`: hash SHA-256 cua refresh token.
- `expires_at`: han refresh token.
- `revoked_at`: thoi diem token bi thu hoi.
- `replaced_by_token_id`: token moi thay the token cu khi rotate.
- `created_by_ip`, `user_agent`: thong tin request tao token.

## JWT Flow

### Dang Ky

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "email": "student@example.com",
  "password": "123456",
  "phoneNumber": "0912345678",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```

Neu khong gui `role`, API dung `AUTH_DEFAULT_ROLE`.

### Dang Nhap

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "student@example.com",
  "password": "123456"
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Dang nhap thanh cong",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "opaque-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": "1",
    "email": "student@example.com",
    "fullName": "Nguyen Van A",
    "roles": ["STUDENT"],
    "permissions": ["account:read"]
  }
}
```

Dung access token:

```http
Authorization: Bearer jwt-access-token
```

### Lay User Hien Tai

Dung de test access token va lay thong tin user dang dang nhap.

```http
GET /api/auth/me
Authorization: Bearer jwt-access-token
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Lay thong tin nguoi dung thanh cong",
  "data": {
    "userId": "1",
    "email": "student@example.com",
    "fullName": "Nguyen Van A",
    "roles": ["STUDENT"],
    "permissions": ["account:read"]
  }
}
```

### Lam Moi Token

```http
POST /api/auth/refresh-token
Content-Type: application/json
```

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Ket qua:

- API tra `accessToken` moi.
- API tra `refreshToken` moi.
- Refresh token cu bi set `revoked_at`.
- Refresh token cu co `replaced_by_token_id` tro toi token moi.

Neu refresh token cu da bi revoke ma bi dung lai, API se coi la dau hieu reuse va revoke cac refresh token active cua user.

### Dang Xuat Mot Thiet Bi

```http
POST /api/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Ket qua:

- Refresh token bi set `revoked_at`.
- Refresh token do khong con dung de refresh duoc.

### Dang Xuat Tat Ca Thiet Bi

Endpoint nay can access token hop le.

```http
POST /api/auth/logout-all
Authorization: Bearer jwt-access-token
```

Ket qua:

- Tat ca refresh token active cua user bi revoke.

## Quen Mat Khau

### Gui Email Dat Lai Mat Khau

```http
POST /api/auth/forgot-password
Content-Type: application/json
```

```json
{
  "email": "student@example.com"
}
```

### Dat Lai Mat Khau

```http
POST /api/auth/reset-password
Content-Type: application/json
```

```json
{
  "token": "reset-token-from-email",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

## Luu Y Bao Mat

- API hien tai chua blacklist access token theo `jti`.
- Khi logout, refresh token bi revoke ngay, nhung access token cu van co the dung toi khi het han.
- Vi access token mac dinh chi song 15 phut, cach nay chap nhan duoc cho hien tai.
- Neu sau nay can logout mat hieu luc ngay lap tuc, hay them bang/cache blacklist access token theo `jti`.

## Kiem Tra DB Refresh Token

```sql
select id, user_id, token_hash, expires_at, revoked_at, replaced_by_token_id
from refresh_tokens
order by id desc;
```

DB chi nen co `token_hash`, khong nen co raw refresh token.

## Merge Code Co Thay Doi Database

Khi can them bang/cot/index:

1. Khong sua migration da chay tren moi truong that.
2. Tao migration moi tang version.
3. Uu tien migration trong `src/main/resources/db/migration/postgres`.

Sau khi pull code moi, chay:

```powershell
.\mvnw.cmd clean test
```

hoac start API de Flyway apply migration con thieu.
