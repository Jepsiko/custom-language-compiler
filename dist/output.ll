
@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: nounwind uwtable
define void @println(i32 %x) #0 {
	%1 = alloca i32, align 4
	store i32 %x, i32* %1, align 4
	%2 = load i32, i32* %1, align 4
	%3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
	ret void
}

declare i32 @printf(i8*, ...) #1

declare i32 @getchar()

define i32 @readInt() {
entry:
	%res = alloca i32
	store i32 0, i32* %res
	br label %loop
loop:
	%0 = call i32 @getchar()
	%1 = sub i32 %0, 48

	%2 = icmp sge i32 %1, 0
	%3 = icmp sle i32 %1, 9
	%4 = and i1 %2, %3
	br i1 %4, label %continue, label %exit
continue:
	%5 = load i32, i32* %res
	%6 = mul i32 %5, 10
	%7 = add i32 %6, %1
	store i32 %7, i32* %res

	br label %loop
exit:
	%8 = load i32, i32* %res
	ret i32 %8
}


define i32 @main() {
entry:

%less = alloca i32
store i32 -1, i32* %less

%more = alloca i32
store i32 1, i32* %more

%OK = alloca i32
store i32 100, i32* %OK

%KO = alloca i32
store i32 -100, i32* %KO

%guess = alloca i32
%0 = call i32 @readInt()
store i32 %0, i32* %guess

%i = alloca i32
store i32 0, i32* %i
br label %forCond0
forCond0:
%1 = load i32, i32* %i
%2 = icmp slt i32 %1, 5
%3 = add i32 %1, 1
store i32 %3, i32* %i

br i1 %2, label %forCode0, label %endfor0
forCode0:
%try = alloca i32
%4 = call i32 @readInt()
store i32 %4, i32* %try

%5 = load i32, i32* %try
%6 = load i32, i32* %guess
%7 = icmp sgt i32 %5, %6

br i1 %7, label %ifCode0, label %elseCode0
ifCode0:
%8 = load i32, i32* %less
call void @println(i32 %8)

br label %endif0
elseCode0:
%9 = load i32, i32* %try
%10 = load i32, i32* %guess
%11 = icmp slt i32 %9, %10

br i1 %11, label %ifCode1, label %elseCode1
ifCode1:
%12 = load i32, i32* %more
call void @println(i32 %12)

br label %endif1
elseCode1:
%13 = load i32, i32* %OK
call void @println(i32 %13)

br label %endif1
endif1:

br label %endif0
endif0:

br label %forCond0
endfor0:

%14 = load i32, i32* %KO
call void @println(i32 %14)

ret i32 0;
}
