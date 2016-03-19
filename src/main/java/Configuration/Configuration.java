package Configuration;

/**
 * @author Robin Duda
 *
 * Configuration file.
 */
public class Configuration {
    public static final int WEB_PORT = 9950;
    public static final int MASTER_PORT = 9494;
    public static final String CONNECTION_STRING = "mongodb://localhost:27017/";
    public static final String DB_NAME = "vote";
    public static final String SERVER_NAME = "server.receiver";
    public static final byte[] SERVER_SECRET = "!!!!!!!!!!!server_secret!!!!!!!!!!".getBytes();
}
