import java.sql.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;

public class P3 {
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@claros.cs.purdue.edu:1524:strep";
    static final String USER = "pshoroff";
    static final String PASS = "vbNaX0xU";

    static String currentUsername = null;
    static String currentPass = null;
    static int currentId = -1;
    static String currentRole = null; //is empty string if
    static String currentKey = null;

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

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

                //Outputting desired information
                System.out.println(commandCount + ": " + line);
                String[] tokens = line.split("\\s");

                //Decision making
                if (tokens[0].equals("LOGIN")) {
                    Login(tokens, conn, stmt);
                    System.out.println();
                } else if (tokens[0].equals("CREATE")) {
                    if (tokens[1].equals("ROLE")) {
                        //First check if current user is admin:
                        if (currentRole == null || !currentRole.equals("ADMIN")) {
                            System.out.println("Authorization failure");
                        } else {
                            CreateRole(tokens, conn, stmt);
                        }
                        System.out.println();
                    } else if (tokens[1].equals("USER")) {
                        if (currentRole == null || !currentRole.equals("ADMIN")) {
                            System.out.println("Authorization failure");
                        } else {
                            CreateUser(tokens, conn, stmt);
                        }
                        System.out.println();
                    } else {
                        System.out.println("Do not support Create " + tokens[1] + " command.");
                    }
                } else if (tokens[0].equals("GRANT")) {
                    if (tokens[1].equals("ROLE")) {
                        if (currentRole == null || !currentRole.equals("ADMIN")) {
                            System.out.println("Authorization failure");
                        } else {
                            GrantRole(tokens, conn, stmt);
                        }
                        System.out.println();
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

    private static void Login(String[] tokens, Connection conn, Statement stmt) {
        String loginString = "Select us.USERID as userid, r.ROLEID as roleid, r.RoleName as rolename, r.Encryptionkey as encryptionkey From USERS us Left Join UsersRoles ur on us.userId = ur.userId Left Join Roles r on r.roleId = ur.roleid where username = ? AND password = ?";
        try {
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

                if (rs.getString("rolename") == null) {
                    System.out.println("WARNING: No Role Existing for Current User");
                }
                currentRole = rs.getString("rolename");
                currentKey = rs.getString("encryptionkey");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void CreateRole(String[] tokens, Connection conn, Statement stmt) {
        String createString = "insert into Roles values(?, ?, ?)";
        try{
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery("select max(roleid) as maxid from roles");
            int newid = 1;
            if (rs.next()){
                newid = rs.getInt("maxid") + 1;
            }
            PreparedStatement createStatement = conn.prepareStatement(createString);
            createStatement.setInt(1, newid);
            createStatement.setString(2, tokens[2]);
            createStatement.setString(3, tokens[3]);
            Boolean successCreate = createStatement.execute();
            System.out.println("Role created successfully");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void CreateUser(String[] tokens, Connection conn, Statement stmt){
        String createString = "insert into Users values(?, ?, ?)";
        try{
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery("select max(userid) as maxid from users");
            int newid = 1;
            if (rs.next()){
                newid = rs.getInt("maxid") + 1;
            }
            PreparedStatement createStatement = conn.prepareStatement(createString);
            createStatement.setInt(1, newid);
            createStatement.setString(2, tokens[2]);
            createStatement.setString(3, tokens[3]);
            Boolean successCreate = createStatement.execute();
            System.out.println("User created successfully");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void GrantRole(String[] tokens, Connection conn, Statement stmt){
        try {
            conn.setAutoCommit(false);
            String uidString = "Select userid from users where username = ?";
            String ridString = "Select roleid from roles where rolename = ?";
            PreparedStatement uidStatement = conn.prepareStatement(uidString);
            PreparedStatement ridStatement = conn.prepareStatement(ridString);
            uidStatement.setString(1, tokens[2]);
            ridStatement.setString(1, tokens[3]);
            int uid = -1;
            int rid = -1;
            ResultSet uiRS, riRS;
            uiRS = uidStatement.executeQuery();
            riRS = ridStatement.executeQuery();
            if (uiRS.next()) uid = uiRS.getInt("userid");
            if (riRS.next()) rid = riRS.getInt("roleid");
            String grantRoleString = "insert into UsersRoles values(?, ?)";
            PreparedStatement grantRoleStatement = conn.prepareStatement(grantRoleString);
            if (uid == -1 || rid == -1){
                System.out.println("Cannot find user");
                return;
            }
            grantRoleStatement.setInt(1,uid);
            grantRoleStatement.setInt(2,rid);
            grantRoleStatement.executeQuery();
            System.out.println("Role assigned successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


