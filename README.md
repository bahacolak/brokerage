# Brokerage API

A Spring Boot API that enables brokerage firm employees to manage stock orders for customers.

## Features

- 📈 Order Management (Create, List, Cancel)
- 💰 Asset Tracking (TRY balance and stocks)
- 🔐 JWT Authentication
- 👥 Role-based Authorization (ADMIN, CUSTOMER)
- ⚡ Order Matching (Admin feature)

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- H2 Database
- Maven

## Quick Start

### 1. Run with Docker

```bash
# Build and run with Docker
docker-compose up --build

# Just run (if image exists)
docker-compose up
```

### 2. Run with Maven Wrapper

```bash
# Build (Unix/Linux/Mac)
./mvnw clean install

# Run
./mvnw spring-boot:run

# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

### 3. Run with System Maven

```bash
# If you have Maven installed
mvn clean install
mvn spring-boot:run
```

### 4. Run with JAR

```bash
./mvnw clean package
java -jar target/brokerage-api-1.0.0.jar
```

## Access

- **API:** http://localhost:8080
- **H2 Console:** http://localhost:8080/h2-console
  - URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: (empty)

## Default Users

| Username | Password | Role | Balance |
|----------|----------|------|---------|
| admin | admin123 | ADMIN | - |
| testuser | test123 | CUSTOMER | 10,000 TRY |

## API Usage

### 1. Login
```bash
POST /api/auth/login
{
  "username": "testuser",
  "password": "test123"
}
```

### 2. Create Order
```bash
POST /api/orders
Authorization: Bearer {token}
{
  "assetName": "AAPL",
  "side": "BUY",
  "size": 10,
  "price": 150.50
}
```

### 3. List Orders
```bash
GET /api/orders
Authorization: Bearer {token}
```

### 4. Cancel Order
```bash
DELETE /api/orders/{orderId}
Authorization: Bearer {token}
```

## Admin Operations

```bash
# Create order for customer
POST /api/admin/orders
Authorization: Bearer {admin_token}
{
  "customerId": 2,
  "assetName": "AAPL",
  "side": "BUY",
  "size": 10,
  "price": 150.50
}

# Match order
POST /api/admin/orders/match
{
  "orderId": 1
}
```

## Postman Collection

Import `brokerage-api-collection.json` into Postman to test all endpoints.

## Docker Commands

```bash
# Build
docker build -t brokerage-api .

# Run
docker run -p 8080:8080 brokerage-api

# Docker Compose
docker-compose up --build
docker-compose down
```

## Development

```bash
# Run tests (Maven Wrapper - recommended)
./mvnw test

# Build with tests
./mvnw clean install

# Windows
mvnw.cmd test
mvnw.cmd clean install

# System Maven (if available)
mvn test
mvn clean install
```