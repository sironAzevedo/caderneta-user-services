package br.com.user.config.cloud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AWSInfraConfig {
    private final AWSCredentialFactory credentialFactory;

    @Value("${cloud.aws.region.static:us-east-1}")
    protected String region;

    @Bean
    public S3Presigner presigner() {
        log.info("Configurando o S3Presigner");
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialFactory.getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        log.info("Configurando o S3Client");
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialFactory.getCredentialsProvider())
                .build();
    }
}
