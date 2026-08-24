## Atomicity
A transaction is a single indivisible unit of work => The entire transaction is either applied or never committed. When transactions are atomic, there is not such things as a partially completed transaction: **all or nothing**

## Consistency
The database should always move from one consistent state to the next. If the transaction is never committed, none of the transaction's changes are ever reflected in the database.

## Isolation
The result of a transaction are usually invisible to other transactions until the transaction is completed.

## Durability
Once committed, a transaction's changes are permanent. This means the changes must be recorded such that data won't be lost in a system crash.