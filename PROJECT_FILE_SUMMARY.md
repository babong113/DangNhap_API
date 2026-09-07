# Tom Tat Chuc Nang Cac File Trong Do An

Tai lieu nay tom tat nhiem vu cua cac file chinh trong do an `DangNhap API`. Project hien la Spring Boot API cho dang ky, dang nhap, JWT, phan quyen RBAC, quen mat khau va ket noi database bang JPA.

## Tong Quan Kien Truc

Project duoc chia theo huong tach core nghiep vu va adapter ha tang:

- `core`: chua logic auth, DTO, model, port interface, security va xu ly loi chung.
- `adapter`: chua code ket noi ben ngoai nhu database JPA va SMTP mail.
- `resources`: chua cau hinh Spring Boot va migration database.
- `test`: chua unit test va context test.

Luong phu thuoc chinh:

```text
Controller -> AuthService -> Port interface -> Adapter JPA/Mail -> Database/SMTP
SecurityFilter -> JwtService + UserDetailsService -> AccountStore
```

## File Build Va Chay Project

### `pom.xml`

File cau hinh Maven cua project.

Nhiem vu:

- Khai bao Spring Boot version `4.1.1`.
- Khai bao Java version `21`.
- Them dependency cho Web MVC, Spring Security, Spring Data JPA, Flyway, MySQL, PostgreSQL, H2, Mail, Validation, JWT va Lombok.
- Cau hinh plugin build Spring Boot va compiler.

### `mvnw`, `mvnw.cmd`

Maven wrapper.

Nhiem vu:

- Cho phep chay Maven ma khong can cai Maven rieng tren may.
- Tren Windows dung `mvnw.cmd`.

Lenh hay dung:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean spring-boot:run
```

### `.env`

File cau hinh moi truong local.

Nhiem vu:

- Chon database bang `APP_PROFILE`.
- Cau hinh `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Cau hinh JWT, mail, role mac dinh va role duoc phep dang ky.
- File nay khong nen commit vi co mat khau that.

### `.env.example`

File mau cho `.env`.

Nhiem vu:

- Cho biet cac bien moi truong can cau hinh.
- Dung de tao `.env` tren may moi.

## File Cau Hinh Spring Boot

### `src/main/resources/application.properties`

File cau hinh chung cua app.

Nhiem vu:

- Dat ten app.
- Import file `.env`.
- Chon profile active bang `spring.profiles.active=${APP_PROFILE:mysql}`.
- Cau hinh port server.
- Cau hinh `ddl-auto`, Flyway, JWT, public API path, CORS, role mac dinh va mail.

Luu y hien trang:

- Neu `.env` co `APP_PROFILE=mysql`, app dung MySQL.
- Neu `.env` co `APP_PROFILE=postgres`, app dung PostgreSQL.
- Neu `.env` khong co `APP_PROFILE`, gia tri sau dau `:` trong `spring.profiles.active` se duoc dung lam mac dinh.

### `src/main/resources/application-mysql.properties`

File cau hinh rieng cho MySQL.

Nhiem vu:

- Lay datasource tu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Dat Hibernate dialect MySQL.
- Tro Flyway den `classpath:db/migration/mysql`.

### `src/main/resources/application-postgres.properties`

File cau hinh rieng cho PostgreSQL.

Nhiem vu:

- Lay datasource tu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Dat Hibernate dialect PostgreSQL.
- Tro Flyway den `classpath:db/migration/postgres`.

### `src/main/resources/application-h2.properties`

File cau hinh test voi H2 in-memory database.

Nhiem vu:

- Tao database tam trong RAM cho test.
- Chay migration H2.
- Cau hinh JWT/mail gia lap cho test.

## File Migration Database

### `src/main/resources/db/migration/mysql/V1__create_auth_rbac_tables.sql`

Migration MySQL.

Nhiem vu hien tai:

- Tao schema MySQL mau cho auth.
- Tao bang `users`, `roles`, `user_roles`.
- Seed role/user mau theo SQL MySQL hien tai.

Luu y:

- File MySQL hien tai dang theo database mau `USER`, `ADMIN`, `MODERATOR`.
- Code Java hien tai co entity them `permissions`, `role_permissions`, `phone_number`, `avatar_url`, `last_login`, `reset_password_token`, `reset_password_token_expiry`.
- Neu dung Flyway validate nghiem ngat voi schema moi, migration MySQL nen duoc dong bo lai voi entity.
- Neu dung database mau SQL tay, co the dung `FLYWAY_ENABLED=false` va `JPA_DDL_AUTO=update`.

