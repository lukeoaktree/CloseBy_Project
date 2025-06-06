const express = require("express");
const db = require("./db");
const cors = require("cors");
const bodyParser = require("body-parser");

const app = express(); // initialize the app

app.use(cors());
app.use(bodyParser.json()); 

const {Firestore} = require('@google-cloud/firestore');
const firestore = new Firestore();

// register user (email only, password now handled by Firebase)
app.post("/api/register", (req, res) => {
    console.log("User Registered");
    console.log(req.body);

    const { email } = req.body;

    if (!email) {
        return res.status(400).json({ error: "Email is required." });
    }

    const query = 'INSERT INTO members (`email`) VALUES (?)';
    db.query(query, [email], (err, result) => {
        if (err) {
            console.log("Database error:", err);
            return res.status(500).json({ error: err.message });
        }
        res.json({ message: "User registered successfully!" });
    });
});

// neighborhood creation 
// neighborhood creation
app.post("/createNeighborhood", async (req, res) => {
    console.log("REQ.BODY = ", req.body);
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
            joinCode = Math.random().toString(36).substring(2, 8).toUpperCase();
            const sql = "SELECT COUNT(*) AS count FROM neighborhoods WHERE join_code = ?";
            const [rows] = await db.query(sql, [joinCode]);

            if (rows[0].count === 0) {
                exists = false;
            }
        }

        return joinCode;
    };

    const generateNeighborhoodId = () => {
        return Math.floor(100000 + Math.random() * 900000);
    };

    try {
        const checkSql = "SELECT COUNT(*) AS count FROM neighborhoods WHERE name = ?";
        const [checkRows] = await db.query(checkSql, [name]);

        if (checkRows[0].count > 0) {
            return res.status(400).json({ message: "Neighborhood with this name already exists." });
        }

        const neighborhoodId = generateNeighborhoodId();
        const joinCode = await generateJoinCode();

        // Send response immediately with joinCode
        res.status(200).json({
            message: "Neighborhood creation in progress.",
            joinCode,
            neighborhoodId
        });

        // Do database work after response
        const insertSql = "INSERT INTO neighborhoods (neighborhood_id, name, location, created_by, join_code) VALUES (?, ?, ST_GeomFromText(?), ?, ?)";
        const values = [neighborhoodId, name, `POINT(${longitude} ${latitude})`, user_id, joinCode];

        db.query(insertSql, values, async (err, result) => {
            if (err) {
                console.error("Full DB Insert Error:", JSON.stringify(err, null, 2));
                return;
            }

            try {
                const neighborhoodRef = firestore.collection('neighborhoods').doc(neighborhoodId.toString());
                await neighborhoodRef.set({
                    name: name,
                    latitude: latitude,
                    longitude: longitude,
                    createdBy: user_id,
                    joinCode: joinCode,
                    createdAt: new Date(),
                });

                console.log("Neighborhood created successfully in MySQL and Firestore.");
            } catch (firestoreError) {
                console.error("Firestore Error:", firestoreError);
            }
        });

    } catch (err) {
        console.error("Error:", err);
        if (!res.headersSent) {
            res.status(500).json({ error: "Server error" });
        }
    }
});




app.post('/joinNeighborhood', async (req, res) => {
    const { userId, neighborhoodCode } = req.body;

    if (!userId || !neighborhoodCode) {
        return res.status(400).json({ message: "User ID and Neighborhood Code are required." });
    }

    try {
        const [neighborhood] = await db.query(
            'SELECT * FROM neighborhoods WHERE join_code = ?',
            [neighborhoodCode]
        );

        if (neighborhood.length === 0) {
            return res.status(404).json({ message: "Neighborhood not found" });
        }

        await db.query(
            'INSERT INTO neighborhood_members (user_id, neighborhood_id) VALUES (?, ?)',
            [userId, neighborhood[0].neighborhood_id]
        );

        res.json({ message: 'Joined neighborhood successfully' });
    } catch (error) {
        console.error("Error:", error);  
        res.status(500).json({ message: "Internal server error" });
    }
});

app.post('/checkNeighborhoodCode', async (req, res) => {
    const { neighborhoodCode } = req.body;

    console.log("Received join code:", neighborhoodCode);
    console.log("Entire body:", req.body);

    if (!neighborhoodCode) {
        console.log("Missing neighborhood code");
        return res.status(400).json({ message: "Neighborhood code is required." });
    }

    const sql = `
        SELECT 
            name, 
            ST_Y(location) AS latitude, 
            ST_X(location) AS longitude 
        FROM neighborhoods 
        WHERE UPPER(TRIM(join_code)) = ?
    `;

    try {
        const [results] = await db.query(sql, [neighborhoodCode.trim().toUpperCase()]);

        console.log("Query results:", results);

        if (results.length === 0) {
            console.log("Neighborhood not found");
            return res.status(404).json({ message: "Neighborhood not found" });
        }

        const neighborhood = results[0];
        console.log("Neighborhood found:", neighborhood);

        return res.json({
            name: neighborhood.name,
            location: {
                lat: neighborhood.latitude,
                lng: neighborhood.longitude
            }
        });

    } catch (err) {
        console.error("Database error:", err);
        return res.status(500).json({ message: "Server error", error: err });
    }
});

// start the server
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://10.0.2.2:${PORT}`);
});
