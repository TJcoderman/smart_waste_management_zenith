// Main JavaScript for Admin Panel
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Firebase
    initFirebase();
    
    // Check Authentication
    checkAuth();
    
    // Navigation
    setupNavigation();
    
    // Page-specific initialization
    initDashboard();
    initBinsPage();
    initUsersPage();
    initRewardsPage();
    initDepositsPage();
    initSimulationPage();
    initSettingsPage();
    
    // Global event handlers
    setupGlobalEvents();
});

// Firebase Authentication
function initFirebase() {
    // Firebase is initialized in firebase-config.js
    const auth = firebase.auth();
    const db = firebase.firestore();
}

function checkAuth() {
    firebase.auth().onAuthStateChanged(function(user) {
        if (!user) {
            // Not logged in, redirect to login page
            // window.location.href = 'login.html';
            console.log('Not logged in, but allowing access for demo purposes');
        } else {
            console.log('Logged in as:', user.email);
            // Update UI with user info
            document.querySelector('.dropdown-toggle').innerHTML = 
                `<i class="bi bi-person-circle me-1"></i> ${user.displayName || user.email}`;
        }
    });
}

function logout() {
    firebase.auth().signOut().then(() => {
        // Sign-out successful, redirect to login page
        // window.location.href = 'login.html';
        console.log('Logged out successfully');
    }).catch((error) => {
        console.error('Error signing out:', error);
    });
}

// Navigation Setup
function setupNavigation() {
    // Handle navigation links
    const navLinks = document.querySelectorAll('.nav-link[data-page]');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const pageId = this.getAttribute('data-page');
            showPage(pageId);
            
            // Update active state
            navLinks.forEach(navLink => {
                navLink.classList.remove('active');
            });
            this.classList.add('active');
        });
    });
    
    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', function() {
        const activePage = document.querySelector('.nav-link.active').getAttribute('data-page');
        refreshPageData(activePage);
    });
    
    // Logout buttons
    document.getElementById('logoutBtn').addEventListener('click', logout);
    document.getElementById('headerLogoutBtn').addEventListener('click', logout);
}

function showPage(pageId) {
    // Hide all pages
    document.querySelectorAll('.content-page').forEach(page => {
        page.classList.add('d-none');
    });
    
    // Show selected page
    document.getElementById(`${pageId}Page`).classList.remove('d-none');
    
    // Update page title
    document.getElementById('pageTitle').textContent = pageId.charAt(0).toUpperCase() + pageId.slice(1);
    
    // Refresh data for the page
    refreshPageData(pageId);
}

function refreshPageData(pageId) {
    switch(pageId) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'bins':
            loadBinsData();
            break;
        case 'users':
            loadUsersData();
            break;
        case 'rewards':
            loadRewardsData();
            break;
        case 'deposits':
            loadDepositsData();
            break;
        case 'simulation':
            loadSimulationData();
            break;
        case 'settings':
            loadSettingsData();
            break;
    }
}

// Global event handlers
function setupGlobalEvents() {
    // Add any global event handlers here
}

// Show alert message
function showAlert(message, type = 'success', container = '.container-fluid') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 end-0 m-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    document.querySelector(container).appendChild(alertDiv);
    
    // Auto dismiss after 5 seconds
    setTimeout(() => {
        alertDiv.classList.remove('show');
        setTimeout(() => alertDiv.remove(), 150);
    }, 5000);
}

// Format date
function formatDate(timestamp) {
    if (!timestamp) return 'N/A';
    
    const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    return date.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Get status badge HTML
function getStatusBadgeHtml(status) {
    let badgeClass = '';
    switch(status.toLowerCase()) {
        case 'active':
            badgeClass = 'status-active';
            break;
        case 'full':
            badgeClass = 'status-full';
            break;
        case 'maintenance':
            badgeClass = 'status-maintenance';
            break;
        default:
            badgeClass = 'status-inactive';
    }
    
    return `<span class="status-badge ${badgeClass}">${status}</span>`;
}

// Format waste type for display
function formatWasteType(type) {
    if (!type) return 'Unknown';
    
    return type
        .replace('recyclable_', '')
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

// Dashboard functionality
function initDashboard() {
    // Initial load
    loadDashboardData();
}

function loadDashboardData() {
    console.log('Loading dashboard data...');
    
    // Simulate data fetch from Firebase
    // In a real app, this would use firebase.firestore() to fetch real data
    
    // Update stats cards
    updateDashboardStats({
        totalUsers: 1250,
        userGrowth: 12,
        totalBins: 85,
        activeBins: 75,
        fullBins: 10,
        totalDeposits: 8547,
        recentDeposits: 156,
        totalWaste: 4250.5,
        organicWaste: 1560.8,
        recyclableWaste: 2689.7
    });
    
    // Initialize/update charts
    initDepositsChart();
    initWasteDistributionChart();
    
    // Load recent activity
    loadRecentActivity();
    
    // Load alerts
    loadAlerts();
}

function updateDashboardStats(stats) {
    document.getElementById('totalUsers').textContent = stats.totalUsers.toLocaleString();
    document.getElementById('userGrowth').textContent = `${stats.userGrowth}%`;
    document.getElementById('totalBins').textContent = stats.totalBins.toLocaleString();
    document.getElementById('activeBins').textContent = stats.activeBins.toLocaleString();
    document.getElementById('fullBins').textContent = stats.fullBins.toLocaleString();
    document.getElementById('totalDeposits').textContent = stats.totalDeposits.toLocaleString();
    document.getElementById('recentDeposits').textContent = stats.recentDeposits.toLocaleString();
    document.getElementById('totalWaste').textContent = `${stats.totalWaste.toLocaleString()} kg`;
    document.getElementById('organicWaste').textContent = `${stats.organicWaste.toLocaleString()} kg`;
    document.getElementById('recyclableWaste').textContent = `${stats.recyclableWaste.toLocaleString()} kg`;
}

function loadRecentActivity() {
    const activitiesList = [
        { timestamp: new Date(Date.now() - 1000 * 60 * 5), type: 'deposit', user: 'Rahul Sharma', details: 'Deposited 1.2kg of recyclable plastic' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 15), type: 'reward', user: 'Priya Patel', details: 'Redeemed "20% Off at Starbucks" coupon' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 45), type: 'deposit', user: 'Amit Kumar', details: 'Deposited 0.8kg of organic waste' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 120), type: 'registration', user: 'Neha Singh', details: 'New user registered' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 180), type: 'deposit', user: 'Vikram Reddy', details: 'Deposited 1.5kg of recyclable paper' }
    ];
    
    const activityList = document.getElementById('recentActivityList');
    activityList.innerHTML = '';
    
    activitiesList.forEach(activity => {
        let iconClass = 'bi-recycle';
        let iconColor = 'text-primary';
        
        switch(activity.type) {
            case 'deposit':
                iconClass = 'bi-recycle';
                iconColor = 'text-success';
                break;
            case 'reward':
                iconClass = 'bi-gift';
                iconColor = 'text-warning';
                break;
            case 'registration':
                iconClass = 'bi-person-plus';
                iconColor = 'text-info';
                break;
        }
        
        const html = `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">
                        <i class="bi ${iconClass} ${iconColor} me-2"></i>
                        ${activity.user}
                    </h6>
                    <small class="text-muted">${formatTimeAgo(activity.timestamp)}</small>
                </div>
                <p class="mb-1">${activity.details}</p>
            </div>
        `;
        
        activityList.innerHTML += html;
    });
}

