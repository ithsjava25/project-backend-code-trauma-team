package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.example.projektarendehantering.common.AppException;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.DocumentEntity;
import org.example.projektarendehantering.infrastructure.persistence.DocumentRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.presentation.dto.DocumentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.retry.RetryCallback;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private S3Template s3Template;
    @Mock
    private S3RetryExecutor s3RetryExecutor;
    @Mock
    private FailedS3DeletionService failedS3DeletionService;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private DocumentService documentService;

    private Actor doctorActor;
    private Actor managerActor;
    private Actor patientActor;
    private UUID caseId;
    private CaseEntity caseEntity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "bucket", "test-bucket");
        lenient().when(s3RetryExecutor.execute(anyString(), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RetryCallback<Object, RuntimeException> callback = invocation.getArgument(1);
            return callback.doWithRetry(null);
        });

        UUID doctorId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        doctorActor = new Actor(doctorId, Role.DOCTOR, "Doctor", "doctor_user");
        managerActor = new Actor(managerId, Role.MANAGER, "Manager", "manager_user");
        patientActor = new Actor(patientId, Role.PATIENT, "Patient", "patient_user");

        PatientEntity patient = new PatientEntity();
        patient.setId(patientId);

        caseId = UUID.randomUUID();
        caseEntity = new CaseEntity();
        caseEntity.setId(caseId);
        caseEntity.setOwnerId(doctorId);
        caseEntity.setPatient(patient);
    }

    @Test
    void uploadDocument_shouldAllowOwner() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(s3RetryExecutor.execute(eq("upload"), any())).thenReturn(null);
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(i -> {
            DocumentEntity e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(documentMapper.toDTO(any(DocumentEntity.class))).thenReturn(new DocumentDTO(UUID.randomUUID(), "test.txt", "text/plain", 5, Instant.now(), doctorActor.userId(), caseId));

        DocumentDTO result = documentService.uploadDocument(doctorActor, caseId, file);

        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("test.txt");
        verify(s3RetryExecutor).execute(eq("upload"), any());
        verify(documentRepository).save(any(DocumentEntity.class));
    }

    @Test
    void uploadDocument_shouldAllowManager() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(s3RetryExecutor.execute(eq("upload"), any())).thenReturn(null);
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(i -> {
            DocumentEntity e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(documentMapper.toDTO(any(DocumentEntity.class))).thenReturn(new DocumentDTO(UUID.randomUUID(), "test.txt", "text/plain", 5, Instant.now(), managerActor.userId(), caseId));

        DocumentDTO result = documentService.uploadDocument(managerActor, caseId, file);

        assertThat(result).isNotNull();
        verify(s3RetryExecutor).execute(eq("upload"), any());
    }

    @Test
    void uploadDocument_shouldDenyUnauthorized() {
        Actor unauthorizedActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Unauthorized", "unauthorized_user");
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> documentService.uploadDocument(unauthorizedActor, caseId, file))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void deleteDocument_shouldAllowOwner() {
        UUID docId = UUID.randomUUID();
        DocumentEntity docEntity = new DocumentEntity();
        docEntity.setId(docId);
        docEntity.setCaseEntity(caseEntity);
        docEntity.setS3Key("some-key");

        when(s3RetryExecutor.execute(eq("delete"), any())).thenReturn(null);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(docEntity));

        documentService.deleteDocument(doctorActor, docId);

        verify(s3RetryExecutor).execute(eq("delete"), any());
        verify(documentRepository).delete(docEntity);
    }

    @Test
    void uploadDocument_shouldBubbleDegradedCodeWhenS3TransientlyUnavailable() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(s3RetryExecutor.execute(eq("upload"), any()))
                .thenThrow(new AppException("S3_SERVICE_DEGRADED", "Temporary S3 issue while trying to upload"));

        assertThatThrownBy(() -> documentService.uploadDocument(doctorActor, caseId, file))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Temporary S3 issue")
                .satisfies(ex -> assertThat(((AppException) ex).errorCode()).isEqualTo("S3_SERVICE_DEGRADED"));
    }

    @Test
    void downloadDocument_shouldUseRetryExecutor() {
        UUID docId = UUID.randomUUID();
        DocumentEntity docEntity = new DocumentEntity();
        docEntity.setId(docId);
        docEntity.setCaseEntity(caseEntity);
        docEntity.setS3Key("download-key");
        S3Resource resource = mock(S3Resource.class);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(docEntity));
        when(s3RetryExecutor.execute(eq("download"), any())).thenReturn(resource);

        S3Resource result = documentService.downloadDocument(doctorActor, docId);

        assertThat(result).isEqualTo(resource);
        verify(s3RetryExecutor).execute(eq("download"), any());
    }

    @Test
    void deleteDocument_shouldQueueFailedDeleteForLaterRecovery() {
        UUID docId = UUID.randomUUID();
        DocumentEntity docEntity = new DocumentEntity();
        docEntity.setId(docId);
        docEntity.setCaseEntity(caseEntity);
        docEntity.setS3Key("failed-key");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(docEntity));
        when(s3RetryExecutor.execute(eq("delete"), any()))
                .thenThrow(new AppException("S3_SERVICE_DEGRADED", "delete degraded"));

        documentService.deleteDocument(doctorActor, docId);

        verify(failedS3DeletionService).enqueue(eq("test-bucket"), eq("failed-key"), argThat(ex ->
                ex instanceof AppException && "S3_SERVICE_DEGRADED".equals(((AppException) ex).errorCode())));
        verify(documentRepository).delete(docEntity);
    }

    @Test
    void uploadDocument_shouldAllowPatientOnOwnCase() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(i -> {
            DocumentEntity e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(documentMapper.toDTO(any(DocumentEntity.class))).thenReturn(
                new DocumentDTO(UUID.randomUUID(), "test.txt", "text/plain", 5, Instant.now(), patientActor.userId(), caseId));

        DocumentDTO result = documentService.uploadDocument(patientActor, caseId, file);

        assertThat(result).isNotNull();
        verify(s3RetryExecutor).execute(eq("upload"), any());
    }

    @Test
    void uploadDocument_shouldDenyPatientOnOtherCase() {
        Actor otherPatient = new Actor(UUID.randomUUID(), Role.PATIENT, "Other", "other_patient");
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> documentService.uploadDocument(otherPatient, caseId, file))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void listDocuments_shouldAllowPatientOnOwnCase() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(documentRepository.findAllByCaseEntityId(caseId)).thenReturn(List.of());

        documentService.listDocuments(patientActor, caseId);

        verify(documentRepository).findAllByCaseEntityId(caseId);
    }

    @Test
    void deleteDocument_shouldDenyPatient() {
        UUID docId = UUID.randomUUID();

        assertThatThrownBy(() -> documentService.deleteDocument(patientActor, docId))
                .isInstanceOf(NotAuthorizedException.class);

        verify(documentRepository, never()).findById(any());
    }
}
