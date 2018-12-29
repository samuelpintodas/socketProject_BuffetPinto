import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.Properties;

public class Client {
    // Server variables
    static String serverName; // Server IP
    static int serverPort = 45000; // port used by the server
    static InetAddress serverAddress = null;
    static ArrayList<FileIP> serverFiles = new ArrayList<>(); // List of files received from the server
    static ServerSocket listeningSkt;

    // Client variables
    static String localName = "127.0.0.1";  // Local/Client IP
    static int clientPort = 45001; // port used by the client
    static int disconnectPort = 45002; // port used by the client to disconnect himself from the server
    static Socket clientSocket; // socket used by the client
    static String filePath = "datas/"; // client folder path
    static ArrayList<FileIP> clientFiles = new ArrayList<>(); // client files list
    //static ArrayList<String> toDownloadList = new ArrayList<String>();
    static FileIP infos;


    // Gui variables
    static DefaultTableModel fileModel = new DefaultTableModel();
    static JTable filesTable = new JTable();
    static JScrollPane scrollPane = new JScrollPane(filesTable);
    static JButton downloadButton = new JButton("No file selected ");
    static JButton refreshButton = new JButton("Refresh files");
    static JLabel folderPath = new JLabel("Current folder: " + System.getProperty("user.dir") + "\\" + filePath.substring(0, filePath.length()-1) + "\\");
    static JFrame clientFrame = new JFrame("Client: " + localName);
    static JPanel northPanel = new JPanel(new BorderLayout());
    static JPanel southPanel = new JPanel(new BorderLayout());


