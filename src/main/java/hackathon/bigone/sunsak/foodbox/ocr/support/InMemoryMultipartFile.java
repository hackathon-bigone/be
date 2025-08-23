package hackathon.bigone.sunsak.foodbox.ocr.support;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class InMemoryMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] bytes;

    public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.bytes = bytes != null ? bytes : new byte[0];
    }

    @Override public String getName() { return name; }
    @Override public String getOriginalFilename() { return originalFilename; }
    @Override public String getContentType() { return contentType; }
    @Override public boolean isEmpty() { return bytes.length == 0; }
    @Override public long getSize() { return bytes.length; }
    @Override public byte[] getBytes() { return bytes; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }

    @Override
    public void transferTo(File dest) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dest))) {
            os.write(bytes);
        } catch (IOException e) {
            // MultipartFile은 IllegalStateException 던지도록 권장
            throw new IllegalStateException("Failed to transfer to file: " + dest, e);
        }
    }

    @Override
    public void transferTo(Path dest) throws IOException {
        Files.write(dest, bytes);
    }
}
