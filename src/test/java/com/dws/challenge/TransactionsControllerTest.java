package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransactionsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class TransactionsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Autowired
    private ObjectMapper objectMapper;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();

        // Create two accounts for testing
        String fromAccountId = "Id-1" + System.currentTimeMillis();
        System.out.println(fromAccountId);
        fromAccount = new Account(fromAccountId);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        this.accountsService.createAccount(fromAccount);

        String toAccountId = "Id-2" + System.currentTimeMillis();
        toAccount = new Account(toAccountId);
        this.accountsService.createAccount(toAccount);
    }

    @Test
    void testTransferBalance_SuccessfulTransfer() throws Exception {
        BigDecimal transferAmount = new BigDecimal("200.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);
        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))  // Send valid transfer request as JSON
                .andExpect(status().isOk())  // Expect a 200 OK status
                .andExpect(content().string("Transfer successful"));  // Expect success message
    }

    @Test
    void testTransferBalance_InsufficientBalance() throws Exception {
        BigDecimal transferAmount = new BigDecimal("1500.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);

        // Perform the POST request to the /transfer endpoint
        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())  // Expect a 400 Bad Request status
                .andExpect(content().string("Insufficient balance"));
    }

    @Test
    void testTransferBalance_AccountNotFound() throws Exception {
        BigDecimal transferAmount = new BigDecimal("500.00");
        String fromAccountId = "Id-3" + System.currentTimeMillis();
        TransferRequest transferRequest = new TransferRequest(fromAccountId, toAccount.getAccountId(), transferAmount);

        // Perform the POST request to the /transfer endpoint
        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())  // Expect a 400 Bad Request status
                .andExpect(content().string("Account not found: " + fromAccountId));
    }

    @Test
    void testTransferBalance_InvalidTransferRequest() throws Exception {
        BigDecimal transferAmount = new BigDecimal("-200.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);
        // Perform the POST request to the /transfer endpoint
        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))  // Send invalid request as JSON
                .andExpect(status().isBadRequest());
    }
}
