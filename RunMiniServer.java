import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

/**
 * @author Meissa Birima Couly Mbaye (P@p!H@ck3R)
 * @since 18/04/19
 * @version 1.0 
 */
public class RunMiniServer
{
    // Port par défaut si ce dernier n'est pas renseigné au niveau de la commande 
    public static final int PORT_PAR_DEFAUT = 4000;

    // Par défaut on active le mode verbeux
    public static final boolean MODE_VERBEUX = true;
    
    public static void main(String[] args) 
    {

        if(args.length == 0)
            runServer(PORT_PAR_DEFAUT, MODE_VERBEUX);

        if(args.length == 1)
        {
            if(args[0].equals("-h") || args[0].equals("--help"))
                displayHelpCommand();
            else
                System.out.println("Commande invalide ! Veuillez consultez l'aide avec java RunMiniServer -h | java RunMiniServer --help");
        }
        else if(args.length == 2)
        {
            if(args[0].equals("-p") || args[0].equals("--port"))
            {
                try
                {
                    runServer(Integer.parseInt(args[1]), MODE_VERBEUX);
                }
                catch(NumberFormatException nfe)
                {
                    System.out.println("Argument invalide: " + nfe.getMessage());
                }
            }
            else if (args[0].equals("-v") || args[0].equals("--verbose"))
            {
                if(isBoolean(args[1]))
                    runServer(PORT_PAR_DEFAUT, Boolean.parseBoolean(args[1]));
                else
                    System.out.println("L'argument de verbose doit être : true ou false !");
            }
            else
                System.out.println("Commande invalide ! Veuillez consultez l'aide avec java RunMiniServer -h | java RunMiniServer --help");
        }
        else if(args.length == 4)
        {
            if( (args[0].equals("-p") || args[0].equals("--port")) && (args[2].equals("-v") || args[2].equals("--verbose")) )
            {
                try
                {
                    if(isBoolean(args[3]))
                        runServer(Integer.parseInt(args[1]), Boolean.parseBoolean(args[3]));
                    else
                        System.out.println("L'argument de verbose doit être : true ou false !");

                }
                catch(NumberFormatException nfe)
                {
                    System.out.println("Port invalide: " + nfe.getMessage());
                }
            }
            else
                System.out.println("Commande invalide ! Veuillez consultez l'aide avec java RunMiniServer -h | java RunMiniServer --help"); 
        }
        else
            System.out.println("Commande invalide ! Veuillez consultez l'aide avec java RunMiniServer -h | java RunMiniServer --help");       
    }

    // Permet de demarrer le serveur sur le port indiqué
    public static void runServer(int port, boolean verboseMode)
    {
        MiniServer myServer = null;
        ServerSocket serverConnect = null;
        try 
        {
            serverConnect = new ServerSocket(port);
            System.out.println("Serveur demarré.\nEcoute de connections au niveau du port : " + port + "...\n");
            
            
            while (true) 
            {
                myServer = new MiniServer(serverConnect.accept());
                myServer.setVerboseMode(verboseMode);
                
                if (myServer.hasVerboseMode())
                    System.out.println("Connecton opened. (" + new Date() + ")");
                // Création d'un thread dédié à la gestion des connections clientes
                Thread thread = new Thread(myServer);
                thread.start();
            }
                
        }     
        catch (IOException e) 
        {
            System.err.println("Server Connection error : " + e.getMessage());
        }   
    }

    // Affichage de l'aide
    public static void displayHelpCommand()
    {
        System.out.println("Ce programme permet de lancer un mini serveur web. \n");
        System.out.println("Description: \n\t Ce programme permet de lancer un serveur (http) web basique. Par défaut le mode verbeux est à true,");
        System.out.println(" \t et le port par défaut est 4000 si vous ne le spécifiez pas. L'option verbose doit prendre l'une des valeurs");
        System.out.println(" \t suivantes: [true | false].");
        System.out.println("Usages: \n\t java RunMiniServer –p port –v verbose \n\t java RunMiniServer --port port --verbose verbose");
        System.out.println("Options: \n\t -p, --port <portAEcouter> pour spécifier le port à écouter\n\t -v, --verbose <verbose> pour activer le mode verbeux \n\t -h, --help pour afficher l'aide");
        System.out.println("Important: \n\t Etant donné qu'il est possible d'éxécuter du code python veuillez utiliser les commandes suivantes: ");
        System.out.println("\t Compilation: javac -cp lib/jython-standalone-2.7.0.jar:. RunMiniServer.java");
        System.out.println("\t Exécution: java -cp lib/jython-standalone-2.7.0.jar:. RunMiniServer.java [-p | --port <port>] [-v | --verbose <verbose>] \t [-h | --help]");
        System.out.println("AUTEUR: \n\t Codé par Meissa Birima Couly Mbaye (itdev | P@p!H@ck3R) <itdev94@gmail.com> Site Web <http://www.itdev.site>");
        System.out.println("Repository: \n\t Retrouvez la plupart de mes projets sur mon repo github <https://github.com/PapiHack>");
        System.out.println("\t\t\t Avril 2018, M1GLSI");
    }

    // Permet de savoir si pattern est vraiment un booléen (pour les besoins de la commande)
    public static boolean isBoolean(String pattern)
    {
        return (pattern.equals("true") || pattern.equals("false")) ? true : false;
    }
}
