package pl.potat0x.potapaas.potapaasservice.datastore;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostFilter;

import java.util.List;
import java.util.Set;

interface DatastoreRepository extends CrudRepository<DatastoreEntity, Long> {

    @PostFilter("filterObject.userId.equals(principal.userId)")
    List<DatastoreEntity> findAllByUuid(String uuid);

    @PostFilter("filterObject.userId.equals(principal.userId)")
    List<DatastoreEntity> findAll();

    @Query(nativeQuery = true,
            value = "SELECT app.uuid FROM app " +
                    "JOIN datastore ON app.datastore_uuid = datastore.uuid " +
                    "WHERE datastore.uuid = :datastore_uuid")
    Set<String> findAllAppsConnectedToDatastore(@Param("datastore_uuid") String datastoreUuid);
}
