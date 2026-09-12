# Frontend Connection Guide

Tai lieu nay huong dan frontend ket noi voi `DangNhap API`.

## 1. Base URL

Backend mac dinh chay tai:

```text
http://localhost:7000
```

Neu dung Vite, tao `.env` frontend:

```env
VITE_API_BASE_URL=http://localhost:7000
```

Trong code:

```js
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
```

## 2. CORS

Backend phai cho phep origin frontend.

Trong `.env` backend:

```properties
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

Neu bi loi CORS:

- Kiem tra frontend dang chay port nao.
- Them origin do vao `CORS_ALLOWED_ORIGINS`.
- Restart backend sau khi sua `.env`.

## 3. Response Chung

Moi response chinh co dang:

```json
{
  "success": true,
  "message": "Thong diep",
  "data": {}
}
```

Khi validation loi:

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

## 4. Auth API

### 4.1 Dang Ky

```http
POST /api/auth/register
Content-Type: application/json
```

Body:

```json
{
  "email": "student@example.com",
  "password": "123456",
  "phoneNumber": "0912345678",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```

Validation:

- `email`: bat buoc, dung dinh dang email.
- `password`: bat buoc, toi thieu 6 ky tu.
- `phoneNumber`: bat buoc, 10-11 chu so.
- `fullName`: bat buoc.
- `role`: optional. Neu khong gui, backend dung role mac dinh.

Response thanh cong:

```json
{
  "success": true,
  "message": "Dang ky thanh cong",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "opaque-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": "1",
    "email": "student@example.com",
    "fullName": "Nguyen Van A",
    "roles": ["STUDENT"],
    "permissions": []
  }
}
```

### 4.2 Dang Nhap

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

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

Frontend can luu:

- `accessToken`: dung trong header `Authorization`.
- `refreshToken`: dung de lay token moi.
- User info: `userId`, `email`, `fullName`, `roles`, `permissions`.

### 4.3 Lay User Hien Tai

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

Response:

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

Dung endpoint nay de:

- Kiem tra access token con hop le.
- Load lai user khi refresh trang.
- Dong bo role/permission moi nhat.

### 4.4 Lam Moi Token

```http
POST /api/auth/refresh-token
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Lam moi token thanh cong",
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-opaque-refresh-token",
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

Luu y:

- Backend rotate refresh token moi lan refresh.
- Frontend phai thay refresh token cu bang refresh token moi.
- Khong tiep tuc dung refresh token cu.
- Neu refresh token cu bi dung lai, backend se revoke cac refresh token active cua user.

### 4.5 Logout Mot Thiet Bi

```http
POST /api/auth/logout
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Sau khi logout thanh cong, frontend xoa token/user local.

### 4.6 Logout Tat Ca Thiet Bi

```http
POST /api/auth/logout-all
Authorization: Bearer <accessToken>
```

Backend revoke tat ca refresh token active cua user hien tai.

### 4.7 Quen Mat Khau

```http
POST /api/auth/forgot-password
Content-Type: application/json
```

Body:

```json
{
  "email": "student@example.com"
}
```

### 4.8 Dat Lai Mat Khau

```http
POST /api/auth/reset-password
Content-Type: application/json
```

Body:

```json
{
  "token": "reset-token-from-email",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

## 5. Goi API Bang Fetch

```js
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function login(email, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    throw new Error(body.message || "Dang nhap that bai");
  }

  return body.data;
}
```

## 6. Luu Token Phia Frontend

Cho do an, co the luu token trong `localStorage`:

```js
export function saveAuth(authData) {
  localStorage.setItem("accessToken", authData.accessToken);
  localStorage.setItem("refreshToken", authData.refreshToken);
  localStorage.setItem("currentUser", JSON.stringify({
    userId: authData.userId,
    email: authData.email,
    fullName: authData.fullName,
    roles: authData.roles,
    permissions: authData.permissions
  }));
}

export function clearAuth() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("currentUser");
}
```

Neu lam production nghiem tuc, nen can nhac HttpOnly cookie.

## 7. Goi API Can Dang Nhap

```js
export async function authFetch(path, options = {}) {
  const accessToken = localStorage.getItem("accessToken");

  return fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Bearer ${accessToken}`
    }
  });
}
```

Vi du:

```js
const response = await authFetch("/api/auth/me");
const body = await response.json();
```

## 8. Tu Dong Refresh Token Khi Gap 401

```js
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");

  if (!refreshToken) {
    throw new Error("Missing refresh token");
  }

  const response = await fetch(`${API_BASE_URL}/api/auth/refresh-token`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ refreshToken })
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    clearAuth();
    throw new Error(body.message || "Refresh token failed");
  }

  saveAuth(body.data);
  return body.data.accessToken;
}

export async function authFetchWithRefresh(path, options = {}) {
  let accessToken = localStorage.getItem("accessToken");

  let response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Bearer ${accessToken}`
    }
  });

  if (response.status !== 401) {
    return response;
  }

  accessToken = await refreshAccessToken();

  return fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Bearer ${accessToken}`
    }
  });
}
```

## 9. Logout Frontend

```js
export async function logout() {
  const refreshToken = localStorage.getItem("refreshToken");

  if (refreshToken) {
    await fetch(`${API_BASE_URL}/api/auth/logout`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ refreshToken })
    });
  }

  clearAuth();
}
```

## 10. Bao Ve Route Frontend

```js
export function isAuthenticated() {
  return Boolean(localStorage.getItem("accessToken"));
}

export function getCurrentUser() {
  const raw = localStorage.getItem("currentUser");
  return raw ? JSON.parse(raw) : null;
}

export function hasRole(role) {
  const user = getCurrentUser();
  return user?.roles?.includes(role);
}

export function hasPermission(permission) {
  const user = getCurrentUser();
  return user?.permissions?.includes(permission);
}
```

## 11. Checklist Frontend

- Dung `accessToken`, khong dung field `token`.
- Header auth phai la `Authorization: Bearer <accessToken>`.
- Sau login/register, luu ca `accessToken` va `refreshToken`.
- Sau refresh token, thay ca access token va refresh token bang gia tri moi.
- Khi logout, goi `/api/auth/logout` voi refresh token roi xoa local state.
- Khi load lai app, goi `/api/auth/me` de kiem tra token/user.
- Neu `/api/auth/me` tra 401, thu refresh token.
- Neu refresh token fail, logout local.
