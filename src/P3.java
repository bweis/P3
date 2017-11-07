import java.sql.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;

public class P3 {
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@claros.cs.purdue.edu:1524:strep";
    static final String USER = "pshoroff";
    static final String PASS = "vbNaX0xU";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        //User variables
        String currentUsername = null;
        String currentPass = null;
        int currentId = -1;
        String currentRole = null;
        String currentKey = null;

        try {
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //Creating a new file object for the input
            BufferedReader inFileBuffer;
            if (args.length != 0) {
                FileReader inFileReader = new FileReader(args[0]);
                inFileBuffer = new BufferedReader(inFileReader);
            } else {
                System.out.println("NO INPUT FILE DETECTED");
                return;
            }

            //Creating a handler for statements
            stmt = conn.createStatement();

            //Executing statements
            //Variables for handling statements
            String line = null;
            int commandCount = 1;

            while ((line = inFileBuffer.readLine()) != null) {
                System.out.println(commandCount + ": " + line);
                String[] tokens = line.split("\\s");

                //Decision making
                if (tokens[0].equals("LOGIN")) {
                    //Setting up the query to check user
                    String loginString = "Select USERID from Users where USERNAME = ? AND Password = ?";
                    conn.setAutoCommit(false);
                    PreparedStatement loginStatement = conn.prepareStatement(loginString);
                    loginStatement.setString(1, tokens[1]);
                    loginStatement.setString(2, tokens[2]);
                    ResultSet rs = loginStatement.executeQuery();
                    //Making sure the user exists
                    if (!rs.next()) {
                        System.out.println("Invalid login");
                    } else { //If the user does exist:
                        System.out.println("Login successful");

                        //Find the UserRole of the user:
                        currentUsername = tokens[1];
                        currentPass = tokens[2];
                        currentId = rs.getInt("userid");

                        //Get the roles information from the database
                        String rolesString = "Select Roleid, rolename, encryptionkey from ROLES where roleid = ?";
                        PreparedStatement roleStatement = conn.prepareStatement(rolesString);
                        roleStatement.setInt(1, currentId);
                        rs = roleStatement.executeQuery();

                        //Setting class user variables
                        if (rs.next()) {
                            currentRole = rs.getString("rolename");
                            currentKey = rs.getString("encryptionkey");
                        } else {
                            //If the query found no RoleId that matched the current userId then states that
                            System.out.println("Warning: no roles for this user");
                        }
                    }
                    System.out.println();
                } else if (tokens[0].equals("CREATE")) {
                    if (tokens[1].equals("ROLE")) {

                    } else if (tokens[1].equals("USER")) {

                    } else {
                        //THROW EXCEPTION HERE
                    }
                } else if (tokens[0].equals("GRANT")) {
                    if (tokens[1].equals("ROLE")) {

                    } else if (tokens[1].equals("PRIVILEGE")) {

                    } else {
                        //THROW EXCEPTION HERE
                    }
                } else if (tokens[0].equals("REVOKE") && tokens[1].equals("PRIVILEGE")) {

                } else if (tokens[0].equals("INSERT") && tokens[1].equals("INTO")) {

                } else if (tokens[0].equals("SELECT")) {

                } else if (tokens[0].equals("QUIT")) {

                }

                commandCount++;
            }
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }

    public static void Tester() {

    }


}


