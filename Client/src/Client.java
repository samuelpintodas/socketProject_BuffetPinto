import java.util.Vector;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class Client
{
    // Server variables
    static String serverName; // Server IP
    static int serverPort = 45000; // port used by the server
    static InetAddress serverAddress = null;

    // Client variables
    static String localName;  // Local/Client IP
    static int clientPort = 45001; // port used by the client
    static Socket clientSocket; // socket used by the client
    static String filePath = "datas/"; // client folder path
    static ArrayList<String> clientFiles = new ArrayList<>(); // client files list
    /* eventually disconect port here */


    // Gui variables

    // main method
    public static void main(String[] args)
    {
        connexion(serverName, serverAddress, clientSocket, clientPort);
        getClientFileList(clientFiles, filePath, localName, serverName, serverPort);
    }

    // Etablish the connection with the server
    public static void connexion(String sName, InetAddress sAddress, Socket cSocket, int cPort) {
        try
        {
            // get the IP address with the IP we've write in sName
            sAddress = InetAddress.getByName(sName);
            System.out.println("Get the address of the server : " + serverAddress);

            //get a connection to the server
            cSocket = new Socket(sAddress, cPort);
            System.out.println("We got the connexion to  " + sAddress);
            System.out.println("Will read data given by server:\n");


        }
        catch (UnknownHostException e)
        {

            e.printStackTrace();
        }
        catch (ConnectException e)
        {
            System.out.println("\n cannot connect to server");
            ;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    // create fileList for the server
    // test
    public static ArrayList<String[]> getClientFileList(ArrayList<String[]> cFiles, String path, String cName, String sName, int sPort)
    {
        File directory = new File(path);

        File[] files = directory.listFiles();
        for (File f : files) {
            cFiles.add(f.getName());
            // test
            int test;
        }
        return cFiles;
    }



}
