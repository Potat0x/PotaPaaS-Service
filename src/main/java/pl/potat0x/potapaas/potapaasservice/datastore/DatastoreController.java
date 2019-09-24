package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.potat0x.potapaas.potapaasservice.api.ResponseResolver;

@RestController
@RequestMapping("/datastore")
class DatastoreController {

    private final DatastoreFacade datastoreFacade;

    @Autowired
    DatastoreController(DatastoreFacade datastoreFacade) {
        this.datastoreFacade = datastoreFacade;
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
