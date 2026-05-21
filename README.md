# Recaf

Recaf is an optimizing compiler for a subset of Pascal,
targeted at x86_64.
```pascal
program HelloWorld;

begin
    WriteLn('Hello, world!')
end.
```

As is common practice in industry nowadays,
I first provide the following benchmark data.

| Compiler | Geomean  | adi | btree* | correlation | cowbasic | ff&#x2011;gram&#x2011;schmidt | mat&#x2011;mult | quicksort |&nbsp;spigot |&nbsp;strassen* |&nbsp;sudoku |
| ----- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| recaf&nbsp;&#x2011;O3 | 688.9&nbsp;ms | 1.523&nbsp;s | 752.8&nbsp;ms | 310.8&nbsp;ms | 1.774&nbsp;s  | 614.1&nbsp;ms | 1.298&nbsp;s  | 249.3&nbsp;ms | 2.701&nbsp;s | 244.3&nbsp;ms | 290.6&nbsp;ms |
| clang&nbsp;&#x2011;O3 | 702.1&nbsp;ms | 1.401&nbsp;s | 1.534&nbsp;s  | 202.3&nbsp;ms | 1.370&nbsp;s  | 533.0&nbsp;ms | 810.0&nbsp;ms | 206.0&nbsp;ms | 2.429&nbsp;s | 1.430&nbsp;s  | 158.3&nbsp;ms |
| recaf&nbsp;&#x2011;O2 | 712.9&nbsp;ms | 1.393&nbsp;s | 742.7&nbsp;ms | 377.1&nbsp;ms | 1.932&nbsp;s  | 614.3&nbsp;ms | 1.576&nbsp;s  | 240.6&nbsp;ms | 2.692&nbsp;s | 246.0&nbsp;ms | 292.0&nbsp;ms |
| gcc&nbsp;&#x2011;O2   | 725.2&nbsp;ms | 1.392&nbsp;s | 1.798&nbsp;s  | 215.5&nbsp;ms | 1.351&nbsp;s  | 578.4&nbsp;ms | 883.5&nbsp;ms | 195.6&nbsp;ms | 2.424&nbsp;s | 1.460&nbsp;s  | 156.0&nbsp;ms |
| clang&nbsp;&#x2011;O2 | 728.4&nbsp;ms | 1.419&nbsp;s | 1.546&nbsp;s  | 216.1&nbsp;ms | 1.365&nbsp;s  | 542.3&nbsp;ms | 810.6&nbsp;ms | 208.6&nbsp;ms | 2.431&nbsp;s | 1.443&nbsp;s  | 201.9&nbsp;ms |
| gcc&nbsp;&#x2011;O3   | 730.2&nbsp;ms | 1.405&nbsp;s | 1.539&nbsp;s  | 214.3&nbsp;ms | 1.398&nbsp;s  | 559.1&nbsp;ms | 870.6&nbsp;ms | 201.1&nbsp;ms | 2.425&nbsp;s | 1.409&nbsp;s  | 199.0&nbsp;ms |
| gcc&nbsp;&#x2011;O1   | 733.6&nbsp;ms | 1.485&nbsp;s | 1.602&nbsp;s  | 226.2&nbsp;ms | 1.391&nbsp;s  | 579.9&nbsp;ms | 883.7&nbsp;ms | 206.6&nbsp;ms | 2.430&nbsp;s | 1.453&nbsp;s  | 161.4&nbsp;ms |
| clang&nbsp;&#x2011;O1 | 737.1&nbsp;ms | 1.395&nbsp;s | 1.526&nbsp;s  | 209.1&nbsp;ms | 1.365&nbsp;s  | 604.9&nbsp;ms | 912.8&nbsp;ms | 203.2&nbsp;ms | 2.428&nbsp;s | 1.475&nbsp;s  | 194.0&nbsp;ms |
| recaf&nbsp;&#x2011;O1 | 818.7&nbsp;ms | 1.557&nbsp;s | 735.6&nbsp;ms | 513.9&nbsp;ms | 2.409&nbsp;s  | 651.3&nbsp;ms | 2.274&nbsp;s  | 245.5&nbsp;ms | 2.707&nbsp;s | 314.0&nbsp;ms | 308.8&nbsp;ms |
| gcc&nbsp;&#x2011;O0   | 1.381&nbsp;s  | 2.346&nbsp;s | 1.746&nbsp;s  | 864.8&nbsp;ms | 2.928&nbsp;s  | 1.387&nbsp;s  | 3.290&nbsp;s  | 265.2&nbsp;ms | 3.200&nbsp;s | 1.761&nbsp;s  | 356.6&nbsp;ms |
| fpc&nbsp;&#x2011;O4   | 1.588&nbsp;s  | 1.738&nbsp;s | 1.303&nbsp;s  | 549.2&nbsp;ms | 9.467&nbsp;s  | 2.273&nbsp;s  | 1.916&nbsp;s  | 233.3&nbsp;ms | 4.930&nbsp;s | 7.112&nbsp;s  | 242.6&nbsp;ms |
| fpc&nbsp;&#x2011;O3   | 1.591&nbsp;s  | 1.781&nbsp;s | 1.285&nbsp;s  | 548.4&nbsp;ms | 9.462&nbsp;s  | 2.277&nbsp;s  | 1.927&nbsp;s  | 235.1&nbsp;ms | 4.937&nbsp;s | 7.080&nbsp;s  | 243.1&nbsp;ms |
| fpc&nbsp;&#x2011;O2   | 1.606&nbsp;s  | 1.772&nbsp;s | 1.300&nbsp;s  | 553.7&nbsp;ms | 9.486&nbsp;s  | 2.277&nbsp;s  | 1.906&nbsp;s  | 232.0&nbsp;ms | 5.420&nbsp;s | 7.110&nbsp;s  | 242.5&nbsp;ms |
| clang&nbsp;&#x2011;O0 | 1.665&nbsp;s  | 5.563&nbsp;s | 1.666&nbsp;s  | 927.8&nbsp;ms | 10.674&nbsp;s | 1.327&nbsp;s  | 2.972&nbsp;s  | 254.7&nbsp;ms | 3.189&nbsp;s | 1.734&nbsp;s  | 322.0&nbsp;ms |
| fpc&nbsp;&#x2011;O1   | 1.782&nbsp;s  | 1.795&nbsp;s | 1.368&nbsp;s  | 683.6&nbsp;ms | 10.424&nbsp;s | 2.431&nbsp;s  | 2.337&nbsp;s  | 264.0&nbsp;ms | 5.633&nbsp;s | 7.222&nbsp;s  | 301.6&nbsp;ms |
| fpc&nbsp;&#x2011;O0   | 1.819&nbsp;s  | 1.840&nbsp;s | 1.409&nbsp;s  | 659.6&nbsp;ms | 10.410&nbsp;s | 2.436&nbsp;s  | 2.374&nbsp;s  | 288.1&nbsp;ms | 5.477&nbsp;s | 7.069&nbsp;s  | 345.7&nbsp;ms |
| recaf&nbsp;&#x2011;O0 | 4.725&nbsp;s  | 10.290&nbsp;s | 1.787&nbsp;s | 6.408&nbsp;s  | 33.606&nbsp;s | 4.166&nbsp;s  | 26.155&nbsp;s | 579.9&nbsp;ms | 5.474&nbsp;s | 2.914&nbsp;s  | 1.390&nbsp;s  |

