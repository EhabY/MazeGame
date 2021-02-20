package website.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import website.models.User;

public interface UserRepository extends MongoRepository<User, String> {

  Optional<User> findByUsernameIgnoreCase(String username);

  Boolean existsByUsernameIgnoreCase(String username);
}