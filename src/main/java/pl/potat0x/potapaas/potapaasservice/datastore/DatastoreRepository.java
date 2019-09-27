package pl.potat0x.potapaas.potapaasservice.datastore;

import org.springframework.data.repository.CrudRepository;

interface DatastoreRepository extends CrudRepository<DatastoreEntity, Long> {
    DatastoreEntity findOneByUuid(String uuid);
}