function loadAlerts() {
    const alerts = [
        { timestamp: new Date(Date.now() - 1000 * 60 * 20), type: 'bin_full', location: 'MG Road', details: 'Bin is full and needs emptying' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 120), type: 'maintenance', location: 'Koramangala', details: 'Scheduled maintenance due' },
        { timestamp: new Date(Date.now() - 1000 * 60 * 240), type: 'bin_full', location: 'Indiranagar', details: 'Bin is full and needs emptying' }
    ];
    
    const alertsList = document.getElementById('alertsList');
    alertsList.innerHTML = '';
    
    if (alerts.length === 0) {
        alertsList.innerHTML = `
            <div class="list-group-item text-center text-muted">
                <i class="bi bi-check-circle"></i> No alerts at this time
            </div>
        `;
        return;
    }
    
    alerts.forEach(alert => {
        let iconClass = 'bi-exclamation-triangle';
        let alertClass = 'text-warning';
        
        switch(alert.type) {
            case 'bin_full':
                iconClass = 'bi-trash';
                alertClass = 'text-danger';
                break;
            case 'maintenance':
                iconClass = 'bi-tools';
                alertClass = 'text-warning';
                break;
        }
        
        const html = `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">
                        <i class="bi ${iconClass} ${alertClass} me-2"></i>
                        ${alert.type === 'bin_full' ? 'Bin Full Alert' : 'Maintenance Alert'}
                    </h6>
                    <small class="text-muted">${formatTimeAgo(alert.timestamp)}</small>
                </div>
                <p class="mb-1"><strong>Location:</strong> ${alert.location}</p>
                <p class="mb-0">${alert.details}</p>
            </div>
        `;
        
        alertsList.innerHTML += html;
    });
}

function formatTimeAgo(date) {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) {
        return 'Just now';
    }
    
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
        return `${diffInMinutes} min ago`;
    }
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
        return `${diffInHours} hr ago`;
    }
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) {
        return `${diffInDays} day ago`;
    }
    
    return date.toLocaleDateString();
}

// Bins page functionality
function initBinsPage() {
    // Setup view toggle buttons
    document.getElementById('binListViewBtn').addEventListener('click', function() {
        showBinView('list');
    });
    
    document.getElementById('binGridViewBtn').addEventListener('click', function() {
        showBinView('grid');
    });
    
    document.getElementById('binMapViewBtn').addEventListener('click', function() {
        showBinView('map');
    });
    
    // Setup add bin form
    document.getElementById('saveBinBtn').addEventListener('click', saveBin);
    
    // Setup filters
    document.getElementById('binSearchInput').addEventListener('input', filterBins);
    document.getElementById('binStatusFilter').addEventListener('change', filterBins);
    document.getElementById('binSortOrder').addEventListener('change', sortBins);
}

function loadBinsData() {
    console.log('Loading bins data...');
    
    // Sample bins data
    const bins = [
        { id: 'bin001', location: 'MG Road', current_fill_level: 75, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 12), latitude: 12.9716, longitude: 77.5946 },
        { id: 'bin002', location: 'Indiranagar', current_fill_level: 30, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 8), latitude: 12.9784, longitude: 77.6408 },
        { id: 'bin003', location: 'Koramangala', current_fill_level: 95, status: 'full', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 24), latitude: 12.9279, longitude: 77.6271 },
        { id: 'bin004', location: 'HSR Layout', current_fill_level: 10, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 2), latitude: 12.9116, longitude: 77.6741 },
        { id: 'bin005', location: 'Whitefield', current_fill_level: 50, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 16), latitude: 12.9698, longitude: 77.7499 },
        { id: 'bin006', location: 'Electronic City', current_fill_level: 85, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 20), latitude: 12.8399, longitude: 77.6770 },
        { id: 'bin007', location: 'Jayanagar', current_fill_level: 60, status: 'active', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 10), latitude: 12.9299, longitude: 77.5932 },
        { id: 'bin008', location: 'JP Nagar', current_fill_level: 40, status: 'maintenance', last_emptied: new Date(Date.now() - 1000 * 60 * 60 * 48), latitude: 12.9105, longitude: 77.5858 }
    ];
    
    // Store bins data for filtering/sorting
    window.binsData = bins;
    
    // Update all views
    updateBinsListView(bins);
    updateBinsGridView(bins);
    initBinsMapView(bins);
}

function updateBinsListView(bins) {
    const tableBody = document.getElementById('binsTableBody');
    tableBody.innerHTML = '';
    
    if (bins.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No bins found</td></tr>';
        return;
    }
    
    bins.forEach(bin => {
        const row = document.createElement('tr');
        
        // Determine fill level class
        let fillLevelClass = 'fill-low';
        if (bin.current_fill_level >= 75) {
            fillLevelClass = 'fill-high';
        } else if (bin.current_fill_level >= 50) {
            fillLevelClass = 'fill-medium';
        }
        
        row.innerHTML = `
            <td>${bin.id}</td>
            <td>${bin.location}</td>
            <td>
                <div class="d-flex align-items-center">
                    <span class="me-2">${bin.current_fill_level}%</span>
                    <div class="fill-level-bar">
                        <div class="fill-level-progress ${fillLevelClass}" style="width: ${bin.current_fill_level}%"></div>
                    </div>
                </div>
            </td>
            <td>${getStatusBadgeHtml(bin.status)}</td>
            <td>${formatDate(bin.last_emptied)}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="viewBinDetails('${bin.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button type="button" class="btn btn-outline-success" onclick="emptyBin('${bin.id}')" ${bin.current_fill_level === 0 ? 'disabled' : ''}>
                        <i class="bi bi-trash"></i> Empty
                    </button>
                    <button type="button" class="btn btn-outline-secondary" onclick="editBin('${bin.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                </div>
            </td>
        `;
        
        tableBody.appendChild(row);
    });
}

