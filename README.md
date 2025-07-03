# Node.js TypeScript API Backend

This folder contains the Node.js + TypeScript backend for the NativeSensor app. It provides REST API endpoints for user registration and other features, connecting to a MySQL database (e.g., running in XAMPP).

## Features
- User registration endpoint
- MySQL database auto-creation if not present
- TypeScript for type safety

## Setup
1. Install dependencies:
   ```sh
   npm install
   ```
2. Configure your MySQL (XAMPP) credentials in `.env`.
3. Start the server:
   ```sh
   npm run dev
   ```

## Endpoints
- `POST /api/register` — Register a new user

--- 