If this raises eyebrows,
there is a resolution of the asterisks and actual performance comparison below.
(TL;DR faster than FPC, about 75% the performance of clang O3,
though there are still some asterisks on these claims.)

This is a personal project and not intended to be of interest to anyone else.

## Quick Start
### Build
Build by running `./gradlew nativeCompile` on a x86_64 machine
with GraalVM JDK>=25 installed.
This will generate the executable `recaf` in `build/native/nativeCompile/`.
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
  We link libc by default.
- Use `-h` to print a more detailed help message.

## Informal Language Specification
### Examples
See the files with extension `.pas` in the `benchmark/` directory,
or under `tests/good/`.

### Key Features
The subset of Pascal that Recaf supports includes:
- The following types:
    * primitive types `Integer` (32-bit), `Int64` (64-bit), and `Boolean`;
    * enumeration types;
    * fixed-size arrays (whose bounds are compile-time evaluable;
      indices are not bounds-checked and out-of-bounds access is undefined behavior);
    * records (but no variant records / tagged union); and
    * pointer types
      (which necessarily point to the heap; allocate and deallocate via `New` and `Dispose`).
  ```pascal
  type
      TColor = (Red, Green, Blue);
      TImage = Array[1..10, 1..10] of TColor;
      TPair = record
          x, y: TImage;
      end;
      PPair = ^TPair;
  ```
