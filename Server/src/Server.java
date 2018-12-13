import com.sun.javafx.binding.StringFormatter;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    static String currentMonth = new SimpleDateFormat("MM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\." ,"");;
    //creation du log secondaire il pemet d'enregistrer de bout d'action
    static  Logger log= Logger.getLogger("");

    // creation d'une liste les fichier du client avec l'ip du proprietaire
    static List<String[]> SFileList = new ArrayList<String[]>() ;

    // creation des infos connections
    static String serverName ="localhost";
    static ServerSocket connectionSocket;
    static int port= 50000;

    public static void main(String[] args)
    {
        Thread loggerThread = new Thread() {
            public void run(){
                WriteLog();
            }
        }
        Thread clientAcceptThread = new Thread(){
            connection();
        }
        Thread clientDisconnectThread = new Thread() {
            disconnection();
        }
    }


    public static void CreateFirstLog(){
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

    }
    //methode pour gérer les nouvelle conenction aux servers
    public static void connection(){

    }
    //methode pour écrire les logs
    public static void WriteLog(){
        CreateFirstLog();
        while(true)
        {
            String tempMonth = new SimpleDateFormat("MM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\.", "") ;
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
