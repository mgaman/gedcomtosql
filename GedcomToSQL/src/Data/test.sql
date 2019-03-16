--select children from Family where id=(select OwnFamily from Individual where id=1);
select children from Family where id=1;
--select ownfamily from Individual where id=1;