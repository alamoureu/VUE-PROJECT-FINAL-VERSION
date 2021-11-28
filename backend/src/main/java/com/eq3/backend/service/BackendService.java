package com.eq3.backend.service;

import com.eq3.backend.model.*;
import com.eq3.backend.repository.*;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.eq3.backend.utils.Utils.*;

@Service
public class BackendService {

    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;
    private final SupervisorRepository supervisorRepository;
    private final InternshipManagerRepository internshipManagerRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final InternshipRepository internshipRepository;
    private final MongoTemplate mongoTemplate;
    private final InternshipApplicationRepository internshipApplicationRepository;

    BackendService(StudentRepository studentRepository,
                   MonitorRepository monitorRepository,
                   SupervisorRepository supervisorRepository,
                   InternshipManagerRepository internshipManagerRepository,
                   InternshipOfferRepository internshipOfferRepository,
                   InternshipRepository internshipRepository,
                   MongoTemplate mongoTemplate,
                   InternshipApplicationRepository internshipApplicationRepository) {
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
        this.supervisorRepository = supervisorRepository;
        this.internshipManagerRepository = internshipManagerRepository;
        this.internshipOfferRepository = internshipOfferRepository;
        this.internshipRepository = internshipRepository;
        this.mongoTemplate = mongoTemplate;
        this.internshipApplicationRepository = internshipApplicationRepository;
    }

