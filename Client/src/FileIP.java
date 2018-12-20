import java.io.File;

public class FileIP
{

    private File file;
    private String IP;

    // constructor
    public FileIP(File file, String ip)
    {
        this.file = file;
        this.IP = IP;

    }

    // ------------------- Getters ---------------------------
    public File getFile() {
        return file;
    }

    public String getIP() {
        return IP;
    }

    // ------------------- Setters ---------------------------
    public void setFile(File file) {
        this.file = file;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}
