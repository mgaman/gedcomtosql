--select children from Family where id=(select OwnFamily from Individual where id=1);
--select children from Family where id=1;
--select ownfamily from Individual where id=1;
--select CurrentFamilyName from Individual where soundex(CurrentFamilyName) like soundex('henr'); -- sounds similar
--select * from Individual where PreviousFamilyNames not null;
--update Individual set PreviousFamilyNames = 'Conley' where ID = 371;
--select CurrentFamilyName from Individual where CurrentFamilyName like 'Henry'; -- exact match
--select Prenames,CurrentFamilyName from Individual where CurrentFamilyName like '%Henr%'; -- regex % is wild card
--select ID,FirstName,MiddleNames,CurrentFamilyName from Individual where CurrentFamilyName like '%Henr%' and FirstName like '%Maurice%'; -- regex % is wild card
--select ID,FirstName,MiddleNames,CurrentFamilyName from Individual where CurrentFamilyName like '%Henr%' and soundex(FirstName) like soundex('morris'); -- regex % is wild card
--update Individual set CurrentFamilyName=NULL where CurrentFamilyName='\unknown\';
--select * from Individual where CurrentFamilyName is null;
select * from Individual where firstname is null;