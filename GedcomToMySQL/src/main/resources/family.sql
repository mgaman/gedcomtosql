use Family;
/*
Drop table Individual;
Create table Individual (ID integer primary key,
	BirthFamilyName text,
	OtherFamilyNames JSON,
	ForeNames JSON, 
    Birthdate DATE , Birthplace text,
    Deathdate DATE , Deathplace text,
    ParentFamily integer, 
    Relationships JSON, -- integer array of indices to Family table
    Gender ENUM ('Male','Female','Other') , Comment Text);
*/
/*
Create table Family (ID integer primary key, father integer, mother integer, 
  relationship ENUM ('Married','Divorced','Other'),
  children JSON, -- index of indices to Individual table
  marriageDate DATE, marriagePlace text,
  divorceDate DATE, divorcePlace text, Comment text);
*/
#insert into Family (ID,relationship) values (1,'Married')
#select * from Family;
#delete from Family;
#delete from Individual;
#insert into Individual (ID,ForeNames) values(1,'['david']');
#select * from Individual;
#select Relationships from Individual where id=11;
#select JSON_DEPTH('[8,7]');
#select JSON_EXTRACT('[8,7]','$[1]','$[0]');
select JSON_EXTRACT(Relationships,'$[1]','$[0]') from Individual where id=11;
