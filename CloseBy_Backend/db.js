const mysql2 = require("mysql2");

const connection = mysql2.createConnection({
    host: "localhost",
    user: "root",
    password: "yourpassword",
    database: "yourdb"
});

connection.connect(err => {
    if (err) throw err;
    console.log("Connected to the database!");
});
