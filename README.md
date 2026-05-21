# Recaf

Recaf is an optimizing compiler for a subset of Pascal,
targeted at x86_64.

## Quick Start
### Build
Build by running `./gradlew nativeCompile` on a x86_64 machine,
which will generate the executable `recaf` in `build/native/nativeCompile/`.
You can also generate a jar file in `build/libs/` by running `./gradlew jar`,
which takes considerably less time.

Compilation on x86_64 is necessary to build the standard library in `stdlib/`;
the compiler will not be able to generate executables otherwise.
(It will still be able to generate assembly via `-t assembly`, 
but you will need to link it to `stdlib/` manually.)

### Usage
Run the native executable `recaf` or the jar file through the JVM.
Besides being linked to `libc`, the former is self-contained.
- Pass in a single file with `.pas` extension to compile.
- Use `-t <ast|ll|ssa|assembly|exe>` to set a compilation stage.
  The default is `exe`, which will generate an executable (linked to `libc`)
  when compiled on an x86_64 target.
  The other stages do not require x86_64.
- Use `-o <outfile>` to set the output file.
  The default output location for `exe` stage simply removes the `.pas` extension.
  For other stages, the default output is stdout.
- There are four levels of optimization: `-O0`, `-O1`, `-O2`, and `-O3`.
- Use `-l <library>` to static link a library. Give the full path to the library.
- Use `-h` to print a more detailed help message.