function updateBinsGridView(bins) {
    const gridContainer = document.getElementById('binsGridContainer');
    gridContainer.innerHTML = '';
    
    if (bins.length === 0) {
        gridContainer.innerHTML = '<div class="col-12 text-center py-4">No bins found</div>';
        return;
    }
    
    bins.forEach(bin => {
        // Determine bin status class and fill level class
        let binStatusClass = 'bin-active';
        let fillLevelClass = 'fill-low';
        
        if (bin.status === 'full') {
            binStatusClass = 'bin-full';
        } else if (bin.status === 'maintenance') {
            binStatusClass = 'bin-maintenance';
        } else if (bin.status === 'inactive') {
            binStatusClass = 'bin-inactive';
        }
        
        if (bin.current_fill_level >= 75) {
            fillLevelClass = 'fill-high';
        } else if (bin.current_fill_level >= 50) {
            fillLevelClass = 'fill-medium';
        }
        
        const binCard = document.createElement('div');
        binCard.className = 'col-md-3 mb-4';
        binCard.innerHTML = `
            <div class="bin-card">
                <div class="bin-header ${binStatusClass}">
                    <h5>${bin.location}</h5>
                    <span class="bin-id">${bin.id}</span>
                </div>
                <div class="bin-content">
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span>Fill Level</span>
                            <span>${bin.current_fill_level}%</span>
                        </div>
                        <div class="fill-level-bar">
                            <div class="fill-level-progress ${fillLevelClass}" style="width: ${bin.current_fill_level}%"></div>
                        </div>
                    </div>
                    <div class="mb-3">
                        <small class="text-muted">Last Emptied</small>
                        <div>${formatDate(bin.last_emptied)}</div>
                    </div>
                    <div class="d-flex justify-content-between">
                        <button class="btn btn-sm btn-outline-primary" onclick="viewBinDetails('${bin.id}')">
                            <i class="bi bi-eye"></i> Details
                        </button>
                        <button class="btn btn-sm btn-outline-success" onclick="emptyBin('${bin.id}')" ${bin.current_fill_level === 0 ? 'disabled' : ''}>
                            <i class="bi bi-trash"></i> Empty
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        gridContainer.appendChild(binCard);
    });
}

function initBinsMapView(bins) {
    // Initialize map if not already initialized
    if (!window.binsMap) {
        window.binsMap = L.map('binsMap').setView([12.9716, 77.5946], 12);
        
        // Add OpenStreetMap tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(window.binsMap);
        
        // Add markers for bins
        window.binMarkers = L.layerGroup().addTo(window.binsMap);
    } else {
        // Clear existing markers
        window.binMarkers.clearLayers();
    }
    
    // Add markers for each bin
    bins.forEach(bin => {
        if (!bin.latitude || !bin.longitude) return;
        
        // Determine marker color based on status and fill level
        let markerColor = 'green';
        if (bin.status === 'full') {
            markerColor = 'red';
        } else if (bin.status === 'maintenance') {
            markerColor = 'orange';
        } else if (bin.current_fill_level >= 75) {
            markerColor = 'red';
        } else if (bin.current_fill_level >= 50) {
            markerColor = 'orange';
        }
        
        // Create custom icon
        const binIcon = L.divIcon({
            className: 'bin-marker',
            html: `<i class="bi bi-trash" style="color: ${markerColor}; font-size: 24px;"></i>`,
            iconSize: [24, 24],
            iconAnchor: [12, 12]
        });
        
        // Create marker
        const marker = L.marker([bin.latitude, bin.longitude], { icon: binIcon }).addTo(window.binMarkers);
        
        // Add popup with bin details
        let fillLevelClass = 'fill-low';
        if (bin.current_fill_level >= 75) {
            fillLevelClass = 'fill-high';
        } else if (bin.current_fill_level >= 50) {
            fillLevelClass = 'fill-medium';
        }
        
        marker.bindPopup(`
            <div class="bin-marker-popup">
                <h6>${bin.location}</h6>
                <div><strong>ID:</strong> ${bin.id}</div>
                <div><strong>Status:</strong> ${bin.status}</div>
                <div>
                    <strong>Fill Level:</strong> ${bin.current_fill_level}%
                    <div class="fill-level">
                        <div class="fill-progress ${fillLevelClass}" style="width: ${bin.current_fill_level}%;"></div>
                    </div>
                </div>
                <div><strong>Last Emptied:</strong> ${formatDate(bin.last_emptied)}</div>
                <div class="mt-2">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewBinDetails('${bin.id}')">Details</button>
                    <button class="btn btn-sm btn-outline-success" onclick="emptyBin('${bin.id}')" ${bin.current_fill_level === 0 ? 'disabled' : ''}>Empty</button>
                </div>
            </div>
        `, { minWidth: 200 });
    });
}

function showBinView(viewType) {
    // Hide all views
    document.querySelectorAll('.bin-view-container').forEach(container => {
        container.classList.add('d-none');
    });
    
    // Show selected view
    switch (viewType) {
        case 'list':
            document.getElementById('binListView').classList.remove('d-none');
            document.getElementById('binListViewBtn').classList.add('active');
            document.getElementById('binGridViewBtn').classList.remove('active');
            document.getElementById('binMapViewBtn').classList.remove('active');
            break;
        case 'grid':
            document.getElementById('binGridView').classList.remove('d-none');
            document.getElementById('binListViewBtn').classList.remove('active');
            document.getElementById('binGridViewBtn').classList.add('active');
            document.getElementById('binMapViewBtn').classList.remove('active');
            break;
        case 'map':
            document.getElementById('binMapView').classList.remove('d-none');
            document.getElementById('binListViewBtn').classList.remove('active');
            document.getElementById('binGridViewBtn').classList.remove('active');
            document.getElementById('binMapViewBtn').classList.add('active');
            
            // Refresh map if needed
            if (window.binsMap) {
                window.binsMap.invalidateSize();
            }
            break;
    }
}

function filterBins() {
    if (!window.binsData) return;
    
    const searchText = document.getElementById('binSearchInput').value.toLowerCase();
    const statusFilter = document.getElementById('binStatusFilter').value;
    
    let filteredBins = window.binsData.filter(bin => {
        // Apply search filter
        const matchesSearch = bin.id.toLowerCase().includes(searchText) || 
                             bin.location.toLowerCase().includes(searchText);
        
        // Apply status filter
        const matchesStatus = statusFilter === 'all' || bin.status === statusFilter;
        
        return matchesSearch && matchesStatus;
    });
    
    // Apply current sort
    sortBins(filteredBins);
}

function sortBins(binsToSort) {
    const sortOrder = document.getElementById('binSortOrder').value;
    let sortedBins = binsToSort || [...window.binsData];
    
    switch (sortOrder) {
        case 'location':
            sortedBins.sort((a, b) => a.location.localeCompare(b.location));
            break;
        case 'fill_level':
            sortedBins.sort((a, b) => b.current_fill_level - a.current_fill_level);
            break;
        case 'last_emptied':
            sortedBins.sort((a, b) => {
                const dateA = a.last_emptied instanceof Date ? a.last_emptied : new Date(a.last_emptied);
                const dateB = b.last_emptied instanceof Date ? b.last_emptied : new Date(b.last_emptied);
                return dateB - dateA;
            });
            break;
    }
    
    // Update views with sorted/filtered bins
    updateBinsListView(sortedBins);
    updateBinsGridView(sortedBins);
    initBinsMapView(sortedBins);
}

function saveBin() {
    const location = document.getElementById('binLocation').value;
    const latitude = parseFloat(document.getElementById('binLatitude').value);
    const longitude = parseFloat(document.getElementById('binLongitude').value);
    const status = document.getElementById('binStatus').value;
    
    if (!location) {
        showAlert('Please enter a location', 'danger');
        return;
    }
    
    // In a real app, this would save to Firebase
    console.log('Saving bin:', { location, latitude, longitude, status });
    
    // Generate a unique ID
    const id = 'bin' + (Math.floor(Math.random() * 900) + 100);
    
    // Add new bin to the data
    const newBin = {
        id,
        location,
        latitude: isNaN(latitude) ? 0 : latitude,
        longitude: isNaN(longitude) ? 0 : longitude,
        current_fill_level: 0,
        status,
        last_emptied: new Date()
    };
    
    window.binsData.push(newBin);
    
    // Update views
    updateBinsListView(window.binsData);
    updateBinsGridView(window.binsData);
    initBinsMapView(window.binsData);
    
    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('addBinModal'));
    modal.hide();
    
    // Reset form
    document.getElementById('addBinForm').reset();
    
    // Show success message
    showAlert('Bin added successfully');
}

function emptyBin(binId) {
    // In a real app, this would update in Firebase
    const binIndex = window.binsData.findIndex(bin => bin.id === binId);
    if (binIndex === -1) return;
    
    window.binsData[binIndex].current_fill_level = 0;
    window.binsData[binIndex].last_emptied = new Date();
    window.binsData[binIndex].status = 'active';
    
    // Update views
    updateBinsListView(window.binsData);
    updateBinsGridView(window.binsData);
    initBinsMapView(window.binsData);
    
    // Show success message
    showAlert(`Bin ${binId} emptied successfully`);
}

function viewBinDetails(binId) {
    // Implement bin details view
    showAlert(`Viewing details for bin ${binId}`, 'info');
}

function editBin(binId) {
    // Implement bin editing
    showAlert(`Editing bin ${binId}`, 'info');
}

// Add more functions for other pages: users, rewards, deposits, simulation, and settings...

// Users page functionality
function initUsersPage() {
    // Setup search and filters
    document.getElementById('userSearchInput').addEventListener('input', filterUsers);
    document.getElementById('userSortOrder').addEventListener('change', sortUsers);
    document.getElementById('userRankFilter').addEventListener('change', filterUsers);
}

function loadUsersData() {
    console.log('Loading users data...');
    
    // Sample users data
    const users = [
        { id: 'user001', name: 'Rahul Sharma', email: 'rahul.sharma@example.com', total_points: 450, rank: 'Eco Warrior', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 30) },
        { id: 'user002', name: 'Priya Patel', email: 'priya.patel@example.com', total_points: 780, rank: 'Eco Warrior', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 45) },
        { id: 'user003', name: 'Amit Kumar', email: 'amit.kumar@example.com', total_points: 120, rank: 'Eco Rookie', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 10) },
        { id: 'user004', name: 'Neha Singh', email: 'neha.singh@example.com', total_points: 50, rank: 'Novice Recycler', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 5) },
        { id: 'user005', name: 'Vikram Reddy', email: 'vikram.reddy@example.com', total_points: 320, rank: 'Green Guardian', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 60) },
        { id: 'user006', name: 'Anjali Gupta', email: 'anjali.gupta@example.com', total_points: 890, rank: 'Master Recycler', created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 90) }
    ];
    
    // Store users data for filtering/sorting
    window.usersData = users;
    
    // Update users table
    updateUsersTable(users);
}

function updateUsersTable(users) {
    const tableBody = document.getElementById('usersTableBody');
    tableBody.innerHTML = '';
    
    if (users.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No users found</td></tr>';
        return;
    }
    
    users.forEach(user => {
        const row = document.createElement('tr');
        
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.name}</td>
            <td>${user.email}</td>
            <td>${user.total_points}</td>
            <td>${user.rank}</td>
            <td>${formatDate(user.created_at)}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="viewUserDetails('${user.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button type="button" class="btn btn-outline-secondary" onclick="editUser('${user.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                </div>
            </td>
        `;
        
        tableBody.appendChild(row);
    });
}

