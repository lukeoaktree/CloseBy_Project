const express = require("express");
const db = require("./db");
const cors = require("cors");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");

const app = express(); // initialize the app
app.use(cors());
app.use(bodyParser.json());

// register user
app.post("/api/register", async (req, res) => {
    console.log("User Registered"); 
    console.log(req.body); 

    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: "Email and password are required." });
    }

    try {
        // hash password
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

// neighborhood creation 
app.post("/createNeighborhood", async (req, res) => {
    
    console.log("Neighborhood Registered"); 
    console.log(req.body); 
    
    const { name, latitude, longitude, user_id } = req.body;

    if (!name || !latitude || !longitude || !user_id) {
        return res.status(400).json({ message: "All fields are required." });
    }

    const generateJoinCode = async () => {
        let joinCode;
        let exists = true;
    
        while (exists) {
            joinCode = Math.random().toString(36).substring(2, 8).toUpperCase(); // generate random 6 character code
    
            // check if the code already exists in the database
            const sql = "SELECT COUNT(*) AS count FROM neighborhoods WHERE join_code = ?";
            const [rows] = await db.promise().query(sql, [joinCode]);
    
            if (rows[0].count === 0) {
                exists = false; // proceed if its unique
            }
        }
    
        return joinCode;
    };

    const generateNeighborhoodId = () => {
        return Math.floor(100000 + Math.random() * 900000); // unique neighborhood id
    };

    try {
        const neighborhoodId = generateNeighborhoodId();
        const joinCode = await generateJoinCode();

        const sql = "INSERT INTO neighborhoods (neighborhood_id, name, location, created_by, join_code) VALUES (?, ?, ST_GeomFromText(?), ?, ?)";
        const values = [neighborhoodId, name, `POINT(${longitude} ${latitude})`, user_id, joinCode];

        db.query(sql, values, (err, result) => {
            if (err) {
                console.error("Error inserting neighborhood:", err);
                return res.status(500).json({ error: "Database error" });
            }
            res.json({ message: "Neighborhood added successfully!", neighborhoodId, joinCode });
        });
    } catch (err) {
        console.error("Error:", err);
        res.status(500).json({ error: "Server error" });
    }
});

// start the server
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
