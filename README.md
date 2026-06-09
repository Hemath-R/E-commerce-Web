# KRITHE STORE — Premium E-Commerce Platform

A full-stack luxury shoe e-commerce application built with HTML/CSS/JavaScript frontend, Spring Boot backend, MySQL database, and Razorpay payment integration.

## Project Structure

```
Shoe website/
├── frontend/                 # Static frontend (HTML, CSS, JS)
│   ├── index.html            # Home page
│   ├── login.html            # Login
│   ├── register.html         # Registration
│   ├── products.html         # Product listing
│   ├── product-details.html  # Product detail
│   ├── cart.html             # Shopping cart
│   ├── checkout.html         # Checkout & address
│   ├── payment.html          # Razorpay payment
│   ├── order-success.html    # Order confirmation
│   ├── css/main.css          # Premium dark theme styles
│   └── js/                   # API, auth, cart, products modules
├── backend/                  # Spring Boot REST API
│   └── src/main/java/com/krithe/store/
│       ├── controller/       # REST controllers
│       ├── service/          # Business logic
│       ├── repository/       # JPA repositories
│       ├── entity/           # Database entities
│       ├── security/         # JWT authentication
│       └── config/           # CORS, data seeder
└── database/
    └── schema.sql            # MySQL schema + sample data
```

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Razorpay Test Account** — [dashboard.razorpay.com](https://dashboard.razorpay.com)
- **Live Server** (VS Code extension) or any static file server for frontend

## Step 1: Database Setup

```bash
# Start MySQL, then run:
mysql -u root -p < database/schema.sql
```

Or let Spring Boot auto-create tables (`ddl-auto=update`) — sample products are seeded automatically on first run.

Update credentials in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

## Step 2: Razorpay Configuration

1. Create a Razorpay account and get **Test API Keys**
2. Update `backend/src/main/resources/application.properties`:

```properties
razorpay.key.id=rzp_test_XXXXXXXXXX
razorpay.key.secret=YOUR_SECRET_KEY
```

3. Update `frontend/js/config.js`:

```javascript
RAZORPAY_KEY: 'rzp_test_XXXXXXXXXX'
```

## Step 3: Start Backend

```bash
cd backend
mvn spring-boot:run
```

API runs at **http://localhost:8080**

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register user |
| POST | `/api/auth/login` | No | Login (returns JWT) |
| GET | `/api/products` | No | List products (search, filter, sort, pagination) |
| GET | `/api/products/{id}` | No | Product details + related |
| GET | `/api/products/featured` | No | Featured products |
| GET | `/api/products/trending` | No | Trending products |
| POST | `/api/cart` | Yes | Add to cart |
| GET | `/api/cart` | Yes | Get cart |
| PUT | `/api/cart/{id}` | Yes | Update quantity |
| DELETE | `/api/cart/{id}` | Yes | Remove item |
| POST | `/api/orders` | Yes | Create order |
| GET | `/api/orders` | Yes | Get user orders |
| POST | `/api/payments/create-order` | Yes | Create Razorpay order |
| POST | `/api/payments/verify` | Yes | Verify payment & finalize |
| POST | `/api/wishlist/{productId}` | Yes | Add to wishlist |

## Step 4: Start Frontend

Open the `frontend/` folder with **Live Server** (VS Code) on port 5500, or:

```bash
cd frontend
npx serve .
```

Visit **http://localhost:5500** (or your Live Server URL).

## User Flow

```
Home → Login/Register → Products → Product Details → Add to Cart
  → Cart → Checkout → Payment → Razorpay Popup → Backend Verification
  → BEGIN TRANSACTION → Save Order → Update Stock → Save Payment → COMMIT
  → Order Success
```

## Payment Transaction Flow

When Razorpay payment succeeds, the backend runs an atomic transaction:

1. **Verify** Razorpay HMAC signature
2. **BEGIN TRANSACTION**
3. Save payment details (status: SUCCESS)
4. Deduct product stock for each order item
5. Update order status to PAID
6. Clear user cart
7. **COMMIT** (or **ROLLBACK** on any failure)

## Features

- Premium dark theme with glassmorphism
- JWT authentication & role-based access
- Product search, category filter, price sort, pagination
- Wishlist & recently viewed products
- Responsive mobile-first design
- Razorpay (UPI, Cards, Net Banking, Wallet)
- Transaction-safe order processing

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 17, Spring Boot 3.2 |
| Database | MySQL 8 |
| Auth | JWT (jjwt) |
| Payments | Razorpay Java SDK |

## Troubleshooting

- **CORS errors**: Add your frontend URL to `app.cors.allowed-origins` in `application.properties`
- **Products not loading**: Ensure backend is running on port 8080
- **Payment fails**: Verify Razorpay test keys match in both backend and frontend config
- **MySQL connection refused**: Check MySQL service is running and credentials are correct

---

Built for portfolio and interview demonstrations. **KRITHE STORE** — Step Into Luxury.
