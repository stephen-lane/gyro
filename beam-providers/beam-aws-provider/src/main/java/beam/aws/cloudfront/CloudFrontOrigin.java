package beam.aws.cloudfront;

import beam.core.diff.Diffable;
import software.amazon.awssdk.services.cloudfront.model.CustomHeaders;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.OriginCustomHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CloudFrontOrigin extends Diffable {

    private String id;
    private String domainName;
    private String originPath;
    private Map<String, String> customHeaders;
    private CloudFrontS3Origin s3origin;
    private CloudFrontCustomOrigin customOrigin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public Map<String, String> getCustomHeaders() {
        if (customHeaders == null) {
            return new HashMap<>();
        }

        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public CloudFrontS3Origin getS3origin() {
        return s3origin;
    }

    public void setS3origin(CloudFrontS3Origin s3origin) {
        this.s3origin = s3origin;
    }

    public CloudFrontCustomOrigin getCustomOrigin() {
        return customOrigin;
    }

    public void setCustomOrigin(CloudFrontCustomOrigin customOrigin) {
        this.customOrigin = customOrigin;
    }

    public Origin toOrigin() {
        List<OriginCustomHeader> headers = getCustomHeaders().entrySet()
            .stream()
            .map(e -> OriginCustomHeader.builder().headerName(e.getKey()).headerValue(e.getValue()).build())
            .collect(Collectors.toList());

        CustomHeaders customHeaders = CustomHeaders.builder()
            .items(headers)
            .quantity(headers.size())
            .build();

        return Origin.builder()
            .id(getId())
            .domainName(getDomainName())
            .originPath(getOriginPath())
            .customHeaders(customHeaders)
            .s3OriginConfig(getS3origin() != null ? getS3origin().toS3OriginConfig() : null)
            .customOriginConfig(getCustomOrigin() != null ? getCustomOrigin().toCustomOriginConfig() : null)
            .build();
    }

    @Override
    public String primaryKey() {
        return getDomainName();
    }

    @Override
    public String toDisplayString() {
        return "origin";
    }
}
