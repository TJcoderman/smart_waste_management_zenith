# Smart Dustbin Hardware Simulation Guide

This guide explains how to simulate IoT hardware for the Smart Dustbin Ecosystem project during the hackathon. Since physical hardware isn't available, these simulation methods allow for realistic testing of the entire system.

## Understanding the Hardware Components

In a real implementation, the smart dustbins would include:

1. **Weight Sensors**: To measure the weight of waste deposited
2. **Waste Type Detection**: Camera/sensors to identify waste type
3. **QR Code Generator/Display**: For user identification
4. **Fill Level Sensor**: To monitor how full the bin is
5. **Networking Components**: For real-time communication

## Simulation Approaches

### 1. Server-Side Simulation API

The project includes ready-made API endpoints to simulate hardware behavior:

#### Simulate Waste Deposit

```javascript
// Example API call to simulate a deposit
fetch('http://localhost:5000/api/simulate/deposit', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': 'your-api-key'
  },
  body: JSON.stringify({
    userId: 'user123',
    binId: 'bin001',
    wasteType: 'recyclable_plastic',
    weight: 1.5
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

#### Simulate Bin Emptying

```javascript
// Example API call to simulate emptying a bin
fetch('http://localhost:5000/api/simulate/empty-bin', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': 'your-api-key'
  },
  body: JSON.stringify({
    binId: 'bin001'
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

### 2. QR Code Generation

For the QR code aspect of the system:

1. Use the admin panel's QR code generator under the Simulation page.
2. The QR code follows this format:
```json
{
  "bin_id": "bin001",
  "waste_type": "recyclable_plastic",
  "weight": 1.5
}
```
3. When scanned by the app, it simulates a user depositing that type and weight of waste.

### 3. Web Interface for Manual Testing

The admin panel provides a simulation interface where you can:

1. Select a user
2. Select a bin
3. Choose waste type
4. Set weight
5. Trigger a simulated deposit

This helps in quick testing without writing code.

## Advanced Simulation Techniques

### 1. Automating Random Deposits

You can create a script to simulate random deposits throughout the day:

```javascript
// simulateRandomDeposits.js
const fetch = require('node-fetch');
const API_URL = 'http://localhost:5000/api/simulate/deposit';
const API_KEY = 'your-api-key';

// Sample data
const users = ['user001', 'user002', 'user003', 'user004', 'user005'];
const bins = ['bin001', 'bin002', 'bin003', 'bin004'];
const wasteTypes = ['organic', 'recyclable_plastic', 'recyclable_paper', 'recyclable_metal'];

// Simulate a random deposit
function simulateRandomDeposit() {
  const userId = users[Math.floor(Math.random() * users.length)];
  const binId = bins[Math.floor(Math.random() * bins.length)];
  const wasteType = wasteTypes[Math.floor(Math.random() * wasteTypes.length)];
  const weight = (0.5 + Math.random() * 2).toFixed(1); // Random weight between 0.5 and 2.5 kg
  
  fetch(API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': API_KEY
    },
    body: JSON.stringify({
      userId,
      binId,
      wasteType,
      weight: parseFloat(weight)
    })
  })
  .then(response => response.json())
  .then(data => console.log(`Simulated deposit: ${weight}kg of ${wasteType} by ${userId} in ${binId}`))
  .catch(error => console.error('Error:', error));
}

// Simulate deposits at random intervals
function startSimulation() {
  // Simulate first deposit
  simulateRandomDeposit();
  
  // Schedule next deposit at random interval (between 1 and 5 minutes)
  const nextInterval = Math.floor(60000 + Math.random() * 240000);
  setTimeout(startSimulation, nextInterval);
  
  console.log(`Next deposit scheduled in ${Math.floor(nextInterval/1000)} seconds`);
}

console.log('Starting automated deposit simulation...');
startSimulation();
```

Run this with Node.js to simulate realistic usage patterns.

### 2. Fill Level Simulation

The system automatically updates bin fill levels based on deposits:

- Each waste deposit increases the bin's fill level
- The formula used is: `new_fill_level = current_fill_level + (weight * 2)`
  - This assumes 1kg of waste = 2% of bin capacity
- When a bin reaches 90% or higher, it's marked as "full"

### 3. Simulating Different Sensor Types

For presentation purposes, you can discuss different sensor types that would be used in a real implementation:

#### Weight Sensor (Load Cell)
- Would measure the exact weight of deposited waste
- Simulation: We directly specify weight in our API calls

#### Waste Type Detection
In a real implementation, this could be:
- Camera with ML for visual identification
- Multiple bins for different waste types
- User selection via touchscreen

Simulation: We specify waste type in the API call or QR code

#### Fill Level Sensor
Real implementations might use:
- Ultrasonic distance sensors
- Infrared sensors
- Pressure sensors at the bottom

Simulation: We calculate fill level based on deposited weight

## Creating a Demo Sensor Dashboard

For a more visual presentation, create a simple HTML dashboard to visualize the simulated sensors:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Smart Bin Sensor Simulation</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .sensor-value {
            font-size: 24px;
            font-weight: bold;
        }
        .sensor-card {
            transition: all 0.3s ease;
        }
        .sensor-card:hover {
            transform: translateY(-5px);
        }
        .fill-indicator {
            height: 200px;
            width: 100%;
            background-color: #f5f5f5;
            border-radius: 10px;
            position: relative;
            overflow: hidden;
        }
        .fill-level {
            position: absolute;
            bottom: 0;
            width: 100%;
            background-color: #28a745;
            transition: height 1s ease;
        }
        .weight-display {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 200px;
            border: 2px solid #f5f5f5;
            border-radius: 10px;
        }
    </style>
</head>
<body>
    <div class="container my-5">
        <h1 class="text-center mb-5">Smart Bin Sensor Simulation</h1>
        
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card sensor-card">
                    <div class="card-header">
                        <h5>Fill Level Sensor</h5>
                    </div>
                    <div class="card-body">
                        <div class="fill-indicator">
                            <div id="fillLevel" class="fill-level" style="height: 30%;"></div>
                        </div>
                        <div class="text-center mt-3">
                            <span id="fillPercentage" class="sensor-value">30%</span>
                            <p class="text-muted">Current Fill Level</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-6">
                <div class="card sensor-card">
                    <div class="card-header">
                        <h5>Weight Sensor</h5>
                    </div>
                    <div class="card-body">
                        <div class="weight-display">
                            <div>
                                <span id="currentWeight" class="sensor-value">0.0</span>
                                <span class="h2">kg</span>
                            </div>
                        </div>
                        <div class="text-center mt-3">
                            <span id="totalWeight" class="text-muted">Total: 45.5 kg collected</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-md-6">
                <div class="card sensor-card">
                    <div class="card-header">
                        <h5>Waste Type Detection</h5>
                    </div>
                    <div class="card-body text-center">
                        <div class="btn-group mb-3" role="group">
                            <button type="button" class="btn btn-outline-success active">Organic</button>
                            <button type="button" class="btn btn-outline-primary">Plastic</button>
                            <button type="button" class="btn btn-outline-warning">Paper</button>
                            <button type="button" class="btn btn-outline-secondary">Metal</button>
                        </div>
                        <div>
                            <span id="detectedType" class="sensor-value">Organic</span>
                            <p class="text-muted">Detected Waste Type</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-6">
                <div class="card sensor-card">
                    <div class="card-header">
                        <h5>Simulation Controls</h5>
                    </div>
                    <div class="card-body">
                        <form id="simulationForm">
                            <div class="mb-3">
                                <label for="wasteType" class="form-label">Waste Type</label>
                                <select id="wasteType" class="form-select">
                                    <option value="organic">Organic</option>
                                    <option value="recyclable_plastic">Plastic</option>
                                    <option value="recyclable_paper">Paper</option>
                                    <option value="recyclable_metal">Metal</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label for="wasteWeight" class="form-label">Weight (kg)</label>
                                <input type="number" class="form-control" id="wasteWeight" min="0.1" max="5" step="0.1" value="1.0">
                            </div>
                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary">Simulate Deposit</button>
                                <button type="button" id="emptyBinBtn" class="btn btn-outline-danger">Empty Bin</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row mt-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5>Sensor Log</h5>
                    </div>
                    <div class="card-body">
                        <pre id="sensorLog" style="max-height: 200px; overflow-y: auto;"></pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        let currentFillLevel = 30;
        let totalWeightCollected = 45.5;
        
        document.getElementById('simulationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get values
            const wasteType = document.getElementById('wasteType').value;
            const wasteWeight = parseFloat(document.getElementById('wasteWeight').value);
            
            // Update weight display with animation
            simulateWeightSensor(wasteWeight);
            
            // Update fill level
            updateFillLevel(wasteWeight);
            
            // Update waste type display
            updateWasteTypeDisplay(wasteType);
            
            // Log the event
            logEvent(`Deposit: ${wasteWeight}kg of ${formatWasteType(wasteType)}`);
            
            // Simulate API call (just logged here)
            logEvent(`API Call: POST /api/simulate/deposit {userId: "user001", binId: "bin001", wasteType: "${wasteType}", weight: ${wasteWeight}}`);
        });
        
        document.getElementById('emptyBinBtn').addEventListener('click', function() {
            // Reset fill level
            currentFillLevel = 0;
            updateFillLevelDisplay();
            
            // Log the event
            logEvent("Bin emptied");
            
            // Simulate API call (just logged here)
            logEvent(`API Call: POST /api/simulate/empty-bin {binId: "bin001"}`);
        });
        
        function simulateWeightSensor(weight) {
            // Reset current weight
            document.getElementById('currentWeight').textContent = "0.0";
            
            // Animate weight increase
            let currentDisplayWeight = 0;
            const interval = setInterval(() => {
                currentDisplayWeight += weight / 20;
                if (currentDisplayWeight >= weight) {
                    currentDisplayWeight = weight;
                    clearInterval(interval);
                    
                    // After a delay, reset to zero
                    setTimeout(() => {
                        document.getElementById('currentWeight').textContent = "0.0";
                    }, 3000);
                }
                document.getElementById('currentWeight').textContent = currentDisplayWeight.toFixed(1);
            }, 100);
            
            // Update total weight
            totalWeightCollected += weight;
            document.getElementById('totalWeight').textContent = `Total: ${totalWeightCollected.toFixed(1)} kg collected`;
        }
        
        function updateFillLevel(wasteWeight) {
            // Assume 1kg = 2% fill level
            currentFillLevel = Math.min(100, currentFillLevel + (wasteWeight * 2));
            updateFillLevelDisplay();
        }
        
        function updateFillLevelDisplay() {
            const fillLevelElement = document.getElementById('fillLevel');
            const fillPercentageElement = document.getElementById('fillPercentage');
            
            fillLevelElement.style.height = `${currentFillLevel}%`;
            fillPercentageElement.textContent = `${Math.round(currentFillLevel)}%`;
            
            // Change color based on fill level
            if (currentFillLevel < 50) {
                fillLevelElement.style.backgroundColor = '#28a745'; // Green
            } else if (currentFillLevel < 75) {
                fillLevelElement.style.backgroundColor = '#ffc107'; // Yellow
            } else {
                fillLevelElement.style.backgroundColor = '#dc3545'; // Red
            }
        }
        
        function updateWasteTypeDisplay(wasteType) {
            const displayElement = document.getElementById('detectedType');
            const buttons = document.querySelectorAll('.btn-group .btn');
            
            // Reset all buttons
            buttons.forEach(btn => btn.classList.remove('active'));
            
            // Set active button and update display
            switch(wasteType) {
                case 'organic':
                    displayElement.textContent = 'Organic';
                    buttons[0].classList.add('active');
                    break;
                case 'recyclable_plastic':
                    displayElement.textContent = 'Plastic';
                    buttons[1].classList.add('active');
                    break;
                case 'recyclable_paper':
                    displayElement.textContent = 'Paper';
                    buttons[2].classList.add('active');
                    break;
                case 'recyclable_metal':
                    displayElement.textContent = 'Metal';
                    buttons[3].classList.add('active');
                    break;
                default:
                    displayElement.textContent = 'Unknown';
            }
        }
        
        function formatWasteType(type) {
            switch(type) {
                case 'organic': return 'Organic';
                case 'recyclable_plastic': return 'Plastic';
                case 'recyclable_paper': return 'Paper';
                case 'recyclable_metal': return 'Metal';
                default: return type;
            }
        }
        
        function logEvent(message) {
            const logElement = document.getElementById('sensorLog');
            const timestamp = new Date().toLocaleTimeString();
            logElement.innerHTML += `[${timestamp}] ${message}\n`;
            logElement.scrollTop = logElement.scrollHeight;
        }
        
        // Log initial state
        logEvent("Sensor simulation initialized");
    </script>
</body>
</html>
```

Save this as `sensor-dashboard.html` in the project root for a visual demo of the simulated hardware.

## Presentation Tips

### 1. Physical Mock-up (Optional)

For a more engaging hackathon presentation:
- Create a cardboard/paper mockup of a smart bin
- Print the QR code and attach it to the mockup
- Demonstrate the app scanning the QR code on the mockup

### 2. Explaining the Hardware Concept

When presenting the project, clearly explain:
- What sensors would be used in a real implementation
- How the hardware and software would communicate
- Costs and feasibility for real-world deployment
- Potential challenges in hardware implementation

### 3. Future Hardware Integration

Discuss how the current simulation could be replaced with real hardware:
- Arduino or Raspberry Pi for bin controllers
- Load cells for weight measurement
- Ultrasonic sensors for fill level
- Camera modules for waste classification using ML
- ESP32/ESP8266 for WiFi connectivity

## Resource Links for Future Implementation

If the project continues beyond the hackathon:

1. **Load Cell (Weight Sensor) Integration**
   - [HX711 Load Cell Amplifier with Arduino](https://www.instructables.com/Arduino-Scale-With-HX711-Module/)

2. **Ultrasonic Sensor for Fill Level**
   - [HC-SR04 Ultrasonic Sensor with Arduino](https://www.instructables.com/HC-SR04-Ultrasonic-Sensor-With-Arduino/)

3. **Waste Classification with ML**
   - [TensorFlow Lite for Microcontrollers](https://www.tensorflow.org/lite/microcontrollers)
   - [Waste Classification Model Example](https://github.com/pedropro/TACO)

4. **WiFi Connectivity**
   - [ESP32 with Arduino IDE](https://randomnerdtutorials.com/getting-started-with-esp32/)
   - [Making HTTP Requests with ESP32](https://randomnerdtutorials.com/esp32-http-get-post-arduino/)

5. **QR Code Generation**
   - [E-Paper Display for QR Codes](https://www.waveshare.com/wiki/E-Paper_ESP32_Driver_Board)