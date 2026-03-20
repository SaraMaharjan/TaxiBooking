package model;

	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.SQLException;
	public class DbConnection {

		private final String url = "jdbc:mysql://localhost:3306/mydb"; 
		private final String user = "root";
		private final String password = "Ranoutofjams2_"; //DB password
		public Connection getConnection() {
		try {
		Connection conn = DriverManager.getConnection(url,user,password);
		System.out.println("Database connected.");
		return conn;
		} catch (SQLException e) {
		System.out.println("Database connection failed.");
		e.printStackTrace();
		return null;
		}
		
		

	}
	}


