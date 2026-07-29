## DEADLOCKS
A deadlock is when two or more transaction are mutually holding and requesting locks on the same resources.

## TRANSACTION LOGGING
**1. The Request:** You send UPDATE or INSERT query to the database.

**2. The RAM write (Instant):** The database finds relevant page in its memory (RAM), modifies it, and marks it as `dirty` (meaning it is newer than what is on the disk)

**3. The Log Write (Fast & Durable):** The database writes a tiny recipe of that change to the transaction log on dis sequentially. Onlny after this log write hits the disk, the databse tell you "Success! Your transaction is committed"

**4. The Lazy Update (The "Twice" Write):** At this point, your data is safe. Later, database writes them to the actual B-Tree tables on disk.