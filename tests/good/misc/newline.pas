program newline;

procedure printf(...); external;

begin
    printf('a\nb'#10'c\nd'#10'e\nf'#10);
end.
