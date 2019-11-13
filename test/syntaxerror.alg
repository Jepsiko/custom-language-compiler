
CO Program with an error : 'then' instead of 'do' on line 8 CO 
begin
  read(a) ;
  read(b) ;
  while b /= 0 do
    c := b ;
    while a >= b then
      a := a-b
    endwhile ;
    b := a ;
    a := c
  endwhile ;
  print(a)
end
