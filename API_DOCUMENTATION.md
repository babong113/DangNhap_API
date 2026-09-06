# GiaSu API Documentation

Tai lieu nay duoc viet dua tren source code hien tai cua du an.

## Base URL

```text
http://localhost:7000
```

Port mac dinh la `7000`. Co the doi nhanh bang bien moi truong `SERVER_PORT` trong file `.env`.

## Quy uoc chung

- Request/response dung `Content-Type: application/json`.
- Cac endpoint trong `/api/auth/**` khong can token.
- Cac endpoint khac can JWT:

```http
Authorization: Bearer <token>
```

## Ma loi chung

| HTTP code | Y nghia | Response/message |
| --- | --- | --- |
| 200 | Request thanh cong | Tuy endpoint |
| 201 | Tao moi thanh cong | Dung cho dang ky tai khoan |
| 400 | Du lieu request khong hop le hoac business validation fail | Custom error: `{"sucess": false, "message": "...", "data": 400}` |
| 401 | Token het han hoac token khong hop le | `{"success":false,"message":"Token da het han"}` hoac `{"success":false,"message":"Token khong hop le"}` |
| 403 | Chua xac thuc khi goi endpoint can token | Spring Security tra ve mac dinh |
| 404 | Endpoint khong ton tai | Spring Boot tra ve mac dinh |
| 500 | Loi server khong duoc handle | Spring Boot tra ve mac dinh |

Luu y: DTO `ApiRespone` trong code dang co field `sucess` thay vi `success`.

## Auth API

### 1. Dang ky tai khoan

```http
POST /api/auth/register
```

#### Request body

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `email` | string | Yes | Khong rong, dung dinh dang email |
| `password` | string | Yes | Khong rong, toi thieu 6 ky tu |
| `phoneNumber` | string | Yes | Khong rong, gom 10-11 chu so |
| `fullName` | string | Yes | Khong rong |
| `role` | string | Yes | Mot trong: `STUDENT`, `TUTOR`, `PARENT`; khong cho dang ky `ADMIN` |

#### Example request

```json
{
  "email": "student@example.com",
  "password": "123456",
  "phoneNumber": "0912345678",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```

#### Success response

HTTP `201 Created`

```json
{
  "token": "<jwt_token>",
  "userID": "1",
  "email": "student@example.com",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```

#### Error responses

HTTP `400 Bad Request`

```json
{
  "sucess": false,
  "message": "email da duoc su dung",
  "data": 400
}
```

Cac message co the gap:

- `email da duoc su dung`
- `So dien thoai da duoc su dung`
- `Khong the dang ky tai khoan ADMIN`
- Loi validate cua Spring khi field rong/sai format

Luu y implementation hien tai bat sai exception khi parse `role`; neu `role` khong thuoc enum, API co the tra `500` thay vi `400`.

### 2. Dang nhap

```http
POST /api/auth/login
```

#### Request body

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `email` | string | Yes | Khong rong, dung dinh dang email |
| `password` | string | Yes | Khong rong |

#### Example request

```json
{
  "email": "student@example.com",
  "password": "123456"
}
```

#### Success response

HTTP `200 OK`

```json
{
  "token": "<jwt_token>",
  "userID": "1",
  "email": "student@example.com",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```

#### Error responses

HTTP `400 Bad Request`

```json
{
  "sucess": false,
  "message": "Email hoac mat khau khong dung",
  "data": 400
}
```

Cac message co the gap:

- `Email hoac mat khau khong dung`
- `Tai khoan hien khong hoat dong`
- Loi validate cua Spring khi field rong/sai format

### 3. Quen mat khau

```http
POST /api/auth/forgot-password
```

Endpoint tao reset token co hieu luc 30 phut va gui email dat lai mat khau.

#### Request body

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `email` | string | Yes | Khong rong, dung dinh dang email |

#### Example request

```json
{
  "email": "student@example.com"
}
```

#### Success response

HTTP `200 OK`

```json
{
  "success": true,
  "message": "Email dat lai mat khau da duoc gui"
}
```

#### Error responses

HTTP `400 Bad Request`

```json
{
  "sucess": false,
  "message": "Khong tim thay tai khoan voi email nay",
  "data": 400
}
```

Cac message co the gap:

- `Khong tim thay tai khoan voi email nay`
- Loi validate cua Spring khi email rong/sai format

### 4. Dat lai mat khau

```http
POST /api/auth/reset-password
```

#### Request body

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `token` | string | Yes | Reset token da duoc gui qua email |
| `newPassword` | string | Yes | Khong rong, toi thieu 6 ky tu |
| `confirmPassword` | string | Yes | Phai trung voi `newPassword` |

#### Example request

```json
{
  "token": "b7e22573-0d80-4af2-a2cd-2a7d2a972d5f",
  "newPassword": "123456",
  "confirmPassword": "123456"
}
```

#### Success response

HTTP `200 OK`

```json
{
  "success": true,
  "message": "Dat lai mat khau thanh cong"
}
```

#### Error responses

HTTP `400 Bad Request`

```json
{
  "sucess": false,
  "message": "xac nhan mat khau khong khop",
  "data": 400
}
```

Cac message co the gap:

- `xac nhan mat khau khong khop`
- `Token dat lai mat khau khong hop le`
- `Da het han dat lai mat khau`
- Loi validate cua Spring khi field rong hoac password ngan hon 6 ky tu

## Schedule API

### 1. Lay danh sach lich hoc

```http
GET /api/shedules/all
```

Luu y: route hien tai trong code la `/api/shedules`, khong phai `/api/schedules`.

#### Headers

| Header | Required | Value |
| --- | --- | --- |
| `Authorization` | Yes | `Bearer <token>` |

#### Request parameters

Khong co query parameter, path parameter hay request body.

#### Success response

HTTP `200 OK`

```json
[
  {
    "id": 1,
    "classId": 10,
    "tutorId": 2,
    "dayOfWeek": 2,
    "startTime": "08:00:00",
    "endTime": "10:00:00",
    "location": "Quan 1, TP.HCM",
    "note": "Hoc tai nha hoc sinh"
  }
]
```

#### Response fields

| Field | Type | Mo ta |
| --- | --- | --- |
| `id` | number | ID lich hoc |
| `classId` | number | ID lop hoc |
| `tutorId` | number | ID gia su |
| `dayOfWeek` | number | Thu trong tuan, dang so nguyen |
| `startTime` | string | Gio bat dau, format `HH:mm:ss` |
| `endTime` | string | Gio ket thuc, format `HH:mm:ss` |
| `location` | string | Dia diem hoc |
| `note` | string | Ghi chu |

#### Error responses

HTTP `401 Unauthorized`

```json
{
  "success": false,
  "message": "Token khong hop le"
}
```

HTTP `403 Forbidden`

```json
{
  "timestamp": "2026-09-05T00:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/shedules/all"
}
```

HTTP `404 Not Found`

```json
{
  "timestamp": "2026-09-05T00:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/schedules/all"
}
```

## Tom tat endpoint

| Method | Endpoint | Auth | Success |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | No | `201` |
| `POST` | `/api/auth/login` | No | `200` |
| `POST` | `/api/auth/forgot-password` | No | `200` |
| `POST` | `/api/auth/reset-password` | No | `200` |
| `GET` | `/api/shedules/all` | Yes | `200` |
