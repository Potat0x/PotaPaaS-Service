package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.api.UuidAndNameResponseDto;
import pl.potat0x.potapaas.potapaasservice.app.AppFacade;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.security.TestAuthUtils;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.validator.UuidValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @Before
    public void setUpSpringSecurityAuthentication() {
        TestAuthUtils.setAuthenticatedPrincipalInSecurityContext("datastore-facade-test-user", 123L);
    }

    @Test
    public void shouldGetDatastoreDetailsIncludingListOfAttachedApps() {
        //given
        String datastoreType = DatastoreType.POSTGRESQL.toString();
        String datastoreName = "test-datastore";
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto(datastoreName, datastoreType);

        //when
        DatastoreResponseDto datastoreResponseDto = datastoreFacade.createDatastore(datastoreRequestDto).get();
        String datastoreUuid = datastoreResponseDto.getUuid();

        //then
        DatastoreResponseDto expectedDatastoreResponseDto = new DatastoreResponseDto("uuid not known yet", datastoreName, DatastoreType.valueOf(datastoreType), null, "running", Set.of());
        assertTrue(UuidValidator.checkIfValid(datastoreUuid));
        assertThat(datastoreResponseDto.getCreatedAt()).isBefore(LocalDateTime.now());
        assertThat(datastoreResponseDto).isEqualToIgnoringGivenFields(expectedDatastoreResponseDto, "uuid", "createdAt");


        //when
        AppResponseDto appResponseDto = createAppAndAttachItToDatastore(datastoreUuid);
        DatastoreResponseDto datastoreResponseDtoAfterAttachingApp = datastoreFacade.getDatastoreDetails(datastoreUuid).get();
        List<UuidAndNameResponseDto> uuidAndNameResponseDtos = datastoreFacade.getUuidsAndNamesOfAllDatastores().get();

        //then
        String attachedAppUuid = appResponseDto.getAppUuid();
        expectedDatastoreResponseDto = new DatastoreResponseDto(datastoreUuid, datastoreName, DatastoreType.valueOf(datastoreType), datastoreResponseDto.getCreatedAt(), "running", Set.of(attachedAppUuid));
        assertThat(datastoreResponseDtoAfterAttachingApp).isEqualTo(expectedDatastoreResponseDto);
        assertThat(datastoreUuid).isEqualTo(appResponseDto.getDatastoreUuid());

        UuidAndNameResponseDto expectedUuidAndNameResponseDto = new UuidAndNameResponseDto(datastoreUuid, datastoreName);
        assertThat(uuidAndNameResponseDtos).contains(expectedUuidAndNameResponseDto);
    }

    @Test
    public void shouldDeleteDatastoreOnlyWhenNoAppsAttached() {
        //given
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto("datastore-name", DatastoreType.POSTGRESQL.toString());
        DatastoreResponseDto datastoreResponseDto = datastoreFacade.createDatastore(datastoreRequestDto).get();
        String datastoreUuid = datastoreResponseDto.getUuid();

        //when
        String attachedAppUuid = createAppAndAttachItToDatastore(datastoreUuid).getAppUuid();
        Either<ErrorMessage, String> deleteResult = datastoreFacade.deleteDatastore(datastoreUuid);

        //then
        assertThat(deleteResult.isLeft()).isTrue();
        assertThat(datastoreFacade.getDatastoreDetails(datastoreUuid).isRight()).isTrue();


        //when
        appFacade.deleteApp(attachedAppUuid).get();
        deleteResult = datastoreFacade.deleteDatastore(datastoreUuid);

        //then
        assertThat(deleteResult.isRight()).isTrue();
        assertThat(datastoreFacade.getDatastoreDetails(datastoreUuid).isLeft()).isTrue();
    }

    private AppResponseDto createAppAndAttachItToDatastore(String datastoreUuid) {
        AppRequestDto appRequestDto = new AppRequestDtoBuilder()
                .withName("test-app" + UUID.randomUUID())
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withDatastoreUuid(datastoreUuid).build();
        return appFacade.createAndDeployApp(appRequestDto).get();
    }
}