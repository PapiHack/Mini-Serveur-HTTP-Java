import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author Meissa Birima Couly Mbaye
 * @since 18/04/19
 * @version 1.0 
 */
public class MiniServer implements Runnable
{
    static final File RACINE_SERVER = new File("htdocs");
    static final File ERROR_DIRECTORY = new File("htdocs/errors");
	static final String FICHIER_INDEX = "index.html";
	static final String PAGE_NOT_FOUND = "404.html";
	static final String METHODE_NON_SUPPORTE = "non_supporte.html";
    
    // port par défaut à écouter pour la connection
    private int portParDefaut = 4000;
    
    // mode verbeux afin d'avoir plus d'info sur l'état du serveur en console
    private boolean modeVerbeux = false;
    
    // Connection cliente via la classe Socket
    private Socket connect;
    
    public MiniServer(Socket sock)
    {
        this.connect = sock;
    }

    public MiniServer(Socket sock, int port)
    {
        this(sock);
        this.portParDefaut = port;
    }

    public MiniServer(Socket sock, int port, boolean verboseMode)
    {
        this(sock, port);
        this.modeVerbeux = verboseMode;
    }

    public int getPortParDefaut()
    {
        return this.portParDefaut;
    }

    public boolean hasVerboseMode()
    {
        return this.modeVerbeux;
    }

    public void setVerboseMode(boolean mode)
    {
        this.modeVerbeux = mode;
    }

    public Socket getSocket()
    {
        return this.connect;
    }

    @Override
    public void run()
    {
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
        try 
        {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parser = new StringTokenizer(input);
			String method = parser.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
            fileRequested = parser.nextToken().toLowerCase();
			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")) {
                
                // we return the not supported file to the client
				File file = new File(ERROR_DIRECTORY, METHODE_NON_SUPPORTE);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//read content to return to client
				byte[] fileData = readFileData(file, fileLength);
                
				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
                
                if (this.modeVerbeux) 
                {
                    this.displayInfoForVerboseMode(501, fileLength, "text/html", "501 Not Implemented ");
                    System.out.println("501 Not Implemented : " + method + " method.");
                }
                else
                    System.out.println("501 Not Implemented : " + method + " method.");
                    
				
			} else {
				// GET
                if (fileRequested.equals("/")) 
                {
                    fileRequested += FICHIER_INDEX;
                }
                else
                {
                    if(fileRequested.endsWith("/"))
                    {
                        fileRequested = fileRequested.substring(0, fileRequested.length()-1);
                    }

                    File resourceRequested = new File(RACINE_SERVER + fileRequested);

                    if(resourceRequested.isDirectory())
                    {
                        //On affiche le contenu du repertoire
                        System.out.println(fileRequested + " est un repertoire");

                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + this.displayDirectoryContent(resourceRequested));
                        out.println("Content-length: " + this.displayDirectoryContent(resourceRequested).length());
                        out.println(); // blank line between headers and content, very important !
                        out.flush(); // flush character output stream buffer
                        dataOut.write(this.displayDirectoryContent(resourceRequested).getBytes());
                        dataOut.flush();
                    }
                }
                        File file = new File(RACINE_SERVER, fileRequested);
                        int fileLength = (int) file.length();
                        String content = getContentType(fileRequested);
                        byte[] fileData = readFileData(file, fileLength);
					
                        // send HTTP Headers
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + content);
                        out.println("Content-length: " + fileLength);
                        out.println(); // blank line between headers and content, very important !
                        out.flush(); // flush character output stream buffer
                        
                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
				
				if (this.modeVerbeux) {
                    this.displayInfoForVerboseMode(200, fileLength, content, "OK");
					System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");
                }
                else
					System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");

			}
			
		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Erreur au niveau de FileNotFoundException : " + ioe.getMessage());
			}
			
        }
        catch(NullPointerException npe)
        {
            System.out.println();
        } 
        catch (IOException ioe) {
			System.err.println("Erreur au niveau du serveur : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
                this.connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Erreur fermeture du stream : " + e.getMessage());
            }
            
            if (this.modeVerbeux) 
            {
				System.out.println("Connection fermé.\n");
			}
		}
    }
    
    private String getContentType(String fileRequested) 
    {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg"))
            return "image/jpeg";
        else if (fileRequested.endsWith(".gif"))
            return "image/gif";
        else if (fileRequested.endsWith(".png"))
            return "image/png";
        else if (fileRequested.endsWith(".class"))
            return "application/octet-stream";
		else
			return "text/plain";
    }
    
    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException 
    {
		File file = new File(ERROR_DIRECTORY, PAGE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
        if (this.modeVerbeux) 
        {
            this.displayInfoForVerboseMode(404, fileLength, content, "File Not Found");
			System.out.println("Le fichier " + fileRequested + " est introuvable");
        }
        else
            System.out.println("Le fichier " + fileRequested + " est introuvable");
    }
    
    private byte[] readFileData(File file, int fileLength) throws IOException 
    {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
    }
    
    private void displayInfoForVerboseMode(int statusCode, int fileLength, String contentType, String statusMsg)
    {
        System.out.println("HTTP/1.1 " + statusCode + " " + statusMsg);
		System.out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
		System.out.println("Date: " + new Date());
		System.out.println("Content-type: " + contentType);
		System.out.println("Content-length: " + fileLength);
    }

    private String displayDirectoryContent(File directory)
    {
        String directoryName = directory.getName();
        String parentDirectoryName = directory.getParent().equals("htdocs") ? "" : directory.getParentFile().getName();

        String display = new String("<html><head><meta charset=\"utf-8\"/><title>Index of /"+ directory.getPath().substring(7) +"</title></head>");
        display += "<body><h1>Index of /"+ directory.getPath().substring(7) +"</h1> <br/>";
        display += "<table><tr><th valign=\"top\"><img src=\"/static/icons/blank.gif\" alt=\"[ICO]\"></th>";
        display += "<th><a href=\"?C=N;O=D\">Nom</a></th> <th><a href=\"?C=M;O=A\">Dernière Modification</a></th>";
        display += "<th><a href=\"?C=S;O=A\">Taille</a></th><th><a href=\"?C=D;O=A\">Description</a></th> </tr> <tr><th colspan=\"5\"><hr></th></tr>";
        display += "<tr><td valign=\"top\"><img src=\"/static/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"/"+ parentDirectoryName +"\">Repertoire Parent</a>  </td><td>&nbsp;</td><td align=\"right\">  - </td><td>&nbsp;</td></tr>";
       
        String[] fileList = directory.list();

        for(String file : fileList)
        {
            File fic = new File(directory, file);
            if(fic.isFile())
            {
                display += "<tr><td valign=\"top\"><img src=\"/static/icons/unknown.gif\" alt=\"[   ]\"></td><td><a href=\" "+ directoryName +"/"+  fic.getName() +" \">"+ fic.getName() +"</a> </td><td align=\"right\">" + new Date(fic.lastModified()) + " </td><td align=\"right\">"+ fic.getTotalSpace() +"</td><td>&nbsp;</td></tr>";
            }
            else if(fic.isDirectory())
            {
                display += "<tr><td valign=\"top\"><img src=\"/static/icons/folder.gif\" alt=\"[   ]\"></td><td><a href=\" "+ directoryName +"/"+ fic.getName() +" \">"+ fic.getName() +"</a> </td><td align=\"right\">" + new Date(fic.lastModified()) + " </td><td align=\"right\">"+ fic.getTotalSpace() +"</td><td>&nbsp;</td></tr>";
            }
        }

        display += "</table></body></html>";

        return display;
    }
}