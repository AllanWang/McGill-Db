package ca.allanwang.mcgill.db.utils

import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database

interface DbConfigs {
    val db: String
    val dbUser: String
    val dbPassword: String
    val dbDriver: String

    fun connect() {
        if (db.isEmpty()) throw RuntimeException("No db value found in configs")
        if (dbDriver.isEmpty()) throw RuntimeException("No db driver value found in configs")
        val log = LogManager.getLogger("DbConfigs")
        log.info("Connecting to $db with $dbUser")
        Database.connect(url = db,
                driver = dbDriver,
                user = dbUser,
                password = dbPassword)
    }
}