package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountTransferException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionsService {
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final AccountsService accountsService;

    @Getter
    private final NotificationService notificationService = new EmailNotificationService();

    @Autowired
    public TransactionsService(AccountsRepository accountsRepository, AccountsService accountsService) {
        this.accountsRepository = accountsRepository;
        this.accountsService = accountsService;
    }

    private Object getLockForAccount(String accountId) {
        return locks.computeIfAbsent(accountId, key -> new Object());
    }

    public void transferBalance(TransferRequest transferRequest) throws AccountTransferException {
        if (transferRequest.getFromAccountId().equalsIgnoreCase(transferRequest.getToAccountId())) {
            throw new AccountTransferException("From and To accountId are same");
        }
        String fromAccountId = transferRequest.getFromAccountId();
        String toAccountId = transferRequest.getToAccountId();
        String lowerAccountId = fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId;
        String higherAccountId = fromAccountId.compareTo(toAccountId) < 0 ? toAccountId : fromAccountId;

        Object lock1 = getLockForAccount(lowerAccountId);
        Object lock2 = getLockForAccount(higherAccountId);
        //get locks in order
        synchronized (lock1) {
            synchronized (lock2) {
                Account fromAccount = accountsService.getAccount(fromAccountId);
                Account toAccount = accountsService.getAccount(toAccountId);

                if(null == fromAccount)
                    throw new AccountTransferException("Account not found: " + fromAccountId);

                if(null == toAccount)
                    throw new AccountTransferException("Account not found: " + toAccountId);

                if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
                    throw new AccountTransferException("Insufficient balance");
                }

                // Perform the balance transfer
                fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
                toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

                this.accountsRepository.save(fromAccount);
                this.accountsRepository.save(toAccount);
                this.notificationService.notifyAboutTransfer(fromAccount, String.format("Amount %s is transferred to %s ", transferRequest.getAmount(), toAccountId));
                this.notificationService.notifyAboutTransfer(toAccount, String.format("Amount %s is transferred from %s ", transferRequest.getAmount(), fromAccountId));
            }
        }
    }
}
