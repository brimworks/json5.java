#!/usr/bin/env perl

BEGIN { $^W=1 }

# Usage: ./extract-utf8.pl Pc < UnicodeData.txt
#
# Result: Prints the ragel definition to match
# the "Pc" code point category UTF-8 encoded
# characters.

if ( @ARGV != 1 ) {
    die "Expected one argument to be the code point category";
}

my $class = $ARGV[0];

while (<STDIN>) {
    my @F = split(";");
    next if $F[2] ne $class;
    my $n = hex($F[0]);
    if ( $n < 0x80 ) {
        push(@r, sprintf("0x%02x", $n));
    } elsif ( $n < 0x800 ) {
        push(@r, sprintf("0x%02x 0x%02x",
            0xC0|($n >> 6),
            0x80|($n & 0x3F)));
    } elsif ( $n < 0x10000 ) {
        push(@r, sprintf("0x%02x 0x%02x 0x%02x",
            0xE0|($n >> 12),
            0x80|(($n >> 6)&0x3F),
            0x80|($n & 0x3F)));
    } else {
        push(@r, sprintf("0x%02x 0x%02x 0x%02x 0x%02x",
            0xF0|($n >> 18),
            0x80|(($n >> 12)&0x3F),
            0x80|(($n >> 6)&0x3F),
            0x80|($n & 0x3F)));
    }
}
print "U_$class =\n    ",join(" |\n    ",@r), ";\n";
