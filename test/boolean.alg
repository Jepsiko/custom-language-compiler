begin
    read(a);
    read(b);
    while a < b or a > b and b > 2 and b < 5 do
        a := a + 1
    endwhile;
    print(a);
    print(b)
end