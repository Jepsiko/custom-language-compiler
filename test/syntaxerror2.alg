
CO Program with an error : ExprArith (a + b) instead of a [VarName] on line 14 CO 
begin
  read(a) ;
  read(b) ;
  while b /= 0 do
    c := b ;
    while a >= b do
      a := a-b
    endwhile ;
    b := a ;
    a := c
  endwhile ;
  print(a + b)
end
