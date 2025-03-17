app.post("/api/register", async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        console.error("Missing fields: email or password");
        return res.status(400).json({ error: "All fields are required" });
    }

    try {
        // Hash password before storing it
        const hashedPassword = await bcrypt.hash(password, 10);

        // Insert email and hashed password into the database
        const query = 'INSERT INTO members (`email`, `password`) VALUES (?, ?)';
        
        db.query(query, [email, hashedPassword], (err, result) => {
            if (err) {
                console.error("Database error:", err); // Log the error if the query fails
                return res.status(500).json({ error: err.message });
            }
            console.log("User registered successfully!");
            res.json({ message: "User registered successfully!" });
        });
    } catch (error) {
        console.error("Error during registration:", error); // Log any error during registration
        res.status(500).json({ error: "Error registering user" });
    }
});
