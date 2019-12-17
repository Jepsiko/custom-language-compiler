CO Game of guessing a number CO

begin
    co Values for printing
    less := -1;
    more := 1;
    OK := 100;
    KO := -100;

    co Value to guess
    read(guess);

    win := 0;
    for i from 0 by 1 to 5 do
        if win = 0 then
            co Value of the user
            read(try);
            if i /= 4 then
                if try = guess then
                    print(OK);
                    win := 1
                endif;

                if try > guess then
                    print(less)
                endif;

                if try < guess then
                    print(more)
                endif
            endif
        endif
    endwhile;
    if win = 0 then
        print(KO)
    endif
end