### `src/main/resources/db/migration/postgres/V1__create_auth_rbac_tables.sql`

Migration PostgreSQL tao schema auth/RBAC chuan.

Nhiem vu:

- Tao bang `users`.
- Tao bang `roles`.
- Tao bang `permissions`.
- Tao bang `user_roles`.
- Tao bang `role_permissions`.
- Seed role `TUTOR`, `STUDENT`, `PARENT`, `ADMIN`.

### `src/main/resources/db/migration/postgres/V2__migrate_user_role_to_user_roles.sql`

Migration PostgreSQL de nang cap database cu.

Nhiem vu:

- Tao cac bang RBAC neu chua ton tai.
- Seed role `TUTOR`, `STUDENT`, `PARENT`, `ADMIN` neu chua co.
- Neu bang `users` cu co cot `role`, chuyen du lieu role sang bang `user_roles`.
- Drop cot `users.role` cu.

### `src/main/resources/db/migration/h2/V1__create_auth_rbac_tables.sql`

Migration cho H2 khi chay test.

Nhiem vu:

- Tao schema auth/RBAC trong database H2 in-memory.
- Giu test gan voi schema PostgreSQL.

## Entry Point

### `src/main/java/com/bteam/platform/PlatformApplication.java`

Class main cua Spring Boot.

Nhiem vu:

- Start app bang `SpringApplication.run`.
- Bat config properties cho `AuthProperties`.

## Auth Controller

### `src/main/java/com/bteam/platform/core/auth/controller/AuthController.java`

REST controller cho auth API.

Nhiem vu:

- `POST /api/auth/register`: dang ky tai khoan.
- `POST /api/auth/login`: dang nhap.
- `POST /api/auth/forgot-password`: tao token reset password va gui email.
- `POST /api/auth/reset-password`: doi mat khau bang token.
- Goi `AuthService` de xu ly nghiep vu.
- Dong goi response bang `ApiResponse`.

## Auth DTO

### `src/main/java/com/bteam/platform/core/auth/dto/RegisterRequest.java`

Request body cho API dang ky.

Nhiem vu:

- Nhan `email`, `password`, `phoneNumber`, `fullName`, `role`.
- Validate email, password toi thieu 6 ky tu, so dien thoai 10-11 chu so, ho ten khong rong.

### `src/main/java/com/bteam/platform/core/auth/dto/LoginRequest.java`

Request body cho API dang nhap.

Nhiem vu:

- Nhan `email`, `password`.
- Validate email hop le va khong rong.

### `src/main/java/com/bteam/platform/core/auth/dto/ForgotPasswordRequest.java`

Request body cho API quen mat khau.

Nhiem vu:

- Nhan email can reset password.
- Validate email khong rong va dung dinh dang.

### `src/main/java/com/bteam/platform/core/auth/dto/ResetPasswordRequest.java`

Request body cho API dat lai mat khau.

Nhiem vu:

- Nhan `token`, `newPassword`, `confirmPassword`.
- Validate token va mat khau moi.

### `src/main/java/com/bteam/platform/core/auth/dto/AuthResponse.java`

Response data cho register/login.

Nhiem vu:

- Tra ve `token`, `userID`, `email`, `fullName`, `roles`, `permissions`.

## Auth Service

### `src/main/java/com/bteam/platform/core/auth/service/AuthService.java`

Service xu ly nghiep vu auth chinh.

Nhiem vu:

- Dang ky:
  - Chuan hoa email va phone.
  - Kiem tra email/phone trung.
  - Chon role mac dinh neu request khong gui role.
  - Kiem tra role co ton tai va duoc phep dang ky.
  - Hash password bang BCrypt.
  - Luu account.
  - Tra JWT response.
- Dang nhap:
  - Tim account bang email.
  - So sanh mat khau voi BCrypt hash.
  - Kiem tra status `ACTIVE`.
  - Cap nhat `lastLogin`.
  - Tra JWT response.
- Quen mat khau:
  - Tao reset token UUID.
  - Dat han token 30 phut.
  - Goi mail sender de gui link reset.
- Dat lai mat khau:
  - Kiem tra confirm password.
  - Tim account bang reset token.
  - Kiem tra token het han.
  - Cap nhat password hash va xoa token.

### `src/main/java/com/bteam/platform/core/auth/service/JwtService.java`

Service tao va doc JWT.

Nhiem vu:

- Tao JWT token tu account.
- Dua `userId`, `email`, `roles`, `permissions` vao claims.
- Extract email tu token.
- Kiem tra token dung user va chua het han.
- Ky token bang HS256 voi `jwt.secret`.

