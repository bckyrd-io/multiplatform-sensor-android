### Steps to run your server:
1. **Initialize your Node.js project (only once):**
```bash
npm init -y
```


2. **Install dependencies:**
```bash
npm install express mysql2 dotenv bcrypt cors 
```


3. **Create a `.env` file** in your project root with:
```env
PORT=4000
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=
MYSQL_DATABASE=perf_db
```


4. **Run your server:**
```bash
node server.js
```


You should then see logs like:
```
✅ Database initialization complete!
🎉 SERVER SUCCESSFULLY STARTED!
🌐 Running on: http://localhost:4000