    public Optional<Binary> saveSignature(String username, MultipartFile signature) {
        Optional<Binary> optionalBinary = Optional.empty();
        Binary image = null;
        try {
            image = new Binary(BsonBinarySubType.BINARY, signature.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image != null) {
            switch (username.charAt(0)) {
                case 'G' :
                    Optional<InternshipManager> optionalInternshipManager = internshipManagerRepository.findByUsernameAndIsDisabledFalse(username);
                    if (optionalInternshipManager.isPresent()) {
                        InternshipManager internshipManager = optionalInternshipManager.get();
                        internshipManager.setSignature(image);
                        internshipManagerRepository.save(internshipManager);
                        optionalBinary = Optional.of(image);
                    }
                    break;

                case 'S' :
                    Optional<Supervisor> optionalSupervisor = supervisorRepository.findByUsernameAndIsDisabledFalse(username);
                    if (optionalSupervisor.isPresent()) {
                        Supervisor supervisor = optionalSupervisor.get();
                        supervisor.setSignature(image);
                        supervisorRepository.save(supervisor);
                        optionalBinary = Optional.of(image);
                    }
                    break;

                case 'M' :
                    Optional<Monitor> optionalMonitor = monitorRepository.findByUsernameAndIsDisabledFalse(username);
                    if (optionalMonitor.isPresent()) {
                        Monitor monitor = optionalMonitor.get();
                        monitor.setSignature(image);
                        monitorRepository.save(monitor);
                        optionalBinary = Optional.of(image);
                    }
                    break;

                case 'E' :
                    Optional<Student> optionalStudent = studentRepository.findByUsernameAndIsDisabledFalse(username);
                    if (optionalStudent.isPresent()) {
                        Student student = optionalStudent.get();
                        student.setSignature(image);
                        studentRepository.save(student);
                        optionalBinary = Optional.of(image);
                    }
                    break;
            }
        }
        return optionalBinary;
    }

    public Optional<List<Student>> getAllStudents() {
        List<Student> students = studentRepository.findAllByIsDisabledFalse();
        students.forEach(student -> cleanUpStudentCVList(Optional.of(student)));
        return students.isEmpty() ? Optional.empty() : Optional.of(students);
    }

    public Optional<TreeSet<String>> getAllSessionOfStudents() {
        TreeSet<String> sessions = new TreeSet<>();
        List<Student> students = studentRepository.findAllByIsDisabledFalse();
        students.forEach(student -> sessions.addAll(student.getSessions()));
        return sessions.isEmpty() ? Optional.empty() : Optional.of((TreeSet<String>) sessions.descendingSet());
    }

    public Optional<List<Student>> getAllStudents(String session) {
        List<Student> students = studentRepository.findAllByIsDisabledFalseAndSessionsContains(session);
        students.forEach(student -> cleanUpStudentCVList(Optional.of(student)));
        return students.isEmpty() ? Optional.empty() : Optional.of(students);
    }

    public Optional<List<Student>> getAllStudentsWithoutSupervisor(Department department, String session) {
        List<Student> students = studentRepository.
                findAllByIsDisabledFalseAndDepartmentAndSupervisorMapIsEmptyAndSessionContains(department, session);
        students.forEach(student -> cleanUpStudentCVList(Optional.of(student)));
        return students.isEmpty() ? Optional.empty() : Optional.of(students);
    }

    public Optional<List<Student>> getAllStudentsWithSupervisor(String idSupervisor, String session) {
        List<Student> students = studentRepository.findAllBySupervisor_IdAndIsDisabledFalse(new ObjectId(idSupervisor), session);
        students.forEach(student -> cleanUpStudentCVList(Optional.of(student)));
        return students.isEmpty() ? Optional.empty() : Optional.of(students);
    }

    public Optional<List<Student>> getAllStudentsWithoutCV(String session) {
        List<Student> students = studentRepository.findAllByIsDisabledFalseAndCVListIsEmptyAndSessionsContains(session);
        students.forEach(student -> cleanUpStudentCVList(Optional.of(student)));
        return students.isEmpty() ? Optional.empty() : Optional.of(students);
    }

    public Optional<List<Student>> getAllStudentsWithApplicationStatusWaitingAndInterviewDatePassed(String session) {
        List<Student> studentWaitingAndInterviewDatePassed = new ArrayList<>();
        List<InternshipApplication> internshipApplicationsWithInterviewDate =
                internshipApplicationRepository.findAllByInterviewDateIsNotNull();

        internshipApplicationsWithInterviewDate.forEach(internshipApplication ->
                setStudentListWithApplicationStatusWaitingAndInterviewDatePassed(studentWaitingAndInterviewDatePassed, internshipApplication, session));
        return studentWaitingAndInterviewDatePassed.isEmpty() ? Optional.empty() : Optional.of(studentWaitingAndInterviewDatePassed);
    }

    private void setStudentListWithApplicationStatusWaitingAndInterviewDatePassed(List<Student> students, InternshipApplication internshipApplication, String session) {
        Student student = internshipApplication.getStudent();
        InternshipOffer internshipOffer = internshipApplication.getInternshipOffer();
        if (internshipApplication.getStatus() == InternshipApplication.ApplicationStatus.WAITING &&
            internshipApplication.getInterviewDate().before(new Date()) &&
            !students.contains(internshipApplication.getStudent()) &&
                student.getSessions().contains(session) &&
                session.equals(internshipOffer.getSession())) {
            students.add(internshipApplication.getStudent());
        }
    }

    public Optional<List<Student>> getAllStudentsWithoutInterviewDate(String session) {
        List<Student> studentsWithoutInterviewDate = studentRepository.findAllByIsDisabledFalseAndSessionsContains(session);
        List<InternshipApplication> internshipApplicationsWithInterviewDate =
                internshipApplicationRepository.findAllByInterviewDateIsNotNull();

        internshipApplicationsWithInterviewDate.forEach(internshipApplication -> {
            InternshipOffer internshipOffer = internshipApplication.getInternshipOffer();
            if (session.equals(internshipOffer.getSession()))
                studentsWithoutInterviewDate.remove(internshipApplication.getStudent());
        });
        return studentsWithoutInterviewDate.isEmpty() ? Optional.empty() : Optional.of(studentsWithoutInterviewDate);
    }

    public Optional<List<Student>> getAllStudentsWithInternship(String session) {
        List<Student> studentsWithInternship = new ArrayList<>();
        List<InternshipApplication> completedInternshipApplications = internshipApplicationRepository.findAllByIsDisabledFalse();
        completedInternshipApplications.forEach(internshipApplication -> {
            if (internshipApplication.statusIsCompleted()){
                InternshipOffer internshipOffer = internshipApplication.getInternshipOffer();
                if (!studentsWithInternship.contains(internshipApplication.getStudent()) &&
                session.equals(internshipOffer.getSession())){
                    studentsWithInternship.add(internshipApplication.getStudent());
                }
            }
        });
        return studentsWithInternship.isEmpty() ? Optional.empty() : Optional.of(studentsWithInternship);
    }

    public Optional<List<Student>> getAllStudentsWaitingInterview(String session) {
        List<Student> studentsWaitingInterview = new ArrayList<>();
        List<InternshipApplication> internshipApplicationsWithoutInterviewDate =
                internshipApplicationRepository.findAllByStatusWaitingAndInterviewDateIsAfterNowAndIsDisabledFalse();

        internshipApplicationsWithoutInterviewDate.forEach(internshipApplication -> {
                    Student student = internshipApplication.getStudent();
                    InternshipOffer internshipOffer = internshipApplication.getInternshipOffer();
                    if (student.getSessions().contains(session) && session.equals(internshipOffer.getSession())){
                        studentsWaitingInterview.add(student);
                    }
                }
        );

        return studentsWaitingInterview.isEmpty() ? Optional.empty() :
                Optional.of(studentsWaitingInterview.stream().distinct().collect(Collectors.toList()));
    }

    public Optional<List<Student>> getAllStudentsWithoutStudentEvaluation(String session){
        List<Internship> internshipListWithoutStudentEvaluation =
                internshipRepository.findByStudentEvaluationNullAndIsDisabledFalse();
        List<Student> studentList = getAllStudentsFromInternships(internshipListWithoutStudentEvaluation, session);
        return studentList.isEmpty() ? Optional.empty() : Optional.of(studentList);
    }

    public Optional<List<Student>> getAllStudentsWithoutEnterpriseEvaluation(String session){
        List<Internship> internshipListWithoutEnterpriseEvaluation =
                internshipRepository.findByEnterpriseEvaluationNullAndIsDisabledFalse();
        List<Student> studentList = getAllStudentsFromInternships(internshipListWithoutEnterpriseEvaluation, session);
        return studentList.isEmpty() ? Optional.empty() : Optional.of(studentList);
    }

    private List<Student> getAllStudentsFromInternships(List<Internship> internshipListWithoutStudentEvaluation, String session){
        List<Student> studentList = new ArrayList<>();
        internshipListWithoutStudentEvaluation .forEach(internship -> {
            InternshipApplication internshipApplication = internship.getInternshipApplication();
            InternshipOffer internshipOffer = internshipApplication.getInternshipOffer();
            if(internshipApplication.statusIsCompleted() && session.equals(internshipOffer.getSession()))
                studentList.add(internshipApplication.getStudent());
        });
        return studentList.stream().distinct().collect(Collectors.toList());
    }

    public Optional<List<Supervisor>> getAllSupervisorsOfSession(String session) {
        List<Supervisor> supervisors = supervisorRepository.findAllByIsDisabledFalseAndSessionsContains(session);
        return supervisors.isEmpty() ? Optional.empty() : Optional.of(supervisors);
    }

    public Optional<List<String>> getAllSessionsOfMonitor(String idMonitor) {
        Query query = new Query(getCriteriaQueryGetAllSessionsOfMonitor(idMonitor));

        List<String> sessions = mongoTemplate
                .getCollection(COLLECTION_NAME_INTERNSHIP_OFFER)
                .distinct(FIELD_SESSION, query.getQueryObject() ,String.class)
                .into(new ArrayList<>());

        Collections.reverse(sessions);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions);
    }