- Compile-time evaluated constants, e.g.
  ```pascal
  const
      N = 6 + 7;
      N2 = N * N;
  ```
- Top-level procedures and functions (i.e. routines), but not nested.
  Routines may have local definitions for types, constant, or variables.
  They may take primitive types, enums, or pointers as parameters,
  and functions may return primitive types, enums, or pointers.
  Arguments are always by value (no `var` parameters),
  which is not that inconvenient because arrays/records cannot be passed
  (use heap instead).
- Most control statements, including if-else, while-do, for loops, and repeat-until.
  There are also intrinsics `Break`, `Continue`, and `Exit`.
  The `Exit` intrinsic procedure returns from a function early.
  (Recall the return-value is set by assigning to a variable with the same name as the function.)
- Standard arithmetic operations `+`, `-`, `*`, `div`, `mod` on integer types,
  relation operations `<`, `<=`, `>=`, `>` on integer types,
  relation operations `=` and `<>` on integers types, booleans, and pointers,
  and boolean operations `and`, `or`, `not`.
  Bit-level operations are not provided.
  Integer overflow is undefined behavior.
- The assignment operation `:=` copies data for arrays and records and copies value for all other types.
- The compiler is single-pass. You may forward-declare routines 
  by writing `forward` instead of the body.
  The following is legal so long as `TType` is defined in the same `type` block or earlier:
  ```pascal
  type
      PType = ^TType;
      TType = ...
  ```
- By writing `external`, you promise that a routine will be linked.
  For external routines, you may use `(...)` instead for its parameters
  if you do not wish to specify them.
  ```pascal
  procedure printf(...); external;
  ```
  No promises are made about the behavior of external routines.
  The compiler generally assumes, for example, that pointers point to the heap,
  and no pointers point to overlapping regions.
  External routines with pathological behavior such as storing a pointer
  and using it later may lead to undefined behavior.
- You may import modules using `uses`, but no way to create modules is offered,
  and the only provided module so far is `Float64`:
  ```pascal
  uses Float64;
  ```
  The `System` module is imported by default. Do not try to import it again.
- Strings are implemented as arrays of `Integer`. 
  For a 0-indexed array `s`, `s[0]` stores the length of the string,
  and `s[1]` through `s[s[0]]` store the characters as 32-bit integers.
  The built-in `ReadLn`, `Write`, and `WriteLn` procedures work with this representation
  of string.
  (String literals correspond to C-style strings and may be passed to external
  C functions. They are translated into the above string representation
  when assigned to variables.)
- The language is case-insensitive. For external routines, the case used in the
  routine declaration is respected throughout the program.
- Unlike standard Pascal, type annotations are everywhere optional and default to `Integer` when
  omitted. 
