# Quick Start Guide for Smart Dustbin Ecosystem

This guide provides the fastest way to get the project up and running for the hackathon. Follow these steps to quickly set up your development environment.

## Team Setup (4 Members)

### Team Member Roles

1. **Backend Developer**
   - Focus: Server APIs, Firebase setup, Database
   
2. **Android Developer**
   - Focus: Mobile app, UI implementation, QR scanning

3. **Full-stack Developer**
   - Focus: Hardware simulation, Integration, Testing

4. **UI/UX Designer**
   - Focus: App design, Admin panel, Animations

## 10-Minute Setup

### 1. Firebase Setup (Backend Developer)

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication with Email/Password method
3. Create Firestore Database in test mode
4. Download `google-services.json` for Android
5. Generate a Service Account Key for the server:
   - Go to Project Settings > Service Accounts
   - Click "Generate new private key"
   - Save as `serviceAccountKey.json`

### 2. Server Setup (Backend Developer)

1. Navigate to the server directory
2. Install dependencies:
   ```bash
   npm install
   ```
3. Copy `serviceAccountKey.json` to the server directory
4. Create a `.env` file with:
   ```
   API_KEY=any-random-string-for-hackathon
   PORT=5000
   ```
5. Start the server:
   ```bash
   node index.js
   ```
6. Test the server is running at: http://localhost:5000

### 3. Android Setup (Android Developer)

1. Open Android Studio and import the android project
2. Copy the `google-services.json` to the `app/` directory
3. Update the API URL in `Constants.kt`:
   ```kotlin
   const val BASE_API_URL = "http://10.0.2.2:5000" // For emulator
   // or
   const val BASE_API_URL = "http://your-computer-ip:5000" // For physical device
   ```
4. Build and run the project on an emulator or device

### 4. Admin Panel Setup (Full-stack Developer)

1. Navigate to the admin-panel directory
2. Update Firebase config in `js/firebase-config.js` with your Firebase project details
3. Serve the admin panel:
   ```bash
   # Using Python simple HTTP server
   python -m http.server 8080
   # Or using Node.js http-server
   npx http-server -p 8080
   ```
4. Access the admin panel at: http://localhost:8080

### 5. Testing the System (All Team Members)

1. **Create a Test Bin** (via Admin Panel)
   - Go to Smart Bins page
   - Click "Add New Bin"
   - Fill in the details and save

2. **Create a Test User** (via Mobile App)
   - Launch the app
   - Register a new account

3. **Generate a QR Code** (via Admin Panel)
   - Go to the Simulation page
   - Fill in the QR Generation form
   - Download the QR code

4. **Scan QR Code** (via Mobile App)
   - Log in to the app
   - Tap the scan button
   - Scan the QR code
   - Verify the points are awarded

5. **Create a Reward** (via Admin Panel)
   - Go to Rewards page
   - Add a new reward
   - Set point requirements

6. **Redeem a Reward** (via Mobile App)
   - Go to Rewards page in the app
   - Select the reward
   - Redeem it

## No-Hardware Simulation Guide

Since this is a hackathon project without physical hardware, use these simulation techniques:

### 1. Simulate Waste Deposit

#### Option 1: Admin Panel
1. Go to the Simulation page in the admin panel
2. Fill out the simulation form (user, bin, waste type, weight)
3. Click "Simulate Deposit"

#### Option 2: API Call
Make a POST request to `/api/simulate/deposit` with:
```json
{
  "userId": "user123",
  "binId": "bin123",
  "wasteType": "recyclable_plastic",
  "weight": 1.5
}
```
Include the header: `x-api-key: your-api-key`

### 2. QR Code Generation

1. In the admin panel, use the QR Code Generator to create bin-specific QR codes
2. Each QR code contains JSON with:
   ```json
   {
     "bin_id": "bin123",
     "waste_type": "recyclable_plastic",
     "weight": 1.5
   }
   ```
3. Use these QR codes for testing the mobile app

## Common Issues & Solutions

1. **App can't connect to the server**
   - Check that the IP address in `Constants.kt` is correct
   - Ensure the server is running
   - Check network settings if using a physical device

2. **Firebase Authentication fails**
   - Verify that Email/Password auth is enabled in Firebase console
   - Check that `google-services.json` is up to date

3. **QR code scanning doesn't work**
   - Ensure camera permissions are granted
   - Check that the QR code format matches what the app expects

4. **Points not updating**
   - Check the server logs for errors
   - Verify Firestore database permissions

## Demo Preparation

For your hackathon demo, prepare:

1. **Demo Flow**
   - Create a step-by-step demo script
   - Prepare test accounts and data in advance
   - Clear app data between test runs if needed

2. **Presentation Materials**
   - Screenshots of all app screens
   - Admin dashboard views
   - Database schema diagram
   - Architecture diagram

3. **Backup Plan**
   - Have pre-recorded demo video ready
   - Prepare screenshots of each step in case of technical issues

## Communication

Use Slack or Discord for team communication during development:
- Create separate channels for:
  - #general
  - #android-dev
  - #backend-dev
  - #ui-design
  - #testing

## Git Workflow

1. Create feature branches for each component:
   - `feature/login-screen`
   - `feature/qr-scanning`
   - `feature/reward-system`

2. Commit often with clear messages:
   ```
   feat: Add QR code scanning
   fix: Resolve points calculation issue
   style: Update home screen layout
   ```

3. Merge to `dev` branch for testing before merging to `main`

## Time-Saving Tips

1. **Use Mock Data**: Create realistic test data upfront
2. **Focus on Core Features**: Leave nice-to-have features for later
3. **Test Early**: Integrate components and test as you go
4. **UI Shortcuts**: Use built-in Material Design components
5. **Regular Sync-ups**: Brief 15-minute team meetings twice a day

Good luck with your hackathon! 🚀