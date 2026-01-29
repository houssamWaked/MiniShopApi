# Mini Market API (Multi-Store)

A Spring Boot backend for a **multi-store** mini market system using Google Firestore for persistence. It supports store management, role-based access control, product/category management per store, and order creation with stock checks.

## Features
- Multi-store support (each store has its own products and categories)
- Roles: **Super Admin**, **Sub Admin (Store Manager)**, **Customer**
- Products: create, list, update, delete per store
- Categories: create, list, update, delete per store
- Orders: create with stock validation and atomic stock decrement, list by role
- Stock management: set stock directly or decrement stock safely
- Centralized JSON error responses
- Firestore-backed data store (no SQL datasource required)
- CORS configured for frontend access via environment variable
- API key protection for all /api/* routes

## Tech Stack
- Java 17
- Spring Boot (Gradle and Maven configs included)
- Google Firebase Admin SDK (Firestore)

## Project Structure
```
src/main/java/com/HoussamAlwaked/minimarket
+-- config
+-- controller
+-- dto
+-- entity
+-- exception
+-- repository
+-- security
+-- service
+-- MiniMarketApplication.java
```

## Environment Variables
Required for Firestore:
- `FIREBASE_PROJECT_ID` - your Firebase project ID
- `FIREBASE_SERVICE_ACCOUNT_JSON` - the full service-account JSON as a single string

Required for API access:
- `API_KEY` - secret key required in request headers

Optional:
- `SUPER_ADMIN_EMAIL` - bootstrap super admin (email address)
- `DEFAULT_STORE_ID` - if set, seed default categories into this store on startup
- `PORT` - app port (default: `8080`)
- `CORS_ALLOWED_ORIGINS` - comma-separated list of allowed frontend origins (default: `http://localhost:8081,http://localhost:5173`)

### Example (PowerShell)
```powershell
$env:FIREBASE_PROJECT_ID="your-project-id"
$env:FIREBASE_SERVICE_ACCOUNT_JSON='{ "type": "service_account", ... }'
$env:API_KEY="your-secret-key"
$env:SUPER_ADMIN_EMAIL="admin@example.com"
$env:DEFAULT_STORE_ID="store-id-to-seed"
$env:CORS_ALLOWED_ORIGINS="http://localhost:8081,http://localhost:5173"
$env:PORT="8080"
```

## Run the App
### Gradle
```bash
./gradlew bootRun
```

### Maven
```bash
mvn spring-boot:run
```

## API Authentication
All `/api/*` routes require the header:
```
X-API-KEY: your-secret-key
```

Role-based access uses either:
```
X-USER-ID: user-id
```
or
```
X-USER-EMAIL: user@example.com
```

If `SUPER_ADMIN_EMAIL` is set, any request with a matching `X-USER-EMAIL` is treated as **Super Admin** (auto-created if missing).

## API Endpoints
Base URL (Railway): `https://url-shortener-production-d863.up.railway.app`

Local URL: `http://localhost:8080`

### Stores (Super Admin)
**Create store**
```
POST /api/stores
```
Body:
```json
{ "name": "Main Store", "address": "Downtown" }
```

**Update store**
```
PUT /api/stores/{storeId}
```

**Delete store**
```
DELETE /api/stores/{storeId}
```

**List stores (public)**
```
GET /api/stores
```

**Assign sub-admin to store**
```
POST /api/stores/{storeId}/sub-admins
```
Body:
```json
{ "userId": "user-id" }
```

**List store sub-admins**
```
GET /api/stores/{storeId}/sub-admins
```

### Users
**Register customer**
```
POST /api/users
```
Body:
```json
{ "name": "Alice", "email": "alice@example.com" }
```

**Create user (Super Admin)**
```
POST /api/admin/users
```
Body:
```json
{ "name": "Store Manager", "email": "manager@example.com", "role": "SUB_ADMIN", "assignedStoreId": "store-id" }
```

### Categories (Store scoped)
**List categories (public)**
```
GET /api/stores/{storeId}/categories
```

**Create category (Super/Sub Admin for store)**
```
POST /api/stores/{storeId}/categories
```
Body:
```json
{ "name": "Beverages", "slug": "beverages" }
```

**Update category**
```
PUT /api/stores/{storeId}/categories/{categoryId}
```

**Delete category**
```
DELETE /api/stores/{storeId}/categories/{categoryId}
```

### Products (Store scoped)
**List products (public)**
```
GET /api/stores/{storeId}/products
```
Optional query:
```
?categoryId=category-id
```

**Create product (Super/Sub Admin for store)**
```
POST /api/stores/{storeId}/products
```
Body:
```json
{
  "name": "Apple",
  "categoryId": "category-id",
  "price": 1.50,
  "stock": 100,
  "image": "https://example.com/apple.png"
}
```

**Update product**
```
PUT /api/stores/{storeId}/products/{id}
```

**Delete product**
```
DELETE /api/stores/{storeId}/products/{id}
```

**Set stock (absolute)**
```
POST /api/stores/{storeId}/products/{id}/stock
```
Body:
```json
{ "stock": 50 }
```

**Decrement stock**
```
POST /api/stores/{storeId}/products/{id}/stock/decrement
```
Body:
```json
{ "quantity": 3 }
```

### Orders
**Create order (Customer)**
```
POST /api/orders
```
Body:
```json
{
  "storeId": "store-id",
  "items": [
    { "productId": "abc123", "quantity": 2 }
  ]
}
```

**List orders (role-based)**
```
GET /api/orders
```
- Super Admin: all orders (optional `?storeId=` filter)
- Sub Admin: orders for assigned store
- Customer: own orders

## Order Logic
When creating an order:
1. Validate store exists
2. Validate each product exists and belongs to store
3. Validate stock >= requested quantity
4. Decrement stock in Firestore in a transaction
5. Calculate total price
6. Save the order with embedded item snapshots

## Error Handling
All errors are returned in a consistent JSON structure:
```json
{
  "timestamp": 1738060000000,
  "status": 400,
  "error": "Bad Request",
  "message": "Product name is required.",
  "path": "/api/stores/123/products",
  "details": []
}
```

Common cases:
- 404 when an entity ID does not exist
- 400 for validation errors
- 401 when API key is missing or invalid
- 403 when role access is missing

## Firestore Data Model
Collections:
- `stores`
- `users`
- `categories`
- `products`
- `orders`

Store document fields:
- `id` (string)
- `name` (string)
- `address` (string)
- `subAdminIds` (array of user ids)

User document fields:
- `id` (string)
- `name` (string)
- `email` (string)
- `role` (string: SUPER_ADMIN | SUB_ADMIN | CUSTOMER)
- `assignedStoreId` (string, optional)

Category document fields:
- `id` (string)
- `name` (string)
- `slug` (string)
- `storeId` (string)

Product document fields:
- `id` (string)
- `name` (string)
- `categoryId` (string)
- `storeId` (string)
- `image` (string, optional)
- `price` (string or number)
- `stock` (number)

Order document fields:
- `id` (string)
- `customerId` (string)
- `storeId` (string)
- `createdAt` (epoch millis)
- `total` (string or number)
- `status` (string)
- `orderItems` (array of embedded items)

Each `orderItems` entry stores:
- `id` (string)
- `quantity` (number)
- `price` (string or number)
- `product` (embedded product snapshot)

## Notes
- Product IDs are Firestore document IDs (string). Use the returned `id` from create responses.
- The CORS config currently allows `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS` methods.

## Postman Quick Test
1. Set `SUPER_ADMIN_EMAIL` and call `POST /api/stores` with `X-USER-EMAIL` header
2. Create a store and a sub-admin user
3. Assign sub-admin to store
4. Create categories/products for the store
5. Create an order with `POST /api/orders`
