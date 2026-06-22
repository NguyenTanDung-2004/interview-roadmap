## What are Indexes?
- Indexes are data structures that storage engines use to find row quicly.
- Index optimization is perhaps most powerful way to improve query performance.

## B-tree Indexes (We have to think about B+)
*When people talk about an index without mentioning a type, they are referring to a B-tree Index*
- We use the term B-tree for these indexes because that is what MySQL uses in CREATE TABLE and other statements. 
- However, other storage engines can use different storage structures internally. (like T-tree, B+tree)
- A B-tree index speeds up data access, because:
    - Does not have to scan the whole table to find desired data.
- If this is Clustered Index (Primary Key), the leaf node of B-Tree (B+Tree) stores the real data. The root, and internal leaf only store pointers.

## How does MySQL store data?
*When we create a table in database, we must have a Primary Key. A primary Key is an indexes (clusted Index)*
Each Index Tree:
- Engine will split hard drive to smaller parts (that are called page - InnoDB page size is 16KB)
- If a page is a root or internal node -> only store the pointers
- If a page is a leaf node -> store real data.
    + If this Tree is clustered Index Tree: The leaf node store all data of row.
    + If this Tree is Secondary Index Tree: The leaf node store (indexed data | row_id)

*Problems with Pages*
- If one row of your data is only 1KB => 1 Leaf page can contains 14, 15 rows.
***What happen if we continue insert 1 row***
    - New leaf page will be created and move a half data of old page to new page. One linkedlist pointer is used to connect two pages. (Page Split)

- If one row of your data is so large - Page Overflow.
    - Unchangeable Rule: Each leaf page is mandatory to store at least 2 rows.
    - At leaf page: InnoDB will not store all data in this row. It only stores small column like (id, name,...) and the first approximately 746 bytes of the large data.
    - Overflow Page: the remainder data of large data will be splited and move to supporting pages.
    - Pointer: in the leaf page, InnoDB puts a pointer that is used to point to supporting pages.


## Adaptive Hashing Index
- When InnoDB notices that some index values is accessed very frequently, it build a hash-index-table for them in memmory on **top of B-Tree indexes**
- Reduce query-time from O(logn) -> O(1)
- We can't control or configure it <=> but we can disable it
    - Hash-Index-Table is used by B-Tree Indexes. 
    - If your system has many INSERT/UPDATE/DELETE commands simultaneously. => B-Trees are constantly changing => MySQL have to use Locking mechanism to update Hashing-Index-Table => CPU usage will be spike

## Types of Queries that can use a B-Tree index
*B-tree indexes work well for lookups by the full key value, a key range, or a key-prefix. They are useful only if the lookup uses a leftmost prefix of the index*
- Match the full value: You are looking for a specific person who you know their exact full name and birthdate.
```
SELECT * FROM people WHERE last_name = 'Alen' AND first_name = 'Cuba' AND dob = '1960-01-01'
```
- Match a leftmost prefix: We are using composite index (name, birthday), you want to find everyone with the name "Smith John" but you dont know when they were born
```
SELECT * FROM people WHERE name = 'Smith John'
```
- Match a column prefix: You are building a search-as-you-type feature where a user types "Smi%" into the last name field. cannot use the index in case "%mith"
```
support: SELECT * FROM people WHERE last_name LIKE 'Smi%'
un-support: SELECT * FROM people WHERE last_name LIKE '%mith'
```
- Match a rage of values: You want to generate a report on all people whose last names fall between 'Allen' and 'Bancroft'
```
SELECT * FROM people WHERE last_name BETWEEN 'Allen' AND 'Bancroft';
```
- Match one part exactly and match a range on another part: You want to find all the people whose last_name = 'Allen' and the first name like 'A%'
```
SELECT * FROM people WHERE last_name = 'Allen' AND first_name LIKE 'M%';
```
- Index-only queries:
```
SELECT last_name, first_name FROM people WHERE last_name = 'Allen';
```

*readable -> sortable*

Let imagine that we have a composite index(a, b)
- ORDER BY without filtering by a. The time complexity of this way is O(nlogn) with n is the size of this table
```
SELECT * FROM my_table 
ORDER BY b;
```
- ORDER BY within filtering by a. The time complexity of this way is O(logn) + O(k). With n is the size of this table and K is the size of filtering result.
Why we can imrpove when using this way.
    - composite(a,b) => sort a, group a and sort b
```
SELECT * FROM my_table 
WHERE a = 10 
ORDER BY b;
```

## Some limitations of B-Tree indexes:
- They are not useful if the lookups does not start from the leftmost side of the indexed columns. 
- The storage engine is not useful for any columns to the right of the first range condition. The index in the example below is only useful for the first two column, because LIKE 'j%' is a range condition.
```
SELECT * FROM people WHERE last_name = 'Smith' AND first_name LIKE 'J%' AND dob = '1976-12-23'
```
=> These limitations above are the reason why the column order is extremely important.

## Full-Text Search
- Need to clarify in another section with Elastic Search.

## Indexing Strategies for High Performance
### 1. Prefix Indexes and Index Slectivity
- What happen if you make indexes BLOB or TEXT columns?
- The solution is to choose the prefix that's long enough to give good selectivity but short enough to save space.
- There are many ways to choose how many prefixes to index. The little experimentation shows that 7 is a good value.
### 2. Multicolumn Indexes
- **Individual indexes on lots of columns**
    + Won't help MySQL improve performance for most queries
    + `SELECT * FROM users WHERE actor_id = 1 OR film_id = 1;` Scan index tree of actor_id, scan index tree of film_id => merge the results.
    + `SELECT * FROM users WHERE last_name = 'Smith' AND age = 30 AND status = 'active';` MySQL picks one index that it thinks will narrow down the rows the most. After that scan all the results to check other conditions.
    + Disabling index merge
- **Composite index:**
    + Single B-Tree index built on two or more columns.
    + MySQL sorts the data inside the Btree from left to right hierachically of composite index.
    + Composite_index (last_name, first_name, dob) `WHERE last_name = 'Smith' AND first_name LIKE 'J%' AND dob = '1976-12-23'` The index is useless for the third condition. Because when find the list of **first_name** We also find the value of **dob** in each **first_name** (Because composite_index tree only store the value of columns in composite_index and the value of primary Key)
    + Sorting Power: If we have composite_index(a,b) `WHERE a = 10 ORDER BY b` the time complexity of this condition will be (O(logn) + O(k)) because the data is pre-sorted inherently.
### 3. Clustered Index
- By default: Clustered index is the index of primary Key.
- If no defined primary key, InoDB will use unique non-nullable index instead.
- If there is not such index, InoDB will define a hidden primary key and cluster on that.
### 4. Covering Indexes
- If we only indexing on `last_name` => `SELECT email FROM users WHERE last_name = 'Smith';` After we find the value of primary key in composite_index B-tree, we refer this value to clustered_index B-tree to find the data of email.
- If we cover email and lastname with composite_index(last_name, email) => `SELECT email FROM users WHERE last_name = 'Smith';` We find the value of email in composite_index B-tree, no waste-time to refer to clustered_index B-tree.


https://d2cvlmmg8c0xrp.cloudfront.net/book/high-performance-mysql-4th.pdf