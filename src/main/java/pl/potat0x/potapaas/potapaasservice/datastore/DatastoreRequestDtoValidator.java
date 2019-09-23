package pl.potat0x.potapaas.potapaasservice.datastore;


import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.utils.EnumValidator;
import pl.potat0x.potapaas.potapaasservice.utils.NameValidator;

final class DatastoreRequestDtoValidator {

    Validation<Seq<String>, DatastoreDto> validate(DatastoreDto requestDto) {
        return Validation.combine(
                NameValidator.validate(requestDto.getName(), "datastore name"),
                EnumValidator.checkIfEnumContainsConstant(requestDto.getType(), DatastoreType.class, "datastore type")
        ).ap(DatastoreDto::new);
    }
}
