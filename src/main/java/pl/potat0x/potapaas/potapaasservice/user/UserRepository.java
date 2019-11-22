package pl.potat0x.potapaas.potapaasservice.user;

import org.springframework.data.repository.CrudRepository;

interface UserRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findOneByUsername(String username);

    long countByUsername(String username);
}
