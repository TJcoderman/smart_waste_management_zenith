// API routes for Smart Dustbin Ecosystem
const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

// Middleware to verify API key
const verifyApiKey = (req, res, next) => {
  const apiKey = req.headers['x-api-key'];
  if (!apiKey || apiKey !== process.env.API_KEY) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  next();
};

// Simulate waste deposit
router.post('/simulate/deposit', verifyApiKey, async (req, res) => {
  try {
    const { userId, binId, wasteType, weight } = req.body;
    
    if (!userId || !binId || !wasteType || !weight) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    
    // Calculate points based on waste type and weight
    let pointsPerKg = 0;
    if (wasteType === 'organic') {
      pointsPerKg = 5;
    } else if (wasteType === 'recyclable_plastic') {
      pointsPerKg = 10;
    } else if (wasteType === 'recyclable_paper') {
      pointsPerKg = 8;
    } else if (wasteType === 'recyclable_metal') {
      pointsPerKg = 15;
    } else {
      return res.status(400).json({ error: 'Invalid waste type' });
    }
    
    const pointsEarned = Math.round(weight * pointsPerKg);
    
    // Create waste deposit record
    const depositRef = db.collection('waste_deposits').doc();
    const timestamp = admin.firestore.FieldValue.serverTimestamp();
    
    await depositRef.set({
      user_id: userId,
      bin_id: binId,
      waste_type: wasteType,
      weight: weight,
      points_earned: pointsEarned,
      timestamp: timestamp
    });
    
    // Update user's total points
    const userRef = db.collection('users').doc(userId);
    await db.runTransaction(async (transaction) => {
      const userDoc = await transaction.get(userRef);
      if (!userDoc.exists) {
        throw new Error('User does not exist!');
      }
      
      const userData = userDoc.data();
      const newTotalPoints = (userData.total_points || 0) + pointsEarned;
      
      // Check if level changed
      const currentLevel = Math.floor((userData.total_points || 0) / 100) + 1;
      const newLevel = Math.floor(newTotalPoints / 100) + 1;
      
      const updates = { total_points: newTotalPoints };
      
      // Update rank if level changed
      if (newLevel > currentLevel) {
        let newRank = userData.rank;
        if (newLevel >= 10) {
          newRank = "Master Recycler";
        } else if (newLevel >= 5) {
          newRank = "Eco Warrior";
        } else if (newLevel >= 3) {
          newRank = "Green Guardian";
        } else {
          newRank = "Eco Rookie";
        }
        updates.rank = newRank;
      }
      
      transaction.update(userRef, updates);
    });
    
    // Update bin fill level
    const binRef = db.collection('smart_bins').doc(binId);
    await db.runTransaction(async (transaction) => {
      const binDoc = await transaction.get(binRef);
      if (!binDoc.exists) {
        throw new Error('Bin does not exist!');
      }
      
      const binData = binDoc.data();
      const newFillLevel = Math.min(100, (binData.current_fill_level || 0) + (weight * 2)); // 1kg = 2% fill level
      const updates = { current_fill_level: newFillLevel };
      
      // Update bin status if it's full
      if (newFillLevel >= 90 && binData.status !== 'full') {
        updates.status = 'full';
      }
      
      transaction.update(binRef, updates);
    });
    
    res.status(200).json({ 
      success: true, 
      deposit_id: depositRef.id,
      points_earned: pointsEarned
    });
  } catch (error) {
    console.error('Error simulating deposit:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all bins
router.get('/bins', verifyApiKey, async (req, res) => {
  try {
    const binsSnapshot = await db.collection('smart_bins').get();
    const bins = [];
    
    binsSnapshot.forEach(doc => {
      bins.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    res.status(200).json(bins);
  } catch (error) {
    console.error('Error getting bins:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get bin details
router.get('/bins/:id', verifyApiKey, async (req, res) => {
  try {
    const binId = req.params.id;
    const binDoc = await db.collection('smart_bins').doc(binId).get();
    
    if (!binDoc.exists) {
      return res.status(404).json({ error: 'Bin not found' });
    }
    
    res.status(200).json({
      id: binDoc.id,
      ...binDoc.data()
    });
  } catch (error) {
    console.error('Error getting bin details:', error);
    res.status(500).json({ error: error.message });
  }
});

// Reset bin fill level (simulate emptying)
router.post('/simulate/empty-bin', verifyApiKey, async (req, res) => {
  try {
    const { binId } = req.body;
    
    if (!binId) {
      return res.status(400).json({ error: 'Missing bin ID' });
    }
    
    const binRef = db.collection('smart_bins').doc(binId);
    const binDoc = await binRef.get();
    
    if (!binDoc.exists) {
      return res.status(404).json({ error: 'Bin not found' });
    }
    
    await binRef.update({
      current_fill_level: 0,
      status: 'active',
      last_emptied: admin.firestore.FieldValue.serverTimestamp()
    });
    
    res.status(200).json({ 
      success: true,
      message: 'Bin emptied successfully' 
    });
  } catch (error) {
    console.error('Error emptying bin:', error);
    res.status(500).json({ error: error.message });
  }
});

// Generate QR code data for bin
router.get('/bins/:id/qr-data', verifyApiKey, async (req, res) => {
  try {
    const binId = req.params.id;
    const wasteType = req.query.waste_type;
    const weight = parseFloat(req.query.weight || 1.0);
    
    if (!binId || !wasteType) {
      return res.status(400).json({ error: 'Missing required parameters' });
    }
    
    // Create QR code data
    const qrData = {
      bin_id: binId,
      waste_type: wasteType,
      weight: weight
    };
    
    res.status(200).json(qrData);
  } catch (error) {
    console.error('Error generating QR data:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user statistics
router.get('/users/:id/stats', verifyApiKey, async (req, res) => {
  try {
    const userId = req.params.id;
    
    // Get user deposits
    const depositsSnapshot = await db.collection('waste_deposits')
      .where('user_id', '==', userId)
      .get();
    
    if (depositsSnapshot.empty) {
      return res.status(200).json({
        total_deposits: 0,
        total_points: 0,
        organic_total: 0,
        recyclable_total: 0,
        co2_saved: 0,
        trees_saved: 0
      });
    }
    
    let organicTotal = 0;
    let plasticTotal = 0;
    let paperTotal = 0;
    let metalTotal = 0;
    let totalDeposits = 0;
    let totalPoints = 0;
    
    depositsSnapshot.forEach(doc => {
      const deposit = doc.data();
      totalDeposits++;
      totalPoints += deposit.points_earned || 0;
      
      switch (deposit.waste_type) {
        case 'organic':
          organicTotal += deposit.weight || 0;
          break;
        case 'recyclable_plastic':
          plasticTotal += deposit.weight || 0;
          break;
        case 'recyclable_paper':
          paperTotal += deposit.weight || 0;
          break;
        case 'recyclable_metal':
          metalTotal += deposit.weight || 0;
          break;
      }
    });
    
    // Calculate recyclable total
    const recyclableTotal = plasticTotal + paperTotal + metalTotal;
    
    // Calculate environmental impact
    const co2Saved = (
      plasticTotal * 2.5 + // 2.5 kg CO2 saved per kg of plastic
      paperTotal * 1.8 +   // 1.8 kg CO2 saved per kg of paper
      metalTotal * 4.5     // 4.5 kg CO2 saved per kg of metal
    );
    
    // Trees saved calculation (approximate: 80kg of paper per tree)
    const treesSaved = paperTotal / 80;
    
    res.status(200).json({
      total_deposits: totalDeposits,
      total_points: totalPoints,
      organic_total: organicTotal,
      plastic_total: plasticTotal,
      paper_total: paperTotal,
      metal_total: metalTotal,
      recyclable_total: recyclableTotal,
      co2_saved: co2Saved,
      trees_saved: treesSaved
    });
  } catch (error) {
    console.error('Error getting user stats:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;