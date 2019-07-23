package pl.potat0x.potapaas.potapaasservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.core.TestCachingJGitCloner;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

@Configuration
@Profile("test")
public class CachingAppManagerFactoryConfig {
    @Bean
    public AppManagerFactory cachingAppManagerFactory() {
        return new AppManagerFactory(new TestCachingJGitCloner(), PotapaasConfig.get("docker_api_uri"), true);
    }
}
