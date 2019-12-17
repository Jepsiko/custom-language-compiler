
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

%win = alloca i32
store i32 0, i32* %win

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
%4 = load i32, i32* %win
%5 = icmp eq i32 %4, 0

br i1 %5, label %ifCode0, label %endif0
ifCode0:
%try = alloca i32
%6 = call i32 @readInt()
store i32 %6, i32* %try

%7 = load i32, i32* %i
call void @println(i32 %7)

%8 = load i32, i32* %i
%9 = icmp ne i32 %8, 5

br i1 %9, label %ifCode1, label %endif1
ifCode1:
%10 = load i32, i32* %try
%11 = load i32, i32* %guess
%12 = icmp eq i32 %10, %11

br i1 %12, label %ifCode2, label %endif2
ifCode2:
%13 = load i32, i32* %OK
call void @println(i32 %13)

store i32 1, i32* %win

br label %endif2
endif2:

%14 = load i32, i32* %try
%15 = load i32, i32* %guess
%16 = icmp sgt i32 %14, %15

br i1 %16, label %ifCode3, label %endif3
ifCode3:
%17 = load i32, i32* %less
call void @println(i32 %17)

br label %endif3
endif3:

%18 = load i32, i32* %try
%19 = load i32, i32* %guess
%20 = icmp slt i32 %18, %19

br i1 %20, label %ifCode4, label %endif4
ifCode4:
%21 = load i32, i32* %more
call void @println(i32 %21)

br label %endif4
endif4:

br label %endif1
endif1:

br label %endif0
endif0:

br label %forCond0
endfor0:

%22 = load i32, i32* %win
%23 = icmp eq i32 %22, 0

br i1 %23, label %ifCode5, label %endif5
ifCode5:
%24 = load i32, i32* %KO
call void @println(i32 %24)

br label %endif5
endif5:

ret i32 0;
}
