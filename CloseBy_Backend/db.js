const mysql = require('mysql2');
const connection = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "Marlins161",
    database: "closeby"
});

connection.connect(err => {
    if (err) throw err;
    console.log("Connected to the database! Yippee!!");
});

// Export the promise-based version
module.exports = connection.promise();
