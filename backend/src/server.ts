import express from 'express';
import type { Request, Response } from 'express';
import { ensureDatabase, getDbConnection } from './db';
import dotenv from 'dotenv';
dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

// Ensure DB and users table exist
(async () => {
    await ensureDatabase();
    const pool = getDbConnection();
    await pool.query(`CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL
    )`);
})();

app.post('/api/register', (req, res) => {
    void (async () => {
        const { name, email, password } = req.body;
        if (!name || !email || !password) {
            return res.status(400).json({ success: false, message: 'Missing fields' });
        }
        try {
            const pool = getDbConnection();
            await pool.query('INSERT INTO users (name, email, password) VALUES (?, ?, ?)', [name, email, password]);
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
            const [rows] = await pool.query('SELECT * FROM users WHERE email = ? AND password = ?', [email, password]);
            if (Array.isArray(rows) && rows.length > 0) {
                res.json({ success: true, message: 'Login successful!' });
            } else {
                res.status(401).json({ success: false, message: 'Invalid email or password.' });
            }
        } catch (err: any) {
            res.status(500).json({ success: false, message: err.message });
        }
    })();
});

app.listen(port, () => {
    console.log(`Server running on port ${port}`);
}); 