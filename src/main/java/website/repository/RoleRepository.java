package website.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import website.models.ERole;
import website.models.Role;

public interface RoleRepository extends MongoRepository<Role, String> {

  Optional<Role> findByName(ERole name);
}
