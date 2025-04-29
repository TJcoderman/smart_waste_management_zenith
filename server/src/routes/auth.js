// Authentication routes for Smart Dustbin Ecosystem
const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

// Verify user token middleware
const verifyToken = async (req, res, next) => {
  try {
    const idToken = req.headers.authorization?.split('Bearer ')[1];
    if (!idToken) {
      return res.status(401).json({ error: 'No token provided' });
    }
    
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Error verifying token:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
};

// Get current user profile
router.get('/profile', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const userDoc = await db.collection('users').doc(userId).get();
    
    if (!userDoc.exists) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const userData = userDoc.data();
    
    // Remove sensitive data
    delete userData.password;
    
    res.status(200).json({
      id: userId,
      ...userData
    });
  } catch (error) {
    console.error('Error getting user profile:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update user profile
router.put('/profile', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const { name, phone } = req.body;
    
    const updates = {};
    if (name) updates.name = name;
    if (phone) updates.phone = phone;
    
    await db.collection('users').doc(userId).update(updates);
    
    res.status(200).json({
      success: true,
      message: 'Profile updated successfully'
    });
  } catch (error) {
    console.error('Error updating user profile:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user rewards
router.get('/rewards', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    
    const rewardsSnapshot = await db.collection('rewards')
      .where('user_id', '==', userId)
      .orderBy('created_at', 'desc')
      .get();
    
    const rewards = [];
    
    rewardsSnapshot.forEach(doc => {
      rewards.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    res.status(200).json(rewards);
  } catch (error) {
    console.error('Error getting user rewards:', error);
    res.status(500).json({ error: error.message });
  }
});

// Redeem a coupon
router.post('/redeem', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const { couponId } = req.body;
    
    if (!couponId) {
      return res.status(400).json({ error: 'Coupon ID is required' });
    }
    
    // Get coupon details
    const couponDoc = await db.collection('partner_offers').doc(couponId).get();
    
    if (!couponDoc.exists) {
      return res.status(404).json({ error: 'Coupon not found' });
    }
    
    const coupon = couponDoc.data();
    
    // Check if points required
    const pointsRequired = coupon.pointsRequired || 0;
    
    // Get user's points
    const userDoc = await db.collection('users').doc(userId).get();
    
    if (!userDoc.exists) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const user = userDoc.data();
    const totalPoints = user.total_points || 0;
    const usedPoints = user.used_points || 0;
    const availablePoints = totalPoints - usedPoints;
    
    if (availablePoints < pointsRequired) {
      return res.status(400).json({ 
        error: 'Not enough points', 
        available: availablePoints,
        required: pointsRequired
      });
    }
    
    // Generate coupon code
    const couponCode = generateCouponCode(coupon.partnerName);
    
    // Calculate expiry date (30 days from now)
    const expiryDate = new Date();
    expiryDate.setDate(expiryDate.getDate() + 30);
    
    // Create reward document
    const rewardData = {
      user_id: userId,
      coupon_id: couponId,
      partner_id: couponId.split('_')[0] || couponId,
      coupon_code: couponCode,
      points_redeemed: pointsRequired,
      created_at: admin.firestore.FieldValue.serverTimestamp(),
      expiry_date: admin.firestore.Timestamp.fromDate(expiryDate),
      is_redeemed: false
    };
    
    const rewardRef = await db.collection('rewards').add(rewardData);
    
    // Update user's used points
    await db.collection('users').doc(userId).update({
      used_points: admin.firestore.FieldValue.increment(pointsRequired)
    });
    
    res.status(200).json({
      success: true,
      reward_id: rewardRef.id,
      coupon_code: couponCode,
      points_redeemed: pointsRequired,
      expiry_date: expiryDate
    });
  } catch (error) {
    console.error('Error redeeming coupon:', error);
    res.status(500).json({ error: error.message });
  }
});

// Generate a random coupon code
function generateCouponCode(partnerName) {
  const prefix = partnerName.substring(0, 2).toUpperCase();
  const randomPart = Math.random().toString(36).substring(2, 10).toUpperCase();
  return `${prefix}-${randomPart}`;
}

// Get user deposit history
router.get('/history', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    
    const depositsSnapshot = await db.collection('waste_deposits')
      .where('user_id', '==', userId)
      .orderBy('timestamp', 'desc')
      .limit(limit)
      .get();
    
    const deposits = [];
    
    depositsSnapshot.forEach(doc => {
      deposits.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    res.status(200).json(deposits);
  } catch (error) {
    console.error('Error getting user history:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;