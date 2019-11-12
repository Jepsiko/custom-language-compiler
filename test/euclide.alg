
CO Euclid's algorithm CO 
begin
  read(a) ;
  read(b) ;
  while b /= 0 do
    c := b ;
    while a >= b do     co computation of modulo
      a := a-b
    endwhile ;
    b := a ;
    a := c
  endwhile ;
  print(a)
end
