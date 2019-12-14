package pl.potat0x.potapaas.potapaasservice.app;

import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PostFilter;

import java.util.List;

interface AppRepository extends CrudRepository<AppEntity, Long> {

    @PostFilter("filterObject.userId.equals(principal.userId)")
    List<AppEntity> findAll();

    //anonymous user is allowed while handling webhook (webhooks are secured by secret token)
    @PostFilter("isAnonymous() || filterObject.userId.equals(principal.userId)")
    List<AppEntity> findOneByUuid(String uuid);

    long countByName(String name);

    long countByUuidAndName(String uuid, String name);
}
