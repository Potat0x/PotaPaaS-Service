package pl.potat0x.potapaas.potapaasservice.user;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface UserRepository extends CrudRepository<UserEntity, Long> {

    List<UserEntity> findAllByUsername(String username);

    long countByUsername(String username);
}
