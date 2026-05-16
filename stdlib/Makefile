SOURCES = $(wildcard *.c)
OBJECTS = $(SOURCES:.c=.o)
LIBS = $(patsubst %.c,lib%.a,$(SOURCES))

all: $(LIBS)

lib%.a: %.o
	ar rcs $@ $<

%.o: %.c
	gcc -Wall -Wextra -O2 -c $< -o $@

clean:
	rm -f *.o *.a
