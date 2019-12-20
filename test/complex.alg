
CO Complex syntax CO
begin
  read(a) ;
  for i from 0 by 1 to (9+1) do
    if not (a /= i or a < i) and not a+2 = i then
      print(i)
    else
      print(a)
    endif
  endwhile
end
