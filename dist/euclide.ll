
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

	%isNegative = alloca i1
	%number = alloca i32
	%0 = call i32 @getchar()
	
	%1 = icmp eq i32 %0, 45
	store i1 %1, i1* %isNegative
	br i1 %1, label %loop, label %firstIteration
firstIteration:
	%2 = sub i32 %0, 48
	store i32 %2, i32* %number

	%3 = icmp sge i32 %2, 0
	%4 = icmp sle i32 %2, 9
	%5 = and i1 %3, %4
	br i1 %5, label %continue, label %exit
loop:
	%6 = call i32 @getchar()
	%7 = sub i32 %6, 48
	store i32 %7, i32* %number

	%8 = icmp sge i32 %7, 0
	%9 = icmp sle i32 %7, 9
	%10 = and i1 %8, %9
	br i1 %10, label %continue, label %exit
continue:
	%11 = load i32, i32* %res
	%12 = mul i32 %11, 10
	%13 = load i32, i32* %number
	%14 = add i32 %12, %13 
	store i32 %14, i32* %res

	br label %loop
exit:

	%15 = load i1, i1* %isNegative
	br i1 %15, label %ifNegative, label %endifNegative

ifNegative:
	%16 = load i32, i32* %res
	%17 = mul i32 %16, -1
	store i32 %17, i32* %res

	br label %endifNegative
endifNegative:
	%18 = load i32, i32* %res
	ret i32 %18
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

br i1 %2, label %forCode0, label %endfor0
forCode0:
%3 = load i32, i32* %win
%4 = icmp eq i32 %3, 0

br i1 %4, label %ifCode0, label %endif0
ifCode0:
%try = alloca i32
%5 = call i32 @readInt()
store i32 %5, i32* %try

%6 = load i32, i32* %try
%7 = load i32, i32* %guess
%8 = icmp eq i32 %6, %7

br i1 %8, label %ifCode1, label %endif1
ifCode1:
%9 = load i32, i32* %OK
call void @println(i32 %9)

store i32 1, i32* %win

br label %endif1
endif1:

%10 = load i32, i32* %i
%11 = icmp ne i32 %10, 4

br i1 %11, label %ifCode2, label %endif2
ifCode2:
%12 = load i32, i32* %try
%13 = load i32, i32* %guess
%14 = icmp sgt i32 %12, %13

br i1 %14, label %ifCode3, label %endif3
ifCode3:
%15 = load i32, i32* %less
call void @println(i32 %15)

br label %endif3
endif3:

%16 = load i32, i32* %try
%17 = load i32, i32* %guess
%18 = icmp slt i32 %16, %17

br i1 %18, label %ifCode4, label %endif4
ifCode4:
%19 = load i32, i32* %more
call void @println(i32 %19)

br label %endif4
endif4:

br label %endif2
endif2:

br label %endif0
endif0:

%20 = add i32 %1, 1
store i32 %20, i32* %i
br label %forCond0
endfor0:

%21 = load i32, i32* %win
%22 = icmp eq i32 %21, 0

br i1 %22, label %ifCode5, label %endif5
ifCode5:
%23 = load i32, i32* %KO
call void @println(i32 %23)

br label %endif5
endif5:

ret i32 0;
}
