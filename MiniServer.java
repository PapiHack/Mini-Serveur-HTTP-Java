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
import org.python.util.PythonInterpreter;

/**
 * @author Meissa Birima Couly Mbaye (P@p!H@ck)
 * @since 18/04/19
 * @version 1.0 
 */
public class MiniServer implements Runnable
{
    // Racine du serveur (repertoire de publication)
    static final File RACINE_SERVER = new File("htdocs");

    // Repertoire contenant les pages d'erreurs
    static final File ERROR_DIRECTORY = new File("htdocs/errors");

    // Page d'acceuil au lancement du serveur (requête sur /)
    static final String FICHIER_INDEX = "index.html";
    
    // Page 404 à afficher lorsque la ressource est innexistante
    static final String PAGE_NOT_FOUND = "404.html";
    
    // Par défaut on ne supporte que du GET
    // Cette page sera affiché lorsque la méthode de la requête est différent de GET
	static final String METHODE_NON_SUPPORTE = "non_supporte.html";
    
    // port par défaut à écouter pour la connection
    private int portParDefaut = 4000;
    
    // mode verbeux afin d'avoir plus d'info sur l'état du serveur en console
    private boolean modeVerbeux = true;
    
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
        BufferedReader in = null; 
        PrintWriter out = null; 
        BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
        try 
        {
			// lecture des caractères du client via l'input stream du socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// recuperation des infos du header
			out = new PrintWriter(connect.getOutputStream());
			// recuperation des donnees de la requête du client
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// recuperation de la 1ére ligne de la requête du client
			String input = in.readLine();
            // on parse la requête avec un StringTokenize
            StringTokenizer parser = new StringTokenizer(input);
			String method = parser.nextToken().toUpperCase(); // On recupère la méthode du client
			// on recupère la ressouce demandée
            fileRequested = parser.nextToken().toLowerCase();
			// on ne supporte que du "GET" pour le moment
			if (!method.equals("GET")) {
                
                // Si c'est pas du "GET", on lui retourne "non_supporte.html"
				File file = new File(ERROR_DIRECTORY, METHODE_NON_SUPPORTE);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//lecture du contenu du fichier à retourner au client
				byte[] fileData = readFileData(file, fileLength);
                
				// on envoi les en-têtes HTTP de la réponse au client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); 
                out.flush(); 
                
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

                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + this.displayDirectoryContent(resourceRequested));
                        out.println("Content-length: " + this.displayDirectoryContent(resourceRequested).length());
                        out.println(); 
                        out.flush(); 
                        dataOut.write(this.displayDirectoryContent(resourceRequested).getBytes());
                        dataOut.flush();
                    }
                }
                    if(fileRequested.endsWith(".py"))
                    {
                        File file = new File(RACINE_SERVER, fileRequested);
                        int fileLength = (int) file.length();
                        String contentType = getContentType(fileRequested);
                        byte[] fileData = readFileData(file, fileLength);
                        PythonInterpreter interpreteurPython = new PythonInterpreter();
                        interpreteurPython.setOut(dataOut); 
                        interpreteurPython.exec("print(\"------ Resultat de l'execution du fichier python ------\\n\")");
                        interpreteurPython.exec(new String(fileData, "UTF-8"));
                        interpreteurPython.exec("print(\"\\n---------------------------------------------------------\")");
                        
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + contentType);
                        out.println("Content-length: " + fileLength);
                        out.println();
                        out.flush(); 
                        
                         //dataOut.write("\n <p>Exécution de fichier python</p>".getBytes());
                         dataOut.flush();
                 
                         if (this.modeVerbeux) 
                         {
                             this.displayInfoForVerboseMode(200, fileLength, contentType, "OK");
                             System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");
                         }
                         else
                             System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");
 
                          interpreteurPython.close();
                    }
                    else
                    {
                        File file = new File(RACINE_SERVER, fileRequested);
                        int fileLength = (int) file.length();
                        String contentType = getContentType(fileRequested);
                        byte[] fileData = readFileData(file, fileLength);
					
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + contentType);
                        out.println("Content-length: " + fileLength);
                        out.println();
                        out.flush(); 
                        
                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
				
                        if (this.modeVerbeux) 
                        {
                            this.displayInfoForVerboseMode(200, fileLength, contentType, "OK");
                            System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");
                        }
                        else
                            System.out.println("Fichier " + fileRequested + " de type " + getContentType(fileRequested) + " retourné");

                    }
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
			System.err.println("Erreur au niveau du serveur : " + ioe.getMessage());
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
                this.connect.close(); // fermeture du socket
			} catch (Exception e) {
				System.err.println("Erreur fermeture du stream : " + e.getMessage());
            }
            
            if (this.modeVerbeux) 
            {
				System.out.println("Connection fermé.\n");
			}
		}
    }
    
    // Retourne le "Content-Type" correspondant à la ressource demandée par le client 
    private String getContentType(String fileRequested) 
    {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")||  fileRequested.endsWith(".py"))
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
    
    // Renvoie la page adéquate ainsi que le status code 404, lorsque la ressource demandée par le client
    // n'existe pas ou n'a pas été trouvé
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
		out.println();
		out.flush(); 
		
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
    
    // Retourne le résultat de la lecture de la ressource demandée par le client
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
    
    // Affiche des infos supplémentaires lorsque le mode verbeux est activé 
    private void displayInfoForVerboseMode(int statusCode, int fileLength, String contentType, String statusMsg)
    {
        System.out.println("HTTP/1.1 " + statusCode + " " + statusMsg);
		System.out.println("Server: JAVA Mini Serveur HTTP by Meissa : 1.0");
		System.out.println("Date: " + new Date());
		System.out.println("Content-type: " + contentType);
		System.out.println("Content-length: " + fileLength);
    }

    // Retourne le contenu du repertoire demandé par le client sous forme de lien hypertexte
    private String displayDirectoryContent(File directory)
    {

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
                display += "<tr><td valign=\"top\"><img src=\"/static/icons/unknown.gif\" alt=\"[   ]\"></td><td><a href=\"/"+ fic.getPath().substring(7) +"\">"+ fic.getName() +"</a> </td><td align=\"right\">" + new Date(fic.lastModified()) + " </td><td align=\"right\">"+ fic.getTotalSpace() +"</td><td>&nbsp;</td></tr>";
            }
            else if(fic.isDirectory())
            {
                display += "<tr><td valign=\"top\"><img src=\"/static/icons/folder.gif\" alt=\"[   ]\"></td><td><a href=\"/"+ fic.getPath().substring(7) +"\">"+ fic.getName() +"</a> </td><td align=\"right\">" + new Date(fic.lastModified()) + " </td><td align=\"right\">"+ fic.getTotalSpace() +"</td><td>&nbsp;</td></tr>";
            }
        }

        display += "</table></body></html>";

        return display;
    }

}