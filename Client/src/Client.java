import javax.swing.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
// Raphael est une bite
public class Client {
    // Server variables
    static String serverName; // Server IP
    static int serverPort = 45000; // port used by the server
    static InetAddress serverAddress = null;

    // Client variables
    static String localName;  // Local/Client IP
    static int clientPort = 45001; // port used by the client
    static Socket clientSocket; // socket used by the client
    static String filePath = "datas/"; // client folder path
    static ArrayList<FileIP> clientFiles = new ArrayList<>(); // client files list
    /* eventually disconect port here */


    // Gui variables


    // main method
    public static void main(String[] args) {
        connexion(serverName, serverAddress, clientSocket, clientPort);
        getClientFileList(clientFiles, filePath, localName, serverName);

    }

    // Method: Etablish the connection with the server
    public static void connexion(String sName, InetAddress sAddress, Socket cSocket, int cPort) {
        try {
            // get the IP address with the IP we've write in sName
            sAddress = InetAddress.getByName(sName);
            System.out.println("Get the address of the server : " + serverAddress);

            //get a connection to the server
            cSocket = new Socket(sAddress, cPort);
            System.out.println("We got the connexion to  " + sAddress);
            System.out.println("Will read data given by server:\n");

        } catch (UnknownHostException e) {

            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("\n cannot connect to server");
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Method : create fileList for the server
    public static ArrayList<FileIP> getClientFileList(ArrayList<FileIP> cFiles, String path, String cName, String sName) {
        File directory = new File(path);

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            FileIP temp = new FileIP(files[i], cName); //create a temporary FileIP
            cFiles.add(temp); // add it to the arrayList
        }

        return cFiles;
    }

    // Method: Accept incoming connection

    // Method: Get the name of the requested file
    public static String getRequestedFileName(Socket cSocket) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ips = new ObjectInputStream(cSocket.getInputStream());
        String fName = (String) ips.readObject();

        return fName;
    }

    // Method: Send file to client
    public static boolean sendFile(Socket cSocket, String path, String fName) throws IOException
    {
        OutputStream ops = cSocket.getOutputStream();
        String pathFile = path + fName;
        Files.copy(Paths.get(pathFile), ops);

        return true;
    }

    // Method: Send fileList to Server


    // Method: Display list from server
    /* 1: Créer la partie graphique (fenêtre, jpanel et jtable)
       2: Récupérer la liste des fichiers que le serveur possède
       3: L'afficher
     */


    // Method: Download file (called from a listner)
    public static boolean downloadFile(int cPort, FileIP fip, String path) throws IOException, ClassNotFoundException, FileAlreadyExistsException {
        String fName = fip.getFile().getName();
        String cName = fip.getIP();

        InetAddress clientAddress = InetAddress.getByName(cName);
        Socket clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(clientAddress, cPort), 5);
            InputStream inputStream = clientSocket.getInputStream();
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

            //send requested file name
            output.writeObject(fName);

            //download requested file on path
            try {
                Files.copy(inputStream, Paths.get(path + fName));
            } catch (Exception e) {
                clientSocket.close();
                String aeMessage = "File " + fName + " already exist"; // aeMessage = already exists message
                JOptionPane.showMessageDialog(mainFrame, aeMessage);
                return false;
            }
            clientSocket.close();
        } catch (Exception e) {
            String afkMessage = "The client " + cName + " is AFK";
            JOptionPane.showMessageDialog(mainFrame, afkMessage);
            return false;
        }
        return true;
    }



}
