package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransactionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionsServiceTest {
    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransactionsService transactionsService;


    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    public void setUp() {
        // Reset the account repository before each test
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
    void transferBalance_Success() {
        BigDecimal transferAmount = new BigDecimal("200.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);
        this.transactionsService.transferBalance(transferRequest);

        assertEquals(new BigDecimal("800.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), toAccount.getBalance());
    }

    @Test
    void transferBalance_InsufficientFunds() {
        BigDecimal transferAmount = new BigDecimal("1500.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.transactionsService.transferBalance(transferRequest);
        });

        assertEquals("Insufficient balance", exception.getMessage());
        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(BigDecimal.ZERO, toAccount.getBalance());
    }

    @Test
    void transferBalance_FromAccountNotFound() {
        BigDecimal transferAmount = new BigDecimal("500.00");
        String fromAccountId = "Id-3" + System.currentTimeMillis();
        TransferRequest transferRequest = new TransferRequest(fromAccountId, toAccount.getAccountId(), transferAmount);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.transactionsService.transferBalance(transferRequest);
        });

        assertEquals("Account not found: " + fromAccountId, exception.getMessage());
        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(BigDecimal.ZERO, toAccount.getBalance());
    }

    @Test
    void transferBalance_ToAccountNotFound() {
        BigDecimal transferAmount = new BigDecimal("500.00");
        String toAccountId = "Id-3" + System.currentTimeMillis();
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccountId, transferAmount);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.transactionsService.transferBalance(transferRequest);
        });

        assertEquals("Account not found: " + toAccountId, exception.getMessage());
        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(BigDecimal.ZERO, toAccount.getBalance());
    }

    @Test
    void transferBalance_SelfTransfer() {
        BigDecimal transferAmount = new BigDecimal("500.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), fromAccount.getAccountId(), transferAmount);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.transactionsService.transferBalance(transferRequest);
        });

        assertEquals("From and To accountId are same", exception.getMessage());
        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(BigDecimal.ZERO, toAccount.getBalance());
    }

    @Test
    void transferBalance_ConcurrentTransfer() throws InterruptedException {
        BigDecimal transferAmount = new BigDecimal("100.00");
        TransferRequest transferRequest = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);
        Runnable task = () -> this.transactionsService.transferBalance(transferRequest);

        // Run two threads simultaneously
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(new BigDecimal("800.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), toAccount.getBalance());
    }
}