    private Criteria getCriteriaQueryGetAllSessionsOfMonitor(String idMonitor) {
        List<Criteria> expression =  new ArrayList<>();
        expression.add(Criteria.where(QUERY_CRITERIA_MONITOR_ID).is(new ObjectId(idMonitor)));
        expression.add(Criteria.where(FIELD_IS_DISABLED).is(false));
        return new Criteria().andOperator(expression.toArray(expression.toArray(new Criteria[0])));
    }

    public Optional<TreeSet<String>> getAllNextSessionsOfInternshipOffersValidated() {
        List<InternshipOffer> internshipOffers = internshipOfferRepository.findAllByIsValidTrueAndIsDisabledFalse();
        TreeSet<String> sessions = setNextSessionsOfInternshipOffers(internshipOffers);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions);
    }

    public Optional<TreeSet<String>> getAllNextSessionsOfInternshipOffersUnvalidated() {
        List<InternshipOffer> internshipOffers = internshipOfferRepository.findAllByIsValidFalseAndIsDisabledFalse();
        TreeSet<String> sessions = setNextSessionsOfInternshipOffers(internshipOffers);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions);
    }

    private TreeSet<String> setNextSessionsOfInternshipOffers(List<InternshipOffer> internshipOffers) {
        TreeSet<String> sessions = new TreeSet<>();

        String currentSession = getSessionFromDate(new Date());
        String[] sessionComponents = currentSession.split(" ");
        String session = sessionComponents[POSITION_TAG_IN_SESSION];
        int year = Integer.parseInt(sessionComponents[POSITION_YEAR_IN_SESSION]);

        internshipOffers.forEach(internshipOffer -> {
            String internshipOfferSession = internshipOffer.getSession();
            int internshipOfferYear = Integer.parseInt(internshipOfferSession.split(" ")[POSITION_YEAR_IN_SESSION]);

            if (internshipOfferYear > year || (WINTER_SESSION.equals(session) && internshipOfferYear == year)) {
                sessions.add(internshipOffer.getSession());
            }
        });
        return sessions;
    }

    public Optional<TreeSet<String>> getAllSessionsOfInvalidInternshipOffers() {
        List<InternshipOffer> internshipOffers = internshipOfferRepository.findAllByIsValidFalseAndIsDisabledFalse();
        System.out.println(internshipOffers.size());
        TreeSet<String> sessions = setSessionsOfInternshipOffers(internshipOffers);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions);
    }

    public Optional<TreeSet<String>> getAllSessionsOfValidInternshipOffers() {
        List<InternshipOffer> internshipOffers = internshipOfferRepository.findAllByIsValidTrueAndIsDisabledFalse();
        TreeSet<String> sessions = setSessionsOfInternshipOffers(internshipOffers);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions);
    }

    private TreeSet<String> setSessionsOfInternshipOffers(List<InternshipOffer> internshipOffers) {
        TreeSet<String> sessions = new TreeSet<>();
        internshipOffers.forEach(internshipOffer -> sessions.add(internshipOffer.getSession()));

        return (TreeSet<String>) sessions.descendingSet();
    }

    public Optional<Monitor> getMonitorByUsername(String username) {
        return monitorRepository.findByUsernameAndIsDisabledFalse(username);
    }

    public Optional<Student> assignSupervisorToStudent(String idStudent, String idSupervisor) {
        Optional<Student> optionalStudent = studentRepository.findById(idStudent);
        Optional<Supervisor> optionalSupervisor = supervisorRepository.findById(idSupervisor);

        optionalStudent.ifPresent(student -> {
            Map<String, Supervisor> supervisorMap = student.getSupervisorMap();
            optionalSupervisor.ifPresent(supervisor -> supervisorMap.put(getNextSessionFromDate(new Date()), supervisor));
            studentRepository.save(student);
        });

        return cleanUpStudentCVList(optionalStudent);
    }

    private Optional<PDFDocument> getCVFromStudent(String idCV, Optional<Student> optionalStudent) {
        Optional<PDFDocument> optionalDocument = Optional.empty();

        if (optionalStudent.isPresent() && optionalStudent.get().getCVList() != null) {
            Student student = optionalStudent.get();
            List<CV> listCV = student.getCVList();
            for (CV cv : listCV) {
                if (cv.getId().equals(idCV))
                    optionalDocument = Optional.of(cv.getPDFDocument());
            }
            student.setCVList(listCV);
        }
        return optionalDocument;
    }

    public Optional<PDFDocument> downloadInternshipOfferDocument(String id) {
        Optional<InternshipOffer> optionalInternshipOffer = internshipOfferRepository.findById(id);
        Optional<PDFDocument> optionalDocument = Optional.empty();

        if (optionalInternshipOffer.isPresent() && optionalInternshipOffer.get().getPDFDocument() != null)
            optionalDocument = Optional.of(optionalInternshipOffer.get().getPDFDocument());

        return optionalDocument;
    }

    public Optional<PDFDocument> downloadStudentCVDocument(String idStudent, String idCV) {
        Optional<Student> optionalStudent = studentRepository.findById(idStudent);
        return getCVFromStudent(idCV, optionalStudent);
    }

    public Optional<PDFDocument> downloadEvaluationDocument(String documentName) {
        Optional<PDFDocument> optionalDocument = Optional.empty();
        try {
            Path pdfPath = Paths.get(ASSETS_PATH + documentName + EVALUATION_FILE_NAME);
            optionalDocument = Optional.of(PDFDocument.builder()
                    .name(documentName + EVALUATION_FILE_NAME)
                    .content(new Binary(BsonBinarySubType.BINARY, Files.readAllBytes(pdfPath)))
                    .build());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return optionalDocument;
    }

    public Optional<PDFDocument> downloadInternshipContractDocument(String idInternship) {
        Optional<Internship> optionalInternship = internshipRepository.findById(idInternship);
        return optionalInternship.map(Internship::getInternshipContract);
    }

    public Optional<PDFDocument> downloadInternshipStudentEvaluationDocument(String idInternship) {
        Optional<Internship> optionalInternship = internshipRepository.findById(idInternship);
        return optionalInternship.map(Internship::getStudentEvaluation);
    }

    public Optional<PDFDocument> downloadInternshipEnterpriseEvaluationDocument(String idInternship) {
        Optional<Internship> optionalInternship = internshipRepository.findById(idInternship);
        return optionalInternship.map(Internship::getEnterpriseEvaluation);
    }
}
