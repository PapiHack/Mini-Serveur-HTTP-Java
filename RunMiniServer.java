import java.net.ServerSocket;
import java.util.Date;
import java.io.IOException;

/**
 * @author Meissa Birima Couly Mbaye (P@p!H@ck)
 * @since 18/04/19
 * @version 1.0 
 */
public class RunMiniServer
{
    public static void main(String[] args) 
    { 
        runServer(4000);       
    }

    public static void runServer(int port)
    {
        MiniServer myServer = null;
        ServerSocket serverConnect = null;
        try 
        {
            serverConnect = new ServerSocket(port);
            System.out.println("Server started.\nListening for connections on port : " + port + " ...\n");
            
            // we listen until user halts server execution
            while (true) 
            {
                myServer = new MiniServer(serverConnect.accept());
                
                if (myServer.hasVerboseMode())
                    System.out.println("Connecton opened. (" + new Date() + ")");
                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }
                
        }     
        catch (IOException e) 
        {
            System.err.println("Server Connection error : " + e.getMessage());
        }   
    }
}
