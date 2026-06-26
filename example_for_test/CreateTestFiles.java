import java.io.*;
import java.nio.file.*;

public class CreateTestFiles {
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("C:/Users/slt/evosuite/example_for_test/testdata");
        Files.createDirectories(dir);

        // MP3 文件（ID3 头 + MPEG 帧同步）
        try (OutputStream os = new FileOutputStream(dir.resolve("test.mp3").toFile())) {
            os.write(new byte[]{
                    0x49, 0x44, 0x33, 0x04, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    (byte)0xFF, (byte)0xFB, (byte)0x90, 0x00
            });
        }

        // MP4 文件（ftyp isom 头）
        try (OutputStream os = new FileOutputStream(dir.resolve("test.mp4").toFile())) {
            os.write(new byte[]{
                    0x00, 0x00, 0x00, 0x18,
                    0x66, 0x74, 0x79, 0x70,
                    0x69, 0x73, 0x6F, 0x6D,
                    0x00, 0x00, 0x00, 0x00,
                    0x69, 0x73, 0x6F, 0x6D,
                    0x61, 0x70, 0x70, 0x32
            });
        }

        System.out.println("OK");
    }
}
