/**
 * server.js
 *
 * Node.js + Express + MySQL (XAMPP)
 * - bcrypt authentication (no JWT, just login check)
 * - open CORS (any domain allowed)
 * - simple REST API for players, sessions, performance, feedback, survey, and reports
 * - Enhanced with comprehensive logging
 *
 * ⚡ Setup:
 * npm init -y
 * npm install express mysql2 dotenv bcrypt cors 
 *
 * Create .env in project root:
 * PORT=4000
 * MYSQL_HOST=127.0.0.1
 * MYSQL_PORT=3306
 * MYSQL_USER=root
 * MYSQL_PASSWORD=
 * MYSQL_DATABASE=silver_strikers_db
 *
 * Run: node server.js
 */

const express = require("express");
const dotenv = require("dotenv");
const mysql = require("mysql2/promise");
const bcrypt = require("bcrypt");
const cors = require("cors");
dotenv.config();

/* -------------------------
   Config
   ------------------------- */
const PORT = process.env.PORT || 4000;

console.log("🔧 Initializing server configuration...");
console.log(`📋 Environment: ${process.env.NODE_ENV || 'development'}`);
console.log(`🔌 Port: ${PORT}`);
console.log(`🗄️  Database: ${process.env.MYSQL_DATABASE || "silver_strikers_db"}`);
console.log(`🏠 MySQL Host: ${process.env.MYSQL_HOST || "127.0.0.1"}:${process.env.MYSQL_PORT || 3306}`);

const pool = mysql.createPool({
  host: process.env.MYSQL_HOST || "127.0.0.1",
  port: process.env.MYSQL_PORT || 3306,
  user: process.env.MYSQL_USER || "root",
  password: process.env.MYSQL_PASSWORD || "",
  database: process.env.MYSQL_DATABASE || "silver_strikers_db",
});

async function query(sql, params = []) {
  try {
    console.log(`🔍 Executing query: ${sql.substring(0, 50)}...`);
    const [rows] = await pool.execute(sql, params);
    console.log(`✅ Query successful, returned ${rows.length || rows.affectedRows || 0} rows`);
    return rows;
  } catch (err) {
    console.error(`❌ Query failed: ${err.message}`);
    throw err;
  }
}

// Helper: combine date (YYYY-MM-DD) and time (HH:mm or HH:mm:ss) into 'YYYY-MM-DD HH:mm:ss'
function combineDateAndTime(dateStr, timeStr) {
  if (!dateStr || !timeStr) return null;
  try {
    const time = timeStr.length === 5 ? `${timeStr}:00` : timeStr;
    const d = new Date(`${dateStr}T${time}`);
    if (isNaN(d.getTime())) return null;
    const pad = (n) => String(n).padStart(2, "0");
    const out = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    return out;
  } catch (e) {
    return null;
  }
}

// Normalize date strings like 'MM/DD/YYYY' to 'YYYY-MM-DD' for SQL DATETIME
function normalizeDateInput(dateStr) {
  if (!dateStr) return null;
  if (/^\d{2}\/\d{2}\/\d{4}$/.test(dateStr)) {
    const [mm, dd, yyyy] = dateStr.split('/');
    const pad = (n) => String(n).padStart(2, '0');
    return `${yyyy}-${pad(mm)}-${pad(dd)}`;
  }
  return dateStr;
}

/* Helper utilities */
function toFiniteNumberOrNull(val) {
  if (val === null || typeof val === 'undefined') return null;
  const n = typeof val === 'number' ? val : Number(val);
  if (!Number.isFinite(n)) return null;
  return n;
}

/* -------------------------
   Init Tables
   ------------------------- */
