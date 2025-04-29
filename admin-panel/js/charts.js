// Charts for Admin Dashboard

// Chart color schemes
const chartColors = {
    primary: '#3a8f6f',
    secondary: '#6c757d',
    success: '#28a745',
    danger: '#dc3545',
    warning: '#ffc107',
    info: '#17a2b8',
    light: '#f8f9fa',
    dark: '#343a40',
    organic: '#7cb342',
    plastic: '#1e88e5',
    paper: '#ff8f00',
    metal: '#6d4c41'
};

// Create gradient for area charts
function createGradient(ctx, startColor, endColor) {
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, startColor);
    gradient.addColorStop(1, endColor);
    return gradient;
}

// Initialize Deposits Chart (Line Chart)
function initDepositsChart() {
    const ctx = document.getElementById('depositsChart').getContext('2d');
    
    // Generate sample data (last 30 days)
    const labels = [];
    const organicData = [];
    const recyclableData = [];
    const totalData = [];
    
    for (let i = 29; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        labels.push(date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }));
        
        // Generate some random data
        const organicValue = Math.floor(Math.random() * 50) + 30;
        const recyclableValue = Math.floor(Math.random() * 70) + 50;
        
        organicData.push(organicValue);
        recyclableData.push(recyclableValue);
        totalData.push(organicValue + recyclableValue);
    }
    
    // Create chart
    if (window.depositsChart) {
        window.depositsChart.destroy();
    }
    
    window.depositsChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Total Deposits',
                    data: totalData,
                    borderColor: chartColors.primary,
                    backgroundColor: createGradient(ctx, 'rgba(58, 143, 111, 0.2)', 'rgba(58, 143, 111, 0)'),
                    borderWidth: 2,
                    tension: 0.3,
                    pointBackgroundColor: chartColors.primary,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    fill: true
                },
                {
                    label: 'Organic Waste',
                    data: organicData,
                    borderColor: chartColors.organic,
                    backgroundColor: 'transparent',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    tension: 0.3,
                    pointBackgroundColor: chartColors.organic,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    fill: false
                },
                {
                    label: 'Recyclable Waste',
                    data: recyclableData,
                    borderColor: chartColors.info,
                    backgroundColor: 'transparent',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    tension: 0.3,
                    pointBackgroundColor: chartColors.info,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        boxWidth: 8
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    padding: 10,
                    bodySpacing: 5,
                    titleFont: {
                        size: 13
                    },
                    bodyFont: {
                        size: 12
                    },
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.raw + ' kg';
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 10
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        borderDash: [3, 3],
                        color: 'rgba(0, 0, 0, 0.05)'
                    },
                    ticks: {
                        callback: function(value) {
                            return value + ' kg';
                        }
                    }
                }
            }
        }
    });
}

// Initialize Waste Distribution Chart (Pie Chart)
function initWasteDistributionChart() {
    const ctx = document.getElementById('wasteDistributionChart').getContext('2d');
    
    // Sample waste distribution data
    const data = {
        labels: ['Organic', 'Plastic', 'Paper', 'Metal'],
        datasets: [{
            data: [40, 25, 20, 15],
            backgroundColor: [
                chartColors.organic,
                chartColors.plastic,
                chartColors.paper,
                chartColors.metal
            ],
            borderWidth: 0,
            hoverOffset: 10
        }]
    };
    
    // Create chart
    if (window.wasteDistributionChart) {
        window.wasteDistributionChart.destroy();
    }
    
    window.wasteDistributionChart = new Chart(ctx, {
        type: 'doughnut',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '60%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 15,
                        boxWidth: 8
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    padding: 10,
                    titleFont: {
                        size: 13
                    },
                    bodyFont: {
                        size: 12
                    },
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.formattedValue;
                            const total = context.dataset.data.reduce((acc, data) => acc + data, 0);
                            const percentage = Math.round((context.raw / total) * 100);
                            return label + ': ' + value + '% (' + percentage + '% of total)';
                        }
                    }
                }
            }
        }
    });
}

// Add more chart initializers for other dashboard sections as needed