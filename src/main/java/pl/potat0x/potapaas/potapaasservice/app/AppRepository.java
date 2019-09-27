package pl.potat0x.potapaas.potapaasservice.app;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppRepository extends CrudRepository<AppEntity, Long> {
    AppEntity findOneByUuid(String uuid);

    List<AppEntity> findAllByDatastoreUuid(String datastoreUuid);
}