function filterUsers() {
    if (!window.usersData) return;
    
    const searchText = document.getElementById('userSearchInput').value.toLowerCase();
    const rankFilter = document.getElementById('userRankFilter').value;
    
    let filteredUsers = window.usersData.filter(user => {
        // Apply search filter
        const matchesSearch = user.id.toLowerCase().includes(searchText) || 
                             user.name.toLowerCase().includes(searchText) ||
                             user.email.toLowerCase().includes(searchText);
        
        // Apply rank filter
        const matchesRank = rankFilter === 'all' || user.rank === rankFilter;
        
        return matchesSearch && matchesRank;
    });
    
    // Apply current sort
    sortUsers(filteredUsers);
}

function sortUsers(usersToSort) {
    const sortOrder = document.getElementById('userSortOrder').value;
    let sortedUsers = usersToSort || [...window.usersData];
    
    switch (sortOrder) {
        case 'name':
            sortedUsers.sort((a, b) => a.name.localeCompare(b.name));
            break;
        case 'points':
            sortedUsers.sort((a, b) => b.total_points - a.total_points);
            break;
        case 'join_date':
            sortedUsers.sort((a, b) => {
                const dateA = a.created_at instanceof Date ? a.created_at : new Date(a.created_at);
                const dateB = b.created_at instanceof Date ? b.created_at : new Date(b.created_at);
                return dateB - dateA;
            });
            break;
    }
    
    // Update table with sorted/filtered users
    updateUsersTable(sortedUsers);
}

