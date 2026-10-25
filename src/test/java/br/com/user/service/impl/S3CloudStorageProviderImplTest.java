package br.com.user.service.impl;

import br.com.user.model.dto.DocumentoLinkInfoDTO;
import br.com.user.model.dto.FileReference;
import com.br.azevedo.infra.aws.s3.AwsS3StorageService;
import com.br.azevedo.infra.aws.s3.PresignedUrlInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3CloudStorageProviderImplTest {

    @Mock
    private AwsS3StorageService awsS3StorageService;

    @InjectMocks
    private S3CloudStorageProviderImpl storageProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageProvider, "bucketName", "test-user-bucket");
    }

    @Test
    @DisplayName("Deve gerar URL de upload pré-assinada delegando para AwsS3StorageService")
    void shouldGeneratePresignedUploadUrl() throws MalformedURLException {
        FileReference fileReference = FileReference.builder()
                .name("avatar123.jpg")
                .contentType("image/jpeg")
                .build();

        URL expectedUrl = new URL("https://s3.amazonaws.com/test-user-bucket/avatar/avatar123.jpg");
        when(awsS3StorageService.generatePresignedUploadUrl(eq("test-user-bucket"), eq("avatar/avatar123.jpg"), any(Duration.class)))
                .thenReturn(expectedUrl);

        URL result = storageProvider.generatePresignedUploadUrl(fileReference);

        assertEquals(expectedUrl, result);
        verify(awsS3StorageService).generatePresignedUploadUrl(eq("test-user-bucket"), eq("avatar/avatar123.jpg"), eq(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("Deve gerar URL de download pré-assinada delegando para AwsS3StorageService")
    void shouldGeneratePresignedDownloadUrl() throws MalformedURLException {
        FileReference fileReference = FileReference.builder()
                .name("avatar123.jpg")
                .contentType("image/jpeg")
                .durationLink(5L)
                .build();

        URL expectedUrl = new URL("https://s3.amazonaws.com/test-user-bucket/avatar/avatar123.jpg?download");
        when(awsS3StorageService.generatePresignedDownloadUrl(
                eq("test-user-bucket"), eq("avatar/avatar123.jpg"), eq(Duration.ofDays(5)), eq("image/jpeg"), eq("inline")))
                .thenReturn(expectedUrl);

        URL result = storageProvider.generatePresignedDownloadUrl(fileReference);

        assertEquals(expectedUrl, result);
        verify(awsS3StorageService).generatePresignedDownloadUrl(
                eq("test-user-bucket"), eq("avatar/avatar123.jpg"), eq(Duration.ofDays(5)), eq("image/jpeg"), eq("inline"));
    }

    @Test
    @DisplayName("Deve verificar se arquivo existe delegando para AwsS3StorageService")
    void shouldCheckFileExists() {
        when(awsS3StorageService.fileExists("test-user-bucket", "avatar/avatar123.jpg")).thenReturn(true);

        boolean exists = storageProvider.fileExists("avatar/avatar123.jpg");

        assertTrue(exists);
        verify(awsS3StorageService).fileExists("test-user-bucket", "avatar/avatar123.jpg");
    }

    @Test
    @DisplayName("Deve mover e remover arquivo no S3 delegando para AwsS3StorageService")
    void shouldMoveAndRemoveFile() {
        storageProvider.moveFile("avatar/old.jpg", "avatar/new.jpg");
        verify(awsS3StorageService).moveFile("test-user-bucket", "avatar/old.jpg", "test-user-bucket", "avatar/new.jpg");

        storageProvider.removeFile("avatar/old.jpg");
        verify(awsS3StorageService).removeFile("test-user-bucket", "avatar/old.jpg");
    }

    @Test
    @DisplayName("Deve verificar se URL pré-assinada expirou delegando para AwsS3StorageService")
    void shouldCheckIfPresignedUrlIsExpired() {
        String url = "https://test.s3.amazonaws.com/avatar/avatar123.jpg?X-Amz-Expires=604800";
        LocalDateTime expiration = LocalDateTime.now().plusDays(2);
        PresignedUrlInfo info = new PresignedUrlInfo(expiration, false, "avatar123.jpg");

        when(awsS3StorageService.isPresignedUrlExpired(url)).thenReturn(info);

        DocumentoLinkInfoDTO result = storageProvider.isPresignedUrlExpired(url);

        assertNotNull(result);
        assertEquals(expiration, result.dataExpiracao());
        assertFalse(result.isExpirate());
    }
}
