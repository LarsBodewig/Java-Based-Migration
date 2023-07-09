package my.db.migration.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PureJava {

	public static void main(String[] args) throws SQLException {
		try (Connection con = DriverManager.getConnection("jdbc:h2:mem:")) {
			initialize(con);
			migrate(con);
		}
	}

	public static void initialize(Connection con) throws SQLException {
		// create scheme for 1.0.0
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate("CREATE TABLE Fruit (name VARCHAR(255), weight INTEGER, color_hex CHAR(6))");

			// insert data for 1.0.0
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			oldFruit.name = "Orange";
			oldFruit.weight = 250;
			oldFruit.colorHex = "ff8800";
			PreparedStatement ps = con.prepareStatement("INSERT INTO Fruit (name, weight, color_hex) VALUES (?, ?, ?)");
			ps.setString(1, oldFruit.name);
			ps.setInt(2, oldFruit.weight);
			ps.setString(3, oldFruit.colorHex);
			ps.execute();

			// print table contents
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM Fruit")) {
				rs.next();
				String name = rs.getString(1);
				int weight = rs.getInt(2);
				String colorHex = rs.getString(3);
				System.out.println("TABLE Fruit initialized:");
				System.out.println("|  name  | weight | color_hex |");
				System.out.println("|--------|--------|-----------|");
				System.out.println("| %s |   %d  |  #%s  |".formatted(name, weight, colorHex));
			}
		}
	}

	public static void migrate(Connection con) throws SQLException {
		try (Statement stmt = con.createStatement()) {

			// read data from 1.0.0
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			ResultSet fruits = stmt.executeQuery("SELECT * FROM Fruit");
			fruits.next();
			oldFruit.name = fruits.getString(1);
			oldFruit.weight = fruits.getInt(2);
			oldFruit.colorHex = fruits.getString(3);

			// migrate scheme to 2.0.0
			stmt.executeUpdate("ALTER TABLE Fruit DROP COLUMN color_hex");
			stmt.executeUpdate("ALTER TABLE Fruit ADD COLUMN color_rgb VARCHAR(11)");

			// migrate data to 2.0.0
			my.db._2_0_0.classes.pkg.food.Fruit newFruit = new my.db._2_0_0.classes.pkg.food.Fruit();
			newFruit.name = oldFruit.name;
			newFruit.weight = oldFruit.weight;
			newFruit.colorRgb = hexToRgb(oldFruit.colorHex); // <--- complex operation that cannot be done in pure sql

			// update data for 2.0.0
			PreparedStatement ps = con.prepareStatement("UPDATE Fruit SET COLOR_RGB = ?");
			ps.setString(1, newFruit.colorRgb);
			ps.execute();

			// print table contents
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM Fruit")) {
				rs.next();
				String name = rs.getString(1);
				int weight = rs.getInt(2);
				String colorRgb = rs.getString(3);
				System.out.println("TABLE Fruit migrated:");
				System.out.println("|  name  | weight | color_rgb |");
				System.out.println("|--------|--------|-----------|");
				System.out.println("| %s |   %d  | %s |".formatted(name, weight, colorRgb));
			}
		}
	}

	private static String hexToRgb(String hex) {
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		return r + "," + g + "," + b;
	}
}