function viewUserDetails(userId) {
    // For demo purposes, just show the first user
    const user = window.usersData.find(u => u.id === userId) || window.usersData[0];
    
    // Populate user details modal
    document.getElementById('userDetailName').textContent = user.name;
    document.getElementById('userDetailRank').textContent = user.rank;
    document.getElementById('userDetailId').value = user.id;
    document.getElementById('userDetailEmail').value = user.email;
    document.getElementById('userDetailPoints').value = user.total_points;
    document.getElementById('userDetailLevel').value = Math.floor(user.total_points / 100) + 1;
    document.getElementById('userDetailJoinDate').value = formatDate(user.created_at);
    
    // Show user stats (mock data for demo)
    document.getElementById('userDetailDeposits').textContent = Math.floor(user.total_points / 10);
    document.getElementById('userDetailOrganic').textContent = (user.total_points * 0.04).toFixed(1) + ' kg';
    document.getElementById('userDetailRecyclable').textContent = (user.total_points * 0.06).toFixed(1) + ' kg';
    document.getElementById('userDetailRewards').textContent = Math.floor(user.total_points / 200);
    
    // Load user activity (mock data for demo)
    const activityTable = document.getElementById('userActivityTable');
    activityTable.innerHTML = '';
    
    if (user.total_points > 0) {
        // Create some activity based on points
        const numActivities = Math.min(5, Math.ceil(user.total_points / 100));
        
        for (let i = 0; i < numActivities; i++) {
            const date = new Date(Date.now() - (i * 1000 * 60 * 60 * 24 * Math.random() * 10));
            
            let activity, details;
            if (i % 3 === 0) {
                activity = 'Deposit';
                details = `Deposited ${(Math.random() * 2).toFixed(1)} kg of ${Math.random() > 0.5 ? 'organic' : 'recyclable'} waste`;
            } else if (i % 3 === 1) {
                activity = 'Reward Redemption';
                details = 'Redeemed a coupon at ' + ['Starbucks', 'Amazon', 'BookMyShow', 'Pizza Hut'][Math.floor(Math.random() * 4)];
            } else {
                activity = 'Level Up';
                details = `Reached Level ${Math.floor(Math.random() * 5) + 2}`;
            }
            
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${formatDate(date)}</td>
                <td>${activity}</td>
                <td>${details}</td>
            `;
            
            activityTable.appendChild(row);
        }
    } else {
        activityTable.innerHTML = '<tr><td colspan="3" class="text-center">No activity yet</td></tr>';
    }
    
    // Show the modal
    const modal = new bootstrap.Modal(document.getElementById('viewUserModal'));
    modal.show();
}

function editUser(userId) {
    // Implement user editing
    showAlert(`Editing user ${userId}`, 'info');
}

// Rewards page functionality
function initRewardsPage() {
    // Setup filter and search for partner offers
    document.getElementById('offerSearchInput').addEventListener('input', filterOffers);
    document.getElementById('offerCategoryFilter').addEventListener('change', filterOffers);
    document.getElementById('offerSortOrder').addEventListener('change', sortOffers);
    
    // Setup filter and search for redeemed rewards
    document.getElementById('redeemedSearchInput').addEventListener('input', filterRedeemed);
    document.getElementById('redeemedStatusFilter').addEventListener('change', filterRedeemed);
    document.getElementById('redeemedSortOrder').addEventListener('change', sortRedeemed);
    
    // Setup add reward form
    document.getElementById('saveRewardBtn').addEventListener('click', saveReward);
    
    // Set current date for reward start date
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('rewardStartDate').value = today;
    
    // Set default end date to 3 months from now
    const threeMonthsLater = new Date();
    threeMonthsLater.setMonth(threeMonthsLater.getMonth() + 3);
    document.getElementById('rewardEndDate').value = threeMonthsLater.toISOString().split('T')[0];
}

function loadRewardsData() {
    console.log('Loading rewards data...');
    
    // Load partner offers
    loadPartnerOffers();
    
    // Load redeemed rewards
    loadRedeemedRewards();
}

function loadPartnerOffers() {
    // Sample partner offers data
    const offers = [
        { id: 'offer001', title: '20% Off at Starbucks', partnerName: 'Starbucks', category: 'food_drinks', pointsRequired: 200, startDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 30), endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 60), featured: true },
        { id: 'offer002', title: '₹500 Off on Amazon', partnerName: 'Amazon', category: 'shopping', pointsRequired: 500, startDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 15), endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 45), featured: false },
        { id: 'offer003', title: 'Buy 1 Get 1 Movie Ticket', partnerName: 'BookMyShow', category: 'entertainment', pointsRequired: 300, startDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 10), endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 80), featured: true },
        { id: 'offer004', title: '30% Off at Pizza Hut', partnerName: 'Pizza Hut', category: 'food_drinks', pointsRequired: 250, startDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 5), endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 25), featured: false },
        { id: 'offer005', title: '₹200 Off on Myntra', partnerName: 'Myntra', category: 'shopping', pointsRequired: 150, startDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 20), endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 30), featured: true }
    ];
    
    // Store offers data for filtering/sorting
    window.offersData = offers;
    
    // Update offers table
    updateOffersTable(offers);
}

function updateOffersTable(offers) {
    const tableBody = document.getElementById('offersTableBody');
    tableBody.innerHTML = '';
    
    if (offers.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No offers found</td></tr>';
        return;
    }
    
    offers.forEach(offer => {
        const row = document.createElement('tr');
        
        row.innerHTML = `
            <td>${offer.title}</td>
            <td>${offer.partnerName}</td>
            <td>${formatCategory(offer.category)}</td>
            <td>${offer.pointsRequired}</td>
            <td>${formatDate(offer.startDate)}</td>
            <td>${formatDate(offer.endDate)}</td>
            <td>
                <span class="badge ${offer.featured ? 'bg-success' : 'bg-secondary'}">
                    ${offer.featured ? 'Yes' : 'No'}
                </span>
            </td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="viewOfferDetails('${offer.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button type="button" class="btn btn-outline-secondary" onclick="editOffer('${offer.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button type="button" class="btn btn-outline-danger" onclick="deleteOffer('${offer.id}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        
        tableBody.appendChild(row);
    });
}

function loadRedeemedRewards() {
    // Sample redeemed rewards data
    const redeemed = [
        { id: 'reward001', userId: 'user001', userName: 'Rahul Sharma', couponId: 'offer001', couponTitle: '20% Off at Starbucks', couponCode: 'SB-ABC123', pointsRedeemed: 200, createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 5), expiryDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 25), isRedeemed: false },
        { id: 'reward002', userId: 'user002', userName: 'Priya Patel', couponId: 'offer003', couponTitle: 'Buy 1 Get 1 Movie Ticket', couponCode: 'BMS-XYZ456', pointsRedeemed: 300, createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 10), expiryDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 20), isRedeemed: true },
        { id: 'reward003', userId: 'user003', userName: 'Amit Kumar', couponId: 'offer002', couponTitle: '₹500 Off on Amazon', couponCode: 'AMZ-DEF789', pointsRedeemed: 500, createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 15), expiryDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 15), isRedeemed: false },
        { id: 'reward004', userId: 'user005', userName: 'Vikram Reddy', couponId: 'offer004', couponTitle: '30% Off at Pizza Hut', couponCode: 'PH-GHI012', pointsRedeemed: 250, createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 30), expiryDate: new Date(Date.now() - 1000 * 60 * 60 * 24 * 1), isRedeemed: false }
    ];
    
    // Store redeemed data for filtering/sorting
    window.redeemedData = redeemed;
    
    // Update redeemed table
    updateRedeemedTable(redeemed);
}

