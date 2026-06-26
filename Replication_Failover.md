## Planned Promotions
1. Determine which replica you are going to promote. (Ensure this replica has all data.) - this replica is called `target`.
2. Check the lag to make sure only in a few seconds.
3. Stop write on the current source by setting `super_read_only`
4. Wait until replication is in sync from current source to with your target. (We can check the GTIDs to make sure that)
5. Unset read_only on the target.
6. Switch application traffic to the target.
7. Repoint all replicas to the new sources.

## Unplanned Promotions
1. Determine which replica you are going to promote. (Ensure this replica has all data.) - this replica is called `target`.
2. Unset read_only on the target.
3. Switch application traffic to the target.
4. Repoint all replicas to the new sources.

