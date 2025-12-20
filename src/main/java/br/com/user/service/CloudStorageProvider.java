package br.com.user.service;

import br.com.user.model.dto.DocumentoLinkInfoDTO;
import br.com.user.model.dto.FileReference;

import java.net.URL;

public interface CloudStorageProvider {
    URL generatePresignedUploadUrl(FileReference fileReference);
    URL generatePresignedDownloadUrl(FileReference fileReference);
    boolean fileExists(String filePath);
    void moveFile(String fromPath, String toPath);
    void removeFile(String filePath);
    DocumentoLinkInfoDTO isPresignedUrlExpired(String presignedUrl);
}