function updateRedeemedTable(redeemed) {
    const tableBody = document.getElementById('redeemedTableBody');
    tableBody.innerHTML = '';
    
    if (redeemed.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No redeemed rewards found</td></tr>';
        return;
    }
    
    redeemed.forEach(reward => {
        // Determine status
        let status = 'Active';
        let statusClass = 'bg-success';
        
        if (reward.isRedeemed) {
            status = 'Used';
            statusClass = 'bg-secondary';
        } else if (new Date(reward.expiryDate) < new Date()) {
            status = 'Expired';
            statusClass = 'bg-danger';
        }
        
        const row = document.createElement('tr');
        
        row.innerHTML = `
            <td>${reward.userName}</td>
            <td>${reward.couponTitle}</td>
            <td><code>${reward.couponCode}</code></td>
            <td>${reward.pointsRedeemed}</td>
            <td>${formatDate(reward.createdAt)}</td>
            <td>${formatDate(reward.expiryDate)}</td>
            <td><span class="badge ${statusClass}">${status}</span></td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="viewRewardDetails('${reward.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button type="button" class="btn btn-outline-secondary" onclick="markAsUsed('${reward.id}')" ${status !== 'Active' ? 'disabled' : ''}>
                        <i class="bi bi-check-circle"></i> Mark Used
                    </button>
                </div>
            </td>
        `;
        
        tableBody.appendChild(row);
    });
}

function formatCategory(category) {
    if (!category) return 'Unknown';
    
    switch (category) {
        case 'food_drinks':
            return 'Food & Drinks';
        case 'shopping':
            return 'Shopping';
        case 'entertainment':
            return 'Entertainment';
        default:
            return category.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase());
    }
}

function filterOffers() {
    if (!window.offersData) return;
    
    const searchText = document.getElementById('offerSearchInput').value.toLowerCase();
    const categoryFilter = document.getElementById('offerCategoryFilter').value;
    
    let filteredOffers = window.offersData.filter(offer => {
        // Apply search filter
        const matchesSearch = offer.title.toLowerCase().includes(searchText) || 
                             offer.partnerName.toLowerCase().includes(searchText);
        
        // Apply category filter
        const matchesCategory = categoryFilter === 'all' || offer.category === categoryFilter;
        
        return matchesSearch && matchesCategory;
    });
    
    // Apply current sort
    sortOffers(filteredOffers);
}

function sortOffers(offersToSort) {
    const sortOrder = document.getElementById('offerSortOrder').value;
    let sortedOffers = offersToSort || [...window.offersData];
    
    switch (sortOrder) {
        case 'title':
            sortedOffers.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'points':
            sortedOffers.sort((a, b) => a.pointsRequired - b.pointsRequired);
            break;
        case 'expiry':
            sortedOffers.sort((a, b) => {
                const dateA = a.endDate instanceof Date ? a.endDate : new Date(a.endDate);
                const dateB = b.endDate instanceof Date ? b.endDate : new Date(b.endDate);
                return dateA - dateB;
            });
            break;
    }
    
    // Update table with sorted/filtered offers
    updateOffersTable(sortedOffers);
}

function filterRedeemed() {
    if (!window.redeemedData) return;
    
    const searchText = document.getElementById('redeemedSearchInput').value.toLowerCase();
    const statusFilter = document.getElementById('redeemedStatusFilter').value;
    
    let filteredRedeemed = window.redeemedData.filter(reward => {
        // Apply search filter
        const matchesSearch = reward.userName.toLowerCase().includes(searchText) || 
                             reward.couponTitle.toLowerCase().includes(searchText) ||
                             reward.couponCode.toLowerCase().includes(searchText);
        
        // Apply status filter
        let status = 'active';
        if (reward.isRedeemed) {
            status = 'used';
        } else if (new Date(reward.expiryDate) < new Date()) {
            status = 'expired';
        }
        
        const matchesStatus = statusFilter === 'all' || status === statusFilter;
        
        return matchesSearch && matchesStatus;
    });
    
    // Apply current sort
    sortRedeemed(filteredRedeemed);
}