async function initDb() {
  console.log("🏗️  Starting database initialization...");
  try {
    console.log("📊 Creating users table...");
    await query(
      `CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(191) UNIQUE,
        email VARCHAR(255),
        password_hash VARCHAR(191),
        role ENUM('player','coach') NOT NULL DEFAULT 'player',
        full_name VARCHAR(255),
        phone VARCHAR(50),
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )`
    );
    console.log("✅ Users table ready");

    console.log("📊 Creating sessions table...");
    await query(
      `CREATE TABLE IF NOT EXISTS sessions (
        id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(255),
        description TEXT,
        coach_id INT,
        start_time DATETIME,
        end_time DATETIME,
        session_type VARCHAR(64),
        location VARCHAR(255),
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE SET NULL
      )`
    );
    console.log("✅ Sessions table ready");

    console.log("📊 Creating performance table...");
    await query(
      `CREATE TABLE IF NOT EXISTS performance (
        id INT AUTO_INCREMENT PRIMARY KEY,
        player_id INT,
        session_id INT,
        distance_meters DOUBLE,
        speed DOUBLE,
        acceleration DOUBLE,
        deceleration DOUBLE,
        cadence_spm DOUBLE,
        heart_rate INT,
        recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (player_id) REFERENCES users(id),
        FOREIGN KEY (session_id) REFERENCES sessions(id)
      )`
    );
    console.log('✅ Performance table ready');

    console.log("📊 Creating feedback table...");
    await query(
      `CREATE TABLE IF NOT EXISTS feedback (
        id INT AUTO_INCREMENT PRIMARY KEY,
        coach_id INT,
        player_id INT,
        session_id INT,
        notes TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (coach_id) REFERENCES users(id),
        FOREIGN KEY (player_id) REFERENCES users(id),
        FOREIGN KEY (session_id) REFERENCES sessions(id)
      )`
    );
    console.log("✅ Feedback table ready");

    console.log("📊 Creating survey table...");
    await query(
      `CREATE TABLE IF NOT EXISTS survey (
        id INT AUTO_INCREMENT PRIMARY KEY,
        player_id INT,
        session_id INT,
        response TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (player_id) REFERENCES users(id),
        FOREIGN KEY (session_id) REFERENCES sessions(id)
      )`
    );
    console.log("✅ Survey table ready");

    console.log("✅ Database initialization complete!");
  } catch (err) {
    console.error("❌ Database initialization failed:", err.message);
    throw err;
  }
}

/* -------------------------
   Express App
   ------------------------- */
const app = express();

console.log("🚀 Configuring Express middleware...");

// Middleware to parse JSON and URL-encoded form data
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());
console.log("✅ CORS and body parsers configured");

// Log all incoming requests
app.use((req, res, next) => {
  console.log(`\n🌐 ${new Date().toISOString()} - ${req.method} ${req.path}`);
  
  if (req.body && Object.keys(req.body).length > 0) {
    console.log(`📦 Request body:`, req.body);
  }

  if (req.query && Object.keys(req.query).length > 0) {
    console.log(`❓ Query params:`, req.query);
  }

  next();
});

// Health check
app.get("/health", (req, res) => {
  res.json({ status: "ok", time: new Date().toISOString() });
});


/* -------------------------
   AUTH
   ------------------------- */
