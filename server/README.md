# Smart Dustbin Server

Backend API server for the Smart Dustbin ecosystem. This server manages user data, bins, waste deposits, rewards, and connections with IoT devices.

## Setup Instructions

1. Install dependencies:
   ```
   npm install
   ```

2. Create a `.env` file in the server root directory with the following variables:
   ```
   PORT=5000
   API_KEY=your_api_key_here
   NODE_ENV=development
   ```

3. Place your Firebase service account key (`serviceAccountKey.json`) in the server root directory.

4. Initialize the database with sample data:
   ```
   npm run setup-db
   ```

5. Start the development server:
   ```
   npm run dev
   ```

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login user
- `GET /auth/profile` - Get user profile
- `PUT /auth/profile` - Update user profile
- `GET /auth/rewards` - Get user rewards
- `POST /auth/redeem` - Redeem a coupon
- `GET /auth/history` - Get user deposit history

### Smart Bins
- `GET /api/bins` - Get all bins
- `GET /api/bins/:id` - Get bin details
- `POST /api/simulate/deposit` - Simulate waste deposit
- `POST /api/simulate/empty-bin` - Simulate emptying a bin
- `GET /api/bins/:id/qr-data` - Generate QR code data for bin

### Admin Routes
- `GET /admin/dashboard/stats` - Get admin dashboard stats
- `GET /admin/users` - Get all users
- `POST /admin/bins` - Add new smart bin
- `PUT /admin/bins/:id` - Update smart bin
- `DELETE /admin/bins/:id` - Delete smart bin
- `POST /admin/partner-offers` - Add new partner offer

## API Authentication

All API endpoints except the authentication routes require an API key header:
```
x-api-key: your_api_key_here
```

Protected user routes require a Bearer token header:
```
Authorization: Bearer {your_firebase_auth_token}
```

## Technologies Used
- Node.js
- Express
- Firebase Admin SDK (Firestore)
- JWT Authentication 