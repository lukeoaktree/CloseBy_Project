const express = require("express");
const db = require("./db");
const cors = require("cors");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");

const app = express(); // Initialize the app
app.use(cors());
app.use(bodyParser.json());

// REGISTER USER API
app.post("/api/register", async (req, res) => {
    console.log(req.body); 

    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: "Email and password are required." });
    }

    try {
        // Hash password before storing it
        const hashedPassword = await bcrypt.hash(password, 10);

        const query = 'INSERT INTO members (`email`, `password`) VALUES (?, ?)';
        db.query(query, [email, hashedPassword], (err, result) => {
            if (err) {
                console.log("Database error:", err);
                return res.status(500).json({ error: err.message });
            }   
            res.json({ message: "User registered successfully!" });
        });
    } catch (error) {
        res.status(500).json({ error: "Error registering user" });
    }
});

// Neighborhood Creation API**
app.post("/createNeighborhood", (req, res) => {
    const { name, latitude, longitude, user_id } = req.body;

    if (!name || !latitude || !longitude || !user_id) {
        return res.status(400).json({ message: "All fields are required." });
    }

    const neighborhoodId = Math.random().toString(36).substring(2, 8).toUpperCase(); // Unique 6-character ID

    const sql = "INSERT INTO neighborhoods (neighborhood_id, name, location, created_by, join_code) VALUES (?, ?, ST_GeomFromText('POINT(? ?)'), ?, ?)";
    db.query(sql, [neighborhoodId, name, longitude, latitude, user_id, Math.random().toString(36).substring(2, 8).toUpperCase()], (err, result) => {
        if (err) {
            console.error("Error inserting neighborhood:", err);
            return res.status(500).json({ message: "Database error" });
        }
        res.status(201).json({ message: "Neighborhood created successfully", neighborhoodId });
    });
});



// Start the server
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
