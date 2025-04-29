// Firebase Configuration for Smart Dustbin Ecosystem

// Firebase configuration (replace with your own config from Firebase console)
const firebaseConfig = {
    apiKey: "AIzaSyAm8HC2wh_hKr5hj2Ff62kzm5TCXAVnJ9g",
    authDomain: "smartdustbin-a6531.firebaseapp.com",
    projectId: "smartdustbin-a6531",
    storageBucket: "smartdustbin-a6531.appspot.com",
    messagingSenderId: "1036715076598",
    appId: "1:1036715076598:android:b41c173969d361ed22f5d7"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

console.log('Firebase initialized');

// Firebase Authentication function for admin login
function adminLogin(email, password) {
    return firebase.auth().signInWithEmailAndPassword(email, password)
        .then((userCredential) => {
            // Check if user has admin role
            return firebase.firestore().collection('users')
                .doc(userCredential.user.uid)
                .get()
                .then((doc) => {
                    if (doc.exists && doc.data().role === 'admin') {
                        return userCredential.user;
                    } else {
                        // Not an admin, sign out
                        return firebase.auth().signOut().then(() => {
                            throw new Error('Access denied. Admin privileges required.');
                        });
                    }
                });
        });
}

// Admin authentication state observer
function checkAdminAuth(onAuthenticated, onUnauthenticated) {
    firebase.auth().onAuthStateChanged((user) => {
        if (user) {
            // User is signed in, check if admin
            firebase.firestore().collection('users')
                .doc(user.uid)
                .get()
                .then((doc) => {
                    if (doc.exists && doc.data().role === 'admin') {
                        onAuthenticated(user);
                    } else {
                        // Not an admin, sign out
                        firebase.auth().signOut().then(() => {
                            onUnauthenticated();
                        });
                    }
                })
                .catch((error) => {
                    console.error('Error checking admin status:', error);
                    onUnauthenticated();
                });
        } else {
            // User is signed out
            onUnauthenticated();
        }
    });
}

// Firebase Firestore helper functions

// Get all bins
function getAllBins() {
    return firebase.firestore().collection('smart_bins')
        .get()
        .then((querySnapshot) => {
            const bins = [];
            querySnapshot.forEach((doc) => {
                bins.push({
                    id: doc.id,
                    ...doc.data()
                });
            });
            return bins;
        });
}

// Update bin status and fill level
function updateBin(binId, updates) {
    return firebase.firestore().collection('smart_bins')
        .doc(binId)
        .update(updates);
}

// Get all users
function getAllUsers() {
    return firebase.firestore().collection('users')
        .get()
        .then((querySnapshot) => {
            const users = [];
            querySnapshot.forEach((doc) => {
                const userData = doc.data();
                // Remove sensitive data
                delete userData.password;
                
                users.push({
                    id: doc.id,
                    ...userData
                });
            });
            return users;
        });
}

// Get user details
function getUserDetails(userId) {
    return firebase.firestore().collection('users')
        .doc(userId)
        .get()
        .then((doc) => {
            if (doc.exists) {
                const userData = doc.data();
                // Remove sensitive data
                delete userData.password;
                
                return {
                    id: doc.id,
                    ...userData
                };
            } else {
                throw new Error('User not found');
            }
        });
}

// Get all partner offers
function getAllPartnerOffers() {
    return firebase.firestore().collection('partner_offers')
        .get()
        .then((querySnapshot) => {
            const offers = [];
            querySnapshot.forEach((doc) => {
                offers.push({
                    id: doc.id,
                    ...doc.data()
                });
            });
            return offers;
        });
}

// Add new partner offer
function addPartnerOffer(offerData) {
    return firebase.firestore().collection('partner_offers')
        .add(offerData)
        .then((docRef) => {
            return {
                id: docRef.id,
                ...offerData
            };
        });
}

// Get all redeemed rewards
function getAllRedeemedRewards() {
    return firebase.firestore().collection('rewards')
        .get()
        .then((querySnapshot) => {
            const rewards = [];
            querySnapshot.forEach((doc) => {
                rewards.push({
                    id: doc.id,
                    ...doc.data()
                });
            });
            return rewards;
        });
}

// Get all waste deposits with pagination
function getWasteDeposits(limit = 20, startAfter = null) {
    let query = firebase.firestore().collection('waste_deposits')
        .orderBy('timestamp', 'desc')
        .limit(limit);
    
    if (startAfter) {
        query = query.startAfter(startAfter);
    }
    
    return query.get()
        .then((querySnapshot) => {
            const deposits = [];
            querySnapshot.forEach((doc) => {
                deposits.push({
                    id: doc.id,
                    ...doc.data()
                });
            });
            
            const lastVisible = querySnapshot.docs[querySnapshot.docs.length - 1];
            
            return {
                deposits,
                lastVisible
            };
        });
}

// Simulate a waste deposit
function simulateWasteDeposit(data) {
    return fetch('https://your-api-endpoint.com/api/simulate/deposit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'x-api-key': 'YOUR_API_KEY'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    });
}

// Get dashboard statistics
function getDashboardStats() {
    return firebase.firestore().collection('statistics')
        .doc('dashboard')
        .get()
        .then((doc) => {
            if (doc.exists) {
                return doc.data();
            } else {
                // If stats document doesn't exist, calculate them
                return calculateDashboardStats();
            }
        });
}

// Calculate dashboard statistics from database
function calculateDashboardStats() {
    // Promise.all to fetch all required data
    return Promise.all([
        firebase.firestore().collection('users').get(),
        firebase.firestore().collection('smart_bins').get(),
        firebase.firestore().collection('waste_deposits').get(),
        firebase.firestore().collection('waste_deposits')
            .where('timestamp', '>=', firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)))
            .get()
    ]).then(([usersSnapshot, binsSnapshot, depositsSnapshot, recentDepositsSnapshot]) => {
        // Calculate stats
        const totalUsers = usersSnapshot.size;
        const totalBins = binsSnapshot.size;
        
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
        
        const totalDeposits = depositsSnapshot.size;
        const recentDeposits = recentDepositsSnapshot.size;
        
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
        
        // Construct stats object
        const stats = {
            totalUsers,
            userGrowth: calculateUserGrowth(usersSnapshot.docs),
            totalBins,
            activeBins,
            fullBins,
            maintenanceBins,
            totalDeposits,
            recentDeposits,
            totalWaste: totalOrganic + totalRecyclable,
            organicWaste: totalOrganic,
            recyclableWaste: totalRecyclable,
            lastUpdated: firebase.firestore.FieldValue.serverTimestamp()
        };
        
        // Save stats to Firestore for caching
        firebase.firestore().collection('statistics')
            .doc('dashboard')
            .set(stats)
            .catch(error => console.error('Error saving stats:', error));
        
        return stats;
    });
}

// Calculate user growth percentage (last 30 days)
function calculateUserGrowth(users) {
    const now = new Date();
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    const sixtyDaysAgo = new Date(now.getTime() - 60 * 24 * 60 * 60 * 1000);
    
    const usersLast30Days = users.filter(user => {
        const createdAt = user.data().created_at?.toDate();
        return createdAt && createdAt >= thirtyDaysAgo;
    }).length;
    
    const usersPrevious30Days = users.filter(user => {
        const createdAt = user.data().created_at?.toDate();
        return createdAt && createdAt >= sixtyDaysAgo && createdAt < thirtyDaysAgo;
    }).length;
    
    if (usersPrevious30Days === 0) {
        return usersLast30Days > 0 ? 100 : 0;
    }
    
    return Math.round(((usersLast30Days - usersPrevious30Days) / usersPrevious30Days) * 100);
}

// Export functions for use in other files
window.firebaseAuth = {
    adminLogin,
    checkAdminAuth
};

window.firebaseFirestore = {
    getAllBins,
    updateBin,
    getAllUsers,
    getUserDetails,
    getAllPartnerOffers,
    addPartnerOffer,
    getAllRedeemedRewards,
    getWasteDeposits,
    simulateWasteDeposit,
    getDashboardStats
};