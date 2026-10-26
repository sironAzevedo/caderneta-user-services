package br.com.user.service.impl;

import br.com.user.model.dto.DocumentoLinkInfoDTO;
import br.com.user.model.dto.FileReference;
import br.com.user.service.CloudStorageProvider;
import com.br.azevedo.infra.aws.s3.AwsS3StorageService;
import com.br.azevedo.infra.aws.s3.PresignedUrlInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CloudStorageProviderImpl implements CloudStorageProvider {

    private final AwsS3StorageService awsS3StorageService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public URL generatePresignedUploadUrl(FileReference fileReference) {
        return awsS3StorageService.generatePresignedUploadUrl(
                bucketName,
                fileReference.getPathAvatar(),
                Duration.ofDays(7)
        );
    }

    @Override
    public URL generatePresignedDownloadUrl(FileReference fileReference) {
        long durationDays = ObjectUtils.defaultIfNull(fileReference.getDurationLink(), 7L);
        return awsS3StorageService.generatePresignedDownloadUrl(
                bucketName,
                fileReference.getPathAvatar(),
                Duration.ofDays(durationDays),
                fileReference.getContentType(),
                "inline"
        );
    }

    @Override
    public boolean fileExists(String filePath) {
        return awsS3StorageService.fileExists(bucketName, filePath);
    }

    @Override
    public void moveFile(String fromPath, String toPath) {
        awsS3StorageService.moveFile(bucketName, fromPath, bucketName, toPath);
    }

    @Override
    public void removeFile(String filePath) {
        awsS3StorageService.removeFile(bucketName, filePath);
    }

    @Override
    public DocumentoLinkInfoDTO isPresignedUrlExpired(String presignedUrl) {
        PresignedUrlInfo info = awsS3StorageService.isPresignedUrlExpired(presignedUrl);
        return new DocumentoLinkInfoDTO(info.dataExpiracao(), info.isExpired());
    }
}
