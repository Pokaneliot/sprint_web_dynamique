import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MultipartFile {

    private String fileName;
    private String contentType;
    private byte[] fileBytes;

    // Constructor
    public MultipartFile(String fileName, String contentType, byte[] fileBytes) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileBytes = fileBytes;
    }

    // Default Constructor
    public MultipartFile() {}

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    // Additional Method: To get InputStream from bytes
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileBytes);
    }

    // Additional Method: To get file size
    public long getSize() {
        return fileBytes != null ? fileBytes.length : 0;
    }
}
