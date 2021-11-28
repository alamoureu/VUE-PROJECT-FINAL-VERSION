package com.eq3.backend.controller;

import com.eq3.backend.model.*;
import com.eq3.backend.service.BackendService;

import org.bson.types.Binary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.TreeSet;

@RestController
@CrossOrigin("http://localhost:8080")
public class BackendController {

    private final BackendService service;

    public BackendController(
            BackendService service) {
        this.service = service;
    }

    @PostMapping(value = "/save/signature/{username}",
            produces = "application/json;charset=utf8",
            consumes = { "multipart/form-data" })
    public ResponseEntity<Binary> saveSignature(@PathVariable String username,
                                                @RequestPart(name = "signature") MultipartFile multipartFile) {
        return service.saveSignature(username, multipartFile)
                .map(_student -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_student))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return service.getAllStudents()
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/sessions/students")
    public ResponseEntity<TreeSet<String>> getAllSessionOfStudents() {
        return service.getAllSessionOfStudents()
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/{session}")
    public ResponseEntity<List<Student>> getAllStudents(@PathVariable String session) {
        return service.getAllStudents(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/noSupervisor/{department}/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithoutSupervisor(@PathVariable Department department, @PathVariable String session) {
        return service.getAllStudentsWithoutSupervisor(department, session)
                .map(_students ->
                   ResponseEntity.status(HttpStatus.ACCEPTED).body(_students)
                )
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/supervisor/{idSupervisor}/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithSupervisor(@PathVariable String idSupervisor, @PathVariable String session) {
        return service.getAllStudentsWithSupervisor(idSupervisor, session)
                .map(_students ->
                        ResponseEntity.status(HttpStatus.ACCEPTED).body(_students)
                )
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/without/CV/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithoutCV(@PathVariable String session) {
        return service.getAllStudentsWithoutCV(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/without/interviewDate/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithoutInterviewDate(@PathVariable String session) {
        return service.getAllStudentsWithoutInterviewDate(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/with/applicationStatus/waiting/and/interviewDate/passed/today/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithApplicationStatusWaitingAndInterviewDatePassed(@PathVariable String session) {
        return service.getAllStudentsWithApplicationStatusWaitingAndInterviewDatePassed(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/with/Internship/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithInternship(@PathVariable String session) {
        return service.getAllStudentsWithInternship(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/students/waiting/interview/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWaitingInterview(@PathVariable String session) {
        return service.getAllStudentsWaitingInterview(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/student/studentEvaluation/unevaluated/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithoutStudentEvaluation(@PathVariable String session){
        return service.getAllStudentsWithoutStudentEvaluation(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/student/enterpriseEvaluation/unevaluated/{session}")
    public ResponseEntity<List<Student>> getAllStudentsWithoutEnterpriseEvaluation(@PathVariable String session){
        return service.getAllStudentsWithoutEnterpriseEvaluation(session)
                .map(_students -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_students))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/supervisors/{session}")
    public ResponseEntity<List<Supervisor>> getAllSupervisorsOfSession(@PathVariable String session){
        return service.getAllSupervisorsOfSession(session)
                .map(_supervisors -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_supervisors))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/sessions/internshipOffer/monitor/{idMonitor}")
    public ResponseEntity<List<String>> getAllSessionsOfMonitor(@PathVariable String idMonitor){
        return service.getAllSessionsOfMonitor(idMonitor)
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/next/sessions/internshipOffer")
    public ResponseEntity<TreeSet<String>> getAllNextSessionsOfInternshipOffers(){
        return service.getAllNextSessionsOfInternshipOffersValidated()
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/next/sessions/internshipOffer/unvalidated")
    public ResponseEntity<TreeSet<String>> getAllNextSessionsOfInternshipOffersUnvalidated(){
        return service.getAllNextSessionsOfInternshipOffersUnvalidated()
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/sessions/invalid/internshipOffer")
    public ResponseEntity<TreeSet<String>> getAllSessionsOfInvalidInternshipOffers(){
        return service.getAllSessionsOfInvalidInternshipOffers()
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/getAll/sessions/valid/internshipOffer")
    public ResponseEntity<TreeSet<String>> getAllSessionsOfValidInternshipOffers(){
        return service.getAllSessionsOfValidInternshipOffers()
                .map(_sessions -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_sessions))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/get/monitor/{username}")
    public ResponseEntity<Monitor> getMonitorByUsername(@PathVariable String username) {
        return service.getMonitorByUsername(username)
                .map(_monitor -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_monitor))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PostMapping("/assign/supervisor/{idStudent}/{idSupervisor}")
    public ResponseEntity<Student> assignSupervisorToStudent(@PathVariable String idStudent, @PathVariable String idSupervisor) {
        return service.assignSupervisorToStudent(idStudent, idSupervisor)
                .map(_student -> ResponseEntity.status(HttpStatus.ACCEPTED).body(_student))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value = "/get/internshipOffer/document/{id}", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadInternshipOfferDocument(@PathVariable(name = "id") String id){
        return service.downloadInternshipOfferDocument(id)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value = "/get/CV/document/{idStudent}/{idCV}", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadStudentCVDocument(@PathVariable String idStudent, @PathVariable String idCV){
        return service.downloadStudentCVDocument(idStudent, idCV)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value="/get/{typeEvaluation}/evaluation/document", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadEvaluationDocument(@PathVariable String typeEvaluation){
        return service.downloadEvaluationDocument(typeEvaluation)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value="/get/internship/document/{id}", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadInternshipContractDocument(@PathVariable String id){
        return service.downloadInternshipContractDocument(id)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value="/get/internship/student/evaluation/document/{idInternship}", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadInternshipStudentEvaluationDocument(@PathVariable String idInternship){
        return service.downloadInternshipStudentEvaluationDocument(idInternship)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(value="/get/internship/enterprise/evaluation/document/{idInternship}", produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadInternshipEnterpriseEvaluationDocument(@PathVariable String idInternship){
        return service.downloadInternshipEnterpriseEvaluationDocument(idInternship)
                .map(this::getDownloadingDocument)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    private ResponseEntity<InputStreamResource> getDownloadingDocument(PDFDocument PDFDocument) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Disposition", "inline; filename=" + PDFDocument.getName());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .headers(headers)
                .contentLength(PDFDocument.getContent().length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(
                        new ByteArrayInputStream(PDFDocument.getContent().getData()))
                );
    }
}