package br.com.user.service.impl;

import br.com.user.model.dto.DocumentoLinkInfoDTO;
import br.com.user.model.dto.FileReference;
import br.com.user.service.CloudStorageProvider;
import com.br.azevedo.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CloudStorageProviderImpl implements CloudStorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public URL generatePresignedUploadUrl(FileReference fileReference) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileReference.getPathAvatar())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    @Override
    public URL generatePresignedDownloadUrl(FileReference fileReference) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileReference.getPathAvatar())
                .responseContentDisposition("inline")
                .responseContentType(fileReference.getContentType())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(ObjectUtils.defaultIfNull(fileReference.getDurationLink(), 7L)))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    @Override
    public boolean fileExists(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }

        // Usamos HeadObjectRequest para buscar apenas os metadados
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        try {
            // Se a chamada for bem-sucedida, os metadados são retornados e o arquivo existe.
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            // Esta é a exceção específica para "arquivo não encontrado".
            log.warn("Arquivo não encontrado no S3: bucket={}, key={}", bucketName, filePath);
            return false;
        } catch (S3Exception e) {
            // Captura outros erros do S3, como "Access Denied" (403 Forbidden)
            log.error("Erro ao acessar o S3 (verifique as permissões): {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void moveFile(String fromPath, String toPath) {
        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceKey(fromPath)
                .destinationKey(toPath)
                .sourceBucket(bucketName)
                .destinationBucket(bucketName)
                .build();

        try {
            s3Client.copyObject(copyObjectRequest);
        } catch (Exception e) {
            log.error(String.format("Erro ao copiar o arquivo %s para %s", fromPath, toPath), e);
            throw new ApplicationException(String.format("Erro ao copiar o arquivo %s para %s", fromPath, toPath));
        }

        removeFile(fromPath);

    }

    @Override
    public void removeFile(String filePath) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error(String.format("Erro ao remover arquivo %s", filePath), e);
            throw new ApplicationException(String.format("Erro ao remover arquivo %s", filePath));
        }
    }

    @Override
    public DocumentoLinkInfoDTO isPresignedUrlExpired(String presignedUrl) {
        try {
            URL url = new URL(presignedUrl);

            // Extract query parameters
            String query = url.getQuery();
            Map<String, String> queryParams = Stream.of(query.split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(
                            entry -> URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                            entry -> URLDecoder.decode(entry[1], StandardCharsets.UTF_8)
                    ));

            // Get the expiration time from the URL parameters
            long expirationSeconds = Long.parseLong(queryParams.get("X-Amz-Expires"));
            String amzDate = queryParams.get("X-Amz-Date");

            // Parse the amzDate
            LocalDateTime creationTime = LocalDateTime.parse(amzDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            LocalDateTime expirationTime = creationTime.plusSeconds(expirationSeconds);

            // Check if the URL is expired
            LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
            return new DocumentoLinkInfoDTO(expirationTime, now.isAfter(expirationTime));
        } catch (Exception e) {
            log.error("Não foi possivel verificar o tempo de expiração da url: [{}], por favor gerar uma nova url", presignedUrl);
            return new DocumentoLinkInfoDTO(LocalDateTime.now().plusDays(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    , true);
        }
    }
}
