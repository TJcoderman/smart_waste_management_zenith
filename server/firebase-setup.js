/**
 * Firebase Firestore Setup Script for Smart Dustbin Ecosystem
 * 
 * This script initializes the database with required collections and sample data.
 * Run this script with Node.js after setting up your Firebase project:
 * 
 * 1. Create a Firebase project at https://console.firebase.google.com/
 * 2. Enable Firestore Database
 * 3. Generate a service account key and save as serviceAccountKey.json
 * 4. Run this script: node firebase-setup.js
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Check if serviceAccountKey.json exists
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
if (!fs.existsSync(serviceAccountPath)) {
  console.error('Error: serviceAccountKey.json not found!');
  console.error('Please download your Firebase service account key and save it as serviceAccountKey.json');
  console.error('https://console.firebase.google.com/ -> Project Settings -> Service Accounts -> Generate New Private Key');
  process.exit(1);
}

// Initialize Firebase Admin
const serviceAccount = require(serviceAccountPath);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

console.log('Connected to Firebase project:', serviceAccount.project_id);
console.log('Initializing database collections...');

// Helper function to add collection data
async function setupCollection(collectionName, data) {
  console.log(`Setting up ${collectionName} collection...`);
  
  const batch = db.batch();
  const collectionRef = db.collection(collectionName);
  
  for (const item of data) {
    const docRef = collectionRef.doc(item.id || collectionRef.doc().id);
    batch.set(docRef, item);
  }
  
  await batch.commit();
  console.log(`✅ ${data.length} ${collectionName} documents created`);
}

// Generate dates relative to now
function getDate(daysOffset) {
  const date = new Date();
  date.setDate(date.getDate() + daysOffset);
  return admin.firestore.Timestamp.fromDate(date);
}

// Sample data generation
async function initializeDatabase() {
  try {
    // Create users collection
    const users = [
      {
        id: 'user001',
        name: 'Rahul Sharma',
        email: 'rahul.sharma@example.com',
        phone: '+91 9876543210',
        total_points: 450,
        used_points: 200,
        rank: 'Eco Warrior',
        created_at: getDate(-30) // Joined 30 days ago
      },
      {
        id: 'user002',
        name: 'Priya Patel',
        email: 'priya.patel@example.com',
        phone: '+91 8765432109',
        total_points: 780,
        used_points: 300,
        rank: 'Eco Warrior',
        created_at: getDate(-45) // Joined 45 days ago
      },
      {
        id: 'user003',
        name: 'Amit Kumar',
        email: 'amit.kumar@example.com',
        phone: '+91 7654321098',
        total_points: 120,
        used_points: 0,
        rank: 'Eco Rookie',
        created_at: getDate(-10) // Joined 10 days ago
      },
      {
        id: 'user004',
        name: 'Neha Singh',
        email: 'neha.singh@example.com',
        phone: '+91 6543210987',
        total_points: 50,
        used_points: 0,
        rank: 'Novice Recycler',
        created_at: getDate(-5) // Joined 5 days ago
      },
      {
        id: 'admin001',
        name: 'Admin User',
        email: 'admin@smartdustbin.com',
        phone: '+91 9999999999',
        total_points: 0,
        used_points: 0,
        rank: 'Administrator',
        role: 'admin', // Special admin role
        created_at: getDate(-60) // Joined 60 days ago
      }
    ];
    
    // Create smart_bins collection
    const smartBins = [
      {
        id: 'bin001',
        location: 'MG Road',
        current_fill_level: 75,
        status: 'active',
        last_emptied: getDate(-2), // Last emptied 2 days ago
        latitude: 12.9716,
        longitude: 77.5946
      },
      {
        id: 'bin002',
        location: 'Indiranagar',
        current_fill_level: 30,
        status: 'active',
        last_emptied: getDate(-1), // Last emptied 1 day ago
        latitude: 12.9784,
        longitude: 77.6408
      },
      {
        id: 'bin003',
        location: 'Koramangala',
        current_fill_level: 95,
        status: 'full',
        last_emptied: getDate(-3), // Last emptied 3 days ago
        latitude: 12.9279,
        longitude: 77.6271
      },
      {
        id: 'bin004',
        location: 'HSR Layout',
        current_fill_level: 10,
        status: 'active',
        last_emptied: getDate(0), // Last emptied today
        latitude: 12.9116,
        longitude: 77.6741
      },
      {
        id: 'bin005',
        location: 'Whitefield',
        current_fill_level: 50,
        status: 'active',
        last_emptied: getDate(-2), // Last emptied 2 days ago
        latitude: 12.9698,
        longitude: 77.7499
      }
    ];
    
    // Create waste_deposits collection
    const wasteDeposits = [
      {
        user_id: 'user001',
        bin_id: 'bin001',
        waste_type: 'organic',
        weight: 1.2,
        points_earned: 6,
        timestamp: getDate(-2)
      },
      {
        user_id: 'user001',
        bin_id: 'bin002',
        waste_type: 'recyclable_plastic',
        weight: 0.8,
        points_earned: 8,
        timestamp: getDate(-1)
      },
      {
        user_id: 'user002',
        bin_id: 'bin003',
        waste_type: 'recyclable_paper',
        weight: 1.5,
        points_earned: 12,
        timestamp: getDate(-3)
      },
      {
        user_id: 'user002',
        bin_id: 'bin001',
        waste_type: 'recyclable_metal',
        weight: 0.7,
        points_earned: 10,
        timestamp: getDate(-2)
      },
      {
        user_id: 'user003',
        bin_id: 'bin004',
        waste_type: 'organic',
        weight: 2.0,
        points_earned: 10,
        timestamp: getDate(0)
      },
      {
        user_id: 'user004',
        bin_id: 'bin005',
        waste_type: 'recyclable_plastic',
        weight: 0.5,
        points_earned: 5,
        timestamp: getDate(0)
      }
    ];
    
    // Create partner_offers collection
    const partnerOffers = [
      {
        id: 'offer001',
        title: '20% Off at Starbucks',
        description: 'Get 20% off on any beverage at Starbucks. Valid at all outlets in Bangalore.',
        imageUrl: 'https://example.com/images/starbucks.jpg',
        partnerName: 'Starbucks',
        partnerLogoUrl: 'https://example.com/logos/starbucks.png',
        category: 'food_drinks',
        pointsRequired: 200,
        termsAndConditions: 'Valid for one-time use. Cannot be combined with other offers. Valid until expiry date.',
        howToRedeem: 'Show the coupon code to the cashier at the time of payment.',
        featured: true,
        startDate: getDate(-30),
        endDate: getDate(30)
      },
      {
        id: 'offer002',
        title: '₹500 Off on Amazon',
        description: 'Get ₹500 off on purchases above ₹2000 on Amazon India.',
        imageUrl: 'https://example.com/images/amazon.jpg',
        partnerName: 'Amazon',
        partnerLogoUrl: 'https://example.com/logos/amazon.png',
        category: 'shopping',
        pointsRequired: 500,
        termsAndConditions: 'Valid for one-time use on Amazon India website or app. Excludes electronics.',
        howToRedeem: 'Enter the coupon code at checkout.',
        featured: false,
        startDate: getDate(-15),
        endDate: getDate(45)
      },
      {
        id: 'offer003',
        title: 'Buy 1 Get 1 Movie Ticket',
        description: 'Buy one movie ticket and get another one free at any PVR Cinema.',
        imageUrl: 'https://example.com/images/pvr.jpg',
        partnerName: 'PVR Cinemas',
        partnerLogoUrl: 'https://example.com/logos/pvr.png',
        category: 'entertainment',
        pointsRequired: 300,
        termsAndConditions: 'Valid for all movies except premieres and special screenings. Valid on weekdays only.',
        howToRedeem: 'Show the coupon code at the ticket counter.',
        featured: true,
        startDate: getDate(-10),
        endDate: getDate(60)
      },
      {
        id: 'offer004',
        title: '30% Off at Pizza Hut',
        description: 'Get 30% off on any medium or large pizza at Pizza Hut.',
        imageUrl: 'https://example.com/images/pizzahut.jpg',
        partnerName: 'Pizza Hut',
        partnerLogoUrl: 'https://example.com/logos/pizzahut.png',
        category: 'food_drinks',
        pointsRequired: 250,
        termsAndConditions: 'Valid for dine-in and takeaway. Not valid on delivery or combo offers.',
        howToRedeem: 'Show the coupon code while ordering.',
        featured: false,
        startDate: getDate(-5),
        endDate: getDate(25)
      },
      {
        id: 'offer005',
        title: '₹200 Off on Myntra',
        description: 'Get ₹200 off on fashion purchases above ₹1000 on Myntra.',
        imageUrl: 'https://example.com/images/myntra.jpg',
        partnerName: 'Myntra',
        partnerLogoUrl: 'https://example.com/logos/myntra.png',
        category: 'shopping',
        pointsRequired: 150,
        termsAndConditions: 'Valid on all products except discounted items.',
        howToRedeem: 'Enter the coupon code at checkout.',
        featured: true,
        startDate: getDate(-20),
        endDate: getDate(40)
      }
    ];
    
    // Create rewards collection
    const rewards = [
      {
        user_id: 'user001',
        coupon_id: 'offer001',
        partner_id: 'starbucks',
        coupon_code: 'SB-XYZ123',
        points_redeemed: 200,
        created_at: getDate(-5),
        expiry_date: getDate(25),
        is_redeemed: false
      },
      {
        user_id: 'user002',
        coupon_id: 'offer003',
        partner_id: 'pvr',
        coupon_code: 'PVR-ABC456',
        points_redeemed: 300,
        created_at: getDate(-10),
        expiry_date: getDate(20),
        is_redeemed: true
      }
    ];
    
    // Initialize collections
    await setupCollection('users', users);
    await setupCollection('smart_bins', smartBins);
    await setupCollection('waste_deposits', wasteDeposits);
    await setupCollection('partner_offers', partnerOffers);
    await setupCollection('rewards', rewards);
    
    console.log('\n✅ Database initialization complete!');
    console.log('\nSample users created:');
    console.log('- Regular User: rahul.sharma@example.com');
    console.log('- Admin User: admin@smartdustbin.com');
    console.log('\nYou can now use these users for testing the application.');
    
    // Exit the process
    process.exit(0);
    
  } catch (error) {
    console.error('❌ Error initializing database:', error);
    process.exit(1);
  }
}

// Run initialization
initializeDatabase(); 