## Optimizations
### Passes
The compiler performs the following optimization passes 
on an [SSA](https://en.wikipedia.org/wiki/Static_single-assignment_form) representation.

On O1:
- Copy propagation
- Dead code elimination
- Sparse simple copy propagation

On O2, the above, and:
- [GVN-PRE](https://docs.lib.purdue.edu/dissertations/AAI3154748/),
  which subsumes loop-invariant code motion, common subexpression elimination,
  and partial redundancy elimination.
- Operator strength reduction with linear function test replacement
- Function inlining
- Tailcall expansion

On O3, the above, and:
- Loop unrolling

Note loop unrolling may cause code-size explosion and compile time explosion,
as well as occasional runtime explosions
for some programs.

On O1, O2, and O3, Chaitin-Briggs graph-coloring register allocation is used.

For heap allocations,
a custom arena allocator is used at runtime via a linked library.
All objects of size at most 1 MB are managed by the arena.
Note the language does not permit dynamic-sized arrays,
so all heap allocations are fixed-size,
and the number of distinct sizes is compile-time bounded.
This makes an arena especially appropriate,
especially considering most programs either make many
allocations with small Node-like records
or few large heap allocations.

### Performance
The performance of the compiler on some benchmarks is given in the table at the top.

Some notes:
- The benchmarks are run with hyperfine on `-w 1 -r 5`.
- The benchmarks with asterisks involve many heap-allocations,
  from which Recaf benefits unfairly due to its arena allocator.
  A comparison where the geometric mean is computed without those two benchmarks
  is given at the bottom.
- O0 is hilariously bad and should not be used if some semblance of speed is desired.
- Surprisingly, Recaf is much faster than FPC. I'm not sure if I'm passing in the correct flags,
  and I think FPC might be doing a bunch of runtime checks Recaf isn't doing,
  but I'm not sure how to turn those off (if they exist).
- O3 achieves slightly above 75% the performance of clang O3 on our benchmarks.
  Though I must note the benchmarks are line-by-line translated into C and therefore
  not necessarily the best implementations. 
  (For instance, `New` and `Dispose` are translated into `malloc` and `free`,
  which lead to the situation above.)
- I ran the benchmarks on a 7-year-old laptop that I think has nontrivial issues with 
  thermal throttling. Sorry if inaccurate. 

The benchmark data, with `btree` and `strassen` removed:

| Compiler | Geomean | Speedup |
| ----- | :---: | :---: |
| clang&nbsp;&#x2011;O3 | 582.6&nbsp;ms | 1.000 |
| gcc&nbsp;&#x2011;O2   | 593.1&nbsp;ms | 0.982 |
| clang&nbsp;&#x2011;O2 | 608.7&nbsp;ms | 0.957 |
| gcc&nbsp;&#x2011;O1   | 610.9&nbsp;ms | 0.954 |
| gcc&nbsp;&#x2011;O3   | 612.7&nbsp;ms | 0.951 |
| clang&nbsp;&#x2011;O1 | 617.1&nbsp;ms | 0.944 |
| recaf&nbsp;&#x2011;O3 | 775.6&nbsp;ms | 0.751 |
| recaf&nbsp;&#x2011;O2 | 810.3&nbsp;ms | 0.719 |
| recaf&nbsp;&#x2011;O1 | 935.3&nbsp;ms | 0.623 |
| gcc&nbsp;&#x2011;O0   | 1.301&nbsp;s  | 0.448 |
| fpc&nbsp;&#x2011;O4   | 1.349&nbsp;s  | 0.432 |
| fpc&nbsp;&#x2011;O3   | 1.356&nbsp;s  | 0.430 |
| fpc&nbsp;&#x2011;O2   | 1.369&nbsp;s  | 0.426 |
| fpc&nbsp;&#x2011;O1   | 1.546&nbsp;s  | 0.377 |
| fpc&nbsp;&#x2011;O0   | 1.585&nbsp;s  | 0.368 |
| clang&nbsp;&#x2011;O0 | 1.657&nbsp;s  | 0.352 |
| recaf&nbsp;&#x2011;O0 | 5.668&nbsp;s  | 0.103 |

The benchmarks are run on a ThinkPad T490
with Intel i7-8665U CPU 
running Linux 6.12.86+deb13-amd64.

## Acknowledgements
A lot of optimizations are based on
*Engineering a Compiler* by Cooper and Torczon.
The GVN-PRE optimization is based on 
Thomas VanDrunen's dissertation.

The implementation of Recaf is based on
a smaller compiler
for a language called [Decaf](https://6110-sp25.github.io/syllabus)
I wrote for the Spring 2025 iteration of 6.110 at MIT.
I received nontrivial help during office hours from the TAs.
