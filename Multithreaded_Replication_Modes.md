## The Problem: The Single-Threaded Bottleneck

**- The Source:** Could handle hundreds or thousands of parallel write transactions at the exact same time from various users.

**- The Replica:** Had only a single thread that reads those changes from relay log and write them to disk.

=> It would easily fall behind the source during high-traffic periods, creating massive replication lag.

## The Solution: Multithreaded Replication Modes
Because the replica replicate data by GTID (So it don't know if these transactions are conflict or not) - (Trying to modify the same row at the same time and causing data corruption)

**1. Data Mode:** Different worker threads to different databases. Distribute work based on which database (or schema) the data belongs to.
- You assign Worker A exclusively to unpack the kitchen boxes, Worker B to the Bedroom boxes, and Worker C to the living Room boxes.
- Worker A and Worker B will never collide because they are working in completely different rooms. They can work at full speed simultaneously.
- The problem: What if the homeowners bought almost everything for the kitchen? If there are 100 Kitchens boxes and only 1 Bedroom box, Workder A is completed overwhelmed while Worker B sits arround doing nothing.

**2. LOGICAL_CLOCK Mode:** Instead of caring which database the data belongs to; it looks at when the data was written on the source. 

On primary database, when mutiple users click "Buy" or "Update Profile" at the exact same millisecond, the primary database processes them at the exact same time. It then writes them into the log with a shared timestamp (a logical clock marker) that basically says "These five transactions happened simultaneously without interfering with each other". By looking at this sign, replication can safely run them in parallel here using multiple threads.


