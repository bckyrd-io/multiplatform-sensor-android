import express from 'express';
import type { Request, Response } from 'express';
import { ensureDatabase, getDbConnection } from './db';
import dotenv from 'dotenv';
dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

// Ensure DB and tables exist
(async () => {
    await ensureDatabase();
    const pool = getDbConnection();
    
    // Create users table with role support
    await pool.query(`CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        role ENUM('user', 'admin') DEFAULT 'user',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`);
    
    // Create sensor_data table
    await pool.query(`CREATE TABLE IF NOT EXISTS sensor_data (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        heart_rate INT,
        steps INT,
        calories INT,
        activity_type VARCHAR(50),
        latitude DECIMAL(10, 8),
        longitude DECIMAL(11, 8),
        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )`);
    
    // Create admin user if not exists
    const [adminUsers] = await pool.query('SELECT * FROM users WHERE role = "admin"');
    if (Array.isArray(adminUsers) && adminUsers.length === 0) {
        await pool.query('INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)', 
            ['Admin User', 'admin@example.com', 'admin123', 'admin']);
    }
})();

app.post('/api/register', (req, res) => {
    void (async () => {
        const { name, email, password } = req.body;
        if (!name || !email || !password) {
            return res.status(400).json({ success: false, message: 'Missing fields' });
        }
        try {
            const pool = getDbConnection();
            await pool.query('INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)', 
                [name, email, password, 'user']);
            res.json({ success: true, message: 'Registration successful!' });
        } catch (err: any) {
            if (err.code === 'ER_DUP_ENTRY') {
                res.status(409).json({ success: false, message: 'Email already registered.' });
            } else {
                res.status(500).json({ success: false, message: err.message });
            }
        }
    })();
});

app.post('/api/login', (req, res) => {
    void (async () => {
        const { email, password } = req.body;
        if (!email || !password) {
            return res.status(400).json({ success: false, message: 'Missing fields' });
        }
        try {
            const pool = getDbConnection();
            const [rows] = await pool.query('SELECT id, name, email, role FROM users WHERE email = ? AND password = ?', 
                [email, password]);
            if (Array.isArray(rows) && rows.length > 0) {
                const user = rows[0] as any;
                res.json({ 
                    success: true, 
                    message: 'Login successful!',
                    user: {
                        id: user.id,
                        name: user.name,
                        email: user.email,
                        role: user.role
                    }
                });
            } else {
                res.status(401).json({ success: false, message: 'Invalid email or password.' });
            }
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

// Get all users (admin only)
app.get('/api/users', (req, res) => {
    void (async () => {
        try {
            const pool = getDbConnection();
            const [rows] = await pool.query('SELECT id, name, email, role, created_at FROM users ORDER BY created_at DESC');
            res.json({ success: true, users: rows });
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

// Submit sensor data from wear device
app.post('/api/sensor-data', (req, res) => {
    void (async () => {
        const { user_id, heart_rate, steps, calories, activity_type, latitude, longitude } = req.body;
        if (!user_id) {
            return res.status(400).json({ success: false, message: 'User ID is required' });
        }
        try {
            const pool = getDbConnection();
            await pool.query(
                'INSERT INTO sensor_data (user_id, heart_rate, steps, calories, activity_type, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)',
                [user_id, heart_rate, steps, calories, activity_type, latitude, longitude]
            );
            res.json({ success: true, message: 'Sensor data saved successfully!' });
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

// Get sensor data for a specific user (admin can view all, users can only view their own)
app.get('/api/sensor-data/:userId?', (req, res) => {
    void (async () => {
        const { userId } = req.params;
        const { user_role, current_user_id } = req.query;
        
        try {
            const pool = getDbConnection();
            let query = 'SELECT sd.*, u.name as user_name FROM sensor_data sd JOIN users u ON sd.user_id = u.id';
            let params: any[] = [];
            
            if (user_role === 'admin') {
                // Admin can view all data or filter by specific user
                if (userId) {
                    query += ' WHERE sd.user_id = ?';
                    params.push(userId);
                }
            } else {
                // Regular users can only view their own data
                query += ' WHERE sd.user_id = ?';
                params.push(current_user_id || userId);
            }
            
            query += ' ORDER BY sd.timestamp DESC LIMIT 100';
            
            const [rows] = await pool.query(query, params);
            res.json({ success: true, sensorData: rows });
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

// Get user statistics
app.get('/api/user-stats/:userId?', (req, res) => {
    void (async () => {
        const { userId } = req.params;
        const { user_role, current_user_id } = req.query;
        
        try {
            const pool = getDbConnection();
            const targetUserId = user_role === 'admin' ? (userId || null) : (current_user_id || userId);
            
            if (!targetUserId) {
                return res.status(400).json({ success: false, message: 'User ID is required' });
            }
            
            // Get total stats
            const [totalStats] = await pool.query(`
                SELECT 
                    COUNT(*) as total_records,
                    AVG(heart_rate) as avg_heart_rate,
                    SUM(steps) as total_steps,
                    SUM(calories) as total_calories,
                    MAX(timestamp) as last_activity
                FROM sensor_data 
                WHERE user_id = ?
            `, [targetUserId]);
            
            // Get today's stats
            const [todayStats] = await pool.query(`
                SELECT 
                    COUNT(*) as today_records,
                    AVG(heart_rate) as today_avg_heart_rate,
                    SUM(steps) as today_steps,
                    SUM(calories) as today_calories
                FROM sensor_data 
                WHERE user_id = ? AND DATE(timestamp) = CURDATE()
            `, [targetUserId]);
            
            res.json({ 
                success: true, 
                totalStats: totalStats[0],
                todayStats: todayStats[0]
            });
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

app.listen(port, () => {
    console.log(`Server running on port ${port}`);
}); 