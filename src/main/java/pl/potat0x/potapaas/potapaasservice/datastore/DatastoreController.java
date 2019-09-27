package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.potat0x.potapaas.potapaasservice.api.ResponseResolver;
import pl.potat0x.potapaas.potapaasservice.utils.UuidValidator;

@RestController
@RequestMapping("/datastore")
class DatastoreController {

    private final DatastoreFacade datastoreFacade;

    @Autowired
    DatastoreController(DatastoreFacade datastoreFacade) {
        this.datastoreFacade = datastoreFacade;
    }

    @GetMapping("/{datastoreUuid}")
    ResponseEntity getDatastoreDetails(@PathVariable String datastoreUuid) {
        if (!UuidValidator.checkIfValid(datastoreUuid)) {
            return ResponseResolver.toErrorResponseEntity("Invalid datastore UUID", HttpStatus.BAD_REQUEST);
        }
        return ResponseResolver.toResponseEntity(datastoreFacade.getDatastoreDetails(datastoreUuid), HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity createDatastore(@RequestBody DatastoreRequestDto requestDto) {
        Validation<Seq<String>, DatastoreRequestDto> validation = new DatastoreRequestDtoValidator().validate(requestDto);

        if (!validation.isValid()) {
            return ResponseResolver.toErrorResponseEntity(validation, HttpStatus.UNPROCESSABLE_ENTITY, validDatastoreRequestExample());
        }

        return ResponseResolver.toResponseEntity(datastoreFacade.createDatastore(requestDto), HttpStatus.CREATED);
    }

    private DatastoreRequestDto validDatastoreRequestExample() {
        return new DatastoreRequestDto("my-database-123", DatastoreType.POSTGRES.toString());
    }
}
