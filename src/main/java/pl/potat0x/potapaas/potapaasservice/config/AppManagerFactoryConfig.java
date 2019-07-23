package pl.potat0x.potapaas.potapaasservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.core.JGitCloner;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

@Configuration
@Profile("production")
public class AppManagerFactoryConfig {
    @Bean
    public AppManagerFactory defaultAppManagerFactory() {
        return new AppManagerFactory(new JGitCloner(), PotapaasConfig.get("docker_api_uri"));
    }
}
