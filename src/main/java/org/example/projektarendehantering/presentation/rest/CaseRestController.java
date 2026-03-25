package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.usecase.CreateCaseCommand;
import org.example.projektarendehantering.application.usecase.CreateCaseResult;
import org.example.projektarendehantering.application.usecase.CreateCaseUseCase;
import org.example.projektarendehantering.presentation.dto.CreateCaseRequest;
import org.example.projektarendehantering.presentation.dto.CreateCaseResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cases")
public class CaseRestController {

    private final CreateCaseUseCase createCaseUseCase;

    public CaseRestController(CreateCaseUseCase createCaseUseCase) {
        this.createCaseUseCase = createCaseUseCase;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CreateCaseResponse createCase(@RequestBody @Valid CreateCaseRequest request) {
        CreateCaseCommand command = new CreateCaseCommand(request.title(), request.description());
        CreateCaseResult result = createCaseUseCase.execute(command);
        return new CreateCaseResponse(result.caseId().value().toString());
    }
}

