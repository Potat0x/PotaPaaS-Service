package pl.potat0x.potapaas.potapaasservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

@Profile("stubbed_docker_api")
@Configuration
public class FakeAppManagerFactoryConfig {
    @Bean
    public AppManagerFactory fakeAppManagerFactory() {
        return new AppManagerFactory(new FakeGitCloner(), PotapaasConfig.get("docker_stubbed_api_uri"));
    }
}
