
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.Thread.sleep;

public class Server {

    // logger --> pour les logs du server

    // creation du log principale
    static Logger logger =Logger.getLogger(Server.class.getName());
    // creation d'un gestionnaire de fichier
    static FileHandler fileH=null;
    // definission de format du mois pour nos logs format choisis a 2 chiffres
    static String currentMonth = new SimpleDateFormat("MMM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\." ,"");;
    //creation du log secondaire il pemet d'enregistrer de bout d'action
    static  Logger log= Logger.getLogger("");

    // creation des infos connections
    static String serverName ="localhost";
    static ServerSocket connectionSocket;
    static ServerSocket disconnectionSocket;
    static int portc= 45001;
    static int portd= 45002;
    //creation d'une arraylist contenantn un vecteur ce vecteur contiendra les nom des fichier et l'ip du client
    static List<FileIP> fileList = new ArrayList<FileIP>() ;
    //creation d'une arraylist permettant le stockage des infos fichiers
    static List<FileIP> syncList  = Collections.synchronizedList(fileList);

    public static void main(String[] args) {
        Thread loggerThread = new Thread(() -> WriteLog());
        loggerThread.start();
        //Starting the server
        try {
            //get InetAddress of the server
            InetAddress serverAddress = InetAddress.getByName(serverName);
            //create the listening ServerSocket on port 50000
            connectionSocket = new ServerSocket(portc, 10, serverAddress);
            disconnectionSocket = new ServerSocket(portd, 10, serverAddress);
            Thread clientAcceptThread = new Thread(() -> connection());
            Thread clientDisconnectThread = new Thread(() -> disconnection());
            clientAcceptThread.start();
            clientDisconnectThread.start();

        }
        catch (Exception e)
            {
                logger.log(Level.SEVERE, e.toString());
            }

    }
    public static void CreateLog(){
        //creation d'un gestionnaire de fichier pour les logs
        try{
            fileH=new FileHandler("ServerLogger_"+currentMonth+".log",true);
            fileH.setFormatter(new SimpleFormatter());
            log.addHandler(fileH);
        }
        catch (IOException event) {
            event.printStackTrace();
        }
        //creation d'un log disant que le server a commencer a tourner
        logger.log(Level.INFO,"Server starting to run ");
    }
    //methode pour gérer les déconnection du server
    public static void disconnection(){

        while(true) {
            try {
                Socket disconnectClientSocket = disconnectionSocket.accept();

                //Creer un nouveau thread pour le client
                //si le client se deco les fichier n'apparaisse plus
                Thread clientThread = new Thread(() -> {
                    InetAddress disconnectClientAddress = disconnectClientSocket.getInetAddress();
                    String disconnectClientName = disconnectClientAddress.getHostAddress();
                    for (int i = 0; i < syncList.size(); i++) {
                        if (syncList.get(i).equals(disconnectClientName)) {
                            syncList.remove(i);
                            i--;
                        }
                    }

                    logger.log(Level.INFO, "Client " + disconnectClientName + " has disconnected");
                });
                clientThread.start();
            } catch (IOException e) {
                {
                    logger.log(Level.WARNING, e.toString());
                }
            }
        }

    }
    //methode pour gérer les nouvelle conenction aux servers
    public static void connection(){

        while(true)
        {
            //accept la connection client
            try
            {
                Socket clientSocket = connectionSocket.accept() ;

                //creer un thread pour le client
                Thread clientThread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            ObjectInputStream inputStream ;
                            ObjectOutputStream outputStream ;
                            //Get l'ip du client
                            InetAddress clientAddress = clientSocket.getInetAddress();
                            String clientName = clientAddress.getHostAddress() ;
                            //Liste le contenu des fichier du client
                            List<FileIP> clientFiles ;
                            //Get la list des fichier clients
                            inputStream = new ObjectInputStream(clientSocket.getInputStream()) ;
                            clientFiles = (ArrayList<FileIP>)inputStream.readObject();
                            //renvoi le nb de fichier clients
                            logger.log(Level.INFO, "Client "+clientName+" has connected with "+clientFiles.size()+" files");
                            for (FileIP file : clientFiles)
                            {
                                    //rajoute le fichier a la lsite des fichier du server
                                    syncList.add(file) ;
                            }
                            //envoi les fichier aux clients --> display
                            outputStream = new ObjectOutputStream(clientSocket.getOutputStream()) ;
                            outputStream.writeObject(fileList);
                            outputStream.flush();
                            //ferme la connection client
                            clientSocket.close();
                        }
                        catch(Exception e)
                        {logger.log(Level.WARNING, e.toString());}
                    }
                } ;
                //démarre un thread pour le client
                clientThread.start();
            } catch (IOException e1)
            {
                {logger.log(Level.WARNING, e1.toString());}
            }
        }
    }
    //methode pour écrire les logs
    public static void WriteLog(){
        CreateLog();
        while(true)
        {
            String tempMonth = new SimpleDateFormat("MMM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\.", "") ;
            // si le mois change ajoute un log pour le signifier
            if(!tempMonth.equals(currentMonth))
            {
                logger.log(Level.INFO,"Month changed "+currentMonth);
                currentMonth = tempMonth ;
                try {
                    log.removeHandler(fileH);
                    fileH = new FileHandler("ServerLogger_"+currentMonth+".log", true);
                    fileH.setFormatter(new SimpleFormatter());
                    log.addHandler(fileH);

                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE,e.toString());
                }

            }
            try {
                sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                logger.log(Level.SEVERE,e1.toString());
            }
        }
    }
}