app.post("/auth/register", async (req, res) => {
  console.log("👤 Processing registration request...");
  try {
    const { username, email, password, role, full_name, phone } = req.body;

    const finalUsername = username || (email ? email.split('@')[0] : null);
    if ((!finalUsername && !email) || !password) {
      console.log("⚠️ Registration failed: Missing email/username or password");
      return res.status(400).json({ error: "Missing email/username or password" });
    }

    console.log(`📝 Registering new user: ${finalUsername || email} (role: ${role || 'player'})`);
    const hashed = await bcrypt.hash(password, 10);
    console.log("🔒 Password hashed successfully");
    
    await query(
      `INSERT INTO users (username, email, password_hash, role, full_name, phone) VALUES (?, ?, ?, ?, ?, ?)`,
      [finalUsername, email || null, hashed, role || "player", full_name || null, phone || null]
    );

    console.log(`✅ User '${finalUsername || email}' registered successfully`);
    res.json({ success: true, message: "User registered" });
  } catch (err) {
    console.error("❌ Registration error:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.post("/auth/login", async (req, res) => {
  console.log("🔐 Processing login request...");
  try {
    const { email, username, password } = req.body;
    const identifier = email || username;
    console.log(`👤 Login attempt for: ${identifier}`);

    if (!identifier || !password) {
      return res.status(400).json({ error: "Missing email/username or password" });
    }

    const users = email
      ? await query(`SELECT * FROM users WHERE email = ?`, [email])
      : await query(`SELECT * FROM users WHERE username = ?`, [username]);
    const user = users[0];
    
    if (!user) {
      console.log(`⚠️ Login failed: User not found for identifier '${identifier}'`);
      return res.status(401).json({ error: "Invalid credentials" });
    }

    console.log("🔍 Verifying password...");
    const match = await bcrypt.compare(password, user.password_hash);
    
    if (!match) {
      console.log(`⚠️ Login failed: Invalid password for '${identifier}'`);
      return res.status(401).json({ error: "Invalid credentials" });
    }

    console.log(`✅ User '${user.username || user.email}' logged in successfully (ID: ${user.id}, Role: ${user.role})`);
    res.json({ success: true, user: { id: user.id, username: user.username, email: user.email, role: user.role } });
  } catch (err) {
    console.error("❌ Login error:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
  USERS
  ------------------------- */
// List users with optional search and limit
app.get("/users", async (req, res) => {
  try {
    const q = (req.query.q || "").toString().trim();
    const limitParam = parseInt(req.query.limit || "50", 10);
    const limit = Number.isFinite(limitParam) ? Math.min(Math.max(limitParam, 1), 200) : 50;
    let users;
    if (q) {
      const like = `%${q}%`;
      users = await query(
        `SELECT id, username, email, role, full_name, phone, created_at
         FROM users
         WHERE username LIKE ? OR full_name LIKE ? OR email LIKE ?
         ORDER BY created_at DESC
         LIMIT ?`,
        [like, like, like, limit]
      );
    } else {
      users = await query(
        `SELECT id, username, email, role, full_name, phone, created_at
         FROM users
         ORDER BY created_at DESC
         LIMIT ?`,
        [limit]
      );
    }
    res.json(users);
  } catch (err) {
    console.error("❌ Error listing users:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/users/:id", async (req, res) => {
  console.log(`👤 Fetching user with ID: ${req.params.id}`);
  try {
    const user = await query(`SELECT id, username, role, full_name, email, phone, created_at FROM users WHERE id = ?`, [req.params.id]);
    
    if (!user[0]) {
      console.log(`⚠️ User with ID ${req.params.id} not found`);
      return res.status(404).json({ error: "Not found" });
    }
    
    console.log(`✅ User found: ${user[0].username}`);
    res.json(user[0]);
  } catch (err) {
    console.error("❌ Error fetching user:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// Update user profile
app.put("/users/:id", async (req, res) => {
  console.log(`✏️ Updating user with ID: ${req.params.id}`);
  try {
    const {
      username,
      email,
      full_name,
      phone,
      role,
      current_password,
      new_password,
      password,
    } = req.body;

    // Validate role if provided
    let finalRole = null;
    if (typeof role !== "undefined") {
      const r = String(role).toLowerCase();
      if (r !== "player" && r !== "coach") {
        return res.status(400).json({ error: "Invalid role. Must be 'player' or 'coach'" });
      }
      finalRole = r;
    }

    // Handle password update if requested
    let newPasswordHash = null;
    const rawNewPassword = (typeof new_password !== "undefined" && new_password !== null && String(new_password).trim() !== "")
      ? String(new_password)
      : (typeof password !== "undefined" && password !== null && String(password).trim() !== "")
        ? String(password)
        : null;
    if (rawNewPassword) {
      // If current_password is provided, verify it matches existing password
      if (typeof current_password !== "undefined" && current_password !== null && String(current_password).length > 0) {
        const existing = await query(`SELECT password_hash FROM users WHERE id = ?`, [req.params.id]);
        if (!existing[0]) {
          return res.status(404).json({ error: "User not found" });
        }
        const ok = await bcrypt.compare(String(current_password), existing[0].password_hash || "");
        if (!ok) {
          return res.status(400).json({ error: "Current password is incorrect" });
        }
      } else {
        // No current password provided. In a production app, you should enforce proper authorization here.
        console.warn("⚠️ Password change without current_password verification. Ensure this route is protected in production.");
      }
      // Basic new password validation
      const np = rawNewPassword;
      if (np.length < 6) {
        return res.status(400).json({ error: "New password must be at least 6 characters" });
      }
      newPasswordHash = await bcrypt.hash(np, 10);
    }

    // Build dynamic update list
    const setClauses = [];
    const params = [];
    setClauses.push("username = COALESCE(?, username)");
    params.push(username || null);
    setClauses.push("email = COALESCE(?, email)");
    params.push(email || null);
    setClauses.push("full_name = COALESCE(?, full_name)");
    params.push(full_name || null);
    setClauses.push("phone = COALESCE(?, phone)");
    params.push(phone || null);
    if (typeof role !== "undefined") {
      setClauses.push("role = COALESCE(?, role)");
      params.push(finalRole);
    }
    if (newPasswordHash) {
      setClauses.push("password_hash = COALESCE(?, password_hash)");
      params.push(newPasswordHash);
    }

    if (setClauses.length === 0) {
      return res.json({ success: true, message: "No changes applied" });
    }

    await query(
      `UPDATE users
       SET ${setClauses.join(", ")}
       WHERE id = ?`,
      [...params, req.params.id]
    );

    const updated = await query(
      `SELECT id, username, email, role, full_name, phone, created_at
       FROM users WHERE id = ?`,
      [req.params.id]
    );
    console.log(`✅ User updated: ${updated[0]?.username || req.params.id}`);
    res.json(updated[0] || { success: true });
  } catch (err) {
    console.error("❌ Error updating user:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
   SESSIONS
   ------------------------- */
app.post("/sessions", async (req, res) => {
  console.log("📅 Creating new session...");
  try {
    const {
      title,
      description,
      coach_id,
      start_time,
      end_time,
      session_type,
      location,
      // UI-shaped fields
      sessionName,
      name,
      sessionType,
      type,
      date,
      startTime,
      endTime,
      notes,
    } = req.body;

    const finalTitle = title || sessionName || name || "Untitled Session";
    const finalDescription = description || notes || null;
    const finalType = session_type || sessionType || type || null;
    const finalLocation = location || null;

    const dateNorm = date ? normalizeDateInput(date) : null;
    const finalStart = start_time || (dateNorm && startTime ? combineDateAndTime(dateNorm, startTime) : null);
    const finalEnd = end_time || (dateNorm && endTime ? combineDateAndTime(dateNorm, endTime) : null);

    console.log(`📝 Session details: "${finalTitle}" coach_id=${coach_id || null}`);
    console.log(`   start=${finalStart} end=${finalEnd} type=${finalType} location=${finalLocation}`);

    const result = await query(
      `INSERT INTO sessions (title, description, coach_id, start_time, end_time, session_type, location)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [finalTitle, finalDescription, coach_id || null, finalStart, finalEnd, finalType, finalLocation]
    );
    
    console.log(`✅ Session created with ID: ${result.insertId}`);
    res.json({ success: true, sessionId: result.insertId });
  } catch (err) {
    console.error("❌ Error creating session:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/sessions", async (req, res) => {
  console.log("📅 Fetching all sessions...");
  try {
    const sessions = await query(`SELECT * FROM sessions`);
    console.log(`✅ Found ${sessions.length} sessions`);
    res.json(sessions);
  } catch (err) {
    console.error("❌ Error fetching sessions:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/sessions/:id", async (req, res) => {
  console.log(`📅 Fetching session with ID: ${req.params.id}`);
  try {
    const session = await query(`SELECT * FROM sessions WHERE id = ?`, [req.params.id]);
    
    if (!session[0]) {
      console.log(`⚠️ Session with ID ${req.params.id} not found`);
      return res.status(404).json({ error: "Not found" });
    }
    
    console.log(`✅ Session found: "${session[0].title}"`);
    res.json(session[0]);
  } catch (err) {
    console.error("❌ Error fetching session:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
   PERFORMANCE
   ------------------------- */
app.post("/performance", async (req, res) => {
  console.log("📊 Recording performance data...");
  try {
    const { player_id, session_id, distance_meters, speed, acceleration, deceleration, heart_rate, cadence_spm } = req.body;

    const playerId = toFiniteNumberOrNull(player_id);
    const sessionId = toFiniteNumberOrNull(session_id);
    const dist = toFiniteNumberOrNull(distance_meters);
    const spd = toFiniteNumberOrNull(speed);
    const accel = toFiniteNumberOrNull(acceleration);
    const decel = toFiniteNumberOrNull(deceleration);
    const hr = toFiniteNumberOrNull(heart_rate);
    const cadence = toFiniteNumberOrNull(cadence_spm);

    console.log(`📝 Performance data for player ID: ${playerId}, session ID: ${sessionId}`);
    console.log(`   Distance: ${dist}m, Speed: ${spd}, HR: ${hr}, Cadence: ${cadence}`);
    
    const result = await query(
      `INSERT INTO performance (player_id, session_id, distance_meters, speed, acceleration, deceleration, cadence_spm, heart_rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [playerId, sessionId, dist, spd, accel, decel, cadence, hr]
    );
    
    console.log(`✅ Performance data recorded with ID: ${result.insertId}`);
    res.json({ success: true, performanceId: result.insertId });
  } catch (err) {
    console.error("❌ Error recording performance:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/performance/:playerId", async (req, res) => {
  console.log(`📊 Fetching performance data for player ID: ${req.params.playerId}`);
  try {
    const data = await query(`SELECT * FROM performance WHERE player_id = ?`, [req.params.playerId]);
    console.log(`✅ Found ${data.length} performance records`);
    res.json(data);
  } catch (err) {
    console.error("❌ Error fetching performance data:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
   FEEDBACK
   ------------------------- */
app.post("/feedback", async (req, res) => {
  console.log("💬 Creating feedback...");
  try {
    const { coach_id, player_id, session_id, notes } = req.body;
    console.log(`📝 Feedback from coach ID: ${coach_id} to player ID: ${player_id}`);
    console.log(`   Session ID: ${session_id}`);
    console.log(`   Notes preview: ${notes?.substring(0, 50)}...`);
    
    const result = await query(
      `INSERT INTO feedback (coach_id, player_id, session_id, notes) VALUES (?, ?, ?, ?)`,
      [coach_id, player_id, session_id, notes]
    );
    
    console.log(`✅ Feedback created with ID: ${result.insertId}`);
    res.json({ success: true, feedbackId: result.insertId });
  } catch (err) {
    console.error("❌ Error creating feedback:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/feedback/:playerId", async (req, res) => {
  console.log(`💬 Fetching feedback for player ID: ${req.params.playerId}`);
  try {
    const data = await query(`SELECT * FROM feedback WHERE player_id = ?`, [req.params.playerId]);
    console.log(`✅ Found ${data.length} feedback entries`);
    res.json(data);
  } catch (err) {
    console.error("❌ Error fetching feedback:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
   SURVEY
   ------------------------- */
app.post("/survey", async (req, res) => {
  console.log("📝 Submitting survey response...");
  try {
    const { player_id, session_id, response, rating, condition, performance, notes } = req.body;
    console.log(`📋 Survey from player ID: ${player_id} for session ID: ${session_id}`);
    const payload = response || { rating, condition, performance, notes };
    console.log(`   Response keys: ${Object.keys(payload || {}).join(', ')}`);
    
    const result = await query(
      `INSERT INTO survey (player_id, session_id, response) VALUES (?, ?, ?)`,
      [player_id, session_id, JSON.stringify(payload || {})]
    );
    
    console.log(`✅ Survey response saved with ID: ${result.insertId}`);
    res.json({ success: true, surveyId: result.insertId });
  } catch (err) {
    console.error("❌ Error saving survey:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get("/survey/:sessionId", async (req, res) => {
  console.log(`📝 Fetching survey responses for session ID: ${req.params.sessionId}`);
  try {
    const data = await query(`SELECT * FROM survey WHERE session_id = ?`, [req.params.sessionId]);
    console.log(`✅ Found ${data.length} survey responses`);
    
    const parsed = data.map((s) => ({ ...s, response: JSON.parse(s.response) }));
    res.json(parsed);
  } catch (err) {
    console.error("❌ Error fetching surveys:", err.message);
    res.status(500).json({ error: err.message });
  }
});

/* -------------------------
   REPORT
   ------------------------- */
app.get("/reports/:sessionId", async (req, res) => {
  console.log(`📊 Generating comprehensive report for session ID: ${req.params.sessionId}`);
  try {
    const sessionId = req.params.sessionId;
    
    console.log("  📅 Fetching session details...");
    const session = await query(`SELECT * FROM sessions WHERE id = ?`, [sessionId]);
    
    console.log("  🏃 Fetching performance data...");
    const perf = await query(`SELECT * FROM performance WHERE session_id = ?`, [sessionId]);
    
    console.log("  💬 Fetching feedback (with coach usernames)...");
    const feedback = await query(
      `SELECT f.*, u.username AS coach_username
       FROM feedback f
       LEFT JOIN users u ON u.id = f.coach_id
       WHERE f.session_id = ?`,
      [sessionId]
    );
    
    console.log("  📝 Fetching survey responses...");
    const survey = await query(`SELECT * FROM survey WHERE session_id = ?`, [sessionId]);

    const report = {
      session: session[0] || null,
      performances: perf,
      feedback,
      survey: survey.map((s) => ({ ...s, response: JSON.parse(s.response) })),
    };

    console.log(`✅ Report generated successfully:`);
    console.log(`   - Session: ${session[0] ? 'Found' : 'Not found'}`);
    console.log(`   - Performance records: ${perf.length}`);
    console.log(`   - Feedback entries: ${feedback.length}`);
    console.log(`   - Survey responses: ${survey.length}`);
    
    res.json(report);
  } catch (err) {
    console.error("❌ Error generating report:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(`💥 Unhandled error: ${err.message}`);
  console.error(err.stack);
  res.status(500).json({ error: "Internal server error" });
});

// 404 handler
app.use((req, res) => {
  console.log(`⚠️ 404 - Route not found: ${req.method} ${req.path}`);
  res.status(404).json({ error: "Route not found" });
});

/* -------------------------
   Start Server
   ------------------------- */
console.log("\n🚀 Starting server initialization...");
initDb()
  .then(() => {
    app.listen(PORT, () => {
      console.log("\n" + "=".repeat(50));
      console.log(`🎉 SERVER SUCCESSFULLY STARTED!`);
      console.log(`🌐 Running on: http://localhost:${PORT}`);
      console.log(`📅 Started at: ${new Date().toISOString()}`);
      console.log("=".repeat(50) + "\n");
      console.log("👀 Watching for incoming requests...\n");
    });
  })
  .catch((err) => {
    console.error("💥 Failed to start server:", err.message);
    console.error(err.stack);
    process.exit(1);
  });