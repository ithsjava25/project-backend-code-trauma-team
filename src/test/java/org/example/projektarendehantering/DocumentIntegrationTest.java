package org.example.projektarendehantering;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.s3.ObjectMetadata;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.DocumentRepository;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.common.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@Transactional
class DocumentIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private S3Template s3Template;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3Presigner s3Presigner;

    private MockMvc mockMvc;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Setup manager employee for SecurityActorAdapter
        EmployeeEntity manager = new EmployeeEntity();
        manager.setId(UUID.nameUUIDFromBytes("manager".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        manager.setRole(Role.MANAGER);
        manager.setGithubUsername("manager");
        manager.setDisplayName("Manager");
        employeeRepository.save(manager);

        CaseEntity caseEntity = new CaseEntity();
        caseEntity.setTitle("Test Case");
        caseEntity.setOwnerId(manager.getId()); 
        CaseEntity saved = caseRepository.save(caseEntity);
        caseId = saved.getId();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void uploadDocument_shouldSucceed() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/ui/cases/{caseId}/documents/upload", caseId)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/ui/cases/" + caseId));

        assertThat(documentRepository.findAllByCaseEntityId(caseId)).hasSize(1);
        verify(s3Template).upload(eq("test-documents"), any(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @WithMockUser(username = "other", roles = {"DOCTOR"})
    void uploadDocument_shouldDenyUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        assertThatThrownBy(() -> 
            mockMvc.perform(multipart("/ui/cases/{caseId}/documents/upload", caseId)
                            .file(file)
                            .with(csrf()))
        ).hasCauseInstanceOf(org.example.projektarendehantering.common.NotAuthorizedException.class);
    }
}