function sortRedeemed(redeemedToSort) {
    const sortOrder = document.getElementById('redeemedSortOrder').value;
    let sortedRedeemed = redeemedToSort || [...window.redeemedData];
    
    switch (sortOrder) {
        case 'date':
            sortedRedeemed.sort((a, b) => {
                const dateA = a.createdAt instanceof Date ? a.createdAt : new Date(a.createdAt);
                const dateB = b.createdAt instanceof Date ? b.createdAt : new Date(b.createdAt);
                return dateB - dateA;
            });
            break;
        case 'user':
            sortedRedeemed.sort((a, b) => a.userName.localeCompare(b.userName));
            break;
        case 'coupon':
            sortedRedeemed.sort((a, b) => a.couponTitle.localeCompare(b.couponTitle));
            break;
    }
    
    // Update table with sorted/filtered redeemed rewards
    updateRedeemedTable(sortedRedeemed);
}

function saveReward() {
    const title = document.getElementById('rewardTitle').value;
    const partnerName = document.getElementById('rewardPartner').value;
    const category = document.getElementById('rewardCategory').value;
    const pointsRequired = parseInt(document.getElementById('rewardPoints').value);
    
    if (!title || !partnerName || !category || isNaN(pointsRequired)) {
        showAlert('Please fill all required fields', 'danger');
        return;
    }
    
    // In a real app, this would save to Firebase
    console.log('Saving reward:', { title, partnerName, category, pointsRequired });
    
    // Generate a unique ID
    const id = 'offer' + (Math.floor(Math.random() * 900) + 100);
    
    // Add new offer to the data
    const newOffer = {
        id,
        title,
        partnerName,
        category,
        pointsRequired,
        description: document.getElementById('rewardDescription').value,
        imageUrl: document.getElementById('rewardImageUrl').value,
        partnerLogoUrl: document.getElementById('rewardLogoUrl').value,
        termsAndConditions: document.getElementById('rewardTerms').value,
        howToRedeem: document.getElementById('rewardRedeem').value,
        startDate: new Date(document.getElementById('rewardStartDate').value),
        endDate: new Date(document.getElementById('rewardEndDate').value),
        featured: document.getElementById('rewardFeatured').checked
    };
    
    window.offersData.push(newOffer);
    
    // Update table
    updateOffersTable(window.offersData);
    
    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('addRewardModal'));
    modal.hide();
    
    // Reset form
    document.getElementById('addRewardForm').reset();
    
    // Show success message
    showAlert('Reward added successfully');
}

function viewOfferDetails(offerId) {
    // Implement offer details view
    showAlert(`Viewing details for offer ${offerId}`, 'info');
}

function editOffer(offerId) {
    // Implement offer editing
    showAlert(`Editing offer ${offerId}`, 'info');
}

function deleteOffer(offerId) {
    // Implement offer deletion
    if (confirm('Are you sure you want to delete this offer?')) {
        // In a real app, this would delete from Firebase
        window.offersData = window.offersData.filter(offer => offer.id !== offerId);
        
        // Update table
        updateOffersTable(window.offersData);
        
        // Show success message
        showAlert(`Offer ${offerId} deleted successfully`);
    }
}

function viewRewardDetails(rewardId) {
    // Implement reward details view
    showAlert(`Viewing details for reward ${rewardId}`, 'info');
}

function markAsUsed(rewardId) {
    // Implement marking reward as used
    const rewardIndex = window.redeemedData.findIndex(reward => reward.id === rewardId);
    if (rewardIndex === -1) return;
    
    window.redeemedData[rewardIndex].isRedeemed = true;
    
    // Update table
    updateRedeemedTable(window.redeemedData);
    
    // Show success message
    showAlert(`Reward ${rewardId} marked as used`);
}

// Add more implementations for deposits, simulation, and settings pages...

// Deposits page
function initDepositsPage() {
    // Implementation for deposits page
    document.getElementById('depositSearchInput').addEventListener('input', filterDeposits);
    document.getElementById('wasteTypeFilter').addEventListener('change', filterDeposits);
    document.getElementById('depositDateFilter').addEventListener('change', filterDeposits);
    document.getElementById('depositSortOrder').addEventListener('change', sortDeposits);
    
    // Export button
    document.getElementById('exportDepositsBtn').addEventListener('click', exportDeposits);
}

function loadDepositsData() {
    console.log('Loading deposits data...');
    
    // Placeholder implementation for deposits data
    showAlert('Deposits data loading feature will be implemented in the next version', 'info');
}

function filterDeposits() {
    // Placeholder implementation for filtering deposits
    showAlert('Deposits filtering will be implemented in the next version', 'info');
}

function sortDeposits() {
    // Placeholder implementation for sorting deposits
    showAlert('Deposits sorting will be implemented in the next version', 'info');
}

function exportDeposits() {
    // Placeholder implementation for exporting deposits
    showAlert('Deposits export feature will be implemented in the next version', 'info');
}

// Simulation page
function initSimulationPage() {
    // Implementation for simulation page
    document.getElementById('depositSimulationForm').addEventListener('submit', function(e) {
        e.preventDefault();
        simulateDeposit();
    });
    
    document.getElementById('qrGenerationForm').addEventListener('submit', function(e) {
        e.preventDefault();
        generateQrCode();
    });
    
    // Initialize user and bin dropdown lists
    initSimulationDropdowns();
}

function loadSimulationData() {
    console.log('Loading simulation data...');
    
    // Refresh dropdown options
    initSimulationDropdowns();
}

function initSimulationDropdowns() {
    // Populate user dropdown
    const userSelect = document.getElementById('simulationUser');
    userSelect.innerHTML = '<option value="" disabled selected>Select User</option>';
    
    if (window.usersData) {
        window.usersData.forEach(user => {
            const option = document.createElement('option');
            option.value = user.id;
            option.textContent = `${user.name} (${user.id})`;
            userSelect.appendChild(option);
        });
    }
    
    // Populate bin dropdowns
    const binSelects = [document.getElementById('simulationBin'), document.getElementById('qrBin')];
    
    binSelects.forEach(select => {
        select.innerHTML = '<option value="" disabled selected>Select Bin</option>';
        
        if (window.binsData) {
            window.binsData.forEach(bin => {
                const option = document.createElement('option');
                option.value = bin.id;
                option.textContent = `${bin.location} (${bin.id})`;
                select.appendChild(option);
            });
        }
    });
}

