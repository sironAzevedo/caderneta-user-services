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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CloudStorageProviderImpl implements CloudStorageProvider {

    private final AwsS3StorageService awsS3StorageService;
    private final S3Client s3Client;

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
        return fileExists(filePath, Duration.ofSeconds(15));
    }

    @Override
    public boolean fileExists(String filePath, Duration timeout) {
        var request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .overrideConfiguration(options -> options.apiCallTimeout(timeout))
                .build();
        try {
            s3Client.headObject(request);
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            throw exception;
        }
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
