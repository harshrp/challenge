# Changes done for supporting transfer

## Upgrade from version 2.6 to 3.2.10
- *Used stable release for SpringBoot*

## TransactionsController

- **Added Transfer Endpoint**:
    - `POST /v1/transactions/transfer`
    - Accepts `TransferRequest` (fromAccountId, toAccountId, amount).
    - Uses `@Valid` to validate the input data.

- **Error Handling**:
    - Catches `AccountTransferException` for invalid transfers (e.g., insufficient balance, missing accounts).
    - Returns appropriate HTTP status codes (`400 Bad Request` on errors, `200 OK` on success).

## TransactionsService

- **Added `transferBalance` Method**:
    - Transfers balance between two accounts.
    - Validates if both accounts exist.
    - Ensures `fromAccount` has enough balance for the transfer.

- **Thread-Safety**:
    - Implements synchronized block using a consistent locking order based on account IDs.
    - Uses a `ConcurrentHashMap` to manage locks for thread-safe transfers.

- **Balance Update**:
    - Deducts the transfer amount from `fromAccount`.
    - Adds the transfer amount to `toAccount`.
    - Saves the updated account balances.
    - Calls notification service

## Exception Handling

- **Custom Exception**:
    - `AccountTransferException` used to handle errors like missing accounts or insufficient balance.

## Testing

- **Unit Tests with MockMvc**:
    - Tests added for different scenarios (e.g., successful transfer, insufficient balance, invalid request).
    - Validates that the correct status codes and messages are returned.

## Additional changes to be done to make application Production Ready
- *Change InMemory repository to JPA repository with database configurations.*
- *Use Spring Transaction Management with other locking mechanism than synchronized block .*
- *Introduce Spring Security to have authentication and authorization*
- *Add support for CI/CD like docker file and Jenkins file*
