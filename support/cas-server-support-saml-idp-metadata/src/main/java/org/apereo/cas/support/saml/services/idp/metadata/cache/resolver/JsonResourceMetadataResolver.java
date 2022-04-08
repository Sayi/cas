package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link JsonResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class JsonResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver implements DisposableBean {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final String metadataTemplate;

    private final Resource jsonResource;

    private Map<String, SamlServiceProviderMetadata> metadataMap;

    private FileWatcherService watcherService;

    public JsonResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                        final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
        this.metadataTemplate = FunctionUtils.doUnchecked(() -> {
            val inputStream = new ClassPathResource("metadata/sp-metadata-template.xml").getInputStream();
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        });
        val md = samlIdPProperties.getMetadata();
        val location = SpringExpressionLanguageValueResolver.getInstance().resolve(md.getFileSystem().getLocation());
        val metadataDir = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(location).getFile());

        this.jsonResource = new FileSystemResource(new File(metadataDir, "saml-sp-metadata.json"));
        if (this.jsonResource.exists()) {
            this.metadataMap = readDecisionsFromJsonResource();
            this.watcherService = FunctionUtils.doUnchecked(() -> new FileWatcherService(jsonResource.getFile(),
                file -> this.metadataMap = readDecisionsFromJsonResource()));
            this.watcherService.start(getClass().getSimpleName());
        }
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        if (metadataMap.containsKey(service.getServiceId())) {
            return FunctionUtils.doUnchecked(() -> {
                val sp = metadataMap.get(service.getServiceId());
                val metadata = metadataTemplate
                    .replace("${entityId}", sp.getEntityId())
                    .replace("${certificate}", sp.getCertificate())
                    .replace("${assertionConsumerServiceUrl}", sp.getAssertionConsumerServiceUrl());
                val metadataResource = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));
                val resolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);
                configureAndInitializeSingleMetadataResolver(resolver, service);
                return CollectionUtils.wrap(resolver);
            });
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        val metadataLocation = service.getMetadataLocation();
        return metadataLocation.trim().startsWith("json://");
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return ResourceUtils.doesResourceExist(this.jsonResource);
    }

    @Override
    public void destroy() {
        if (this.watcherService != null) {
            this.watcherService.close();
        }
    }

    /**
     * The Saml service provider metadata.
     */
    @Getter
    @Setter
    public static class SamlServiceProviderMetadata implements Serializable {
        private static final long serialVersionUID = -7347473226470492601L;

        private String entityId;

        private String certificate;

        private String assertionConsumerServiceUrl;
    }

    private Map<String, SamlServiceProviderMetadata> readDecisionsFromJsonResource() {
        return FunctionUtils.doUnchecked(() -> {
            try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, SamlServiceProviderMetadata>>() {
                };
                return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            }
        });
    }
}

