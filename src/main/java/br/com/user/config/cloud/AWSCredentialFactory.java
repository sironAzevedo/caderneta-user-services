package br.com.user.config.cloud;

import com.br.azevedo.security.secretManager.VaultSecretManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AWSCredentialFactory {

    @Value("${cloud.aws.credentials.access-key:#{null}}")
    protected String accessKeyId;

    @Value("${cloud.aws.credentials.secret-key:#{null}}")
    protected String secretAccessKey;

    private final Environment environment;

    private final VaultSecretManager vaultSecretManager;

    protected StaticCredentialsProvider getCredentialsProvider() {
        if (isProductionEnvironment()) {
            Map<String, Object> bucketS3 = vaultSecretManager.getSecret("AUTH_BUCKET_S3");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                    bucketS3.get("access_key_id").toString(),
                    bucketS3.get("secret_access_key").toString()));
        }

        // Usar credenciais locais para desenvolvimento
        ObjectUtils.requireNonEmpty(accessKeyId, "AccessKeyId não pode ser nulo para ambiente local");
        ObjectUtils.requireNonEmpty(secretAccessKey, "SecretAccessKey não pode ser nulo para ambiente local");

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }

    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();

        // Considerar produção se algum dos profiles de produção estiver ativo
        for (String profile : activeProfiles) {
            if (profile.equalsIgnoreCase("prod") ||
                    profile.equalsIgnoreCase("production") ||
                    profile.equalsIgnoreCase("staging") ||
                    profile.equalsIgnoreCase("prd")) {
                return true;
            }
        }

        return false;
    }
}
