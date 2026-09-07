# DangNhap API

Day la do an API dang nhap co tich hop Flyway de quan ly database migration. Khi merge code giua cac thanh vien, neu co thay doi schema database thi them file migration moi thay vi sua database bang tay. Flyway se tu chay cac migration con thieu khi start API.

Project hien co:

- Auth API: register, login, forgot password, reset password.
- JWT authentication.
- RBAC voi `roles`, `permissions`, `user_roles`, `role_permissions`.
- JPA/Hibernate de thao tac database.
- Flyway de tao va cap nhat schema.
- Ho tro PostgreSQL, MySQL va H2 cho test.

## Yeu Cau

- Java 21 tro len.
- MySQL hoac PostgreSQL.
- Maven da duoc cai tren may.
- SMTP Gmail neu muon dung chuc nang quen mat khau.

## Cau Truc Chinh

```text
src/main/java/com/bteam/platform/
  PlatformApplication.java
  core/
    auth/
      controller/
      dto/
      model/
      port/
      service/
    common/
      exception/
      response/
    security/
  adapter/
    mail/
    persistence/jpa/
```

Migration database nam o:

```text
src/main/resources/db/migration/
  h2/
  mysql/
  postgres/
```

## Huong Dan Chay Du Lieu

### Buoc 1: Tao file `.env`

Tao file `.env` o thu muc goc project:

```text
D:\LogGin\DangNhap_API\.env
```

Noi dung `.env` cau hinh giong nhu file `.env.example`.

Vi du voi MySQL:

```properties
APP_PROFILE=mysql
SERVER_PORT=7000

DB_URL=jdbc:mysql://localhost:3306/dangnhap_auth_api?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=123456

JWT_SECRET=replace_with_at_least_32_characters_secret_key
JWT_EXPIRATION=86400000

CORS_ALLOWED_ORIGINS=*

AUTH_DEFAULT_ROLE=STUDENT
AUTH_ALLOWED_ROLES=TUTOR,STUDENT,PARENT,ADMIN

FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false

MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Vi du voi PostgreSQL:

```properties
APP_PROFILE=postgres
SERVER_PORT=7000

DB_URL=jdbc:postgresql://localhost:5432/dangnhap_auth_api
DB_USERNAME=postgres
DB_PASSWORD=123456

JWT_SECRET=replace_with_at_least_32_characters_secret_key
JWT_EXPIRATION=86400000

CORS_ALLOWED_ORIGINS=*

AUTH_DEFAULT_ROLE=STUDENT
AUTH_ALLOWED_ROLES=TUTOR,STUDENT,PARENT,ADMIN

FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false

MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

### Buoc 2: Chon database trong `application.properties`

Mo file:

```text
src/main/resources/application.properties
```

Kiem tra dong:

```properties
spring.profiles.active=${APP_PROFILE:mysql}
```

Gia tri sau dau `:` la database mac dinh neu `.env` khong co `APP_PROFILE`.

Dung MySQL:

```properties
spring.profiles.active=${APP_PROFILE:mysql}
```

Dung PostgreSQL:

```properties
spring.profiles.active=${APP_PROFILE:postgres}
```

Neu file `.env` da co `APP_PROFILE=mysql` hoac `APP_PROFILE=postgres` thi Spring se uu tien gia tri trong `.env`.

### Buoc 3: Kiem tra file profile database

Neu dung MySQL, app se doc:

```text
src/main/resources/application-mysql.properties
```

Neu dung PostgreSQL, app se doc:

```text
src/main/resources/application-postgres.properties
```

Moi profile se cau hinh Flyway location rieng:

```properties
spring.flyway.locations=classpath:db/migration/mysql
```

hoac:

```properties
spring.flyway.locations=classpath:db/migration/postgres
```

### Buoc 4: Chay API

Chay lenh tai thu muc goc project:

```powershell
cd D:\LogGin\DangNhap_API
mvn clean spring-boot:run
```

Khi chay thanh cong se thay log gan giong:

```text
Tomcat started on port 7000
Started PlatformApplication
```

API chay tai:

```text
http://localhost:7000
```

### Buoc 5: Chay test

```powershell
mvn test
```

## Luu Y Khi Dung Flyway

Flyway se tu chay migration trong thu muc dung voi profile database dang active.

Vi du:

- `APP_PROFILE=mysql` se chay migration trong `db/migration/mysql`.
- `APP_PROFILE=postgres` se chay migration trong `db/migration/postgres`.

Khi database moi hoan toan, cau hinh:

```properties
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false
```

Khi database da co bang tu truoc nhung chua co bang `flyway_schema_history`, co the can cau hinh tam thoi:

```properties
FLYWAY_BASELINE_ON_MIGRATE=true
```

Sau khi baseline/migrate xong, nen doi lai:

```properties
FLYWAY_BASELINE_ON_MIGRATE=false
```

Neu da xoa hoac doi ten file migration, nen chay lai bang lenh `clean` de xoa file migration cu trong `target/classes`:

```powershell
mvn clean spring-boot:run
```

## Schema RBAC

Schema chinh gom:

- `users`: thong tin tai khoan.
- `roles`: danh sach vai tro.
- `permissions`: danh sach quyen.
- `user_roles`: lien ket user voi role.
- `role_permissions`: lien ket role voi permission.

Role mac dinh duoc seed:

```text
TUTOR
STUDENT
PARENT
ADMIN
```

Neu muon dung database mau khac co role nhu `USER`, `ADMIN`, `MODERATOR`, can doi `.env` cho khop:

```properties
AUTH_DEFAULT_ROLE=USER
AUTH_ALLOWED_ROLES=USER,ADMIN,MODERATOR
```

Neu database mau duoc tao bang SQL tay va khong muon Flyway tao schema, co the tat Flyway:

```properties
FLYWAY_ENABLED=false
JPA_DDL_AUTO=update
```

## API Mau

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

Neu khong gui `role`, API se dung `AUTH_DEFAULT_ROLE` trong `.env`.

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

Dung token tra ve de goi API can dang nhap:

```http
Authorization: Bearer jwt-token
```

### Quen Mat Khau

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

## Cach Merge Code Co Thay Doi Database

Khi can them bang, them cot, them role hoac permission:

1. Khong sua truc tiep file migration da chay tren may nguoi khac.
2. Tao file migration moi tang version.
3. Tao file tuong ung cho DB can ho tro.

Vi du:

```text
src/main/resources/db/migration/mysql/V2__add_course_tables.sql
src/main/resources/db/migration/postgres/V2__add_course_tables.sql
```

Sau khi pull code moi, chi can chay lai API, Flyway se tu apply migration con thieu.
