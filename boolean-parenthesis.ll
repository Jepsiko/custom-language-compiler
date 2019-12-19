

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
	%1 = icmp eq i32 %0, 45
	%2 = sub i32 %0, 48

	%3 = icmp sge i32 %2, 0
	%4 = icmp sle i32 %2, 9
	%5 = and i1 %3, %4
	%6 = or i1 %1, %5
	br i1 %6, label %continue, label %exit
continue:
	%7 = load i32, i32* %res
	%8 = mul i32 %7, 10
	%9 = add i32 %8, %2
	store i32 %9, i32* %res

	br label %loop
exit:
	%8 = load i32, i32* %res
	ret i32 %8
}


define i32 @main() {
entry:
%a = alloca i32
%0 = call i32 @readInt()
store i32 %0, i32* %a

%b = alloca i32
%1 = call i32 @readInt()
store i32 %1, i32* %b

%c = alloca i32
%2 = call i32 @readInt()
store i32 %2, i32* %c

%3 = load i32, i32* %a
%4 = icmp sle i32 %3, 0
%5 = load i32, i32* %b
%6 = icmp sgt i32 %5, 2
%7 = or i1 %4, %6
%8 = load i32, i32* %c
%9 = icmp slt i32 %8, 1
%10 = and i1 %7, %9

br i1 %10, label %ifCode0, label %endif0
ifCode0:
%ok = alloca i32
store i32 1, i32* %ok

%11 = load i32, i32* %ok
call void @println(i32 %11)

br label %endif0
endif0:

ret i32 0;
}