    // main method
    public static void main(String[] args) {


        // ------------------ GUI part -----------------------------------

        // -------------Listeners

        // downloadButton Listener : On click, it download the selected file
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(infos == null)
                        JOptionPane.showMessageDialog(clientFrame, "Empty file: Download impossible");
                    else
                        {
                        if (downloadFile(clientPort, infos, filePath))
                            downloadButton.setText(infos.getName() + "." + infos.getExtension() + " : downloaded");

                         else
                            JOptionPane.showMessageDialog(clientFrame, "Empty file: Download impossible");
                        }


                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                infos = null;

                try {
                    createFileList(filePath, localName, serverName, serverPort);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
                downloadButton.setEnabled(false);
            }
        });

        // refresh Listener : On click, it refreshes the list of files
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFileList(filePath, localName, serverName, serverPort);
                infos = null;
                downloadButton.setText("No file selected");
                filesTable.clearSelection();
                downloadButton.setEnabled(false);
            }
        });

        // WindowsListener: windowsClosing --> disconnect from server when we close the window
        clientFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                disconnectFromServer(serverName, disconnectPort);
            }
        });

        clientFrame.addWindowListener(new WindowAdapter() {
        });

        // -------------adding window components
        folderPath.setPreferredSize(new Dimension(700, 20));
        downloadButton.setEnabled(false);
        scrollPane.setPreferredSize(new Dimension(600, 350));

        northPanel.add(folderPath,BorderLayout.WEST);

        southPanel.add(downloadButton, BorderLayout.CENTER);
        southPanel.add(refreshButton, BorderLayout.EAST);

        clientFrame.add(scrollPane, BorderLayout.CENTER);
        clientFrame.add(northPanel, BorderLayout.NORTH);
        clientFrame.add(southPanel, BorderLayout.SOUTH);

        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.pack();
        clientFrame.setVisible(true);


        // ------------------ Start methods -----------------------------------
        getConfigFromFile("config.properties");
        connexion(serverName, serverAddress, clientSocket, clientPort);
        startFileList(filePath, localName, serverName, serverPort);

        // Thread: accept incomming connections
        Thread waitToSend = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //accept incoming  connection
                        Socket clientSendingSkt;
                        clientSendingSkt = listeningSkt.accept();
                        //Create new thread for connecting client
                        Thread sendingThread = new Thread() {
                            @Override
                            public void run() {
                                //get requested file name
                                String fileName;
                                try {
                                    fileName = getRequestedFileName(clientSendingSkt);
                                    //send requested file to client
                                    sendFiles(clientSendingSkt, filePath, fileName);

                                    //close connection to client
                                    clientSendingSkt.close();
                                } catch (ClassNotFoundException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        //start the client thread
                        sendingThread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        waitToSend.start();



    }

    // Method: get config from a properties file
    public static void getConfigFromFile(String fileName)
    {
        try {
            InputStream input = new FileInputStream(fileName);
            Properties prop = new Properties();
            prop.load(input);
            serverName = prop.getProperty("ServerName");
            localName = prop.getProperty("LocalName");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(clientFrame, "No config file found");
        }
    }

    // Method: Etablish the connection with the server
    public static void connexion(String sName, InetAddress sAddress, Socket cSocket, int cPort) {
        try {
            InetAddress localAddress = InetAddress.getByName(localName);
            listeningSkt = new ServerSocket(clientPort, 5, localAddress);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    // Method : create fileList for the server
    public static ArrayList<FileIP> getLocalFilesList(ArrayList<FileIP> cFiles, String path, String cName, String sName) {
        File directory = new File(path);

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            FileIP temp = new FileIP(files[i], cName); //create a temporary FileIP
            cFiles.add(temp); // add it to the arrayList
        }
        return cFiles;
    }


    // Method: Get the name of the requested file
    public static String getRequestedFileName(Socket cSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream ips = new ObjectInputStream(cSocket.getInputStream());
        String fName = (String) ips.readObject();

        return fName;
    }

    // Method: Send file to client
    public static boolean sendFiles(Socket cSocket, String path, String fName) throws IOException {
        OutputStream ops = cSocket.getOutputStream();
        String pathFile = path + fName;
        Files.copy(Paths.get(pathFile), ops);

        return true;
    }

    // Method: Etablish the first fileList with the JTable
    public static void startFileList(String path, String cName, String sName, int sPort)
    {
        try {
            serverFiles = createFileList(path, cName, sName, sPort);
            configTableModel();
        } catch (Exception e) {
            infos = null;
            filesTable.clearSelection();
            downloadButton.setEnabled(false);
            JOptionPane.showMessageDialog(clientFrame, "Could not connect to server");
        }
    }

    // Method: Etablish the fileList (server files list + client files list)
    public static ArrayList<FileIP> createFileList(String path, String cName, String sName, int sPort) throws IOException, ClassNotFoundException {
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();

        ArrayList<FileIP> cFiles = getLocalFilesList(clientFiles, path, cName, sName);
        ArrayList<FileIP> sList = null;
        ArrayList<FileIP> fileList = new ArrayList<FileIP>();

        InetAddress serverAddress = InetAddress.getByName(sName);
        Socket serverSocket = new Socket();

        serverSocket.connect(new InetSocketAddress(serverAddress, sPort), 5);

        ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
        outputStream.writeObject(cFiles);
        outputStream.flush();

        ObjectInputStream inputStream = new ObjectInputStream(serverSocket.getInputStream());
        sList = (ArrayList<FileIP>) inputStream.readObject();

        serverSocket.close();

        for (FileIP file : sList) {
            if (!file.getIP().equals(localName))
                fileList.add(file);

        }
        if (fileList.isEmpty()) {
            JOptionPane.showMessageDialog(clientFrame, "No available files");
        }
        return fileList;
    }

    // Method: Download file (called from a listner)
    public static boolean downloadFile(int cPort, FileIP fip, String path) throws IOException, ClassNotFoundException, FileAlreadyExistsException {
        String fName = fip.getName();
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
                JOptionPane.showMessageDialog(clientFrame, aeMessage);
                return false;
            }
            clientSocket.close();
        } catch (Exception e) {
            String afkMessage = "The client " + cName + " is AFK";
            JOptionPane.showMessageDialog(clientFrame, afkMessage);
            return false;
        }
        return true;
    }


    // Method: Disconnect the client from the server
    public static int disconnectFromServer(String sName, int port) {
        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(serverName);
            Socket serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(serverAddress, port), 5);
            serverSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Method: config the defaultTableModel
    public static void configTableModel()
    {
        fileModel.addColumn("Name");
        fileModel.addColumn("Type");
        fileModel.addColumn("Size");
        fileModel.addColumn("Owner IP");

        for (int i = 0; i< serverFiles.size();i++){
            //array contenant le prenom, le nom et l'icone du contact
            Object[] serverFilesDatas = {   serverFiles.get(i).getName(),
                                            serverFiles.get(i).getExtension(),
                                            serverFiles.get(i).getSize(),
                                            serverFiles.get(i).getIP()};
            //ajout de la ligne au modele de table
            fileModel.addRow(serverFilesDatas) ;
        }

        filesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(filesTable.getSelectedRow() == -1)
                {
                    return;
                }
                int index = filesTable.getSelectedRow();
                infos = serverFiles.get(index);
                downloadButton.setText("Download " + infos.getName() + "." + infos.getExtension());
                downloadButton.setEnabled(true);
            }
        });

        filesTable = new JTable(fileModel);

    }


}
