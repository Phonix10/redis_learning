package redis_learning_app.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import redis_learning_app.app.dto.Studentdto;

@Repository
public interface StudentRepository extends JpaRepository<Studentdto, Long> {
    
}
