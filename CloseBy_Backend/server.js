const express = require("express");
const db = require("./db");
const cors = require("cors");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");

const app = express();  // Initialize the app here
app.use(cors());
app.use(bodyParser.json());

//  REGISTER USER API
app.post("/api/register", async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: "All fields are required" });
    }

    try {
        // Hash password before storing it
        const hashedPassword = await bcrypt.hash(password, 10);
        
        const query = 'INSERT INTO members (`email`, `password`) VALUES (?, ?)';
        
        db.query(query, [email, hashedPassword], (err, result) => {
            if (err) {
                console.error("Error during query:", err); // Log the error for more detail
                return res.status(500).json({ error: err.message });
            }
            res.json({ message: "User registered successfully!" });
        });
    } catch (error) {
        console.error("Error during registration:", error); // Log the error for more detail
        res.status(500).json({ error: "Error registering user" });
    }
});


// Start the server
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
