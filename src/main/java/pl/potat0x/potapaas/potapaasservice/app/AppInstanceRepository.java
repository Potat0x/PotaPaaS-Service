package pl.potat0x.potapaas.potapaasservice.app;

import org.springframework.data.repository.CrudRepository;

interface AppInstanceRepository extends CrudRepository<AppInstanceEntity, Long> {
}
