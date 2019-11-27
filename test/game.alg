CO Game of guessing a number CO

begin
    co Values for printing
    less := -1;
    more := 1;
    OK := 100;
    KO := -100;

    co Value to guess
    read(guess);

    for i from 0 by 1 to 5 do
        co Value of the user
        read(try);

        if try > guess then
            print(less)
        else
            if try < guess then
                print(more)
            else
                print(OK)
            endif
        endif
    endwhile;
    print(KO)
end