function simulateDeposit() {
    const userId = document.getElementById('simulationUser').value;
    const binId = document.getElementById('simulationBin').value;
    const wasteType = document.getElementById('simulationWasteType').value;
    const weight = parseFloat(document.getElementById('simulationWeight').value);
    
    if (!userId || !binId || !wasteType || isNaN(weight)) {
        showAlert('Please fill all fields correctly', 'danger');
        return;
    }
    
    // In a real app, this would call the API endpoint
    console.log('Simulating deposit:', { userId, binId, wasteType, weight });
    
    // Get user and bin objects
    const user = window.usersData.find(u => u.id === userId);
    const bin = window.binsData.find(b => b.id === binId);
    
    if (!user || !bin) {
        showAlert('User or bin not found', 'danger');
        return;
    }
    
    // Calculate points
    let pointsPerKg = 5; // Default for organic
    switch (wasteType) {
        case 'recyclable_plastic':
            pointsPerKg = 10;
            break;
        case 'recyclable_paper':
            pointsPerKg = 8;
            break;
        case 'recyclable_metal':
            pointsPerKg = 15;
            break;
    }
    
    const pointsEarned = Math.round(weight * pointsPerKg);
    
    // Update bin fill level
    const oldFillLevel = bin.current_fill_level;
    bin.current_fill_level = Math.min(100, bin.current_fill_level + (weight * 2));
    
    if (bin.current_fill_level >= 90 && oldFillLevel < 90) {
        bin.status = 'full';
    }
    
    // Update user points
    user.total_points += pointsEarned;
    
    // Update UI with simulation results
    document.getElementById('noSimulationResults').classList.add('d-none');
    document.getElementById('qrCodeResult').classList.add('d-none');
    document.getElementById('simulationResults').classList.remove('d-none');
    
    document.getElementById('resultDepositId').textContent = 'DEP' + Date.now().toString().substr(-6);
    document.getElementById('resultUser').textContent = user.name;
    document.getElementById('resultBin').textContent = bin.location;
    document.getElementById('resultWasteType').textContent = formatWasteType(wasteType);
    document.getElementById('resultWeight').textContent = weight.toFixed(1);
    document.getElementById('resultPoints').textContent = pointsEarned;
    document.getElementById('resultFillLevel').textContent = bin.current_fill_level.toFixed(1);
    document.getElementById('resultTimestamp').textContent = new Date().toLocaleString();
    
    // Update tables for bins and users
    updateBinsListView(window.binsData);
    updateBinsGridView(window.binsData);
    initBinsMapView(window.binsData);
    updateUsersTable(window.usersData);
    
    // Show success message
    showAlert('Deposit simulated successfully', 'success');
}

function generateQrCode() {
    const binId = document.getElementById('qrBin').value;
    const wasteType = document.getElementById('qrWasteType').value;
    const weight = parseFloat(document.getElementById('qrWeight').value);
    
    if (!binId || !wasteType || isNaN(weight)) {
        showAlert('Please fill all fields correctly', 'danger');
        return;
    }
    
    // Create QR code data
    const qrData = {
        bin_id: binId,
        waste_type: wasteType,
        weight: weight
    };
    
    const qrString = JSON.stringify(qrData);
    
    // Generate QR code
    document.getElementById('qrCodeDisplay').innerHTML = '';
    
    QRCode.toCanvas(document.getElementById('qrCodeDisplay'), qrString, {
        width: 200,
        margin: 2,
        color: {
            dark: '#000000',
            light: '#ffffff'
        }
    }, function(error) {
        if (error) {
            console.error(error);
            showAlert('Error generating QR code', 'danger');
            return;
        }
        
        // Show QR code
        document.getElementById('noSimulationResults').classList.add('d-none');
        document.getElementById('simulationResults').classList.add('d-none');
        document.getElementById('qrCodeResult').classList.remove('d-none');
        
        document.getElementById('qrCodeData').textContent = qrString;
        
        // Setup download button
        document.getElementById('downloadQrBtn').onclick = function() {
            const canvas = document.querySelector('#qrCodeDisplay canvas');
            const dataUrl = canvas.toDataURL('image/png');
            
            const link = document.createElement('a');
            link.href = dataUrl;
            link.download = `qr_${binId}_${wasteType}.png`;
            link.click();
        };
    });
}

// Settings page
function initSettingsPage() {
    // Implementation for settings page
    document.getElementById('generalSettingsForm').addEventListener('submit', function(e) {
        e.preventDefault();
        saveGeneralSettings();
    });
    
    document.getElementById('rewardsSettingsForm').addEventListener('submit', function(e) {
        e.preventDefault();
        saveRewardsSettings();
    });
    
    document.getElementById('notificationSettingsForm').addEventListener('submit', function(e) {
        e.preventDefault();
        saveNotificationSettings();
    });
    
    document.getElementById('apiSettingsForm').addEventListener('submit', function(e) {
        e.preventDefault();
        saveApiSettings();
    });
    
    // Setup API key buttons
    document.getElementById('regenerateApiKey').addEventListener('click', regenerateApiKey);
    document.getElementById('copyApiKey').addEventListener('click', copyApiKey);
}

function loadSettingsData() {
    console.log('Loading settings data...');
    
    // Placeholder implementation for settings data
    // In a real app, this would fetch settings from Firebase
}

function saveGeneralSettings() {
    // Placeholder implementation for saving general settings
    showAlert('General settings saved successfully', 'success');
}

function saveRewardsSettings() {
    // Placeholder implementation for saving rewards settings
    showAlert('Rewards settings saved successfully', 'success');
}

function saveNotificationSettings() {
    // Placeholder implementation for saving notification settings
    showAlert('Notification settings saved successfully', 'success');
}

function saveApiSettings() {
    // Placeholder implementation for saving API settings
    showAlert('API settings saved successfully', 'success');
}

function regenerateApiKey() {
    // Generate a new API key (in a real app, this would be done server-side)
    const newKey = 'sk_test_' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    document.getElementById('apiKey').value = newKey;
    
    showAlert('API key regenerated successfully', 'success');
}

function copyApiKey() {
    const apiKey = document.getElementById('apiKey');
    apiKey.select();
    document.execCommand('copy');
    
    showAlert('API key copied to clipboard', 'success');
}