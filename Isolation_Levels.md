## READ UNCOMMITED
In the READ UNCOMMITED isolation level, transactions can view the results of uncommited transactions. At this level, many problems can occur unless you really, really know what are you doing and have a good reason for doing it. This level is rarely used in practice because its performance isn't much better than the other levels, which have many advantages. Reading uncommited data is also knows as a dirty read.

## READ COMMITED
The default isolation level for most databaes systems (but not MySQL) is READ COMMITED. It satisfied the simple definition of isolation used earlier: a transaction will continue to see changes made by transactions that were committed. This level still allows a non-repeatable read. This means you can run the same statement twice and see different data.

## REPEATABE READ
This solves the problems that READ UNCOMMITED allows. It guarantees that any rows a transaction reads will "look the same" in subsequent reads within the same transaction. But the phantom reads problem still occurs for case (when you select some range of rows, another transaction inserts a new row into the rangw, and the you select the same range again, you will see the new **phantom** row) - this problem was solved by MySQL with multiversion concurrency control. - And this level is MySQL default. (Although it guarantees tht any rows a transaction reads will "look the same" in subsequent reads within the same transaction, this problem still occurs because the differences between existed rows and new rows)

## SERIALIZABLE
The highest level of isolation, SERIALIZABLE, solves the phantom read problem by forcing transactions to be ordered so that they can't possibly conflict. SERIALIZABLE places a lock on every row it reads. At this level, a lot of timeouts, and lock contention can occur. We've rarely seen people use this isolation level, but your application's needs might force you to accept the decreased concurrency in favor of the data safety.