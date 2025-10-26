package redis_learning_app.app.Controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import redis_learning_app.app.service.StudentService;
import redis_learning_app.app.dto.Studentdto;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * ➕ Add new student
     */
    @PostMapping
    public ResponseEntity<Studentdto> addStudent(@RequestBody Studentdto student) {
        Studentdto saved = studentService.addStudent(student);
        return ResponseEntity.ok(saved);
    }

    /**
     * 🧾 Get all students (cached)
     */
    @GetMapping
    public ResponseEntity<List<Studentdto>> getAllStudents() {
        List<Studentdto> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * 🔍 Get single student by ID (cached)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Studentdto> getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ✏️ Update student details
     */
    @PutMapping("/{id}")
    public ResponseEntity<Studentdto> updateStudent(@PathVariable Long id, @RequestBody Studentdto updatedData) {
        try {
            Studentdto updated = studentService.updateStudent(id, updatedData);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ❌ Delete student by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}

