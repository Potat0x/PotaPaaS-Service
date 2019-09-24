package pl.potat0x.potapaas.potapaasservice.datastore;


import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.utils.EnumValidator;
import pl.potat0x.potapaas.potapaasservice.utils.NameValidator;

final class DatastoreRequestDtoValidator {

    Validation<Seq<String>, DatastoreRequestDto> validate(DatastoreRequestDto requestDto) {
        return Validation.combine(
                NameValidator.validate(requestDto.getName(), "datastore name"),
                EnumValidator.checkIfEnumContainsConstant(requestDto.getType(), DatastoreType.class, "datastore type")
        ).ap(DatastoreRequestDto::new);
    }
}
