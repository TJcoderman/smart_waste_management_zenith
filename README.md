# Smart Dustbin Ecosystem for India

> A comprehensive waste management system with IoT integration and rewards

## 🌟 Project Overview

Smart Dustbin Ecosystem is a hackathon project that aims to encourage responsible waste disposal by rewarding users for proper segregation and recycling. The system consists of smart bins equipped with IoT sensors, a mobile application for users, an admin dashboard, and a backend server.

### Key Features

- **Smart Bins with IoT Sensors**: Weight sensors, waste type identification, fill level monitoring
- **Mobile App for Users**: 
  - QR code scanning
  - Points system for waste deposits
  - Rewards marketplace with partner coupons
  - Personal environmental impact statistics
  - Gamification elements (levels, achievements)
- **Admin Dashboard**:
  - Real-time monitoring of bins
  - User management
  - Rewards management
  - Analytics and reporting
  - Simulation tools
- **Reward System**:
  - Points based on waste type and quantity
  - Partner collaboration for coupons and discounts
  - Loyalty program

## 📋 Project Structure

```
smart-dustbin-ecosystem/
├── android/                  # Android mobile application
├── server/                   # Backend server with APIs
├── admin-panel/             # Web-based admin dashboard
└── docs/                    # Documentation
```

## 🛠️ Technology Stack

- **Mobile App**: Android (Kotlin)
- **Backend**: Node.js, Express.js
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Admin Dashboard**: HTML, CSS, JavaScript, Bootstrap
- **IoT Simulation**: Node.js, Express.js

## 🚀 Getting Started

### Prerequisites

- Android Studio 4.0+
- Node.js 14.0+
- npm 6.0+
- Firebase account
- Git

