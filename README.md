# Mini Market API

A Spring Boot backend for a mini market system using Google Firestore for persistence. It supports product management, order creation with stock checks, and dedicated stock adjustment endpoints.

## Features
- Products: create, list, update, delete
- Categories: list predefined categories
- Orders: create with stock validation and atomic stock decrement, list all orders
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
- `PORT` - app port (default: `8080`)
- `CORS_ALLOWED_ORIGINS` - comma-separated list of allowed frontend origins (default: `http://localhost:5173`)

### Example (PowerShell)
```powershell
$env:FIREBASE_PROJECT_ID="your-project-id"
$env:FIREBASE_SERVICE_ACCOUNT_JSON='{ "type": "service_account", ... }'
$env:API_KEY="your-secret-key"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"
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
Requests without the header or with the wrong key return 401.

## API Endpoints
Base URL (Railway): `https://url-shortener-production-d863.up.railway.app`

Local URL: `http://localhost:8080`

### Products
**Create product**
```
POST /api/products
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

**List products**
```
GET /api/products
```

**Update product** (full replace)
```
PUT /api/products/{id}
```
Body:
```json
{
  "name": "Green Apple",
  "categoryId": "category-id",
  "price": 1.75,
  "stock": 80,
  "image": "https://example.com/green-apple.png"
}
```

**Delete product**
```
DELETE /api/products/{id}
```

**Set stock (absolute)**
```
POST /api/products/{id}/stock
```
Body:
```json
{ "stock": 50 }
```

**Decrement stock**
```
POST /api/products/{id}/stock/decrement
```
Body:
```json
{ "quantity": 3 }
```

### Orders
**Create order**
```
POST /api/orders
```
Body:
```json
{
  "items": [
    { "productId": "abc123", "quantity": 2 },
    { "productId": "def456", "quantity": 1 }
  ]
}
```

**List orders**
```
GET /api/orders
```

### Categories
**List categories**
```
GET /api/categories
```

## Order Logic
When creating an order:
1. Validate each product exists
2. Validate stock >= requested quantity
3. Decrement stock in Firestore in a transaction
4. Calculate total price
5. Save the order with embedded item snapshots

## Error Handling
All errors are returned in a consistent JSON structure:
```json
{
  "timestamp": 1738060000000,
  "status": 400,
  "error": "Bad Request",
  "message": "Product name is required.",
  "path": "/api/products",
  "details": []
}
```

Common cases:
- 404 when a product ID does not exist
- 400 for validation errors (missing fields, negative stock, etc.)
- 401 when API key is missing or invalid

## Firestore Data Model
Collections:
- `products`
- `categories`
- `orders`

Product document fields:
- `id` (string)
- `name` (string)
- `categoryId` (string)
- `image` (string, optional)
- `price` (string or number)
- `stock` (number)

Category document fields:
- `id` (string)
- `name` (string)
- `slug` (string)

Order document fields:
- `id` (string)
- `createdAt` (epoch millis)
- `total` (string or number)
- `orderItems` (array of embedded items)

Each `orderItems` entry stores:
- `id` (string)
- `quantity` (number)
- `price` (string or number)
- `product` (embedded product snapshot)

## Notes
- Product IDs are Firestore document IDs (string). Use the returned `id` from create responses.
- The CORS config currently allows `GET`, `POST`, and `OPTIONS` methods. If your frontend uses `PUT` or `DELETE`, update CORS settings accordingly.

## Postman Quick Test
1. Create a product with `POST /api/products` (include `X-API-KEY` header)
2. Copy the returned `id`
3. Create an order using that `productId`
4. Verify stock decreases automatically
