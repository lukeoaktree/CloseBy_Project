const express = require("express");
const db = require("./db");
const cors = require("cors");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");

const app = express();
app.use(cors());
app.use(bodyParser.json());

// 🔹 REGISTER USER API
app.post("/api/register", async (req, res) => {
    const { username, email, password } = req.body;

    if (!username || !email || !password) {
        return res.status(400).json({ error: "All fields are required" });
    }

    try {
        // Hash password before storing it
        const hashedPassword = await bcrypt.hash(password, 10);
        
        const query = 'INSERT INTO members (`username`, `email`,`password`) VALUES (`username`, `email`,`password`)';
        db.query(query, [ username, email, hashedPassword], (err, result) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }
            res.json({ message: "User registered successfully!" });
        });
    } catch (error) {
        res.status(500).json({ error: "Error registering user" });
    }
});

// Start the server
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
