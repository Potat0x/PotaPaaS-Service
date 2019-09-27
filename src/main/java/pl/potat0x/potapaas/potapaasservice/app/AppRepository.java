package pl.potat0x.potapaas.potapaasservice.app;

import org.springframework.data.repository.CrudRepository;

interface AppRepository extends CrudRepository<AppEntity, Long> {
    AppEntity findOneByUuid(String uuid);
}