## Auth Model

### `src/main/java/com/bteam/platform/core/auth/model/Account.java`

Domain model cho tai khoan.

Nhiem vu:

- Dai dien user trong core auth, tach khoi JPA entity.
- Chua thong tin email, passwordHash, phone, fullName, status, reset token, roles, permissions.

### `src/main/java/com/bteam/platform/core/auth/model/AccountStatus.java`

Enum trang thai tai khoan.

Gia tri:

- `ACTIVE`
- `INACTIVE`
- `BLOCKED`

## Auth Config Va Port

### `src/main/java/com/bteam/platform/core/auth/config/AuthProperties.java`

Doc cau hinh role tu property `auth.registration`.

Nhiem vu:

- Doc `auth.registration.default-role`.
- Doc `auth.registration.allowed-roles`.
- Gan mac dinh `STUDENT` va `TUTOR, STUDENT, PARENT, ADMIN` neu khong cau hinh.

### `src/main/java/com/bteam/platform/core/auth/port/AccountStore.java`

Port interface cho viec luu/doc account.

Nhiem vu:

- Cho core auth lam viec voi account ma khong phu thuoc truc tiep JPA.
- Dinh nghia cac ham tim email, tim reset token, check trung email/phone, save account.

### `src/main/java/com/bteam/platform/core/auth/port/RolePolicy.java`

Port interface cho chinh sach role/permission.

Nhiem vu:

- Kiem tra role co duoc phep dang ky khong.
- Chuan hoa role.
- Lay role mac dinh.
- Lay permissions tu danh sach roles.

### `src/main/java/com/bteam/platform/core/auth/port/MailSender.java`

Port interface cho gui mail.

Nhiem vu:

- Cho core auth gui email reset password ma khong phu thuoc truc tiep SMTP.

## Security

### `src/main/java/com/bteam/platform/core/security/SecurityConfig.java`

Cau hinh Spring Security.

Nhiem vu:

- Tat CSRF cho stateless API.
- Bat CORS.
- Cau hinh session stateless.
- Cho phep public cac endpoint auth.
- Yeu cau authentication cho endpoint con lai.
- Dang ky `JwtAuthenticationFilter`.
- Tao bean `PasswordEncoder` dung BCrypt.

### `src/main/java/com/bteam/platform/core/security/JwtAuthenticationFilter.java`

Filter doc JWT tu request.

Nhiem vu:

- Lay header `Authorization: Bearer <token>`.
- Extract email tu token.
- Load user details.
- Kiem tra token hop le.
- Set authentication vao `SecurityContextHolder`.
- Tra 401 JSON neu token het han hoac khong hop le.

### `src/main/java/com/bteam/platform/core/security/CustomUserDetailsService.java`

Bridge giua account trong database va Spring Security.

Nhiem vu:

- Load user theo email.
- Tao `UserDetails` cho Spring Security.
- Map roles thanh authority dang `ROLE_<role>`.
- Map permissions thanh authority truc tiep.
- Disable user neu status khong phai `ACTIVE`.

## Common Response Va Exception

### `src/main/java/com/bteam/platform/core/common/response/ApiResponse.java`

Class response chuan cua API.

Nhiem vu:

- Dong goi response theo format `success`, `message`, `data`.

### `src/main/java/com/bteam/platform/core/common/exception/InvalidDataException.java`

Exception cho loi du lieu nghiep vu.

Nhiem vu:

- Nem loi khi email trung, role sai, mat khau sai, token reset sai, v.v.

### `src/main/java/com/bteam/platform/core/common/exception/BadRequestException.java`

Exception chung cho request sai.

Nhiem vu:

- Hien tai chua duoc dung nhieu, de san cho loi bad request rieng.

### `src/main/java/com/bteam/platform/core/common/exception/GlobalExceptionHandler.java`

Xu ly exception toan cuc.

Nhiem vu:

- Bat `InvalidDataException` va tra HTTP 400.
- Bat loi validation request va tra danh sach field loi.
- Giu response loi theo format `ApiResponse`.

## Adapter Mail

### `src/main/java/com/bteam/platform/adapter/mail/SmtpMailSender.java`

Adapter gui mail qua SMTP.

Nhiem vu:

- Implement `MailSender`.
- Tao link reset password voi token.
- Gui email bang `JavaMailSender`.

