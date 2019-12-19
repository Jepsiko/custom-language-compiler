
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

ret i32 0;
}
