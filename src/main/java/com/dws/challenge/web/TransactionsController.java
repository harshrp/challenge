package com.dws.challenge.web;

import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountTransferException;
import com.dws.challenge.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
@Slf4j
public class TransactionsController {

    private final TransactionsService transactionsService;

    @Autowired
    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferBalance(@RequestBody @Valid TransferRequest transferRequest) {
        try {
            this.transactionsService.transferBalance(transferRequest);
        } catch (AccountTransferException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Transfer successful", HttpStatus.OK);
    }
}
