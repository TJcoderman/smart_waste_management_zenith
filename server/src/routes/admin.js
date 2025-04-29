// Admin routes for Smart Dustbin Ecosystem
const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

// Middleware to verify admin credentials
const verifyAdmin = async (req, res, next) => {
  try {
    const idToken = req.headers.authorization?.split('Bearer ')[1];
    if (!idToken) {
      return res.status(401).json({ error: 'No token provided' });
    }
    
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    
    // Check if user has admin role (stored in custom claims)
    if (!decodedToken.admin) {
      const userDoc = await db.collection('users').doc(decodedToken.uid).get();
      if (!userDoc.exists || userDoc.data().role !== 'admin') {
        return res.status(403).json({ error: 'Access denied. Admin privileges required.' });
      }
    }
    
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Error verifying admin token:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
};

// Get admin dashboard stats
router.get('/dashboard/stats', verifyAdmin, async (req, res) => {
  try {
    // Get total users
    const usersSnapshot = await db.collection('users').get();
    const totalUsers = usersSnapshot.size;
    
    // Get total bins
    const binsSnapshot = await db.collection('smart_bins').get();
    const totalBins = binsSnapshot.size;
    
    // Count bins by status
    let activeBins = 0;
    let fullBins = 0;
    let maintenanceBins = 0;
    
    binsSnapshot.forEach(doc => {
      const bin = doc.data();
      switch (bin.status) {
        case 'active':
          activeBins++;
          break;
        case 'full':
          fullBins++;
          break;
        case 'maintenance':
          maintenanceBins++;
          break;
      }
    });
    
    // Get total deposits
    const depositsSnapshot = await db.collection('waste_deposits').get();
    const totalDeposits = depositsSnapshot.size;
    
    // Calculate waste stats
    let totalOrganic = 0;
    let totalRecyclable = 0;
    
    depositsSnapshot.forEach(doc => {
      const deposit = doc.data();
      if (deposit.waste_type === 'organic') {
        totalOrganic += deposit.weight || 0;
      } else {
        totalRecyclable += deposit.weight || 0;
      }
    });
    
    // Get recent deposits (last 7 days)
    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
    
    const recentDepositsSnapshot = await db.collection('waste_deposits')
      .where('timestamp', '>=', admin.firestore.Timestamp.fromDate(oneWeekAgo))
      .get();
    
    const recentDeposits = recentDepositsSnapshot.size;
    
    res.status(200).json({
      total_users: totalUsers,
      total_bins: totalBins,
      bins_status: {
        active: activeBins,
        full: fullBins,
        maintenance: maintenanceBins
      },
      total_deposits: totalDeposits,
      recent_deposits: recentDeposits,
      waste_stats: {
        total_organic: totalOrganic,
        total_recyclable: totalRecyclable,
        total_waste: totalOrganic + totalRecyclable
      }
    });
  } catch (error) {
    console.error('Error getting dashboard stats:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all users
router.get('/users', verifyAdmin, async (req, res) => {
  try {
    const usersSnapshot = await db.collection('users').get();
    const users = [];
    
    usersSnapshot.forEach(doc => {
      users.push({
        id: doc.id,
        ...doc.data(),
        password: undefined // Remove sensitive data
      });
    });
    
    res.status(200).json(users);
  } catch (error) {
    console.error('Error getting users:', error);
    res.status(500).json({ error: error.message });
  }
});

// Add new smart bin
router.post('/bins', verifyAdmin, async (req, res) => {
  try {
    const { location, latitude, longitude } = req.body;
    
    if (!location) {
      return res.status(400).json({ error: 'Location is required' });
    }
    
    const newBin = {
      location,
      latitude: latitude || 0,
      longitude: longitude || 0,
      current_fill_level: 0,
      status: 'active',
      last_emptied: admin.firestore.FieldValue.serverTimestamp(),
      created_at: admin.firestore.FieldValue.serverTimestamp()
    };
    
    const binRef = await db.collection('smart_bins').add(newBin);
    
    res.status(201).json({
      id: binRef.id,
      ...newBin
    });
  } catch (error) {
    console.error('Error adding bin:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update smart bin
router.put('/bins/:id', verifyAdmin, async (req, res) => {
  try {
    const binId = req.params.id;
    const { location, latitude, longitude, status } = req.body;
    
    const updates = {};
    if (location) updates.location = location;
    if (latitude !== undefined) updates.latitude = latitude;
    if (longitude !== undefined) updates.longitude = longitude;
    if (status) updates.status = status;
    
    await db.collection('smart_bins').doc(binId).update(updates);
    
    res.status(200).json({
      id: binId,
      ...updates
    });
  } catch (error) {
    console.error('Error updating bin:', error);
    res.status(500).json({ error: error.message });
  }
});

// Delete smart bin
router.delete('/bins/:id', verifyAdmin, async (req, res) => {
  try {
    const binId = req.params.id;
    
    await db.collection('smart_bins').doc(binId).delete();
    
    res.status(200).json({
      success: true,
      message: 'Bin deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting bin:', error);
    res.status(500).json({ error: error.message });
  }
});

// Add new partner offer (coupon)
router.post('/partner-offers', verifyAdmin, async (req, res) => {
  try {
    const { title, description, imageUrl, partnerName, partnerLogoUrl, category, pointsRequired, termsAndConditions, howToRedeem, featured } = req.body;
    
    if (!title || !partnerName || !category || !pointsRequired) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    
    // Set expiry date (3 months from now)
    const now = new Date();
    const expiryDate = new Date();
    expiryDate.setMonth(now.getMonth() + 3);
    
    const newOffer = {
      title,
      description: description || '',
      imageUrl: imageUrl || '',
      partnerName,
      partnerLogoUrl: partnerLogoUrl || '',
      category,
      pointsRequired,
      termsAndConditions: termsAndConditions || 'Terms and conditions apply',
      howToRedeem: howToRedeem || 'Show the coupon code to the cashier',
      featured: featured || false,
      startDate: admin.firestore.Timestamp.now(),
      endDate: admin.firestore.Timestamp.fromDate(expiryDate),
      created_at: admin.firestore.Timestamp.now()
    };
    
    const offerRef = await db.collection('partner_offers').add(newOffer);
    
    res.status(201).json({
      id: offerRef.id,
      ...newOffer
    });
  } catch (error) {
    console.error('Error adding partner offer:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all deposit history with pagination
router.get('/deposits', verifyAdmin, async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const startAfter = req.query.startAfter;
    
    let query = db.collection('waste_deposits')
      .orderBy('timestamp', 'desc')
      .limit(limit);
    
    if (startAfter) {
      const docSnapshot = await db.collection('waste_deposits').doc(startAfter).get();
      query = query.startAfter(docSnapshot);
    }
    
    const depositsSnapshot = await query.get();
    const deposits = [];
    
    depositsSnapshot.forEach(doc => {
      deposits.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    // Get the last document for pagination
    const lastVisible = depositsSnapshot.docs[depositsSnapshot.docs.length - 1];
    
    res.status(200).json({
      deposits,
      pagination: {
        page,
        limit,
        hasMore: deposits.length === limit,
        next: lastVisible ? lastVisible.id : null
      }
    });
  } catch (error) {
    console.error('Error getting deposits:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;