package com.example.timesaver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDirectory", tempDir.toString());
    }

    @Test
    public void testStoreFileSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        Integer projectId = 1;
        Integer applicantId = 2;
        Integer questionNumber = 3;

        String relativePath = fileStorageService.storeFile(file, projectId, applicantId, questionNumber);

        assertNotNull(relativePath);
        assertTrue(relativePath.startsWith("1/2/q3_"));
        assertTrue(relativePath.endsWith(".txt"));

        Path fullPath = tempDir.resolve(relativePath);
        assertTrue(Files.exists(fullPath));
        assertEquals("hello world", Files.readString(fullPath));
    }

    @Test
    public void testStoreFileEmptyThrows() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        assertThrows(IOException.class, () -> fileStorageService.storeFile(file, 1, 2, 3));
        assertThrows(IOException.class, () -> fileStorageService.storeFile(null, 1, 2, 3));
    }

    @Test
    public void testStoreFileTooLargeThrows() {
        // Max is 100MB; create a file slightly over that (the service checks >100MB)
        byte[] largeContent = new byte[100 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "large.txt", "text/plain", largeContent);
        assertThrows(IOException.class, () -> fileStorageService.storeFile(file, 1, 2, 3));
    }

    @Test
    public void testDeleteFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "data");
        assertTrue(Files.exists(testFile));

        fileStorageService.deleteFile("test.txt");
        assertFalse(Files.exists(testFile));

        // Should not throw when file doesn't exist
        fileStorageService.deleteFile("nonexistent.txt");
        fileStorageService.deleteFile(null);
        fileStorageService.deleteFile("");
    }

    @Test
    public void testGetFilePath() {
        Path path = fileStorageService.getFilePath("rel/path.txt");
        assertEquals(tempDir.resolve("rel/path.txt"), path);
    }

    @Test
    public void testFileExists() throws IOException {
        String relPath = "exists.txt";
        Files.writeString(tempDir.resolve(relPath), "data");

        assertTrue(fileStorageService.fileExists(relPath));
        assertFalse(fileStorageService.fileExists("no.txt"));
    }

    @Test
    public void testGetFileSize() throws IOException {
        String relPath = "size.txt";
        Files.writeString(tempDir.resolve(relPath), "12345");

        assertEquals(5, fileStorageService.getFileSize(relPath));
    }
}