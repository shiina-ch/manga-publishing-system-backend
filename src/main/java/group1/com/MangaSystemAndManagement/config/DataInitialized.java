package com.kilig.sba_assignment.config;

import com.kilig.sba_assignment.model.*;
import com.kilig.sba_assignment.security.service.AuthenticationService;
import com.kilig.sba_assignment.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitialized implements CommandLineRunner {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private QuestionTypeService questionTypeService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private TeacherService   teacherService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public void run(String... args) throws Exception {




        // Initialize Roles first
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        roleService.save(adminRole);

        Role teacherRole = new Role();
        teacherRole.setRoleName("TEACHER");
        roleService.save(teacherRole);


        Subject subject1 = new Subject();
        subject1.setSubjectCode("CS101");
        subject1.setSubjectName("Math");
        subject1.setCreateAt(LocalDateTime.now());
        subjectService.save(subject1);

        Subject subject2 = new Subject();
        subject2.setSubjectCode("CS102");
        subject2.setSubjectName("English");
        subject2.setCreateAt(LocalDateTime.now());
        subjectService.save(subject2);

        Subject subject3 = new Subject();
        subject3.setSubjectCode("CS103");
        subject3.setSubjectName("History");
        subject3.setCreateAt(LocalDateTime.now());
        subjectService.save(subject3);

        Subject subject4 = new Subject();
        subject4.setSubjectCode("CS104");
        subject4.setSubjectName("Geography");
        subject4.setCreateAt(LocalDateTime.now());
        subjectService.save(subject4);

        Subject subject5 = new Subject();
        subject5.setSubjectCode("CS105");
        subject5.setSubjectName("Science");
        subject5.setCreateAt(LocalDateTime.now());
        subjectService.save(subject5);


        Grade grade1 = new Grade();
        grade1.setGradeName("Lớp 1");
        grade1.setCreatedAt(LocalDateTime.now());
        grade1.setGradeDescription("This is grade 1");
        grade1.setGradeLevel("Mẫu giáo");
        grade1.setUpdatedAt(LocalDateTime.now());
        grade1.setSubject(subject1);
        gradeService.save(grade1);

        Grade grade2 = new Grade();
        grade2.setGradeName("Lớp 6");
        grade2.setCreatedAt(LocalDateTime.now());
        grade2.setGradeDescription("This is grade 6");
        grade2.setGradeLevel("THCS");
        grade2.setUpdatedAt(LocalDateTime.now());
        grade2.setSubject(subject1);
        gradeService.save(grade2);

        Grade grade3 = new Grade();
        grade3.setGradeName("Lớp 9");
        grade3.setCreatedAt(LocalDateTime.now());
        grade3.setGradeDescription("This is grade 9");
        grade3.setGradeLevel("Ending THCS");
        grade3.setUpdatedAt(LocalDateTime.now());
        grade3.setSubject(subject1);
        gradeService.save(grade3);

        Grade grade4 = new Grade();
        grade4.setGradeName("Lớp 12");
        grade4.setCreatedAt(LocalDateTime.now());
        grade4.setGradeDescription("This is grade 12");
        grade4.setGradeLevel("THPT");
        grade4.setUpdatedAt(LocalDateTime.now());
        grade4.setSubject(subject1);
        gradeService.save(grade4);


        Lesson lesson1 = new Lesson();
        lesson1.setLessonTitle("Introduction to Algebra");
        lesson1.setCreatedAt(LocalDateTime.now());
        lesson1.setUpdatedAt(LocalDateTime.now());
        lesson1.setLessonContent("This lesson covers the basics of algebra.");
        lesson1.setLessonCode("algebra");
        lesson1.setLessonContent("Algebra is a branch of mathematics dealing with symbols and the rules for manipulating those symbols.");
        lesson1.setLearningObjectives("Understand variables, expressions, and equations.");
        lesson1.setGrade(grade2);
        lessonService.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setLessonTitle("Introduction to VietNam");
        lesson2.setCreatedAt(LocalDateTime.now());
        lesson2.setUpdatedAt(LocalDateTime.now());
        lesson2.setLessonContent("This lesson covers Vietnam map.");
        lesson2.setLessonCode("Map");
        lesson2.setLessonContent(" VietNam is a beautiful country in Southeast Asia.");
        lesson2.setLearningObjectives("Understand VietNam geography.");
        lesson2.setGrade(grade3);
        lessonService.save(lesson2);

        Lesson lesson3 = new Lesson();
        lesson3.setLessonTitle("Introduction to Alphabet");
        lesson3.setCreatedAt(LocalDateTime.now());
        lesson3.setUpdatedAt(LocalDateTime.now());
        lesson3.setLessonContent("This lesson covers the basics of alphabet english.");
        lesson3.setLessonCode("alphabet");
        lesson3.setLessonContent(" The alphabet is a set of letters or symbols in a fixed order used to represent the basic sounds of a language.");
        lesson3.setLearningObjectives("Understand letters and sounds.");
        lesson3.setGrade(grade4);
        lessonService.save(lesson3);

        Level level1 = new Level();
        level1.setLevelName("Easy");
        level1.setCreatedAt(LocalDateTime.now());
        level1.setDifficultyScore(10);
        level1.setDescription("Level 1");
        levelService.save(level1);

        Level level2 = new Level();
        level2.setLevelName("Medium");
        level2.setCreatedAt(LocalDateTime.now());
        level2.setDifficultyScore(20);
        level2.setDescription("Level 2");
        levelService.save(level2);

        Level level3 = new Level();
        level3.setLevelName("Hard");
        level3.setCreatedAt(LocalDateTime.now());
        level3.setDifficultyScore(30);
        level3.setDescription("Level 3");
        levelService.save(level3);

        QuestionType type1 = new QuestionType();
        type1.setTypeName("Multiple Choice");
        type1.setTypeDescription("Type 1");
        type1.setCreatedAt(LocalDateTime.now());
        questionTypeService.save(type1);

        QuestionType type2 = new QuestionType();
        type2.setTypeName("True/False");
        type2.setTypeDescription("Type 2");
        type2.setCreatedAt(LocalDateTime.now());
        questionTypeService.save(type2);

        QuestionType type3 = new QuestionType();
        type3.setTypeName("Short Answer");
        type3.setTypeDescription("Type 3");
        type3.setCreatedAt(LocalDateTime.now());
        questionTypeService.save(type3);

        // Initialize teacher data
        Teacher teacher1 = new Teacher();
        teacher1.setFirstName("John");
        teacher1.setLastName("Doe");
        teacher1.setEmail("admin@gmail.com");
        String rawPassword = "password123";
        teacher1.setPassword(authenticationService.encodePassword(rawPassword));
        teacher1.setAccountStatus(AccountStatus.ACTIVE);
        teacher1.setCreatedAt(LocalDateTime.now());
        teacher1.setUpdatedAt(LocalDateTime.now());
        teacher1.setRole(adminRole);
        Teacher savedTeacher = teacherService.save(teacher1);

        // Generate token for the teacher
        String token = authenticationService.generateToken(savedTeacher);
        System.out.println("Teacher token: " + token);

        Teacher teacher2 = new Teacher();
        teacher2.setFirstName("Jane");
        teacher2.setLastName("Smith");
        teacher2.setEmail("user@gmail.com");
        String rawPassword2 = "password456";
        teacher2.setPassword(authenticationService.encodePassword(rawPassword2));
        teacher2.setAccountStatus(AccountStatus.ACTIVE);
        teacher2.setCreatedAt(LocalDateTime.now());
        teacher2.setUpdatedAt(LocalDateTime.now());
        teacher2.setRole(teacherRole);
        Teacher savedTeacher2 = teacherService.save(teacher2);

        // Generate token for the second teacher
        String token2 = authenticationService.generateToken(savedTeacher2);
        System.out.println("Teacher 2 token: " + token2);
    }

}
