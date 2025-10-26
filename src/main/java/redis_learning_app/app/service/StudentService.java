package redis_learning_app.app.service;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import redis_learning_app.app.Repository.StudentRepository;
import redis_learning_app.app.dto.Studentdto;



@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STUDENT_ALL_KEY = "students:all";
    private static final String STUDENT_KEY_PREFIX = "student:";

    /**
     * Get all students (cached for 24 hours)
     */
    public List<Studentdto> getAllStudents() {
        // Try fetching from Redis
        List<Studentdto> cachedList = (List<Studentdto>) redisTemplate.opsForValue().get(STUDENT_ALL_KEY);
        if (cachedList != null) {
            System.out.println("✅ Fetching all students from Redis cache");
            return cachedList;
        }

        // If not in cache, fetch from DB
        System.out.println("🗄️ Fetching all students from MySQL DB");
        List<Studentdto> students = studentRepository.findAll();

        // Store in Redis with 24-hour TTL
        redisTemplate.opsForValue().set(STUDENT_ALL_KEY, students, 24, TimeUnit.HOURS);

        return students;
    }

    /**
     * Get single student (cached for 24 hours)
     */
    public Optional<Studentdto> getStudentById(Long id) {
        String key = STUDENT_KEY_PREFIX + id;

        // Try fetching from Redis
        Studentdto cachedStudent = (Studentdto) redisTemplate.opsForValue().get(key);
        if (cachedStudent != null) {
            System.out.println("✅ Fetching student ID " + id + " from Redis cache");
            return Optional.of(cachedStudent);
        }

        // If not in cache, fetch from DB
        System.out.println("🗄️ Fetching student ID " + id + " from MySQL DB");
        Optional<Studentdto> student = studentRepository.findById(id);

        // Store in Redis for 24 hours
        student.ifPresent(s -> redisTemplate.opsForValue().set(key, s, 24, TimeUnit.HOURS));

        return student;
    }

    /**
     * Add new student (invalidate cache)
     */
    public Studentdto addStudent(Studentdto student) {
        Studentdto saved = studentRepository.save(student);

        // Invalidate cache
        redisTemplate.delete(STUDENT_ALL_KEY);

        return saved;
    }

    /**
     * Delete student (invalidate cache)
     */
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);

        // Invalidate specific and all cache
        redisTemplate.delete(STUDENT_KEY_PREFIX + id);
        redisTemplate.delete(STUDENT_ALL_KEY);
    }

    /**
     * Update student (invalidate cache)
     */
    public Studentdto updateStudent(Long id, Studentdto newData) {
        Studentdto existing = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        existing.setName(newData.getName());
        existing.setCourse(newData.getCourse());

        Studentdto updated = studentRepository.save(existing);

        // Invalidate cache entries
        redisTemplate.delete(STUDENT_ALL_KEY);
        redisTemplate.delete(STUDENT_KEY_PREFIX + id);

        return updated;
    }
}
