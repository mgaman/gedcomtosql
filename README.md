# gedcomtosql
I started using the DOS based FHS application 30 years ago and it has served me well. Now its time to enter the 21st century.
GEDCOM contains 2 basic entities that can be recast as SQL tables, Individual and Family.
The GEDCOM top level tag INDI is transformed to a table containing names, notes, indices of parent family and own family(s), date and place of birth and death.
The GEDCOM top level tag FAM is transformed to a table containing indices of father, mother, child(ren), date & place of marriage / divorce, notes.
I handled all lower level tags that were present in my data. There are more in later versions of GEDCOM and you are invited to add them at will.

Choosing a database provider is a bit tricky. I use SQLITE3 for this project but it is not ideal. Its advantage is its' small footprint. It runs as a stand alone entity and there is no need to install or access a server such as mySQL or postgres. Its main disadvantage is lack of features, particularily lack of support for Arrays.
One needs arrays in the Individual table for multiple families and in the Family table for multiple children.
My work-around is to record numeric arrays as a comma delimited string.

SQLite supports soundex.
