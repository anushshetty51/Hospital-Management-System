import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8000)) {
            System.out.println("Server is listening on port 8000");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String table = reader.readLine(); // Read the table name from the client
                String username = reader.readLine();
                String password = reader.readLine();

                // Perform authentication logic by checking credentials against the selected table
                boolean isAuthenticated = authenticate(table, username, password);
                //String sql = getSQLInformation(table);
                  

                if (isAuthenticated) {
                    writer.println("success");
                      //writer.println(sql);

                    // Send the SQL information based on the table
                    
                } else {
                    writer.println("failure");
                  
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Authenticate against a SQL database
        private boolean authenticate(String table, String username, String password) {
            String jdbcUrl = "jdbc:mysql://localhost:3306/hospital"; // Replace with your database connection URL
            String dbUsername = "root";
            String dbPassword = "";

            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                String query;
                PreparedStatement preparedStatement;

                switch (table) {
                    case "admin":
                        query = "SELECT * FROM admin WHERE username = ? AND password = ?";
                        break;
                    case "doctor":
                        query = "SELECT * FROM doctor WHERE doctor_id = ? AND password = ?  AND delete_date IS NULL";
                        break;
                    case "patient":
                        query = "SELECT * FROM patient WHERE patient_id = ? AND password = ?  AND date_delete IS NULL";
                        break;
                    default:
                        // Invalid table name
                        return false;
                }
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                return resultSet.next(); // Authentication successful if a record is found
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false; // Authentication failed
        }

     
        
    }
}
