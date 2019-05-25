# gedcomtosql
## Introduction
I started using the DOS based FHS application 30 years ago and it has served me well. Now its time to enter the 21st century. While FHS has done me a sterling job for 40 years it's time to move on.
There was no internet 40 years ago so no online Genealogy searches as we have now.
SQL gives so much more, for example in FHS a female is only recorded by her birth name, So if Jill Smith married Bob Jones I could search for Jill Smith but not Jill Jones. SQL changes all that.
## SQL Tables
- Individual
The GEDCOM top level tag INDI is transformed to a table containing names, notes, indices of parent family and own family(s), date and place of birth and death.
- Relationships
The GEDCOM top level tag FAM is transformed to a table containing indices of father, mother, child(ren), date & place of marriage / divorce, notes.
I handled all lower level tags that were present in my data. There are more in later versions of GEDCOM and you are invited to add them at will.
- ExtraFiles
My original website has directories of jpeg files for portraits, graves, ketubot etc. These are now placed in a single directory and an index recorded in the ExtraFiles table. This will record the owner(s), classification and pathname. A benefit of multiple owners allows, for example, both husband and wife to share a marriage certificate instead of just the husband. 
## Database provider
Choosing a database provider is a bit tricky. I use SQLITE3 for this project but it is not ideal. Its advantage is its' small footprint. It runs as a stand alone entity and there is no need to install or access a server such as mySQL or postgres.
SQLite supports soundex.
## Case Sensitivity
FHS data is strictly ASCII so probably a non-issue for many people. However as more data gets added with European variations and other languages e.g. Hebrew, UNICODE support is needed.
SQLITE3 has no Unicode support and LIKE is case insensitive for Ascii.
MySQL has proper Unicode support.
## Array Handling
FHS stores multiple names e.g. forenames as a blank separated string. In my internal handling I separate the parts into a Java ArrayList.
An obvious choice for database handlers would be to use JSON as the method of storing arrays, whether we are talking about multiple forenames or multiple indices into the Relationships table.
However this is not practical. For example one cannot do a case-insensitive search on a JSON entity. In general SQL provided handling of JSON is pretty weak. Far better to store a JSON like string and let the higher level scripting language to the work.
For example, searching for someone who has a forename (not necessary firstname) of "David" do:
- Forenames column whould be varchar and JSON like e.g. '["Derek","David"]' for a guy whose second name is David, or '["David"]' whose only forename is David or yet still '["Isdavidjim"]' whose name includes the string 'david' but thats not his forename. 
- Do a select into a temporary table e.g.
- CREATE TEMPORARY TABLE IF NOT EXISTS tempfnames AS (select * from Individuals where forenames col_name COLLATE utf8mb4_0900_as_ci LIKE '%david%');
- Then read each line in the temporary table, use the languages JSON library to do the work of splitting forenames into its component parts. 
