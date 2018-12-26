import java.io.File;
import org.apache.commons.io.FilenameUtils;

public class FileIP {

    private String name;
    private String extension;
    private String size;
    private String IP;

    // constructor
    public FileIP(File file, String ip) {
        this.name = FilenameUtils.removeExtension(file.getName());
        this.extension = FilenameUtils.getExtension(file.getName());
        this.size = (double) (file.length() / 1024) + "ko";
        this.IP = IP;

    }

    // ----------------- Getters ---------------------------
    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getSize() {
        return size;
    }

    public String getIP() {
        return IP;
    }

    // ----------------- Setters ---------------------------
    public void setName(String name) {
        this.name = name;
    }

    public void setSize(String size) {
        this.size = size;
    }


    public void setIP(String IP) {
        this.IP = IP;
    }
}


