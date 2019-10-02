package pl.potat0x.potapaas.potapaasservice.datastore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.app.AppFacade;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.validator.UuidValidator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class DatastoreFacadeTest {

    @Autowired
    private DatastoreFacade datastoreFacade;

    @Autowired
    private AppFacade appFacade;

    @Test
    public void shouldGetDatastoreDetailsIncludingListOfAttachedApps() {
        //given
        String datastoreType = DatastoreType.POSTGRES.toString();
        String datastoreName = "test-datastore";
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto(datastoreName, datastoreType);

        //when
        DatastoreResponseDto datastoreResponseDto = datastoreFacade.createDatastore(datastoreRequestDto).get();
        String datastoreUuid = datastoreResponseDto.getUuid();

        //then
        DatastoreResponseDto expectedDatastoreResponseDto = new DatastoreResponseDto("uuid not known yet", datastoreName, DatastoreType.valueOf(datastoreType), Set.of());
        assertTrue(UuidValidator.checkIfValid(datastoreUuid));
        assertThat(datastoreResponseDto).isEqualToIgnoringGivenFields(expectedDatastoreResponseDto, "uuid");


        //when
        AppResponseDto appResponseDto = createAppAndAttachItToDatastore(datastoreUuid);
        DatastoreResponseDto datastoreResponseDtoAfterAttachingApp = datastoreFacade.getDatastoreDetails(datastoreUuid).get();

        //then
        String attachedAppUuid = appResponseDto.getAppUuid();
        expectedDatastoreResponseDto = new DatastoreResponseDto(datastoreUuid, datastoreName, DatastoreType.valueOf(datastoreType), Set.of(attachedAppUuid));
        assertThat(datastoreResponseDtoAfterAttachingApp).isEqualTo(expectedDatastoreResponseDto);
        assertThat(datastoreUuid).isEqualTo(appResponseDto.getDatastoreUuid());
    }

    private AppResponseDto createAppAndAttachItToDatastore(String datastoreUuid) {
        AppRequestDto appRequestDto = new AppRequestDtoBuilder()
                .withName("test-app")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withDatastoreUuid(datastoreUuid).build();
        return appFacade.createAndDeployApp(appRequestDto).get();
    }
}