## Adapter Persistence JPA

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/entity/UserEntity.java`

JPA entity map bang `users`.

Nhiem vu:

- Map thong tin user.
- Map many-to-many voi `roles` thong qua bang `user_roles`.
- Tu set `createdAt`, `updatedAt` bang Hibernate timestamp.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/entity/RoleEntity.java`

JPA entity map bang `roles`.

Nhiem vu:

- Luu ten role va mo ta.
- Map many-to-many voi `permissions` thong qua bang `role_permissions`.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/entity/PermissionEntity.java`

JPA entity map bang `permissions`.

Nhiem vu:

- Luu ten permission va mo ta.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/repository/UserRepository.java`

Spring Data repository cho `UserEntity`.

Nhiem vu:

- CRUD user.
- Tim user theo email.
- Tim user theo reset password token.
- Kiem tra email/phone da ton tai.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/repository/RoleRepository.java`

Spring Data repository cho `RoleEntity`.

Nhiem vu:

- CRUD role.
- Tim role theo name.
- Tim nhieu role theo danh sach name.
- Kiem tra role name ton tai.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/repository/PermissionRepository.java`

Spring Data repository cho `PermissionEntity`.

Nhiem vu:

- CRUD permission.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/JpaAccountStore.java`

Adapter implement `AccountStore` bang JPA.

Nhiem vu:

- Chuyen `UserEntity` thanh domain `Account`.
- Chuyen `Account` thanh `UserEntity` khi save.
- Gan roles cho user bang `RoleRepository`.
- Tong hop permissions tu roles cua user.

### `src/main/java/com/bteam/platform/adapter/persistence/jpa/JpaRolePolicy.java`

Adapter implement `RolePolicy` bang JPA va config.

Nhiem vu:

- Doc default/allowed role tu `AuthProperties`.
- Chuan hoa role thanh uppercase.
- Kiem tra role co trong database.
- Lay permissions tu roles.

## Test

### `src/test/java/com/bteam/platform/PlatformApplicationTests.java`

Spring Boot context test.

Nhiem vu:

- Chay app context voi profile `h2`.
- Kiem tra cau hinh Spring, bean, JPA va migration H2 co load duoc khong.

### `src/test/java/com/bteam/platform/core/auth/service/AuthServiceTest.java`

Unit test cho `AuthService`.

Nhiem vu:

- Test register tao account voi role mac dinh `STUDENT`.
- Test register reject role khong hop le.
- Test login tra token khi credentials dung.
- Mock `AccountStore`, `MailSender`, `JwtService`, `RolePolicy`.

## Luong Chuc Nang Chinh

### Dang Ky

```text
AuthController.register
-> AuthService.register
-> AccountStore check email/phone
-> RolePolicy normalize/check role
-> PasswordEncoder hash password
-> AccountStore save
-> JwtService generate token
-> ApiResponse<AuthResponse>
```

### Dang Nhap

```text
AuthController.login
-> AuthService.login
-> AccountStore findByEmail
-> PasswordEncoder matches
-> Check AccountStatus.ACTIVE
-> Update lastLogin
-> JwtService generate token
-> ApiResponse<AuthResponse>
```

### Xac Thuc JWT

```text
Request co Authorization Bearer token
-> JwtAuthenticationFilter
-> JwtService extract email
-> CustomUserDetailsService load user
-> JwtService validate token
-> SecurityContextHolder set authentication
```

### Quen Mat Khau

```text
AuthController.forgotPassword
-> AuthService.forgotPassword
-> AccountStore findByEmail
-> Tao reset token UUID, expiry 30 phut
-> AccountStore save
-> MailSender sendResetPasswordEmail
```

### Dat Lai Mat Khau

```text
AuthController.resetPassword
-> AuthService.resetPassword
-> Kiem tra confirm password
-> AccountStore findByResetPasswordToken
-> Kiem tra token con han
-> Hash password moi
-> Xoa reset token
-> AccountStore save
```

## Ghi Chu Hien Trang

- Project dang co nhieu file cu package `com.bteam.giasu` da bi xoa trong git status; source hien tai dang nam trong package `com.bteam.platform`.
- `application.properties` hien dang de mac dinh profile `mysql`, Flyway disabled va Hibernate `ddl-auto=update`.
- PostgreSQL migration dang day du RBAC theo `TUTOR`, `STUDENT`, `PARENT`, `ADMIN`.
- MySQL migration hien tai dang theo database mau `USER`, `ADMIN`, `MODERATOR`.
- Neu muon MySQL va PostgreSQL giong nhau ve schema, can dong bo lai `db/migration/mysql/V1__create_auth_rbac_tables.sql` voi PostgreSQL V1.
