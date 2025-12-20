package br.com.user.model.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class FileReference {
    private String name;
    private String contentType;
    private String path;
    private Long contentLength;
    private Long durationLink;
    private boolean isPublicAccessible;

    public String getPathAvatar() {
        return "avatar".concat("/").concat(name);
    }

}
