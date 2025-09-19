package org.pablito.secureCommands.managers;

import org.mindrot.jbcrypt.BCrypt;
import org.pablito.secureCommands.SecureCommands;

import java.sql.*;

public class DatabaseManager {
    private SecureCommands plugin;
    private Connection connection;

    public DatabaseManager(SecureCommands plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String database = plugin.getConfig().getString("database.name");
            String user = plugin.getConfig().getString("database.user");
            String password = plugin.getConfig().getString("database.password");

            if (host == null || database == null || user == null || password == null) {
                plugin.getLogger().severe("Database is not configured properly in config.yml!");
                return false;
            }

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    user, password);

            plugin.getLogger().info("Connected to database successfully!");
            setupTables();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to database: " + e.getMessage());
            return false;
        }
    }

    private void setupTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_data (" +
                    "player_name VARCHAR(16) PRIMARY KEY, " +
                    "code VARCHAR(60) NOT NULL, " +
                    "authenticated BOOLEAN DEFAULT FALSE, " +
                    "is_premium BOOLEAN DEFAULT FALSE)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating table: " + e.getMessage());
        }
    }

    public boolean isSecureCodeSet(String player) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT code FROM player_data WHERE player_name = ?")) {
            ps.setString(1, player.toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking code for player " + player + ": " + e.getMessage());
            return false;
        }
    }

    public boolean setSecureCode(String player, String code, boolean isPremium) {
        if (isSecureCodeSet(player)) {
            return false;
        }

        String hashedCode = BCrypt.hashpw(code, BCrypt.gensalt(12));

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_data (player_name, code, authenticated, is_premium) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, player.toLowerCase());
            ps.setString(2, hashedCode);
            ps.setBoolean(3, false);
            ps.setBoolean(4, isPremium);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting code for player " + player + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isSecureCodeCorrect(String player, String enteredCode) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT code FROM player_data WHERE player_name = ?")) {
            ps.setString(1, player.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("code");
                return BCrypt.checkpw(enteredCode, storedHash);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking code for player " + player + ": " + e.getMessage());
        }
        return false;
    }

    public boolean resetSecureCode(String player) {
        String sql = "DELETE FROM player_data WHERE player_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.toLowerCase());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error when resetting secure code for player " + player + ": " + e.getMessage());
            return false;
        }
    }

    public boolean resetAllSecureCodes() {
        String sql = "DELETE FROM player_data";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error when resetting all secure codes: " + e.getMessage());
            return false;
        }
    }

    public boolean isPremiumAccount(String playerName) {
        String sql = "SELECT is_premium FROM player_data WHERE player_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerName.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_premium");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error checking premium status for player " + playerName + ": " + e.getMessage());
        }
        return false;
    }

    public void setPremiumAccount(String playerName, boolean isPremium) {
        String sql = "UPDATE player_data SET is_premium = ? WHERE player_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, isPremium);
            ps.setString(2, playerName.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error setting premium status for player " + playerName + ": " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database disconnected.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}