### Initial Setup

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/smart-dustbin-ecosystem.git
cd smart-dustbin-ecosystem
```

2. **Set up Firebase**

- Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
- Enable Firestore, Authentication, and Storage
- Set up Authentication methods (Email/Password)
- Create the following Firestore collections:
  - `users`
  - `smart_bins`
  - `waste_deposits`
  - `partner_offers`
  - `rewards`
- Download the `google-services.json` for Android and the Admin SDK service account key

3. **Set up environment variables**

Create a `.env` file in the server directory:

```
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PRIVATE_KEY=your-private-key
FIREBASE_CLIENT_EMAIL=your-client-email
API_KEY=your-api-key
PORT=5000
```

### Backend Server Setup

1. **Install server dependencies**

```bash
cd server
npm install
```

2. **Place your Firebase service account key**

Save your Firebase Admin SDK service account key as `serviceAccountKey.json` in the server directory.

3. **Start the server**

```bash
npm start
```

The server will run on http://localhost:5000

### Android App Setup

1. **Open the project in Android Studio**

```bash
cd android
```

Open the project in Android Studio.

2. **Add Firebase configuration**

- Place the `google-services.json` file in the app module directory
- Make sure to update the `app/build.gradle` with your package name

3. **Update API endpoint**

In `Constants.kt`, update the `BASE_API_URL` with your server URL:

```kotlin
const val BASE_API_URL = "http://your-server-address:5000"
```

4. **Build and run the app**

Build the app and run it on an emulator or a physical device.

### Admin Panel Setup

1. **Configure Firebase in admin panel**

Update the Firebase configuration in `admin-panel/js/firebase-config.js`:

```javascript
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "your-project-id.firebaseapp.com",
    projectId: "your-project-id",
    storageBucket: "your-project-id.appspot.com",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID"
};
```

2. **Serve the admin panel**

You can use a simple HTTP server like `http-server` to serve the admin panel:

```bash
cd admin-panel
npx http-server
```

Access the admin panel at http://localhost:8080

## 💻 Hackathon Development Workflow

### For a team of 4, here's a suggested division of work:

1. **Backend Developer**
   - Set up Firebase project
   - Implement server APIs
   - Create database schema
   - Test API endpoints

2. **Android Developer**
   - Implement user authentication
   - Create QR code scanning
   - Build user interface
   - Implement rewards redemption

3. **IoT Specialist / Full-stack Developer**
   - Create hardware simulation
   - Implement bin-app communication
   - Design bin management system
   - Test communication between all components

4. **UI/UX Designer / Front-end Developer**
   - Design mobile app interfaces
   - Create admin dashboard
   - Implement animations and visual elements
   - Ensure consistent design language

### Development Tips

1. **Use Git branches effectively**:
   - `main`: Stable version
   - `dev`: Development branch
   - Feature branches for specific components

2. **Hardware Simulation**:
   - If physical hardware is not available, use the simulation endpoints
   - Test with the admin panel's simulation tools

3. **Testing QR Codes**:
   - Generate QR codes using the admin panel
   - Test scanning with the mobile app

## 🗃️ Database Schema

### Users Collection
```
{
  "id": string,
  "name": string,
  "email": string,
  "phone": string,
  "total_points": number,
  "used_points": number,
  "rank": string,
  "created_at": timestamp
}
```

### Smart Bins Collection
```
{
  "id": string,
  "location": string,
  "current_fill_level": number,
  "status": string,
  "last_emptied": timestamp,
  "latitude": number,
  "longitude": number
}
```

### Waste Deposits Collection
```
{
  "id": string,
  "user_id": string,
  "bin_id": string,
  "waste_type": string,
  "weight": number,
  "points_earned": number,
  "timestamp": timestamp
}
```

### Partner Offers Collection
```
{
  "id": string,
  "title": string,
  "description": string,
  "imageUrl": string,
  "partnerName": string,
  "partnerLogoUrl": string,
  "category": string,
  "pointsRequired": number,
  "termsAndConditions": string,
  "howToRedeem": string,
  "featured": boolean,
  "startDate": timestamp,
  "endDate": timestamp
}
```

### Rewards Collection
```
{
  "id": string,
  "user_id": string,
  "coupon_id": string,
  "partner_id": string,
  "coupon_code": string,
  "points_redeemed": number,
  "created_at": timestamp,
  "expiry_date": timestamp,
  "is_redeemed": boolean
}
```

## 🔄 API Endpoints

### Authentication Endpoints
- `POST /auth/register` - Register a new user
- `POST /auth/login` - User login
- `GET /auth/profile` - Get user profile
- `PUT /auth/profile` - Update user profile
- `GET /auth/rewards` - Get user rewards
- `POST /auth/redeem` - Redeem a coupon
- `GET /auth/history` - Get user deposit history

### Bin Management Endpoints
- `GET /api/bins` - Get all bins
- `GET /api/bins/:id` - Get bin details
- `POST /api/simulate/deposit` - Simulate waste deposit
- `POST /api/simulate/empty-bin` - Reset bin fill level
- `GET /api/bins/:id/qr-data` - Generate QR code data for bin
- `GET /api/users/:id/stats` - Get user statistics

### Admin Endpoints
- `GET /admin/dashboard/stats` - Get dashboard statistics
- `GET /admin/users` - Get all users
- `POST /admin/bins` - Add new smart bin
- `PUT /admin/bins/:id` - Update smart bin
- `DELETE /admin/bins/:id` - Delete smart bin
- `POST /admin/partner-offers` - Add new partner offer
- `GET /admin/deposits` - Get all deposit history

## 📱 App Screens

1. **Splash Screen**: App introduction with animations
2. **Login/Register**: User authentication
3. **Home Screen**: Dashboard with points, level, and recent activity
4. **QR Scanner**: For scanning bins and depositing waste
5. **Rewards Marketplace**: Browse and redeem rewards
6. **Impact Stats**: Environmental impact visualizations
7. **Profile**: User profile and settings

## 🔮 Future Enhancements

1. **Real IoT Hardware Integration**:
   - Connect actual sensors to bins
   - Implement real-time communication

2. **Machine Learning for Waste Classification**:
   - Automatic waste type detection using computer vision
   - Improve accuracy of waste categorization

3. **Community Features**:
   - Leaderboards
   - Challenges and group targets
   - Social sharing

4. **Expanded Partner Network**:
   - More brands and rewards
   - Tiered partnership program

5. **Analytics and Reporting**:
   - Advanced analytics for waste management
   - Predictive fill levels and collection routing

## 🤝 Contributing

This is a hackathon project, but contributions are welcome. Please fork the repository and submit a pull request.

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgements

- [Firebase](https://firebase.google.com/) for backend services
- [Material Design](https://material.io/design) for design guidelines
- [Bootstrap](https://getbootstrap.com/) for admin dashboard UI
- [Chart.js](https://www.chartjs.org/) for data visualization
- [Express.js](https://expressjs.com/) for API development