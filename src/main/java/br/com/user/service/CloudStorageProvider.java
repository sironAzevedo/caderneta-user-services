package br.com.user.service;

import br.com.user.model.dto.DocumentoLinkInfoDTO;
import br.com.user.model.dto.FileReference;

import java.net.URL;
import java.time.Duration;

public interface CloudStorageProvider {
    URL generatePresignedUploadUrl(FileReference fileReference);
    URL generatePresignedDownloadUrl(FileReference fileReference);
    boolean fileExists(String filePath);
    boolean fileExists(String filePath, Duration timeout);
    void moveFile(String fromPath, String toPath);
    void removeFile(String filePath);
    DocumentoLinkInfoDTO isPresignedUrlExpired(String presignedUrl);
}
